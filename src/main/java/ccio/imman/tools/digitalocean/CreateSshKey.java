package ccio.imman.tools.digitalocean;

import java.io.ByteArrayOutputStream;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Key;

import ccio.imman.tools.SshService;
import ccio.imman.tools.digitalocean.model.DoSshKey;

public class CreateSshKey {
	
	public static DoSshKey create(String clusterName, DigitalOcean apiClient) throws JSchException, DigitalOceanException, RequestUnsuccessfulException {
		System.out.println("Creating SSH Key for cluster '"+clusterName+"'");
		SshService sshService=new SshService();
		ByteArrayOutputStream keyOs=new ByteArrayOutputStream();
		KeyPair keyPair=sshService.getNewKeyPair();
		keyPair.writePrivateKey(keyOs);
		final String sshPrivateKey = new String(keyOs.toByteArray());
		keyOs=new ByteArrayOutputStream();
		keyPair.writePublicKey(keyOs, clusterName);
		final String sshPublicKey = new String(keyOs.toByteArray());
		
		Key key = new Key(clusterName, sshPublicKey);
		return new DoSshKey(apiClient.createKey(key), sshPrivateKey);
	}

	public static void main(String[] args) throws JsonProcessingException, JSchException, DigitalOceanException, RequestUnsuccessfulException {
		Scanner keyboard = new Scanner(System.in);
		
		System.out.print("Cluster Name: ");
		String clusterName = keyboard.nextLine();
		
		System.out.print("DigitalOcean Token: ");
		String token = keyboard.nextLine();
		
		DigitalOcean apiClient = new DigitalOceanClient(token);
		
		System.out.println(new ObjectMapper().writeValueAsString(CreateSshKey.create(clusterName, apiClient)));
		
		keyboard.close();
	}
}
