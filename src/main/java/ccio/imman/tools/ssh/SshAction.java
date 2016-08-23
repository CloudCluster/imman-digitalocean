package ccio.imman.tools.ssh;

import java.io.IOException;
import java.util.Scanner;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ccio.imman.tools.SshService;
import ccio.imman.tools.digitalocean.model.ImmanCluster;

public abstract class SshAction {

	
	public abstract void apply(ImmanCluster imageCluster);
	
	public static void main(SshAction sshAction, String... args){
		String clusterName = null;
		if(args.length > 0){
			clusterName = args[0];
		}
		
		try (Scanner keyboard = new Scanner(System.in)){
			if(clusterName == null){
				System.out.print("Cluster Name: ");
				clusterName = keyboard.nextLine();
			}
			
			ImmanCluster cluster = ImmanCluster.read(clusterName);
			sshAction.apply(cluster);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected SshService getSshService(ImmanCluster cluster){
		SshService sshService=new SshService();
		sshService.setDefaultUser("root");
		sshService.setDefaultSshKeyFile(cluster.getPrivateSshKey());
		return sshService;
	}
	
	protected void copyToFileAndExecute(SshService sshService, String remoteHost, String fileName, String body) throws JSchException, IOException{
		Session session=null;
		try {
			session = sshService.openNewSession(remoteHost);
			if(session!=null){
				String fldr = fileName.substring(0, fileName.lastIndexOf("/"));
				StringBuffer respMsg=new StringBuffer();
				sshService.execute("mkdir "+fldr, session, respMsg);
				boolean res=sshService.copyToRemoteServer(body, fileName, session);
				if(res){
					int resCode = sshService.execute("chmod 700 "+fileName, session, respMsg);
					if(resCode!=0){
						System.out.println("Cannot set permissions on "+remoteHost+" with message: "+respMsg);
					}else{
						respMsg=new StringBuffer();
						resCode = sshService.execute(fileName, session, respMsg);
						if(resCode!=0){
							System.out.println("Cannot execute "+fileName+": "+resCode+" - "+respMsg);
						}
						System.out.println(remoteHost+" is completed with: "+respMsg);
					}
				}else{
					System.out.println("Cannot copy "+fileName+" to "+remoteHost);
				}
			}else{
				System.out.println("Cannot connect to "+remoteHost);
			}
		}finally{
			if(session!=null){
				session.disconnect();
			}
		}
	}
}
