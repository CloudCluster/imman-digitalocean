package ccio.imman.tools.command.digitalocean;

import java.io.IOException;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class SetDigitalOcean extends CliCommand{

	private String doToken; 
	
	public SetDigitalOcean() {
		super("set-do", "doToken");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		if(cmdArgs == null || cmdArgs.length!=2){
			throw new CliParseException("requered arguments: doToken");
		}
		doToken = cmdArgs[1];
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		cluster.setDoToken(doToken);
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			throw new CliException(e);
		}
		return true;
	}

}
