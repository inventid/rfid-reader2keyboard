package nl.inventid.rfidReader2keyboard;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Hex;

/**
 * This little program handles the reading of the RFID miFare chips.
 */
public class Read {

	private static final String NO_CONNECT = "connect() failed";
	private static final String EMPTY_CODE = "Scanned code was empty";
	private static final String NO_CARD = "sun.security.smartcardio.PCSCException: SCARD_E_NO_SMARTCARD";
	private static final String READER_UNAVAILABLE = "sun.security.smartcardio.PCSCException: SCARD_E_READER_UNAVAILABLE";
	private static final String FAILED_CARD_TRANSACTION =
			"sun.security.smartcardio.PCSCException: SCARD_E_NOT_TRANSACTED";
	private static final CommandAPDU READ_COMMAND = new CommandAPDU(new byte[] { (byte) 0xFF, (byte) 0xCA, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 });

	private static final List<String> TERMINAL_PREFERENCES = new ArrayList<>();
	private static final Map<String, Integer> errorMap = new HashMap();
	private static final String CARD_READ_FAILURE = "Card read failure";
	public static TerminalDetector detectorLoop;

	private static ErrorLogger errorLogger;

	public static void main(String[] args) {
		System.out.println("Starting rfid-reader2keyboard");
		System.out.println("The following terminals were detected:");
		System.out.println(Read.listTerminals());

		System.out.println();
		System.out.println("inventid RFID capturing is currently active. Close this dialog to deactivate.");
		System.out.println(
				"The most likely reason you see this is in order to resolve any issue you ay have found. Please follow"
						+ " the instructions of inventid support and send these lines to the given email address");

		errorLogger = new ErrorLogger();
		(new Thread(errorLogger)).start();

		detectorLoop = new TerminalDetector();
		(new Thread(detectorLoop)).start();

		Read reader = new Read();
		reader.startTerminalLoop();
		reader.loop();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				detectorLoop.stop();
				errorLogger.printErrorMap();
				errorLogger.stop();
				System.out.println("inventid RFID capturing is now inactive. You can close this dialog");
			}
		});
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

	public Read() {
		TERMINAL_PREFERENCES.add("ACS ACR122U PICC Interface"); // Best match
		TERMINAL_PREFERENCES.add("ACR122"); // That'll do (Windows does not include the U)
		TERMINAL_PREFERENCES.add(""); // Fuck, attach with anything (SHOULD BE LAST)

	}


	public void startTerminalLoop() {

	}

	/*
	 * In face the constructor does all the work.
	 * First a keyboardrobot is initialized
	 * The card terminal is connected to
	 * Then a loop is initiated which loops indefinitely (with bounds on run-away)
	 * The loop "types" the relevant data to the PC itself
	 * Webapps handle the rest
	 */
	private CardTerminal findAndConnectToTerminal() {
		try {
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
						System.out.println("Attached to '" + requiredTerminal + "'");
						return terminals.get(i);
					}
				}
			}
		}
		catch (Exception e) {
			// Probably no reader found...
			System.err.println("Unable to connect to RFID reader");
			e.printStackTrace();
		}
		return null;
	}

	private void loop() {
		CardTerminal terminal = findAndConnectToTerminal();
		if (terminal == null) {
			System.err.println("No terminal connected, loop is exiting");
//			return;
		}
		Keyboard keyboard = new Keyboard();

		// Random String; no UID of any chip. Still true though
		String oldUID = "inventid bravo!";
		Instant lastScan = null;
		int i = 0;
		// Keep looping
		while (true) {
			try {
				// Connect to card and read
				Card card = terminal.connect("T=1");
				CardChannel channel = card.getBasicChannel();

				// Send data and retrieve output
				String uid = getCardUid(channel);

				if (!isNewCard(uid, oldUID, lastScan)) {
					continue;
				}

				System.out.println("This is a new card! " + uid);
				// Emulate a keyboard and "type" the uid, followed by a newline
				keyboard.type(uid);
				keyboard.type("\n");

				i++;
				oldUID = uid;
				lastScan = Instant.now();
				System.out.println("ready for next card");
				card.disconnect(false);
				System.out.println("Test run: " + i);
			}
			catch (CardException e) {
				// Something went wrong when scanning the card
				if (e.getMessage().equals(FAILED_CARD_TRANSACTION) || e.getMessage().equals(READER_UNAVAILABLE)) {
					logError(e.getMessage());
					terminal = findAndConnectToTerminal();
					continue;
				}
				// Card is not present while scanning
				if (e.getMessage().equals(NO_CARD) || e instanceof CardNotPresentException) {
					logError(e.getMessage());
					continue;
				}
				// Could not reliably connect to the reader (this can mean there is simply no card)
				if (e.getMessage().equals(NO_CONNECT) || e.getMessage().equals(CARD_READ_FAILURE)) {
					logError(e.getMessage());
					continue;
				}
				if (e.getMessage().equals(EMPTY_CODE)) {
					logError(e.getMessage());
					System.err.println("Empty code was read");
					continue;
				}
				System.err.println("Help something uncatched happened! This should not happen!");
				logError(e.getMessage());
				e.printStackTrace();
				System.out.println(e.getMessage());
				terminal = findAndConnectToTerminal();
			}
		}
	}

	/**
	 * Get the uid of a card
	 *
	 * @param channel the channel to transmit over
	 * @return a String with the value of the uid (not empty)
	 * @throws CardException in case of an error
	 */
	private String getCardUid(CardChannel channel) throws CardException {
		ResponseAPDU response = channel.transmit(READ_COMMAND);
		String uid = new String(Hex.encodeHex(response.getData())).toUpperCase();
		if (!new String(Hex.encodeHex(response.getBytes())).endsWith("9000")) {
			throw new CardException(CARD_READ_FAILURE);
		}
		if (uid.isEmpty()) {
			throw new CardException(EMPTY_CODE);
		}
		return uid;
	}

	/**
	 * @param newUid   the newly scanned UID
	 * @param oldUid   the previously scanned code
	 * @param lastScan the time of the last successful scan
	 * @return Return true if the card is different OR if the previous card was scanned over 1 second before
	 */
	private boolean isNewCard(String newUid, String oldUid, Instant lastScan) {
		return !newUid.equals(oldUid) || lastScan == null ||
				(lastScan != null && lastScan.plus(1, ChronoUnit.SECONDS).isBefore(Instant.now()));
	}

	private void logError(String errorCause) {
		Integer newValue = errorMap.getOrDefault(errorCause, 0) + 1;
		errorMap.put(errorCause, newValue);
	}

	private static class ErrorLogger implements Runnable {

		private boolean interrupted;

		public void run() {
			while(!interrupted) {
				printErrorMap();
				try {
					Thread.sleep(60000);
				}
				catch (InterruptedException e) {
				}
			}
		}

		public void stop() {
			interrupted = true;
		}

		public void printErrorMap() {
			System.out.println("Error map: " + errorMap.entrySet());
		}
	}

}
