package nl.inventid.rfidReader2keyboard;

import javax.smartcardio.CommandAPDU;

public class Commands {
	static final CommandAPDU READ =
			new CommandAPDU(new byte[] { (byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00 });

	static final CommandAPDU DISABLE_BUZZER =
			new CommandAPDU(new byte[] { (byte) 0xFF, (byte) 0x00, (byte) 0x52, (byte) 0x00, (byte) 0x00 });

	static final byte[] ONE_BUZZ_APDU =
			new byte[] { (byte) 0xFF, (byte) 0x00, (byte) 0x40, (byte) 0x40, (byte) 0x04 };
	// This is the format to set the initial leds to off, set the beep timing to 100ms, beep just once and return to
	// default. http://www.acs.com.hk/download-manual/419/API-ACR122U-2.03.pdf
	static final byte[] ONE_BUZZ_DATA =
			new byte[] { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x02 };
}
