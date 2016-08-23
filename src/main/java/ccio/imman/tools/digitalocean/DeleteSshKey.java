package ccio.imman.tools.digitalocean;

import java.io.IOException;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;

import ccio.imman.tools.digitalocean.model.ImmanCluster;

public class DeleteSshKey extends DoAction<Void>{
	
	@Override
	public Void process(DigitalOcean apiClient, ImmanCluster immanCluster) {
		System.out.println("Deleting SSH Key with ID: "+immanCluster.getSshKeyId());
		try {
			apiClient.deleteKey(immanCluster.getSshKeyId());
		} catch (DigitalOceanException | RequestUnsuccessfulException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		main(new DeleteSshKey(), args);
	}

}
