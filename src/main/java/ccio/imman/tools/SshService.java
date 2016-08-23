package ccio.imman.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

public class SshService {
	
	private final static Logger LOG = LoggerFactory.getLogger(SshService.class);
	private final static byte[] EMPTY_PASSPHRASE=new byte[0];
	
	private String defaultUser;
	private String defaultSshKeyFile;
	
	static{
		JSch.setConfig("StrictHostKeyChecking", "no");
	}
	
	/**
	 * How to use:
	 * kpair.writePrivateKey(filename+"_rsa");
     * kpair.writePublicKey(filename+"_rsa.pub", comment);
     * System.out.println("Finger print: "+kpair.getFingerPrint());
     * kpair.dispose();
     * 
	 * @return a new key pair
	 * @throws JSchException
	 */
	public KeyPair getNewKeyPair() throws JSchException{
		return KeyPair.genKeyPair(new JSch(), KeyPair.RSA);
	}
	
	public int execute(String command, Session session, StringBuffer result) throws JSchException, IOException{
		int exitStatus=-1;
		Channel channel=session.openChannel("exec");
		((ChannelExec)channel).setCommand(command);
//		channel.setInputStream(null);
//	    ((ChannelExec)channel).setErrStream(System.err);
	 
	    InputStream in=channel.getInputStream();
	    channel.connect();
	 
	    byte[] tmp=new byte[1024];
	    while(true){
	    	while(in.available()>0){
	    		int i=in.read(tmp, 0, 1024);
	    		if(i<0) break;
	    		result.append(new String(tmp, 0, i));
	        }
	        if(channel.isClosed()){
	        	exitStatus=channel.getExitStatus();
	        	break;
	        }
	    }
	    channel.disconnect();
	    return exitStatus;
	}
	
	/**
	 * Creates a new session
	 * Don't forget to disconnect the session when done <code>session.disconnect()</code> 
	 * @param remoteHost address to the host <code>dev.cloudcluster.io</code>
	 * @param user username <code>root</code>
	 * @param sshKeyFile path to the key file <code>/Users/myname/.ssh/id_rsa</code>
	 */
//	public Session openNewSession(String remoteHost, String user, String sshKeyFile) throws JSchException{
//		JSch jsch=new JSch();
//		jsch.addIdentity(sshKeyFile);
//		Session session=jsch.getSession(user, remoteHost, 22);
//		session.connect();
//		return session;
//	}
	
	public Session openNewSession(String remoteHost, String user, String sshPrivateKey){
		//trying 5 times
		for(int i=0; i<5; i++){
			try {
				JSch jsch=new JSch();
				jsch.addIdentity(user, sshPrivateKey.getBytes(), null, EMPTY_PASSPHRASE);
				Session session=jsch.getSession(user, remoteHost);
				Properties config = new Properties();
		        config.put("StrictHostKeyChecking", "no");
		        session.setConfig(config);
				session.connect();
				return session;
			} catch (JSchException e) {
				if(i<4){
					LOG.debug(e.getMessage());
				}else{
					LOG.error(e.getMessage());
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// ignore
				}
			}
		}
		return null;
		
	}
	
//	public Session openNewSession(String remoteHost, String user, String sshPrivKey, String sshPublicKey) throws JSchException{
//		JSch jsch=new JSch();
//		jsch.addIdentity(user, sshPrivKey.getBytes(), sshPublicKey.getBytes(), null);
//		Session session=jsch.getSession(remoteHost);
//		session.connect();
//		return session;
//	}
	
	public Session openNewSession(String remoteHost) throws JSchException{
		return openNewSession(remoteHost, defaultUser, defaultSshKeyFile);
	}
	
	public boolean copyFileToRemoteServer(String localFileName, String remoteFileName, Session session){
		return copyFileToRemoteServer(new String[]{localFileName}, new String[]{remoteFileName}, session, false);
	}
	
	public boolean copyFileToRemoteServer(String localFileName, String remoteFileName, Session session, boolean checkTimestamp){
		return copyFileToRemoteServer(new String[]{localFileName}, new String[]{remoteFileName}, session, checkTimestamp);
	}
	
	public boolean copyFileToRemoteServer(String[] localFileNames, String[] remoteFileNames, Session session){
		return copyFileToRemoteServer(localFileNames, remoteFileNames, session, false);
	}
	
