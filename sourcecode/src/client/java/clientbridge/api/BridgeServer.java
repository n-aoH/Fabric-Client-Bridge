package clientbridge.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class BridgeServer {

    public static void start(int port) {
        new Thread(() -> {
            try (ServerSocket server =
                    new ServerSocket(port, 0, InetAddress.getLoopbackAddress())) {
                System.out.println("[Bridge] Listening on "+port);

                while (true) {
                    Socket socket = server.accept();
                    new Thread(() -> handle(socket), "Bridge-Client").start();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "Bridge-Server-"+port).start();
    }

    private static void handle(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
                ) {
            String cmd = in.readLine();
            if (cmd == null) return;

            final String[] result = new String[1];
            final Object lock = new Object();

            Minecraft.getInstance().execute(() -> {
                try {
                    result[0] = executecommand(cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                    result[0] = "error";
                } finally {
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });

            synchronized (lock) {
                while (result[0] == null) {
                    lock.wait();
                }
            }

            out.println(result[0]);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String executecommand(String cmd) {
        Minecraft mc = Minecraft.getInstance();


        List<String> allowed = new ArrayList<String>() {{
            add("connectip");
            add("startup");
        }};

        String[] args = cmd.split(":",2);

        if (mc.player == null && (!allowed.contains(args[0]))) {
            return "no_player";
        }


        return switch (args[0]) {
            case "pos" -> mc.player.blockPosition().toShortString();
            case "health" -> String.valueOf(mc.player.getHealth());
            case "say" ->  {
                if (args.length < 2) {
                    yield "Usage: say:<message>";
                }
                //assert Minecraft.getInstance().player != null;
                mc.player.connection.sendChat(args[1]);


                yield "Sent: "+args[1];
            }
            case "exec" -> {
                if (args.length < 2 || args[1].isBlank()) {
                    yield "Usage: exec:<message>";
                }
                mc.player.connection.sendCommand(args[1]);
                yield "Executed: "+args[1];
            }
            case "startup" -> {
                if (mc.player != null) {
                    mc.player.displayClientMessage(Component.literal("Client connected!"), false);
                }

                yield "OK";
            }
            case "disconnect" -> {
                mc.disconnectFromWorld(Component.literal("Disconnected from panel."));
                yield "Disconnected.";
            }
            case "connectip" -> {
                if (args.length > 1) {
                    String ip = args[1];

                    ServerAddress address = ServerAddress.parseString(ip);

                    if (mc.level != null) {
                        mc.disconnectFromWorld(Component.literal("Disconnected from panel."));
                        mc.setScreen(new TitleScreen());
                    }

                    ServerData serverData = new ServerData(address.toString(), address.getHost(), ServerData.Type.OTHER);

                    // connect
                    ConnectScreen.startConnecting(mc.screen, mc, address, serverData, false, null);

                    yield "connecting";
                } else {
                    yield "Usage: connectip:<servername>";
                }
            }
            default -> "unknown_command";
        };

    }
}
