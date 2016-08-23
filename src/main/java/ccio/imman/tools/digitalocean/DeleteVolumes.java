package ccio.imman.tools.digitalocean;

import java.io.IOException;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;

import ccio.imman.tools.digitalocean.model.ImmanCluster;
import ccio.imman.tools.digitalocean.model.ImmanNode;

public class DeleteVolumes extends DoAction<Void>{

	@Override
	public Void process(DigitalOcean apiClient, ImmanCluster immanCluster) {
		System.out.println("Deleting volumes");
		for(ImmanNode node : immanCluster.getImageNodes()){
			try {
				if(node.getVolumeId() != null){
					System.out.println("Deleting volume for droplet "+node.getDropletName());
					apiClient.deleteVolume(node.getVolumeId());
				}
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {
				System.out.println("Cannot delete volume for droplet "+node.getDropletName());
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		main(new DeleteVolumes(), args);
	}
}
