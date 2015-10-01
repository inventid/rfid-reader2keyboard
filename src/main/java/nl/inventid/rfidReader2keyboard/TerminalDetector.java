package nl.inventid.rfidReader2keyboard;

/**
 * Created by Rogier on 01-10-15.
 */
public class TerminalDetector implements Runnable {

	private boolean interrupted;

	public void run() {
		while(!interrupted) {
			System.out.println(Read.listTerminals());
			try {
				Thread.sleep(10000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Terminating run");
	}

	public void stop() {
		interrupted = true;
	}
}
