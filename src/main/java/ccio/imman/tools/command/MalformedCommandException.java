package ccio.imman.tools.command;

public class MalformedCommandException extends CliException {

	private static final long serialVersionUID = 1L;

	public MalformedCommandException(String message) {
        super(message);
    }
}