package ccio.imman.tools;

public class ImmanNode {

	private Integer dropletId;
	private String dropletName;
	private String privateIp;
	private String publicIp;
	private String regionSlug;
	private String volumeId;
	private String cloudFlareDnsId;
	
	public ImmanNode(){
		super();
	}
	
	public ImmanNode(Integer dropletId, String dropletName, String privateIp, String publicIp, String regionSlug) {
		super();
		this.dropletId = dropletId;
		this.dropletName = dropletName;
		this.privateIp = privateIp;
		this.publicIp = publicIp;
		this.regionSlug = regionSlug;
	}
	
	public String getPrivateIp() {
		return privateIp;
	}
	public void setPrivateIp(String privateIp) {
		this.privateIp = privateIp;
	}
	public String getPublicIp() {
		return publicIp;
	}
	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}
	public Integer getDropletId() {
		return dropletId;
	}
	public void setDropletId(Integer dropletId) {
		this.dropletId = dropletId;
	}
	public String getDropletName() {
		return dropletName;
	}
	public void setDropletName(String dropletName) {
		this.dropletName = dropletName;
	}
	public String getCloudFlareDnsId() {
		return cloudFlareDnsId;
	}
	public void setCloudFlareDnsId(String cloudFlarDnsId) {
		this.cloudFlareDnsId = cloudFlarDnsId;
	}
	public String getRegionSlug() {
		return regionSlug;
	}
	public void setRegionSlug(String regionSlug) {
		this.regionSlug = regionSlug;
	}
	public String getVolumeId() {
		return volumeId;
	}
	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}
}
