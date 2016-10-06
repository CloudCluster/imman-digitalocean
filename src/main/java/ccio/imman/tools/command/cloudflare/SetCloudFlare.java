package ccio.imman.tools.command.cloudflare;

import java.io.IOException;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class SetCloudFlare extends CliCommand{

	private String cfToken; 
	private String cfEmail;
	private String cfDns;
	private String cfZone;
	
	public SetCloudFlare() {
		super("set-cf", "cfToken cfEmail cfSubdomain cfZone");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		if(cmdArgs == null || cmdArgs.length!=5){
			throw new CliParseException("requered arguments: cfToken cfEmail cfSubdomain cfZone");
		}
		cfToken = cmdArgs[1];
		cfEmail = cmdArgs[2];
		cfDns = cmdArgs[3];
		cfZone = cmdArgs[4];
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		cluster.setCfToken(cfToken);
		cluster.setCfEmail(cfEmail);
		cluster.setCfSubDomain(cfDns);
		cluster.setCfZone(cfZone);
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			throw new CliException(e);
		}
		return true;
	}

}
