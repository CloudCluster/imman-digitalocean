package ccio.imman.tools.command.digitalocean;

import java.io.IOException;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class DeleteSshKey  extends CliCommand{

	public DeleteSshKey() {
		super("delete-ssh", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
		try {
			apiClient.deleteKey(cluster.getSshKeyId());
		} catch (DigitalOceanException | RequestUnsuccessfulException e) {
			e.printStackTrace();
		}
		cluster.setSshKeyId(null);
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}
