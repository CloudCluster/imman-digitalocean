package ccio.imman.tools.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ccio.imman.tools.Main;
import jline.console.ConsoleReader;

public class HelpCommand extends CliCommand{

	public HelpCommand() {
		super("help", "");
	}

	@Override
	public CliCommand parse(String[] cmdArgs, ConsoleReader reader) throws CliParseException {
		return this;
	}

	@Override
	public boolean exec() throws CliException {
		System.err.println("imman-tools {clustername}");
        List<String> cmdList = new ArrayList<String>(Main.COMMANDS.keySet());
        Collections.sort(cmdList);
        for (String cmd : cmdList) {
            System.err.println("\t"+cmd+ " " + Main.COMMANDS.get(cmd));
        }
		return true;
	}

}
