package clientbridge.api;

import java.io.*;
import java.net.Socket;

public class BridgeReciever {
    private final String host;
    private final int port;

    public BridgeReciever(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String sendCommand(String cmd) {
        try (
                Socket socket = new Socket(host, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                    writer.println(cmd);
                    return reader.readLine();

                } catch (IOException e) {
            e.printStackTrace();
            return "error: "+e.getMessage();
        }


    }
}
