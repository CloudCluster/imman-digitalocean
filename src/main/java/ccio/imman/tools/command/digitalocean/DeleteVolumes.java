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

public class DeleteVolumes extends CliCommand{

	public DeleteVolumes() {
		super("delete-volume", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
		for(ImmanNode node : cluster.getImageNodes()){
			try {
				if(node.getVolumeId() != null){
					System.out.println("Deleting volume for droplet "+node.getDropletName());
					apiClient.deleteVolume(node.getVolumeId());
				}
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {
				System.out.println("Cannot delete volume for droplet "+node.getDropletName());
			}
		}
		return true;
	}

}
