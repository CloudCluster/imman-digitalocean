package ccio.imman.tools.command.digitalocean;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;

import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class DropletStop extends CliCommand{

	public DropletStop() {
		super("stop-droplet", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
		
		System.out.println("Stoping "+cluster.getClusterName()+" cluster's droplets");
		for(ImmanNode node : cluster.getImageNodes()){
			try {
				System.out.println("Stoping droplet: "+node.getDropletName());
				apiClient.powerOffDroplet(node.getDropletId());
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {
				throw new CliException(e);
			}
		}
		return true;
	}

}
