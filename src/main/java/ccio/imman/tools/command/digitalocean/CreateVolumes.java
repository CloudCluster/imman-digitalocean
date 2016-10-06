package ccio.imman.tools.command.digitalocean;

import org.apache.commons.lang3.StringUtils;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Volume;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class CreateVolumes extends CliCommand{

	private int sizeInGb; 
	
	public CreateVolumes() {
		super("create-volume", "sizeInGb");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader console) throws CliParseException {
		if(cmdArgs == null || cmdArgs.length!=2){
			throw new CliParseException("requered arguments: sizeInGb");
		}
		try{
			sizeInGb = Integer.valueOf(cmdArgs[1]);
		}catch(NumberFormatException e){
			throw new CliParseException(e.getMessage());
		}
		
		if(StringUtils.isBlank(cluster.getDoToken())){
			throw new CliParseException("DigitalOcean Token is not set. Use set-do command to set it.");
		}
		if(cluster.getSshKeyId() == null){
			throw new CliParseException("DigitalOcean SSH Key is not set. Use create-ssh command to set it.");
		}
		
		cluster.setVolumeSizeGigabytes(sizeInGb);
		
		return this;
	}
	
	@Override
	public boolean exec() throws CliException {
		DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
		try{
			Volume[] result = new Volume[cluster.getImageNodes().size()];
			int i=0;
			for(ImmanNode node : cluster.getImageNodes()){
				System.out.println("Creating volume of size "+cluster.getVolumeSizeGigabytes()+"Gb for Droplet "+node.getDropletName());
				Volume volume = new Volume();
				volume.setName("vol-"+node.getDropletName());
				volume.setRegion(new Region(node.getRegionSlug()));
				volume.setSize((double)cluster.getVolumeSizeGigabytes());
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
			ImmanCluster.save(cluster);
		}catch(Exception e){
			throw new CliParseException(e.getMessage());
		}
		return true;
	}
}
