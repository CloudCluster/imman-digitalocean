package ccio.imman.tools.ssh;

import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ccio.imman.tools.SshService;
import ccio.imman.tools.digitalocean.model.ImmanCluster;
import ccio.imman.tools.digitalocean.model.ImmanNode;

public class ConfigFile extends SshAction{
	
	private static final String CONFIG_TEMPLATE= "files.space.reserved=1000000000\n"
					+ "files.locations=/opt/ccio/store,/mnt/vol-storage\n"
					+ "transport.port=9900\n"
					+ "http.port=80\n";

//		http.ip=127.0.0.1
//	http.port=80
//
//			transport.ip=127.0.0.1
//			transport.port=9900
//			transport.seeds=127.0.0.1:9900
//
//			files.space.reserved=1000000000
//			files.locations=/opt/ccio/store,/mnt/vol-storage
//
//			secret=kjeq@Llkje9023kjKJWElksdnslkreK22
//			name=ccio-image-X
//
//			image.width=100,300
//			image.height=
//
//			aws.s3.access=AAA
//			aws.s3.secret=SSS
//			aws.s3.bucket=BBB
	
	@Override
	public void apply(ImmanCluster imageCluster){
		System.out.println("Applying configuration file");
		String separ="";
		StringBuilder initialHosts=new StringBuilder();
		for(ImmanNode imageNode:imageCluster.getImageNodes()){
			initialHosts.append(separ).append(imageNode.getPrivateIp()).append(":9900");
			separ=",";
		}
		
		SshService sshService=getSshService(imageCluster);
		
		for(ImmanNode imageNode:imageCluster.getImageNodes()){
			StringBuilder body = new StringBuilder(CONFIG_TEMPLATE);
			if(!StringUtils.isBlank(imageCluster.getWidths())){
				body.append("image.width=").append(imageCluster.getWidths()).append("\n");
			}
			if(!StringUtils.isBlank(imageCluster.getHeights())){
				body.append("mage.height=").append(imageCluster.getHeights()).append("\n");
			}
			body.append("name=").append(imageCluster.getClusterName()).append("\n");
			body.append("secret=").append(imageCluster.getSecret()).append("\n");
			body.append("aws.s3.access=").append(imageCluster.getS3Access()).append("\n");
			body.append("aws.s3.secret=").append(imageCluster.getS3Secret()).append("\n");
			body.append("aws.s3.bucket=").append(imageCluster.getS3Bucket()).append("\n");

			body.append("http.ip=").append(imageNode.getPublicIp()).append("\n");
			body.append("transport.seeds=").append(initialHosts).append("\n");
			body.append("transport.ip=").append(imageNode.getPrivateIp()).append("\n");
//			body.append("managment.url=http://104.131.22.187/mancenter\n");
			
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
	}
	
	public void withInput(Scanner keyboard, ImmanCluster cluster){
		if(cluster.getS3Access() == null){
			System.out.print("AWS S3 Access: ");
			String s3Access = keyboard.nextLine();
			cluster.setS3Access(s3Access);
		}
		
		if(cluster.getS3Secret() == null){
			System.out.print("AWS S3 Secret: ");
			String s3Secret = keyboard.nextLine();
			cluster.setS3Secret(s3Secret);
		}
		
		if(cluster.getS3Bucket() == null){
			System.out.print("AWS S3 Bucket: ");
			String s3Bucket = keyboard.nextLine();
			cluster.setS3Bucket(s3Bucket);
		}
		
		if(StringUtils.isBlank(cluster.getWidths())){
			System.out.print("Widths: ");
			String widths = keyboard.nextLine();
			cluster.setWidths(widths);
		}
		
		if(StringUtils.isBlank(cluster.getHeights())){
			System.out.print("Heights: ");
			String heights = keyboard.nextLine();
			cluster.setHeights(heights);
		}
		
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		apply(cluster);
	}

	public static void main(String[] args){
		main(new ConfigFile(), args);
	}
}
