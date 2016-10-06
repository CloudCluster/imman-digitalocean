package ccio.imman.tools.command.ssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;

import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.SshService;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class Firewall extends SshAction{

	private static final String SET_IPTABLES_SH_BEGIN="#!/bin/sh\n" + 
			"# Flushing all rules\n" + 
			"/sbin/iptables -F\n" + 
			"/sbin/iptables -X\n" + 
			"\n" + 
			"# Allow outgoing traffic and disallow any passthroughs\n" + 
			"/sbin/iptables -P INPUT DROP\n" + 
			"/sbin/iptables -P OUTPUT ACCEPT\n" + 
			"/sbin/iptables -P FORWARD DROP\n" + 
			"\n" + 
			"# Allow traffic already established to continue\n" + 
			"/sbin/iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT\n" + 
			"\n" + 
			"# Allow ssh, dns, ldap, ftp and web services\n" + 
			"/sbin/iptables -A INPUT -p tcp --dport ssh -j ACCEPT\n" + 
			"/sbin/iptables -A INPUT -p tcp --dport 80 -j ACCEPT\n" + 
			"/sbin/iptables -A OUTPUT -p tcp --sport 80 -j ACCEPT\n" + 
			"/sbin/iptables -A OUTPUT -p udp --dport 123 -m conntrack --ctstate NEW,ESTABLISHED,RELATED -j ACCEPT\n" +
			"/sbin/iptables -A INPUT -p udp --sport 123 -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT" +
			"\n" + 
			"# The Cluster\n"; 
	
	private static final String SET_IPTABLES_SH_NODE="/sbin/iptables -A INPUT --source ##IP## -j ACCEPT\n"; 
	
	private static final String SET_IPTABLES_SH_END="\n" +
//			"# Managment Center\n" + 
//			"/sbin/iptables -A INPUT --source 104.131.22.187 -j ACCEPT\n" +
//			"/sbin/iptables -A INPUT --source 10.132.0.81 -j ACCEPT\n" +
			"# Allow local loopback services\n" + 
			"/sbin/iptables -A INPUT -i lo -j ACCEPT\n" + 
			"\n" + 
			"# Allow pings\n" + 
			"/sbin/iptables -I INPUT -p icmp --icmp-type destination-unreachable -j ACCEPT\n" + 
			"/sbin/iptables -I INPUT -p icmp --icmp-type source-quench -j ACCEPT\n" + 
			"/sbin/iptables -I INPUT -p icmp --icmp-type time-exceeded -j ACCEPT\n" + 
			"\n" + 
			"/sbin/service iptables save\n";
	
	public Firewall() {
		super("firewall", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader console) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		System.out.println("Applying firewall");
		StringBuilder bodyBuilder=new StringBuilder(SET_IPTABLES_SH_BEGIN);
		for(ImmanNode imageNode : cluster.getImageNodes()){
			bodyBuilder.append(SET_IPTABLES_SH_NODE.replace("##IP##", imageNode.getPublicIp()));
			bodyBuilder.append(SET_IPTABLES_SH_NODE.replace("##IP##", imageNode.getPrivateIp()));
		}
		bodyBuilder.append(SET_IPTABLES_SH_END);
		
		String body=bodyBuilder.toString();

		SshService sshService=getSshService(cluster);
		
		for(ImmanNode imageNode : cluster.getImageNodes()){
			System.out.println("Applying Firewall to "+imageNode.getDropletName()+" Droplet");
			try {
				copyToFileAndExecute(sshService, imageNode.getPublicIp(), "/opt/scripts/Firewall.sh", body);
			} catch (JSchException | IOException e) {
				System.out.println("Problem appling firewall on "+imageNode.getDropletName()+" Droplet: "+e.getMessage());
			}
		}
		return true;
	}
}