package ccio.imman.tools.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.google.common.io.Files;

import jline.console.ConsoleReader;

public class SaveSshKeysLocaly extends CliCommand{

	public SaveSshKeysLocaly() {
		super("save-keys", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		try {
			File file=Paths.get(System.getProperty("user.home"), ".ssh", cluster.getClusterName()).toAbsolutePath().toFile();
			if(!file.exists()){
				file.createNewFile();
			}
			Files.write(cluster.getPrivateSshKey().getBytes(), file);
			
			file=Paths.get(System.getProperty("user.home"), ".ssh", cluster.getClusterName()+".pub").toAbsolutePath().toFile();
			if(!file.exists()){
				file.createNewFile();
			}
			Files.write(cluster.getPublicSshKey().getBytes(), file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			throw new CliException(e.getMessage());
		}
	}

}
