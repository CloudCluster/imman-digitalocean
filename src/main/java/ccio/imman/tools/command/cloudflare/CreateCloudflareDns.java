package ccio.imman.tools.command.cloudflare;

import java.io.IOException;

import org.json.JSONException;

import com.mashape.unirest.http.exceptions.UnirestException;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.cloudflair.CloudFlareService;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class CreateCloudflareDns extends CliCommand {
	
	public CreateCloudflareDns() {
		super("create-cf", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		CloudFlareService service = new CloudFlareService(cluster.getCfEmail(), cluster.getCfToken());
		for(ImmanNode node : cluster.getImageNodes()){
			try {
				String recId = service.addRecord(cluster.getCfSubDomain(), cluster.getCfZone(), node.getPublicIp());
				node.setCloudFlareDnsId(recId);
			} catch (JSONException | UnirestException e) {
				e.printStackTrace();
			}
		}
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
