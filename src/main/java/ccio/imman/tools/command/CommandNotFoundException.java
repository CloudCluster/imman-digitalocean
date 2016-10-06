package ccio.imman.tools.command;

public class CommandNotFoundException extends CliException {

	private static final long serialVersionUID = 1L;

	public CommandNotFoundException(String command) {
        super("Command not found: " + command, 127);
    }
}
