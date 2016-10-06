package ccio.imman.tools.command;

import org.apache.commons.cli.ParseException;

public class CliParseException extends CliException {
	
	private static final long serialVersionUID = 1L;

	public CliParseException(ParseException parseException) {
        super(parseException);
    }

    public CliParseException(String message) {
        super(message);
    }
}