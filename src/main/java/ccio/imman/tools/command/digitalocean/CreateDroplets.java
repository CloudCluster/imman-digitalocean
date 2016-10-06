package ccio.imman.tools.command.digitalocean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

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
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class CreateDroplets extends CliCommand{

	private int numberOfDroplets; 
	private Image image;
	private String region;
	private Size size;
	
	public CreateDroplets() {
		super("create-droplet", "numberOfDroplets");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader console) throws CliParseException {
		if(cmdArgs == null || cmdArgs.length!=2){
			throw new CliParseException("requered arguments: numberOfDroplets");
		}
		try{
			numberOfDroplets = Integer.valueOf(cmdArgs[1]);
		}catch(NumberFormatException e){
			throw new CliParseException(e.getMessage());
		}
		
		if(StringUtils.isBlank(cluster.getDoToken())){
			throw new CliParseException("DigitalOcean Token is not set. Use set-do command to set it.");
		}
		if(cluster.getSshKeyId() == null){
			throw new CliParseException("DigitalOcean SSH Key is not set. Use create-ssh command to set it.");
		}
		
		DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
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
			String res = console.readLine("["+cluster.getClusterName()+"] Enter OS Number > ");
			image = images.getImages().get(selectedNumber(res)-1);
			
			
			System.out.println("Available Regions: ");
			i=1;
			for(String region : image.getRegions()){
				System.out.println((i++)+": "+region);
			}
			
			res = console.readLine("["+cluster.getClusterName()+"]["+image.getName()+"] Enter Region Number > ");
			region = image.getRegions().get(selectedNumber(res)-1);
			
			System.out.println("Available Sizes: ");
			Sizes sizes = apiClient.getAvailableSizes(0);
			i=1;
			for(Size size : sizes.getSizes()){
				System.out.println((i++)+": "+size.getSlug());
			}
			res = console.readLine("["+cluster.getClusterName()+"]["+image.getDistribution()+"]["+region+"] Enter Droplet Size Number > ");
			size = sizes.getSizes().get(selectedNumber(res)-1);
		}catch(Exception e){
			throw new CliParseException(e.getMessage());
		}
		return this;
	}
	
	private int selectedNumber(String line) throws CliParseException{
		try{
			return Integer.valueOf(line);
		}catch(NumberFormatException e){
			throw new CliParseException(e.getMessage());
		}
	}

	@Override
	public boolean exec() throws CliException {
		Droplet[] droplets = createDroplets();
		
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
		
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			throw new CliException(e);
		}
		return true;
	}

	private Droplet[] createDroplets(){
		System.out.println("Creating "+numberOfDroplets+" droplets of '"+image.getDistribution()+" "+image.getName()+"' in '"+region+"' region for cluster '"+cluster.getClusterName()+"'");

		DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
		Key sshKey = new Key(cluster.getSshKeyId());
		Droplet[] nodes = new Droplet[numberOfDroplets];
		
		for(int i = 0; i<nodes.length; i++) {
			Droplet droplet = new Droplet();
			droplet.setName(cluster.getClusterName() + "-" + (i<10?"0":"") +i);
			droplet.setSize(size.getSlug());
			droplet.setRegion(new Region(region));
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
}
