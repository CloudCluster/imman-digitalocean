package ccio.imman.tools.cloudflair;

import org.json.JSONException;

import com.mashape.unirest.http.exceptions.UnirestException;

import ccio.imman.tools.digitalocean.model.ImmanCluster;
import ccio.imman.tools.digitalocean.model.ImmanNode;

public class DeleteDnsRecords extends CloudFlareAction{

	@Override
	public void apply(CloudFlareService service, ImmanCluster cluster) {
		System.out.println("Deleting DNS Records for "+cluster.getCfSubDomain()+"."+cluster.getCfZone());
		boolean res = true;
		for(ImmanNode node : cluster.getImageNodes()){
			try {
				res = res && service.deleteRecord(cluster.getCfZone(), node.getCloudFlareDnsId());
			} catch (JSONException | UnirestException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All records deleted successfuly: "+res);
	}

}
