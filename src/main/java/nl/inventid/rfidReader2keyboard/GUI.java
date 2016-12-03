package nl.inventid.rfidReader2keyboard;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import nl.inventid.rfidReader2keyboard.reader.Read;

/**
 * This class starts and handles the GUI
 */
public class GUI {

	public static final String STOP = "Stop";
	private static final String START = "Start";
	private final Status systemStatus;

	private JFrame frame;
	private JLabel textLabel;
	private JButton button;
	private String buttonActionType;
	private Runnable onStartPressedAction;
	private Runnable onStopPressedAction;

	public GUI(Status systemStatus) {
		this.systemStatus = systemStatus;

		frame = new JFrame();
		frame.setBounds(10, 10, 300, 80);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new FlowLayout());

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
			if(buttonActionType.equals(START)) {
				onStartPressedAction.run();
				setButtonText(STOP);
				buttonActionType = STOP;
			} else if(buttonActionType.equals(STOP)) {
				onStopPressedAction.run();
				setButtonText(START);
				buttonActionType = START;
			} else {
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
		setButtonText(STOP);
		buttonActionType = STOP;
		frame.repaint();
		systemStatus.onChange(() -> setLabelText(statusString()));
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
		if(!systemStatus.isRunning()) {
			return "System is not active";
		}
		// System is active
		if(!systemStatus.isFoundReader()) {
			return "Could not find an appropriate RFID reader";
		}
		if (systemStatus.isReaderRunning()) {
			return "Ready to scan tickets";
		}
		System.out.println(systemStatus);
		return "Unknown status";
	}
}
