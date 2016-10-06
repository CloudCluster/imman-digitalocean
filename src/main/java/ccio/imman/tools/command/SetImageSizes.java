package ccio.imman.tools.command;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class SetImageSizes extends CliCommand{

	private String widths; 
	private String heghts; 
	
	public SetImageSizes() {
		super("set-sizes", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader console) throws CliParseException {
		try {
			String res = console.readLine("["+cluster.getClusterName()+"] comma separated widths > ");
			if(StringUtils.isNotBlank(res)){
				widths = res;
			}else{
				widths = null;
			}
			res = console.readLine("["+cluster.getClusterName()+"] comma separated heights > ");
			if(StringUtils.isNotBlank(res)){
				heghts = res;
			}else{
				heghts = null;
			}
		} catch (IOException e) {
			throw new CliParseException(e.getMessage());
		}
		
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		try {
			cluster.setWidths(widths);
			cluster.setHeights(heghts);
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			throw new CliException(e);
		}
		return true;
	}

}
