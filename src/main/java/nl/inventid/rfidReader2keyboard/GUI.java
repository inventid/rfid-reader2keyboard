package nl.inventid.rfidReader2keyboard;

import javax.swing.*;

import java.awt.*;

/**
 * This class starts and handles the GUI
 */
public class GUI {

	enum ButtonStatus {
		STOP("Stop"),
		START("Start");

		private final String buttonValue;

		ButtonStatus(String s) {
			buttonValue = s;
		}
	}

	private static final int OFFSET_X = 10;
	private static final int OFFSET_Y = 10;
	private static final int WIDTH = 300;
	private static final int HEIGHT = 80;

	private final SystemStatus systemStatus;
	private final JFrame frame;
	private final JLabel textLabel;
	private final JButton button;

	private ButtonStatus buttonActionType;
	private Runnable onStartPressedAction;
	private Runnable onStopPressedAction;

	public GUI(SystemStatus systemStatus, boolean shouldAutostart) {
		this.systemStatus = systemStatus;

		frame = new JFrame();
		frame.setBounds(frameRectangle());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(2, 1));

		textLabel = new JLabel(statusString());
		textLabel.setForeground(Color.BLACK);
		textLabel.setOpaque(true);
		textLabel.setHorizontalAlignment(JLabel.CENTER);
		frame.getContentPane().add(textLabel);

		button = new JButton();
		button.setForeground(Color.BLACK);
		button.setOpaque(true);
		button.setHorizontalAlignment(JLabel.CENTER);
		button.addActionListener(e -> {
			if (buttonActionType == ButtonStatus.START) {
				onStartPressedAction.run();
				setButtonText(ButtonStatus.STOP.buttonValue);
				buttonActionType = ButtonStatus.STOP;
			}
			else if (buttonActionType == ButtonStatus.STOP) {
				onStopPressedAction.run();
				setButtonText(ButtonStatus.START.buttonValue);
				buttonActionType = ButtonStatus.START;
			}
			else {
				System.err.println("Undefined action detected");
				System.err.println(systemStatus);
				return;
			}
			setLabelText(statusString());
			frame.repaint();
		});
		frame.getContentPane().add(button);
		frame.setVisible(true);
		setLabelText(statusString());
		if(shouldAutostart) {
			setButtonText(ButtonStatus.STOP.buttonValue);
			buttonActionType = ButtonStatus.STOP;
		} else {
			setButtonText(ButtonStatus.START.buttonValue);
			buttonActionType = ButtonStatus.START;
		}
		frame.repaint();
		systemStatus.onChange(() -> setLabelText(statusString()));
	}

	private Rectangle frameRectangle() {
		Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
		int frameLocationX = (int) resolution.getWidth() - WIDTH - OFFSET_X;
		int frameLocationY = (int) resolution.getHeight() - HEIGHT - OFFSET_Y;

		return new Rectangle(frameLocationX, frameLocationY, WIDTH, HEIGHT);
	}

	public void onStartPressed(Runnable action) {
		onStartPressedAction = action;
	}

	public void onStopPressed(Runnable action) {
		onStopPressedAction = action;
	}

	private void setLabelText(String text) {
		textLabel.setText(text);
		textLabel.repaint();
		frame.repaint();
	}

	private void setButtonText(String text) {
		button.setText(text);
		frame.repaint();
	}

	private String statusString() {
		if (!systemStatus.isRunning()) {
			return "RFID scanning system is not active";
		}
		// System is active
		if (!systemStatus.isFoundReader()) {
			return "Could not find an appropriate RFID reader";
		}
		if (systemStatus.isReaderRunning()) {
			return "Ready to scan tickets";
		}
		System.out.println(systemStatus);
		return "Unknown status";
	}
}
