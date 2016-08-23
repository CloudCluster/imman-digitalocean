package ccio.imman.tools.digitalocean;

import java.io.IOException;
import java.util.Scanner;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;

import ccio.imman.tools.digitalocean.model.ImmanCluster;

public abstract class DoAction<T> {
	
	public abstract T process(DigitalOcean apiClient, ImmanCluster immanCluster);
	
	public T withInput(Scanner keyboard, DigitalOcean apiClient, ImmanCluster cluster) throws IOException{
		if(cluster == null){
			System.out.print("Cluster Name: ");
			String clusterName = keyboard.nextLine();
			cluster = ImmanCluster.read(clusterName);
		}

		if(apiClient == null){
			System.out.print("DigitalOcean Token: ");
			String token = keyboard.nextLine();
			apiClient = new DigitalOceanClient(token);
		};
		
		return process(apiClient, cluster);
	}
	
	public static void main(DoAction<?> action, String... args) throws IOException {
		Scanner keyboard = new Scanner(System.in);
		
		ImmanCluster cluster = null;
		if(args.length>0){
			cluster = ImmanCluster.read(args[0]);
		}

		DigitalOcean apiClient = null;
		if(args.length>1){
			apiClient = new DigitalOceanClient(args[1]);
		}
		
		action.withInput(keyboard, apiClient, cluster);
		
		keyboard.close();
	}
}
