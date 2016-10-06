package ccio.imman.tools.command.digitalocean;

import java.io.IOException;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Network;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class DropletStatus extends CliCommand{

	public DropletStatus() {
		super("status-droplet", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		try{
			DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
			
			boolean result = true;
			for(ImmanNode immanNode : cluster.getImageNodes()){
				try {
					Droplet d = apiClient.getDropletInfo(immanNode.getDropletId());
					System.out.println(d.getName() + ": " + d.isActive());
					result = result && d.isActive();
					if(d.isActive()){
						for(Network network : d.getNetworks().getVersion4Networks()){
							if("private".equalsIgnoreCase(network.getType())){
								immanNode.setPrivateIp(network.getIpAddress());
							} else if("public".equalsIgnoreCase(network.getType())){
								immanNode.setPublicIp(network.getIpAddress());
							}
						}
					}
				} catch (DigitalOceanException | RequestUnsuccessfulException e) {
					System.out.println("Droplet "+immanNode.getDropletName()+" message: "+e.getMessage());
				}
			}
			try {
				ImmanCluster.save(cluster);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Cluster ready: "+result);
			return true;
		}catch(Exception e){
			throw new CliException(e);
		}
	}

}
