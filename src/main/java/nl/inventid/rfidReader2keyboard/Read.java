package nl.inventid.rfidReader2keyboard;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * This little program handles the reading of the RFID miFare chips.
 */
public class Read {

	private static int scanCount = 0;
	private static final List<String> TERMINAL_PREFERENCES = new ArrayList<>();
	private static final byte[] READ_COMMAND = new byte[] { (byte) 0xFF, (byte) 0xCA, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };

	public static void main(String[] args) {
		System.out.println("Starting rfid-reader2keyboard");
		System.out.println("The following terminals were detected:");
		System.out.println(Read.listTerminals());

		System.out.println();
		System.out.println("inventid RFID capturing is currently active. Close this dialog to deactivate.");
		System.out.println(
				"The most likely reason you see this is in order to resolve any issue you ay have found. Please follow"
						+ " the instructions of inventid support and send these lines to the given email address");
		Read reader = new Read();
		reader.startTerminalLoop();
		reader.loop();
		System.out.println("inventid RFID capturing is now inactive. You can close this dialog");
	}

	/**
	 * Get the currently connected terminals
	 *
	 * @return the list of found terminals
	 */
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

	private Keyboard keyboard;
	private CardTerminal terminal;

	public Read() {
		setupPrefs();
		prepare();
	}

	private void setupPrefs() {
		TERMINAL_PREFERENCES.add("ACS ACR122U PICC Interface"); // Best match
		TERMINAL_PREFERENCES.add("ACR122"); // That'll do (Windows does not include the U)
		TERMINAL_PREFERENCES.add(""); // Fuck, attach to anything (SHOULD BE LAST)
	}

	public void startTerminalLoop() {
		(new Thread(new TerminalDetector())).start();
	}
	/*
	 * In face the constructor does all the work.
	 * First a keyboardrobot is initialized
	 * The card terminal is connected to
	 * Then a loop is initiated which loops indefinitely (with bounds on run-away)
	 * The loop "types" the relevant data to the PC itself
	 * Webapps handle the rest
	 */
	private void prepare() {
		try {
			keyboard = new Keyboard();

			// show the list of available terminals
			TerminalFactory factory = TerminalFactory.getDefault();
			List<CardTerminal> terminals = factory.terminals().list();

			System.out.println("There are " + TERMINAL_PREFERENCES.size() + " possible terminal matches");
			System.out.println("There are " + terminals.size() + " terminals attached to this machine");

			for (int j = 0; j < TERMINAL_PREFERENCES.size(); j++) {
				String requiredTerminal = TERMINAL_PREFERENCES.get(j);
				System.out.println("Trying to attach to '" + requiredTerminal + "'");
				for (int i = 0; i < terminals.size(); i++) {
					if (terminals.get(i).getName().contains(requiredTerminal)) {
						terminal = terminals.get(i);
						return;
					}
				}
			}
		}
		catch (Exception e) {
			// Probably no reader found...
			System.err.println("Unable to connect to RFID reader");
			e.printStackTrace();

			terminal = null;
		}
	}

	private void loop() {
		if (terminal == null) {
			System.err.println("No terminal connected, loops is exiting");
			return;
		}

		String uid;
		// Random String; no UID of any chip. Still true though
		String oldUID = "inventid bravo!";
		// Keep looping
		while (true) {
			// Establish a connection with the card

			try {
				System.out.println("Waiting for a card...");
				// method holds indefinitely until a card was detected
//				if (!terminal.isCardPresent()) {
//					terminal.waitForCardPresent(0);
//				}

				// Connect to card and read
				Card card = terminal.connect("T=1");
				CardChannel channel = card.getBasicChannel();
				System.out.println("Card detected...");
				Read.scanCount++;
				System.out.println("Scan count is now " + Read.scanCount);

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
				System.out.println("will type");
				keyboard.type(uid);
				keyboard.type("\n");

				System.out.println("typed stuff");
				card.disconnect(false);
				System.out.println("disconnected card");
				Thread.sleep(1000);
				System.out.println("ready for next card");
			}
			catch (CardException e) {
				// Something went wrong when scanning the card
				System.err.println("No card was found while scanning");
				e.printStackTrace();
//				try {
//				Thread.sleep(1000);
//			}
//			catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
			}
			catch (Exception e) {
				System.err.println("Something went wrong. Remove card and try again.");
				e.printStackTrace();
			}
			finally {
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
					System.err.println("Got interrupted while sleeping! Strange!");
				}
			}
		}
	}

	/**
	 * Function send data to the card. Retrieves a hex String with the result
	 *
	 * @param cmd     The request to send in hex
	 * @param channel The channel to talk to the card
	 * @return String containing requested data
	 */
	private String send(byte[] cmd, CardChannel channel) {

		byte[] baResp = new byte[258];
		ByteBuffer bufCmd = ByteBuffer.wrap(cmd);
		ByteBuffer bufResp = ByteBuffer.wrap(baResp);

		// output = The length of the received response APDU
		int output = 0;

		try {
			output = channel.transmit(bufCmd, bufResp);
			String res = "";
			for (int i = 0; i < output; i++) {
				res += String.format("%02X", baResp[i]);
				// The result is formatted as a hexadecimal integer
			}

			return res;
		}
		catch (CardException ex) {
			ex.printStackTrace();
			return "";
		}

	}

}
