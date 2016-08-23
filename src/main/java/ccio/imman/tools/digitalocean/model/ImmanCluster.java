package ccio.imman.tools.digitalocean.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

public class ImmanCluster {
	
	private String clusterName;
	private Integer sshKeyId;
	private String privateSshKey;
	private String publicSshKey;
	private List<ImmanNode> imageNodes;
	private String widths;
	private String heights;
	private String s3Access;
	private String s3Secret;
	private String s3Bucket;
	private String secret;
	private String cfZone;
	private String cfSubDomain;
	private Integer volumeSizeGigabytes;
	
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public Integer getSshKeyId() {
		return sshKeyId;
	}
	public void setSshKeyId(Integer sshKeyId) {
		this.sshKeyId = sshKeyId;
	}
	public String getPrivateSshKey() {
		return privateSshKey;
	}
	public void setPrivateSshKey(String privateSshKey) {
		this.privateSshKey = privateSshKey;
	}
	public String getPublicSshKey() {
		return publicSshKey;
	}
	public void setPublicSshKey(String publicSshKey) {
		this.publicSshKey = publicSshKey;
	}
	public List<ImmanNode> getImageNodes() {
		return imageNodes;
	}
	public void setImageNodes(List<ImmanNode> imageNodes) {
		this.imageNodes = imageNodes;
	}
	public String getS3Access() {
		return s3Access;
	}
	public void setS3Access(String s3Access) {
		this.s3Access = s3Access;
	}
	public String getS3Secret() {
		return s3Secret;
	}
	public void setS3Secret(String s3Secret) {
		this.s3Secret = s3Secret;
	}
	public String getS3Bucket() {
		return s3Bucket;
	}
	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}
	public String getWidths() {
		return widths;
	}
	public void setWidths(String widths) {
		this.widths = widths;
	}
	public String getHeights() {
		return heights;
	}
	public void setHeights(String heights) {
		this.heights = heights;
	}
	public String getSecret() {
		if(secret == null){
			secret = RandomStringUtils.random(32, "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890");
		}
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getCfSubDomain() {
		return cfSubDomain;
	}
	public void setCfSubDomain(String cfSubDomain) {
		this.cfSubDomain = cfSubDomain;
	}
	public String getCfZone() {
		return cfZone;
	}
	public void setCfZone(String cfZone) {
		this.cfZone = cfZone;
	} 
	public Integer getVolumeSizeGigabytes() {
		return volumeSizeGigabytes;
	}
	public void setVolumeSizeGigabytes(Integer volumeSizeGigabytes) {
		this.volumeSizeGigabytes = volumeSizeGigabytes;
	}
	
	public static void save(ImmanCluster cluster) throws IOException{
		String result = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(cluster);
		File file = new File("/opt/ccio/clusters/"+cluster.getClusterName()+".json");
		Files.createParentDirs(file);
		Files.write(result.getBytes(), file);
	}
	
	public static void delete(ImmanCluster cluster) throws IOException{
		File file = new File("/opt/ccio/clusters/"+cluster.getClusterName()+".json");
		file.delete();
	}
	
	public static ImmanCluster read(String clusterName) throws IOException{
		return new ObjectMapper().readValue(new File("/opt/ccio/clusters/"+clusterName+".json"), ImmanCluster.class);
	}
}
