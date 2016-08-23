package ccio.imman.tools.ssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;

import ccio.imman.tools.SshService;
import ccio.imman.tools.digitalocean.model.ImmanCluster;
import ccio.imman.tools.digitalocean.model.ImmanNode;

public class SetupNode extends SshAction{
	
	private static final String SCRIPT = "#!/bin/sh\n" +
			//IP Tables
			"yum -y install iptables-services\n" +
			"systemctl stop firewalld\n"+
			"systemctl start iptables\n"+
			"systemctl disable firewalld\n"+
			"systemctl mask firewalld\n"+
			"systemctl enable iptables\n"+
			//SWAP File
			"fallocate -l 512M /swapfile\n"+
			"chmod 600 /swapfile\n"+
			"mkswap /swapfile\n"+
			"swapon /swapfile\n"+
			"echo /swapfile   swap    swap    sw  0   0 | tee -a /etc/fstab\n"+
			//Volume
			"mkfs.ext4 -F /dev/disk/by-id/*\n"+
			"mkdir -p /mnt/vol-storage\n"+
			"mount -o discard,defaults /dev/disk/by-id/* /mnt/vol-storage\n"+
			"echo /dev/disk/by-id/* /mnt/vol-storage ext4 defaults,nofail,discard 0 0 | tee -a /etc/fstab\n"+
			//Java
			"yum -y remove java\n" +
			"cd /opt\n" +
			"wget --no-cookies --no-check-certificate --header \"Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie\" \"http://download.oracle.com/otn-pub/java/jdk/8u101-b13/jdk-8u101-linux-x64.rpm\"\n" +
			"yum -y localinstall jdk-8u101-linux-x64.rpm\n" +
			"rm /opt/jdk-8u101-linux-x64.rpm\n" +
			//NTP
			"yum -y install ntp\n" + 
			"chkconfig --level 234 ntpd on\n" + 
			"ntpdate pool.ntp.org\n" + 
			"/etc/init.d/ntpd start\n" +
			
			"mkdir /opt/logs"
			;
	
	@Override
	public void apply(ImmanCluster cluster){
		SshService sshService=getSshService(cluster);
		
		for(ImmanNode imageNode : cluster.getImageNodes()){
			System.out.println("Setting Up "+imageNode.getDropletName()+" Droplet");
			try {
				copyToFileAndExecute(sshService, imageNode.getPublicIp(), "/opt/scripts/NodeSetup.sh", SCRIPT);
			} catch (JSchException | IOException e) {
				e.printStackTrace();
			}
		}
		
		new Firewall().apply(cluster);
	}

	public static void main(String... args) {
		main(new SetupNode(), args);
	}

}
