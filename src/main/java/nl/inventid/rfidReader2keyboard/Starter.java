package nl.inventid.rfidReader2keyboard;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Optional;

import nl.inventid.rfidReader2keyboard.reader.Read;

/**
 * This class is the service starter
 */
public class Starter {

	private static final String NO_GUI_FLAG = "--no-gui";

	private static Read currentReader;
	private static Status systemStatus;

	public static void main(String[] args) {
		boolean withGui = !Arrays.stream(args).filter(s -> NO_GUI_FLAG.equals(s)).findAny().isPresent();
		Starter.systemStatus = new Status();

		if(withGui) {
			// start the gui
			GUI gui = new GUI(systemStatus);
			startReader();
			gui.onStartPressed(() -> startReader());
			gui.onStopPressed(() -> stopReader());
		} else {
			startReader();
		}

	}

	public static void startReader() {
		initializeReader(Starter.systemStatus).ifPresent(read -> Starter.currentReader = read);
	}

	public static void stopReader() {
		Optional.ofNullable(Starter.currentReader).ifPresent(read -> read.stop());
		currentReader = null;
	}

	private static Optional<Read> initializeReader(Status systemStatus) {
		try {
			return Optional.ofNullable(new Read(systemStatus));
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
