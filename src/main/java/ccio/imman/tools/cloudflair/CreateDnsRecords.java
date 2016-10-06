package ccio.imman.tools.cloudflair;

import java.io.IOException;

import org.json.JSONException;

import com.mashape.unirest.http.exceptions.UnirestException;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.ImmanNode;

public class CreateDnsRecords extends CloudFlareAction{

	@Override
	public void apply(CloudFlareService service, ImmanCluster cluster) {
		System.out.println("Adding DNS Records for "+cluster.getCfSubDomain()+"."+cluster.getCfZone());
		for(ImmanNode node : cluster.getImageNodes()){
			try {
				String recId = service.addRecord(cluster.getCfSubDomain(), cluster.getCfZone(), node.getPublicIp());
				node.setCloudFlareDnsId(recId);
			} catch (JSONException | UnirestException e) {
				e.printStackTrace();
			}
		}
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
