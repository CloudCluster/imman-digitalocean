package ccio.imman.tools.command;

import com.fasterxml.jackson.core.JsonProcessingException;

import ccio.imman.tools.ImmanCluster;
import jline.console.ConsoleReader;

public class ShowClusterCommand extends CliCommand{

	public ShowClusterCommand() {
		super("show", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		try {
			out.println(ImmanCluster.toString(cluster));
			return true;
		} catch (JsonProcessingException e) {
			throw new CliException(e.getMessage());
		}
	}

}