	public boolean copyFileToRemoteServer(String[] localFileNames, String[] remoteFileNames, Session session, boolean checkTimestamp){
		if(localFileNames==null || remoteFileNames==null || localFileNames.length!=remoteFileNames.length){
			throw new IllegalArgumentException("Number of local files must be the same as remote files");
		}
		
		for(int i=0; i<localFileNames.length; i++){
			String localFileName=localFileNames[i];
			String remoteFileName="\""+remoteFileNames[i]+"\"";
//			LOG.debug("Copying local file {} to remote file {} on host {}. Checking timestamp: {}", localFileName, remoteFileName, session.getHost(), checkTimestamp);
			
			try{
				// exec 'scp -t rfile' remotely
				String command="scp " + (checkTimestamp ? "-p" :"") +" -t "+remoteFileName;
				Channel channel=session.openChannel("exec");
				((ChannelExec)channel).setCommand(command);
		 
				// get I/O streams for remote scp
				OutputStream out=channel.getOutputStream();
				InputStream in=channel.getInputStream();
		 
				channel.connect();
		 
				if(checkAck(in)!=0){
					return false;
				}
		 
				File _lfile = new File(localFileName);
		 
				if(checkTimestamp){
					command="T "+(_lfile.lastModified()/1000)+" 0";
					// The access time should be sent here,
					// but it is not accessible with JavaAPI ;-<
					command+=(" "+(_lfile.lastModified()/1000)+" 0\n"); 
					out.write(command.getBytes()); 
					out.flush();
			        if(checkAck(in)!=0){
			        	return false;
			        }
		        }
		 
				// send "C0644 filesize filename", where filename should not include '/'
				long filesize=_lfile.length();
				command="C0644 "+filesize+" ";
				if(localFileName.lastIndexOf('/')>0){
					command+=localFileName.substring(localFileName.lastIndexOf('/')+1);
				}else{
					command+=localFileName;
				}
				command+="\n";
				out.write(command.getBytes()); 
				out.flush();
				if(checkAck(in)!=0){
					return false;
				}
		 
				// send a content of lfile
				FileInputStream fis=new FileInputStream(localFileName);
				byte[] buf=new byte[1024];
				while(true){
					int len=fis.read(buf, 0, buf.length);
					if(len<=0) break;
					out.write(buf, 0, len); //out.flush();
				}
				fis.close();
				fis=null;
				// send '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
				if(checkAck(in)!=0){
					return false;
				}
				out.close();
				channel.disconnect();
			} catch (Exception e) {
		    	LOG.error("Cannot copy file: "+localFileName+" to remote server: "+session.getHost(), e);
		    	return false;
		    }
		}
//		session.disconnect();
    
		return true;
	}
	
	public boolean copyToRemoteServer(String content, String remoteFileName, Session session){
		if(content==null || remoteFileName==null){
			throw new IllegalArgumentException("Number of local files must be the same as remote files");
		}
		
		String justFileName=remoteFileName;
		String[] str=justFileName.split("/");
		justFileName = str[str.length-1];
		
		remoteFileName="\""+remoteFileName+"\"";
		LOG.debug("Copying byte array as file to remote file {} on host {}. Checking timestamp: {}", remoteFileName, session.getHost());
		
		try{
			// exec 'scp -t rfile' remotely
			String command="scp -t "+remoteFileName;
			Channel channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
	 
			// get I/O streams for remote scp
			OutputStream out=channel.getOutputStream();
			InputStream in=channel.getInputStream();
	 
			channel.connect();
	 
			if(checkAck(in)!=0){
				return false;
			}
	 
			// send "C0644 filesize filename", where filename should not include '/'
			long filesize=content.getBytes().length;
			command="C0644 "+filesize+" "+justFileName+"\n";
			out.write(command.getBytes()); 
			out.flush();
			if(checkAck(in)!=0){
				return false;
			}
	 
			// send a content of lfile
			ByteArrayInputStream fis=new ByteArrayInputStream(content.getBytes());
			byte[] buf=new byte[1024];
			while(true){
				int len=fis.read(buf, 0, buf.length);
				if(len<=0) break;
				out.write(buf, 0, len); //out.flush();
			}
			fis.close();
			fis=null;
			// send '\0'
			buf[0]=0; out.write(buf, 0, 1); out.flush();
			if(checkAck(in)!=0){
				return false;
			}
			out.close();
			channel.disconnect();
		} catch (Exception e) {
	    	LOG.error("Cannot copy file: "+content+" to remote server: "+session.getHost(), e);
	    	return false;
	    }
    
		return true;
	}
	
	private int checkAck(InputStream in) throws IOException{
	    int b=in.read();
	    // b may be 0 for success,
	    //          1 for error,
	    //          2 for fatal error,
	    //          -1
	    if(b==0) return b;
	    if(b==-1) return b;
	 
	    if(b==1 || b==2){
	    	StringBuffer sb=new StringBuffer();
	    	int c;
	    	do {
	    		c=in.read();
	    		sb.append((char)c);
	    	}while(c!='\n');
	    	
	    	if(b==1){ // error
	    		LOG.error("Ack Check Error: "+sb);
	    	}
	    	if(b==2){ // fatal error
	    		LOG.error("Ack Check Fatal Error: "+sb);
	    	}
	    }
	    return b;
	}

	public void setDefaultUser(String defaultUser) {
		this.defaultUser = defaultUser;
	}

	public void setDefaultSshKeyFile(String defaultSshKeyFile) {
		this.defaultSshKeyFile = defaultSshKeyFile;
	}
}
