package ccio.imman.tools.command.ssh;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.SshService;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class CopyConfigFile extends SshAction{

	private static final String CONFIG_TEMPLATE= "files.space.reserved=1000000000\n"
//			+ "files.locations=/opt/ccio/store,/mnt/vol-storage\n"
			+ "files.locations=/mnt/vol-storage\n"
			+ "http.port.private=8081\n"
			+ "http.port.public=80\n";
	
	/*
	 * http.ip.private=127.0.0.1
http.port=8080

files.space.reserved=1000000000
files.locations=/opt/ccio/store1,/opt/ccio/store2

#private IPs of all nodes
nodes=127.0.0.1
secret=kjeq@Llkjd9023kjKJWElksdnslkreK22

image.width=100,300
image.height=350

aws.s3.access=160HMTTBY0CZN8FZ4MR2
aws.s3.secret=1Mui/nOh6l676vDI1DDPD2nKjUTRjMQv8cVG4hjJ
aws.s3.bucket=IndulgyDEV
	 */
	
	public CopyConfigFile() {
		super("copy-config", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader console) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		System.out.println("Applying configuration file");
		String separ="";
		StringBuilder initialHosts=new StringBuilder();
		for(ImmanNode imageNode:cluster.getImageNodes()){
			initialHosts.append(separ).append(imageNode.getPrivateIp());
			separ=",";
		}
		
		SshService sshService=getSshService(cluster);
		
		for(ImmanNode imageNode:cluster.getImageNodes()){
			StringBuilder body = new StringBuilder(CONFIG_TEMPLATE);
			if(!StringUtils.isBlank(cluster.getWidths())){
				body.append("image.width=").append(cluster.getWidths()).append("\n");
			}
			if(!StringUtils.isBlank(cluster.getHeights())){
				body.append("mage.height=").append(cluster.getHeights()).append("\n");
			}
			body.append("secret=").append(cluster.getSecret()).append("\n");
			body.append("aws.s3.access=").append(cluster.getS3Access()).append("\n");
			body.append("aws.s3.secret=").append(cluster.getS3Secret()).append("\n");
			body.append("aws.s3.bucket=").append(cluster.getS3Bucket()).append("\n");

			body.append("http.ip.public=").append(imageNode.getPublicIp()).append("\n");
			body.append("http.ip.private=").append(imageNode.getPrivateIp()).append("\n");
			body.append("nodes=").append(initialHosts).append("\n");
			
			Session session=null;
			try {
				session = sshService.openNewSession(imageNode.getPublicIp());
				if(session!=null){
					boolean res=sshService.copyToRemoteServer(body.toString(), "/opt/ccio-imman.properties", session);
					if(res){
						System.out.println(imageNode.getDropletName()+" is set successfully");
					}else{
						System.out.println("Cannot copy ccio-image.properties to "+imageNode.getPublicIp());
					}
				}else{
					System.out.println("Cannot connect to "+imageNode.getPublicIp());
				}
			} catch (JSchException e) {
				e.printStackTrace();
			}finally{
				if(session!=null){
					session.disconnect();
				}
			}
		}
		return true;
	}
}