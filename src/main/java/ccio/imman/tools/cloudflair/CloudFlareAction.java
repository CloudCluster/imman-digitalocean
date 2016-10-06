package ccio.imman.tools.cloudflair;

import java.util.Scanner;

import ccio.imman.tools.ImmanCluster;

public abstract class CloudFlareAction {
	
	public abstract void apply(CloudFlareService service, ImmanCluster cluster);
	
	public void withInput(Scanner keyboard, CloudFlareService service, ImmanCluster cluster){
		
		if(cluster.getCfZone() == null){
			System.out.print("CloudFalir Zone (eg. ccio.co): ");
			String val = keyboard.nextLine();
			cluster.setCfZone(val);
		}
		
		if(cluster.getCfSubDomain() == null){
			System.out.print("CloudFalir Subdomain: ");
			String val = keyboard.nextLine();
			cluster.setCfSubDomain(val);
		}
		
		apply(service, cluster);
	}
}