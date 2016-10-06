package ccio.imman.tools.digitalocean;

import java.io.IOException;
import java.util.Scanner;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Volume;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.ImmanNode;

public class CreateVolumes extends DoAction<Volume[]>{
	
	@Override
	public Volume[] process(DigitalOcean apiClient, ImmanCluster immanCluster) {

		Volume[] result = new Volume[immanCluster.getImageNodes().size()];
		int i=0;
		for(ImmanNode node : immanCluster.getImageNodes()){
			System.out.println("Creating volume of size "+immanCluster.getVolumeSizeGigabytes()+"Gb for Droplet "+node.getDropletName());
			Volume volume = new Volume();
			volume.setName("vol-"+node.getDropletName());
			volume.setRegion(new Region(node.getRegionSlug()));
			volume.setSize((double)immanCluster.getVolumeSizeGigabytes());
			try {
				Volume v = apiClient.createVolume(volume);
				result[i]=v;
				apiClient.attachVolume(node.getDropletId(), v.getId(), node.getRegionSlug());
				node.setVolumeId(v.getId());
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {
				System.out.println("Cannot create volume for Droplet "+node.getDropletName()+": "+e.getMessage());
			}
			i++;
		}
		return result;
	}

	@Override
	public Volume[] withInput(Scanner keyboard, DigitalOcean apiClient, ImmanCluster cluster) throws IOException {
		if(cluster == null){
			System.out.print("Cluster Name: ");
			String clusterName = keyboard.nextLine();
			cluster = ImmanCluster.read(clusterName);
		}

		if(apiClient == null){
			System.out.print("DigitalOcean Token: ");
			String token = keyboard.nextLine();
			apiClient = new DigitalOceanClient(token);
		}
		
		System.out.print("Enter Volume Size in Gigabytes: ");
		int volumeSize = keyboard.nextInt();
		cluster.setVolumeSizeGigabytes(volumeSize);
		
		Volume[] volumes = process(apiClient, cluster);
		
		ImmanCluster.save(cluster);
//		System.out.println("----------------");
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(cluster));
		
		return volumes;
	}
	
	public static void main(String[] args) throws IOException {
		main(new CreateVolumes(), args);
	}

}
