package ccio.imman.tools.command.ssh;

import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import com.jcraft.jsch.JSchException;
import com.mashape.unirest.http.exceptions.UnirestException;

import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.SshService;
import ccio.imman.tools.cloudflair.CloudFlareService;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class DeleteCachedFile extends SshAction{

	private String path;
	
	public DeleteCachedFile(){
		super("delete-cached", "path/to/image/bbe0a56e38739c47046b257eac987fd8.jpg");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader console) throws CliParseException {
		if(cmdArgs == null || cmdArgs.length!=2){
			throw new CliParseException("requered arguments: path");
		}
		path = cmdArgs[1];
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		SshService sshService=getSshService(cluster);
		
		ImmanNode node = cluster.getImageNodes().get(Math.abs(path.hashCode() % cluster.getImageNodes().size()));
		String fileName = fileName(path);
		
		try {
			
			execute(sshService, node.getPublicIp(), "rm -f /mnt/vol-storage/"+fileName);
			execute(sshService, node.getPublicIp(), "rm -f /opt/ccio/store/"+fileName);
			CloudFlareService service = new CloudFlareService(cluster.getCfEmail(), cluster.getCfToken());
			service.purgeFile(cluster.getCfZone(), "http://"+cluster.getCfSubDomain()+"."+cluster.getCfZone()+path);
		} catch (JSchException | IOException | UnirestException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private static String fileName(String filePath) {
		String fileName=DigestUtils.md5Hex(filePath);
		if(fileName.length()>2){
			fileName=fileName.substring(0, 2)+File.separator+fileName.substring(2, fileName.length());
		}
		if(fileName.length()>5){
			fileName=fileName.substring(0, 5)+File.separator+fileName.substring(5, fileName.length());
		}
		if(fileName.length()>8){
			fileName=fileName.substring(0, 8)+File.separator+fileName.substring(8, fileName.length());
		}
		return fileName;
	}
}
