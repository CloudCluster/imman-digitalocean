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

public class DeleteDroplets extends CliCommand{

	public DeleteDroplets() {
		super("delete-droplet", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		System.out.println("Deleting "+cluster.getClusterName()+" cluster's droplets");
		DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
		for(ImmanNode node : cluster.getImageNodes()){
			try {
				System.out.println("Deleting droplet: "+node.getDropletName());
				apiClient.deleteDroplet(node.getDropletId());
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}