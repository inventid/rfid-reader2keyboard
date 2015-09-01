package nl.inventid.rfidReader2keyboard;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * This little program handles the reading of the RFID miFare chips.
 */
public class Read extends Thread {

	private static final String TERMINAL_REQUIRED = "ACR122";
	private static final byte[] READ_COMMAND = new byte[] { (byte) 0xFF, (byte) 0xCA, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };

	/*
	 * In face the constructor does all the work.
	 * First a keyboardrobot is initialized
	 * The card terminal is connected to
	 * Then a loop is initiated which loops indefinitely (with bounds on run-away)
	 * The loop "types" the relevant data to the PC itself
	 * Webapps handle the rest
	 */
	public Read() {
		String uid;
		// Random String; no UID of any chip. Still true though
		String oldUID = "inventid bravo!";

		try {
			Keyboard k = new Keyboard();
			CardTerminal terminal = null;

			// show the list of available terminals
			TerminalFactory factory = TerminalFactory.getDefault();
			List<CardTerminal> terminals = factory.terminals().list();

			System.out.println("Trying to attach to " + TERMINAL_REQUIRED);
			for (int i = 0; i < terminals.size(); i++) {
				System.out.println(terminals.get(i));
				if (terminals.get(i).getName().contains(TERMINAL_REQUIRED)) {
					terminal = terminals.get(i);
				}
			}

			// If our terminal is not found, throw error
			if (terminal == null) {
				throw new Exception();
			}

			// Keep looping
			while (true) {
				// Establish a connection with the card

				try {
					System.out.println("Waiting for a card...");
					// method holds indefinetely until a card was detected
					if (!terminal.isCardPresent()) {
						terminal.waitForCardPresent(0);
					}

					// Connect to card and read
					Card card = terminal.connect("T=1");
					CardChannel channel = card.getBasicChannel();
					System.out.println("Card detected...");

					// Send data and retrieve output
					uid = send(READ_COMMAND, channel);
					// If successful, the output will end with 9000, so we strip that
					uid = uid.substring(0, uid.length() - 4);

					if (!uid.equals(oldUID)) {
						System.out.println("Card detected... UID: " + uid);
						oldUID = uid;
					}
					else {
						System.out.println("Card detected... Same card");
					}
					// Emulate a keyboard and "type" the uid, followed by a newline
					k.type(uid + "\n");

					terminal.waitForCardAbsent(1000);
				}
				catch (CardException e) {
					// Something went wrong when scanning the card
					System.out.println("No card was found while scanning");
				}
				catch (Exception e) {
					// Set boldface for error
					System.out.println("Something went wrong. Remove card and try again.");
					// Sleep 1.5 seconds to allow the operator to check
					e.printStackTrace();
				}
				finally {
					Thread.sleep(500);
				}
			}
		}
		catch (Exception e) {
			// Probably no reader found...
			System.out.println("Unable to connect to RFID reader");
			//e.printStackTrace();
		}
	}

	/**
	 * Function send data to the card. Retrieves a hex String with the result
	 *
	 * @param cmd     The request to send in hex
	 * @param channel The channel to talk to the card
	 * @return String containing requested data
	 */
	public String send(byte[] cmd, CardChannel channel) {

		String res = "";

		byte[] baResp = new byte[258];
		ByteBuffer bufCmd = ByteBuffer.wrap(cmd);
		ByteBuffer bufResp = ByteBuffer.wrap(baResp);

		// output = The length of the received response APDU
		int output = 0;

		try {
			output = channel.transmit(bufCmd, bufResp);
		}
		catch (CardException ex) {
			ex.printStackTrace();
		}

		for (int i = 0; i < output; i++) {
			res += String.format("%02X", baResp[i]);
			// The result is formatted as a hexadecimal integer
		}

		return res;
	}

	public void run() {
		System.out.println("inventid RFID capturing is currently active. Close this dialog to deactivate.");
		new Read();
	}

	public static void main(String[] args) {
		System.out.println("Starting rfid-reader2keyboard");
		System.out.println("The following terminals were detected:");
		System.out.println(Read.listTerminals());

		System.out.println();
		Thread reader = new Read();
		reader.start();
	}

	public static List<CardTerminal> listTerminals() {
		// show the list of available terminals
		TerminalFactory factory = TerminalFactory.getDefault();
		try {
			return factory.terminals().list();
		}
		catch (Exception e) {
			return Lists.newArrayList();
		}

	}
}
