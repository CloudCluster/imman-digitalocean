package ccio.imman.tools.command.ssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.SshService;
import ccio.imman.tools.command.CliCommand;

public abstract class SshAction extends CliCommand{

	public SshAction(String cmdStr, String optionStr) {
		super(cmdStr, optionStr);
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
