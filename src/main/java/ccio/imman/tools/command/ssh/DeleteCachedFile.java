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
		super("delete-cached", "/path/to/image/bbe0a56e38739c47046b257eac987fd8.jpg");
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
		
		FileInfo fileInfo = new FileInfo(path);
		System.out.println(fileInfo);
//		ImmanNode node = cluster.getImageNodes().get(Math.abs(path.hashCode() % cluster.getImageNodes().size()));
		ImmanNode node = cluster.getImageNodes().get(fileInfo.getHostNumber(cluster.getImageNodes().size()));
		
		try {
			String fileName = fileName(fileInfo.getPath());			
			execute(sshService, node.getPublicIp(), "rm -f /mnt/vol-storage/"+fileName);
			execute(sshService, node.getPublicIp(), "rm -f /opt/ccio/store/"+fileName);
			CloudFlareService service = new CloudFlareService(cluster.getCfEmail(), cluster.getCfToken());
			service.purgeFile(cluster.getCfZone(), "http://"+cluster.getCfSubDomain()+"."+cluster.getCfZone()+fileInfo.getPath());
			if(!fileInfo.isOriginalFile()){
				String fileNameCan = fileName(fileInfo.canonicalPath());				
				execute(sshService, node.getPublicIp(), "rm -f /mnt/vol-storage/"+fileNameCan);
				execute(sshService, node.getPublicIp(), "rm -f /opt/ccio/store/"+fileNameCan);
				service.purgeFile(cluster.getCfZone(), "http://"+cluster.getCfSubDomain()+"."+cluster.getCfZone()+fileInfo.canonicalPath());
			}
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
	
	private static class FileInfo {
		private String path;
		private Integer width;
		private Integer height;
		
		public FileInfo(String fileName){
			if(!fileName.startsWith("/")){
				fileName="/"+fileName;
			}
			String[] res=fileName.split("\\?");
			this.path = res[0];
			if(res.length>1){
				String[] parRes = res[1].split("&");
				for(String par : parRes){
					if(par.startsWith("iw=")){
						this.width = Integer.valueOf(par.substring(3));
					}else if(par.startsWith("ih=")){
						this.height = Integer.valueOf(par.substring(3));
					}
				}
			}
		}
		
		public String getPath() {
			return path;
		}

		public boolean isOriginalFile() {
			return height == null && width == null;
		}
		
		public String canonicalPath() {
			StringBuilder sb = new StringBuilder(path);
			String div = "?";
			if(width != null){
				sb.append(div).append("iw=").append(width);
				div = "&";
			}
			if(height != null){
				sb.append(div).append("ih=").append(height);
			}
			return sb.toString();
		}
		
		public int getHostNumber(int hostsCount){
			if(hostsCount==0){
				return 0;
			}
			return Math.abs(canonicalPath().hashCode() % hostsCount);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((height == null) ? 0 : height.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((width == null) ? 0 : width.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FileInfo other = (FileInfo) obj;
			if (height == null) {
				if (other.height != null)
					return false;
			} else if (!height.equals(other.height))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			if (width == null) {
				if (other.width != null)
					return false;
			} else if (!width.equals(other.width))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "FileInfo [path=" + path + ", width=" + width + ", height=" + height + "]";
		}
	}
}
