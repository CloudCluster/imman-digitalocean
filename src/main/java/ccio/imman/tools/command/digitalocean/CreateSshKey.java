package ccio.imman.tools.command.digitalocean;

import java.io.ByteArrayOutputStream;

import com.jcraft.jsch.KeyPair;
import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Key;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.SshService;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class CreateSshKey extends CliCommand{

	public CreateSshKey() {
		super("create-ssh", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		try{
			SshService sshService=new SshService();
			ByteArrayOutputStream keyOs=new ByteArrayOutputStream();
			KeyPair keyPair=sshService.getNewKeyPair();
			keyPair.writePrivateKey(keyOs);
			final String sshPrivateKey = new String(keyOs.toByteArray());
			keyOs=new ByteArrayOutputStream();
			keyPair.writePublicKey(keyOs, cluster.getClusterName());
			final String sshPublicKey = new String(keyOs.toByteArray());
			
			DigitalOcean apiClient = new DigitalOceanClient(cluster.getDoToken());
			Key key = new Key(cluster.getClusterName(), sshPublicKey);
			key = apiClient.createKey(key);
			
			cluster.setSshKeyId(key.getId());
			cluster.setPublicSshKey(sshPublicKey);
			cluster.setPrivateSshKey(sshPrivateKey);
			
			ImmanCluster.save(cluster);
			return true;
		}catch(Exception e){
			throw new CliException(e);
		}
	}

}
