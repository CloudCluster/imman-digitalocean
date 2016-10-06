package ccio.imman.tools.digitalocean;

import java.io.IOException;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Network;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.ImmanNode;

public class CheckDropletStatus extends DoAction<Boolean>{
	
	@Override
	public Boolean process(DigitalOcean apiClient, ImmanCluster immanCluster){
		System.out.println("Checking status of "+immanCluster.getClusterName()+" cluster");
		boolean result = true;
		for(ImmanNode immanNode : immanCluster.getImageNodes()){
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
			ImmanCluster.save(immanCluster);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Cluster ready: "+result);
		return result;
	}
	
	public static void main(String... args) throws IOException {
		main(new CheckDropletStatus(), args);
	}

}
