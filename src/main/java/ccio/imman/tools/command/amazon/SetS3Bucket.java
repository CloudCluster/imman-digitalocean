package ccio.imman.tools.command.amazon;

import java.io.IOException;

import ccio.imman.tools.ImmanCluster;
import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CliParseException;
import jline.console.ConsoleReader;

public class SetS3Bucket extends CliCommand{

	private String s3bucket; 
	private String awsAccess;
	private String awsSecret;
	
	public SetS3Bucket() {
		super("set-s3", "s3bucket awsAccess awsSecret");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		if(cmdArgs == null || cmdArgs.length!=4){
			throw new CliParseException("requered arguments: s3bucket awsAccess awsSecret");
		}
		s3bucket = cmdArgs[1];
		awsAccess = cmdArgs[2];
		awsSecret = cmdArgs[3];
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		cluster.setS3Bucket(s3bucket);
		cluster.setS3Access(awsAccess);
		cluster.setS3Secret(awsSecret);
		try {
			ImmanCluster.save(cluster);
		} catch (IOException e) {
			throw new CliException(e);
		}
		return true;
	}

}
