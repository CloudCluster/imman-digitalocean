package ccio.imman.tools.ssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;

import ccio.imman.tools.SshService;
import ccio.imman.tools.digitalocean.model.ImmanCluster;
import ccio.imman.tools.digitalocean.model.ImmanNode;

public class CleanFiles extends SshAction{
	
	private static final String SCRIPT = "#!/bin/sh\n" +
			"rm -Rf /mnt/vol-storage/*\n"+
			"rm -Rf /opt/ccio/store/*\n"
			;
	
	@Override
	public void apply(ImmanCluster cluster){
		SshService sshService=getSshService(cluster);
		
		for(ImmanNode imageNode : cluster.getImageNodes()){
			System.out.println("Cleaning Up "+imageNode.getDropletName()+" Droplet");
			try {
				copyToFileAndExecute(sshService, imageNode.getPublicIp(), "/opt/scripts/DeleteCachedFiles.sh", SCRIPT);
			} catch (JSchException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String... args) {
		main(new CleanFiles(), args);
	}

}
