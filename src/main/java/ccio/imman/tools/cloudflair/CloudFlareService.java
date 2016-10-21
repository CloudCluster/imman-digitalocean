package ccio.imman.tools.cloudflair;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CloudFlareService {
	
	private static final String API_URL="https://api.cloudflare.com/client/v4";
	
	private String cfEmail;
	private String cfToken;
	private HashMap<String, String> zoneIds = new HashMap<>();
	
	public CloudFlareService(){
		super();
	}
	
	public CloudFlareService(String cfEmail, String cfToken) {
		super();
		this.cfEmail = cfEmail;
		this.cfToken = cfToken;
	}

	public String addRecord(String subDomainName, String cfZone, String ip) throws UnirestException, JSONException{
		String zoneId = getZoneId(cfZone);
		if(zoneId == null){
			System.out.println("ERROR: Cannot find CF Zone: "+cfZone);
			return null;
		}
		HttpResponse<JsonNode> result = Unirest.post(API_URL+"/zones/"+zoneId+"/dns_records")
				.header("X-Auth-Email", cfEmail)
				.header("X-Auth-Key", cfToken)
				.header("Content-Type", "application/json")
				.body("{\n" + 
						"    \"type\": \"A\",\n" + 
						"    \"name\": \""+subDomainName+"\",\n" + 
						"    \"content\": \""+ip+"\",\n" + 
						"    \"proxied\": true\n" + 
						"}")
			.asJson();
			
		JSONObject resp = result.getBody().getObject().getJSONObject("result");
		System.out.println(resp);
		if(resp!=null){
			return resp.getString("id");
		}
		
		return null;
	}
	
	private String getZoneId(String cfZone) throws UnirestException{
		String zoneId = zoneIds.get(cfZone);
		if(zoneId != null){
			return zoneId;
		}
		HttpResponse<JsonNode> result = Unirest.get(API_URL+"/zones")
				.header("X-Auth-Email", cfEmail)
				.header("X-Auth-Key", cfToken)
				.header("Content-Type", "application/json")
				.asJson();
		JSONArray resp = result.getBody().getObject().getJSONArray("result");
		if(resp != null){
			for(int i=0; i<resp.length(); i++){
				if(cfZone.equalsIgnoreCase(resp.getJSONObject(i).getString("name"))){
					zoneId = resp.getJSONObject(i).getString("id");
					zoneIds.put(cfZone, zoneId);
					return zoneId;
				}
			}
		}
		return null;
	}
	
	public boolean deleteRecord(String cfZone, String dnsRecordId) throws UnirestException {
		String zoneId = getZoneId(cfZone);
		if(zoneId == null){
			return false;
		}
		HttpResponse<JsonNode> result = Unirest.delete(API_URL+"/zones/"+cfZone+"/dns_records/"+dnsRecordId)
				.header("X-Auth-Email", cfEmail)
				.header("X-Auth-Key", cfToken)
				.header("Content-Type", "application/json")
				.asJson();
		JSONObject obj = result.getBody().getObject();
		System.out.println(obj);
		return obj.getBoolean("success");
	}
	
	public boolean purgeFile(String cfZone, String fileUrl) throws UnirestException{
		String zoneId = getZoneId(cfZone);
		if(zoneId == null){
			return false;
		}
		HttpResponse<JsonNode> result = Unirest.delete(API_URL+"/zones/"+cfZone+"/purge_cache")
				.header("X-Auth-Email", cfEmail)
				.header("X-Auth-Key", cfToken)
				.header("Content-Type", "application/json")
				.body("{\"files\":[\""+fileUrl+"\"]}'")
				.asJson();
		JSONObject obj = result.getBody().getObject();
		System.out.println(obj);
		return obj.getBoolean("success");
	}

	public void setCloudFlareEmail(String cloudFlareEmail){
		this.cfEmail = cloudFlareEmail;
	}
	
	public void setCloudFlareToken(String cloudFlareToken){
		this.cfToken = cloudFlareToken;
	}
}