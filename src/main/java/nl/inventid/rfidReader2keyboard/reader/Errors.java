package nl.inventid.rfidReader2keyboard.reader;

public class Errors {
	// Annoyingly catching java errors which are inconsistent across platforms
	static final String NO_CONNECT = "connect() failed";
	static final String EMPTY_CODE = "Scanned code was empty";
	static final String NO_CARD = "sun.security.smartcardio.PCSCException: SCARD_E_NO_SMARTCARD";
	static final String REMOVED_CARD = "sun.security.smartcardio.PCSCException: SCARD_W_REMOVED_CARD";
	static final String READER_UNAVAILABLE =
			"sun.security.smartcardio.PCSCException: SCARD_E_READER_UNAVAILABLE";
	static final String CARD_READ_FAILURE = "Card read failure";
	static final String FAILED_CARD_TRANSACTION =
			"sun.security.smartcardio.PCSCException: SCARD_E_NOT_TRANSACTED";
}
