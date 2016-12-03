package nl.inventid.rfidReader2keyboard;

import java.util.Optional;

import lombok.Getter;
import lombok.ToString;

/**
 * This class functions as a global status class which communicates the status between the reader and the gui
 */
@ToString
public class Status {

	@Getter
	private boolean running;

	@Getter
	private boolean terminalsDetected;

	@Getter
	private boolean schedulersStarted;

	@Getter
	private boolean readerStarted;

	@Getter
	private boolean readerRunning;

	@Getter
	private boolean foundReader;

	private Optional<Runnable> onChangeAction = Optional.empty();

	public void onChange(Runnable action) {
		this.onChangeAction = Optional.of(action);
	}


	public void setRunning(boolean running) {
		this.running = running;
		onChangeAction.ifPresent(Runnable::run);
	}

	public void setTerminalsDetected(boolean terminalsDetected) {
		this.terminalsDetected = terminalsDetected;
		onChangeAction.ifPresent(Runnable::run);
	}

	public void setSchedulersStarted(boolean schedulersStarted) {
		this.schedulersStarted = schedulersStarted;
		onChangeAction.ifPresent(Runnable::run);
	}

	public void setReaderStarted(boolean readerStarted) {
		this.readerStarted = readerStarted;
		onChangeAction.ifPresent(Runnable::run);
	}

	public void setReaderRunning(boolean readerRunning) {
		this.readerRunning = readerRunning;
		onChangeAction.ifPresent(Runnable::run);
	}

	public void setFoundReader(boolean foundReader) {
		this.foundReader = foundReader;
	}
}
