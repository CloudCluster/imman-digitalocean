package ccio.imman.tools.digitalocean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.apache.commons.lang.NotImplementedException;

import com.jcraft.jsch.JSchException;
import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Images;
import com.myjeeva.digitalocean.pojo.Key;
import com.myjeeva.digitalocean.pojo.Network;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Size;
import com.myjeeva.digitalocean.pojo.Sizes;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.digitalocean.model.DoSshKey;

public class CreateDroplets extends DoAction<Droplet[]> {
	
	@Override
	public Droplet[] process(DigitalOcean apiClient, ImmanCluster immanCluster) {
		throw new NotImplementedException();
	}
	
	public static Droplet[] process(DigitalOcean apiClient, Image image, Size size, String regionSlug, Key sshKey, int numberOfDroplets, String clusterName){
		System.out.println("Creating "+numberOfDroplets+" droplets of '"+image.getDistribution()+" "+image.getName()+"' in '"+regionSlug+"' region for cluster '"+clusterName+"'");
		
		Droplet[] nodes = new Droplet[numberOfDroplets];
		
		for(int i = 0; i<nodes.length; i++) {
			Droplet droplet = new Droplet();
			droplet.setName(clusterName + "-" + (i<10?"0":"") +i);
			droplet.setSize(size.getSlug());
			droplet.setRegion(new Region(regionSlug));
			droplet.setImage(image);
			droplet.setEnableBackup(false);
			droplet.setEnableIpv6(false);
			droplet.setEnablePrivateNetworking(true);
			
			try {
				droplet.setKeys(Collections.singletonList(sshKey));
				nodes[i] = apiClient.createDroplet(droplet);
			} catch (DigitalOceanException | RequestUnsuccessfulException e) {
				e.printStackTrace();
			}
		}
		
		return nodes;
	}
	
	@Override
	public Droplet[] withInput(Scanner keyboard, DigitalOcean apiClient, ImmanCluster cluster) throws IOException {
		
		if(cluster.getClusterName() == null){
			System.out.print("Cluster Name: ");
			String clusterName = keyboard.nextLine();
			cluster.setClusterName(clusterName);
		}
		
		if(apiClient == null){
			System.out.print("DigitalOcean Token: ");
			String token = keyboard.nextLine();
			apiClient = new DigitalOceanClient(token);
		}
		
		try{
			Images images = apiClient.getAvailableImages(0, 100);
			System.out.println("Available Images:");
			int i=1;
			for(Image image : images.getImages()){
				if("CentOS".equals(image.getDistribution())){
					System.out.println(i+": "+image.getDistribution()+" "+image.getName()+" ("+image.getId()+")");
				}
				i++;
			}
			
			System.out.print("Enter Image Number: ");
			int imageNum = keyboard.nextInt();
	
			Image image = images.getImages().get(--imageNum);
			
			i=1;
			for(String region : image.getRegions()){
				System.out.println((i++)+": "+region);
			}
			System.out.print("Enter Region Number: ");
			int regionNum = keyboard.nextInt();
			String region = image.getRegions().get(--regionNum);
			
			Sizes sizes = apiClient.getAvailableSizes(0);
			i=1;
			for(Size size : sizes.getSizes()){
				System.out.println((i++)+": "+size.getSlug());
			}
			System.out.print("Enter Droplet Size: ");
			int sizeNum = keyboard.nextInt();
			
			Size size = sizes.getSizes().get(--sizeNum);
			
			System.out.print("How many droplets: ");
			int dropletNumber = keyboard.nextInt();
			
			DoSshKey sshKey = CreateSshKey.create(cluster.getClusterName(), apiClient);
			
			Droplet[] droplets = process(apiClient, image, size, region, sshKey.getDigitalOceanKey(), dropletNumber, cluster.getClusterName());
			
			ArrayList<ImmanNode> imageNodes = new ArrayList<>();
			for(Droplet droplet:droplets){
				ImmanNode imageNode=new ImmanNode();
				imageNode.setDropletId(droplet.getId());
				imageNode.setDropletName(droplet.getName());
				imageNode.setRegionSlug(droplet.getRegion().getSlug());
				for(Network network : droplet.getNetworks().getVersion4Networks()){
					if("private".equalsIgnoreCase(network.getType())){
						imageNode.setPrivateIp(network.getIpAddress());
					} else if("public".equalsIgnoreCase(network.getType())){
						imageNode.setPublicIp(network.getIpAddress());
					}
				}
				imageNodes.add(imageNode);
			}
			cluster.setImageNodes(imageNodes);
			cluster.setPrivateSshKey(sshKey.getSshPrivateKey());
			cluster.setPublicSshKey(sshKey.getDigitalOceanKey().getPublicKey());
			cluster.setSshKeyId(sshKey.getDigitalOceanKey().getId());
			
			ImmanCluster.save(cluster);
//			System.out.println("----------------");
//			System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(cluster));
			return droplets;
		}catch(JSchException | DigitalOceanException | RequestUnsuccessfulException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String... args) throws IOException, DigitalOceanException, RequestUnsuccessfulException, JSchException{
		main(new CreateDroplets(), args);
	}

}
