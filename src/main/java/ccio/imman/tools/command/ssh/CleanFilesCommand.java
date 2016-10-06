package ccio.imman.tools.command.ssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;

import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.SshService;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class CleanFilesCommand extends SshAction{

	private static final String SCRIPT = "#!/bin/sh\n" +
			"rm -Rf /mnt/vol-storage/*\n"+
			"rm -Rf /opt/ccio/store/*\n";
			
	public CleanFilesCommand() {
		super("delete-files", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader console) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		SshService sshService=getSshService(cluster);
		
		for(ImmanNode imageNode : cluster.getImageNodes()){
			System.out.println("Cleaning Up "+imageNode.getDropletName()+" Droplet");
			try {
				copyToFileAndExecute(sshService, imageNode.getPublicIp(), "/opt/scripts/DeleteCachedFiles.sh", SCRIPT);
			} catch (JSchException | IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

}
