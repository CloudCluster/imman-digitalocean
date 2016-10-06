package ccio.imman.tools.command.cloudflare;

import org.json.JSONException;

import com.mashape.unirest.http.exceptions.UnirestException;

import ccio.imman.tools.ImmanNode;
import ccio.imman.tools.cloudflair.CloudFlareService;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class DeleteCloudflareDns  extends CliCommand {
	
	public DeleteCloudflareDns() {
		super("delete-cf", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		boolean res = true;
		CloudFlareService service = new CloudFlareService(cluster.getCfEmail(), cluster.getCfToken());
		for(ImmanNode node : cluster.getImageNodes()){
			try {
				res = res && service.deleteRecord(cluster.getCfZone(), node.getCloudFlareDnsId());
			} catch (JSONException | UnirestException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All records deleted successfuly: "+res);
		return true;
	}
}
