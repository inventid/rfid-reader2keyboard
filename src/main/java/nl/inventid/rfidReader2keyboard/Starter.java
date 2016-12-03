package nl.inventid.rfidReader2keyboard;

import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import nl.inventid.rfidReader2keyboard.reader.Reader;

/**
 * This class is the service starter
 */
public class Starter {

	enum Flags {
		NO_GUI("--no-gui"),
		NO_AUTOSTART("--no-autostart"),
		NO_BUZZ("--no-buzzer"),
		DEBUG("--debug"),
		;

		private final String flagParameter;

		Flags(String s) {
			this.flagParameter = s;
		}

		static Optional<Flags> stringToFlag(String s) {
			return Arrays.stream(Flags.values()).filter(flags -> flags.flagParameter.equals(s)).findFirst();
		}
	}

	private static Reader currentReader;
	private static SystemStatus systemStatus;
	private static boolean shouldBuzz;


	public static void main(String[] args) {
		List<Flags> parameters = Arrays.stream(args).map(s -> Flags.stringToFlag(s))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		boolean withGui = !parameters.contains(Flags.NO_GUI);
		boolean shouldAutostart = !parameters.contains(Flags.NO_AUTOSTART);
		shouldBuzz = !parameters.contains(Flags.NO_BUZZ);

		System.out.println("Will start with the following parameters: " + parameters);

		Starter.systemStatus = new SystemStatus();
		if(parameters.contains(Flags.DEBUG)) {
			System.out.println("Will periodically log all information about the system");
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.scheduleAtFixedRate(() -> System.out.println(systemStatus), 1, 5, TimeUnit.SECONDS);
		}

		if (withGui) {
			// start the gui
			GUI gui = new GUI(systemStatus, shouldAutostart);
			startReader(shouldAutostart);
			// When restarting, always start it
			gui.onStartPressed(() -> startReader(true));
			gui.onStopPressed(() -> stopReader());
		}
		else {
			startReader(shouldAutostart);
		}

	}

	public static void startReader(boolean start) {
		initializeReader(Starter.systemStatus).ifPresent(reader -> {
			Starter.currentReader = reader;
			if (start) {
				reader.start();
			}
		});
	}

	public static void stopReader() {
		Optional.ofNullable(Starter.currentReader).ifPresent(reader -> reader.stop());
		currentReader = null;
	}

	private static Optional<Reader> initializeReader(SystemStatus systemStatus) {
		try {
			return Optional.ofNullable(new Reader(systemStatus, shouldBuzz));
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
