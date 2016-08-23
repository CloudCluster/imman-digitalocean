package ccio.imman.tools;

import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;

import ccio.imman.tools.cloudflair.CloudFlareService;
import ccio.imman.tools.cloudflair.CreateDnsRecords;
import ccio.imman.tools.cloudflair.DeleteDnsRecords;
import ccio.imman.tools.digitalocean.CheckDropletStatus;
import ccio.imman.tools.digitalocean.CreateDroplets;
import ccio.imman.tools.digitalocean.CreateVolumes;
import ccio.imman.tools.digitalocean.DeleteDropolets;
import ccio.imman.tools.digitalocean.DeleteSshKey;
import ccio.imman.tools.digitalocean.DeleteVolumes;
import ccio.imman.tools.digitalocean.StartDropolets;
import ccio.imman.tools.digitalocean.StopDropolets;
import ccio.imman.tools.digitalocean.model.ImmanCluster;
import ccio.imman.tools.ssh.CleanFiles;
import ccio.imman.tools.ssh.ConfigFile;
import ccio.imman.tools.ssh.CopyJar;
import ccio.imman.tools.ssh.Firewall;
import ccio.imman.tools.ssh.SetupNode;

public class Main {

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder("c").longOpt("cluster-name").argName("cluster-name").hasArg().required().desc("Cluster Name").build());
		options.addOption(Option.builder("n").longOpt("nodes-number").argName("nodes-number").hasArg().required(false).desc("Number of nodes in the cluster").build());
		options.addOption(Option.builder("iw").longOpt("image-widths").argName("image-widths").hasArg().required(false).desc("Comma separated acceptable widths (e.g. 100,300). No width resize if not set.").build());
		options.addOption(Option.builder("ih").longOpt("image-heights").argName("image-heights").hasArg().required(false).desc("Comma separated acceptable heghts (e.g. 100,300). No heght resize if not set.").build());
		options.addOption(Option.builder("s3a").longOpt("s3-access").argName("s3-access").hasArg().required(false).desc("AWS S3 Access Code").build());
		options.addOption(Option.builder("s3s").longOpt("s3-secret").argName("s3-secret").hasArg().required(false).desc("AWS S3 Secret Code").build());
		options.addOption(Option.builder("s3b").longOpt("s3-bucket").argName("s3-bucket").hasArg().required(false).desc("AWS S3 Bucket Name").build());
		options.addOption(Option.builder("do").longOpt("do-token").argName("do-token").hasArg().required().desc("Digital Ocean API Token").build());
		options.addOption(Option.builder("cft").longOpt("cf-token").argName("cf-token").hasArg().required(false).desc("Cloud Flare API Token").build());
		options.addOption(Option.builder("cfe").longOpt("cf-email").argName("cf-email").hasArg().required(false).desc("Cloud Flare API Email").build());
		options.addOption(Option.builder("cfz").longOpt("cf-zone").argName("cf-zone").hasArg().required(false).desc("Cloud Flare DNS Zone, e.q. cloudcluster.io").build());
		options.addOption(Option.builder("cfd").longOpt("cf-dns-subdomain").argName("cf-dns-subdomain").hasArg().required(false).desc("Cloud Flare DNS sub domain, e.g. mycluster for mycluster.cloudcluster.io").build());
		options.addOption("h", "help", false, "Print help");

		CommandLineParser parser = new DefaultParser();

		try (Scanner keyboard = new Scanner(System.in);){
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("h")) {
				printHelp(options);
				return;
			}
			
			String clusterName = line.getOptionValue("c");
			if(clusterName == null){
				System.out.print("Cluster Name: ");
				clusterName = keyboard.nextLine();
			}else{
				System.out.println("Cluster Name: "+clusterName);
			}
			
			ImmanCluster cluster = null;
			try{
				cluster = ImmanCluster.read(clusterName);
			}catch(IOException e){
				cluster = new ImmanCluster();
				cluster.setClusterName(clusterName);
				cluster.setCfSubDomain(line.getOptionValue("cfd"));
				cluster.setCfZone(line.getOptionValue("cfz"));
				cluster.setS3Access(line.getOptionValue("s3a"));
				cluster.setS3Secret(line.getOptionValue("s3s"));
				cluster.setS3Bucket(line.getOptionValue("s3b"));
				cluster.setWidths(line.getOptionValue("iw"));
				cluster.setHeights(line.getOptionValue("ih"));
				
				ImmanCluster.save(cluster);
			}

			String token = line.getOptionValue("do");
			if(token == null){
				System.out.print("DigitalOcean Token: ");
				token = keyboard.nextLine();
			}
			
			DigitalOcean apiClient = new DigitalOceanClient(token);
			
			String cfEmail = line.getOptionValue("cfe");
			if(cfEmail == null){
				System.out.print("CloudFlair Email: ");
				cfEmail = keyboard.nextLine();
			}
			
			String cfToken = line.getOptionValue("cft");
			if(cfToken == null){
				System.out.print("CloudFlair Token: ");
				cfToken = keyboard.nextLine();
			}
			
			CloudFlareService cloudFlareService = new CloudFlareService(cfEmail, cfToken);
			
			boolean loop = true;
			
			while(loop){
				System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				System.out.println("What do you want to do next:");
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				System.out.println("1: Create SSH Key and Droplets for the Cluster");
				System.out.println("2: Check Droplet's Status");
				System.out.println("3: Create and attach Volumes to each Droplet");
				System.out.println("4: Setup Nodes");
				System.out.println("5: Copy Config File");
				System.out.println("6: Copy JAR to the nodes and set Service up");
				System.out.println("7: Create DNS Records");
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				System.out.println("41: re-Apply Firewall");
				System.out.println("61: Stopping all Droplets");
				System.out.println("62: Starting all Droplets");
				System.out.println("81: Delete all cached files");
				System.out.println("99: Delete whole Cluster");
				System.out.println("0: Exit");
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				
				int oper = keyboard.nextInt();
				
				switch(oper){
				
					case 1:
						cluster = ImmanCluster.read(clusterName);
						cluster.setClusterName(clusterName);
						new CreateDroplets().withInput(keyboard, apiClient, cluster);
						break;
						
					case 2:
						cluster = ImmanCluster.read(clusterName);
						new CheckDropletStatus().withInput(keyboard, apiClient, cluster);
						break;
						
					case 3:
						cluster = ImmanCluster.read(clusterName);
						new CreateVolumes().withInput(keyboard, apiClient, cluster);
						break;
						
					case 4:
						cluster = ImmanCluster.read(clusterName);
						new SetupNode().apply(cluster);
						break;
						
					case 41:
						cluster = ImmanCluster.read(clusterName);
						new Firewall().apply(cluster);
						break;
						
					case 5:
						cluster = ImmanCluster.read(clusterName);
						new ConfigFile().withInput(keyboard, cluster);
						break;
						
					case 6:
						cluster = ImmanCluster.read(clusterName);
						keyboard.nextLine();
						new CopyJar().withInput(keyboard, cluster);
						break;
					
					case 7:
						cluster = ImmanCluster.read(clusterName);
						new CreateDnsRecords().withInput(keyboard, cloudFlareService, cluster);
						break;
						
					case 61:
						cluster = ImmanCluster.read(clusterName);
						new StopDropolets().process(apiClient, cluster);
						break;
						
					case 62:
						cluster = ImmanCluster.read(clusterName);
						new StartDropolets().process(apiClient, cluster);
						break;
						
					case 81:
						cluster = ImmanCluster.read(clusterName);
						new CleanFiles().apply(cluster);
						break;
						
					case 99:
						cluster = ImmanCluster.read(clusterName);
						new DeleteDropolets().withInput(keyboard, apiClient, cluster);
						new DeleteVolumes().withInput(keyboard, apiClient, cluster);
						new DeleteSshKey().withInput(keyboard, apiClient, cluster);
						new DeleteDnsRecords().withInput(keyboard, cloudFlareService, cluster);
						ImmanCluster.delete(cluster);
						break;
						
					default: 
						loop = false;
				}
				keyboard.nextLine();
			}
		} catch (ParseException | IOException exp) {
			System.out.println(exp.getMessage());
			printHelp(options);
		}
	}
	
	private static void printHelp(Options options){
		new HelpFormatter().printHelp("ccio-image-tools [OPTIONS] [COMMAND]", options);
		System.out.println("Available commands:");
		System.out.println("cluster-create");
		System.out.println("cluster-destroy");
	}

}
