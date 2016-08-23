package ccio.imman.tools.digitalocean.model;

import com.myjeeva.digitalocean.pojo.Key;

public class DoSshKey {
	
	private Key digitalOceanKey;
	private String sshPrivateKey;
	
	public DoSshKey(){
		super();
	}

	public DoSshKey(Key digitalOceanKey, String sshPrivateKey) {
		super();
		this.digitalOceanKey = digitalOceanKey;
		this.sshPrivateKey = sshPrivateKey;
	}
	
	public Key getDigitalOceanKey() {
		return digitalOceanKey;
	}
	public void setDigitalOceanKey(Key digitalOceanKey) {
		this.digitalOceanKey = digitalOceanKey;
	}
	public String getSshPrivateKey() {
		return sshPrivateKey;
	}
	public void setSshPrivateKey(String sshPrivateKey) {
		this.sshPrivateKey = sshPrivateKey;
	}
}
