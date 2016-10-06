package ccio.imman.tools.command;

import java.io.PrintStream;
import java.util.Map;

import ccio.imman.tools.ImmanCluster;
import jline.console.ConsoleReader;

abstract public class CliCommand {

    protected PrintStream out;
    protected PrintStream err;
    protected ImmanCluster cluster;
    private String cmdStr;
    private String optionStr;

    /**
     * a CLI command with command string and options.
     * Using System.out and System.err for printing
     * @param cmdStr the string used to call this command
     * @param optionStr the string used to call this command 
     */
    public CliCommand(String cmdStr, String optionStr) {
        this.out = System.out;
        this.err = System.err;
        this.cmdStr = cmdStr;
        this.optionStr = optionStr;
    }

    /**
     * Set out printStream (useable for testing)
     * @param out 
     */
    public void setOut(PrintStream out) {
        this.out = out;
    }

    /**
     * Set err printStream (useable for testing)
     * @param err 
     */
    public void setErr(PrintStream err) {
        this.err = err;
    }
    
    public void setCluster(ImmanCluster cluster){
    	this.cluster = cluster;
    }

    /**
     * get the string used to call this command
     * @return 
     */
    public String getCmdStr() {
        return cmdStr;
    }

    /**
     * get the option string
     * @return 
     */
    public String getOptionStr() {
        return optionStr;
    }

    /**
     * get a usage string, contains the command and the options
     * @return 
     */
    public String getUsageStr() {
        return cmdStr + " " + optionStr;
    }

    /**
     * add this command to a map. Use the command string as key.
     * @param cmdMap 
     */
    public void addToMap(Map<String, CliCommand> cmdMap) {
        cmdMap.put(cmdStr, this);
    }
    
    /**
     * parse the command arguments
     * @param cmdArgs
     * @return this CliCommand
     * @throws CliParseException
     */
    abstract public CliCommand parse(String cmdArgs[], ConsoleReader console) throws CliParseException;
    
    /**
     * 
     * @return
     * @throws CliException
     */
    abstract public boolean exec() throws CliException;
}