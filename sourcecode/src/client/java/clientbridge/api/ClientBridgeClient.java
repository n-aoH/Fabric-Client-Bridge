package clientbridge.api;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.PlainTextButton;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ClientBridgeClient implements ClientModInitializer {
	static boolean Client1 = true;
	private void startServer(int input, int output) {
		//boolean Client1 = true;



		BridgeReciever client = new BridgeReciever("127.0.0.1",input);
		BridgeServer.start(output);
		String resp = client.sendCommand("startup");
		System.out.println(resp);



		ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
			if (message.startsWith("b:")) {

				String command = message.substring("b:".length());

				String response = client.sendCommand(command);
				System.out.println("Bridge Command: "+command);

				Minecraft.getInstance().player.displayClientMessage(
						Component.literal("[Bridge] "+response),
						false
				);

					return false;
			}
                    return true;
                }
		);

	}

	static boolean Started = false;
	static String id;


	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.



		//Instance 1
		System.out.println("[Bridge] Mod loaded");


		//Instance 2

		ScreenEvents.AFTER_INIT.register((mcclient, screen, width, height) -> {
			if (!(screen instanceof TitleScreen)) return;



			EditBox portSelect = new EditBox(
					screen.getFont(),
					screen.width - 100, 0, //x y
					50, 20, // w l
					Component.literal("Number")
			);

			portSelect.setTooltip(Tooltip.create(Component.literal("Input Port")));
			portSelect.setFilter(text -> text.matches("\\d*"));

			portSelect.setValue("10000");

			Screens.getButtons(screen).add(portSelect);

			EditBox portSelect2 = new EditBox(
					screen.getFont(),
					screen.width - 50, 0, //x y
					50, 20, // w l
					Component.literal("Number")
			);

			portSelect2.setTooltip(Tooltip.create(Component.literal("Output Port")));
			portSelect2.setFilter(text -> text.matches("\\d*"));

			portSelect2.setValue("10001");

			Screens.getButtons(screen).add(portSelect2);

			Screens.getButtons(screen).add(Button.builder(
					Component.literal("Start Server"),
					button -> {
						System.out.println("hi");
						button.setAlpha(.5f);

						if (!Started) {
							String inputval = portSelect.getValue();
							int input = inputval.isEmpty() ? 0 : Integer.parseInt(inputval);

							String outputval = portSelect2.getValue();
							int output = outputval.isEmpty() ? 0 : Integer.parseInt(outputval);
							startServer(input,output);
							Started = true;
							button.setTooltip(Tooltip.create(Component.literal("Started! Input:"+inputval+" Output:"+outputval)));
						}

					})
					.pos( screen.width-100, 20)
					.size(100,20)
							.tooltip(Tooltip.create(Component.literal("Starts the server.")))
					.build());

		});


		System.out.println("Client Started.");


	}
}