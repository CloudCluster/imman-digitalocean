package ccio.imman.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccio.imman.tools.command.CliCommand;
import ccio.imman.tools.command.CliException;
import ccio.imman.tools.command.CommandNotFoundException;
import ccio.imman.tools.command.HelpCommand;
import ccio.imman.tools.command.MalformedCommandException;
import ccio.imman.tools.command.SaveSshKeysLocaly;
import ccio.imman.tools.command.SetImageSizes;
import ccio.imman.tools.command.ShowClusterCommand;
import ccio.imman.tools.command.amazon.SetS3Bucket;
import ccio.imman.tools.command.cloudflare.CreateCloudflareDns;
import ccio.imman.tools.command.cloudflare.DeleteCloudflareDns;
import ccio.imman.tools.command.cloudflare.SetCloudFlare;
import ccio.imman.tools.command.digitalocean.CreateDroplets;
import ccio.imman.tools.command.digitalocean.CreateSshKey;
import ccio.imman.tools.command.digitalocean.CreateVolumes;
import ccio.imman.tools.command.digitalocean.DeleteDroplets;
import ccio.imman.tools.command.digitalocean.DeleteSshKey;
import ccio.imman.tools.command.digitalocean.DeleteVolumes;
import ccio.imman.tools.command.digitalocean.DropletStart;
import ccio.imman.tools.command.digitalocean.DropletStatus;
import ccio.imman.tools.command.digitalocean.DropletStop;
import ccio.imman.tools.command.digitalocean.SetDigitalOcean;
import ccio.imman.tools.command.ssh.CopyConfigFile;
import ccio.imman.tools.command.ssh.CopyJarAndConfig;
import ccio.imman.tools.command.ssh.DeleteCachedFile;
import ccio.imman.tools.command.ssh.Firewall;
import ccio.imman.tools.command.ssh.SetupNodeCommand;
import jline.console.ConsoleReader;

public class Main {
	
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    public static final Map<String, String> COMMANDS = new HashMap<String, String>( );
    protected static final Map<String, CliCommand> CLI_COMMANDS = new HashMap<String,CliCommand>( );

    protected MyCommandOptions cl = new MyCommandOptions();
    protected HashMap<Integer,String> history = new HashMap<Integer,String>( );
    protected int commandCount = 0;
    protected int exitCode = 0;

    protected ImmanCluster cluster = null;

    static {
        COMMANDS.put("history","");
        COMMANDS.put("redo","cmdno");
        COMMANDS.put("quit", "");

        new ShowClusterCommand().addToMap(CLI_COMMANDS);
        new HelpCommand().addToMap(CLI_COMMANDS);
        new SetS3Bucket().addToMap(CLI_COMMANDS);
        new SetCloudFlare().addToMap(CLI_COMMANDS);
        new SetDigitalOcean().addToMap(CLI_COMMANDS);
        new CreateDroplets().addToMap(CLI_COMMANDS);
        new CreateSshKey().addToMap(CLI_COMMANDS);
        new DropletStatus().addToMap(CLI_COMMANDS);
        new CreateVolumes().addToMap(CLI_COMMANDS);
        new SetupNodeCommand().addToMap(CLI_COMMANDS);
        new CopyJarAndConfig().addToMap(CLI_COMMANDS);
        new CopyConfigFile().addToMap(CLI_COMMANDS);
        new SetImageSizes().addToMap(CLI_COMMANDS);
        new DropletStart().addToMap(CLI_COMMANDS);
        new DropletStop().addToMap(CLI_COMMANDS);
        new SaveSshKeysLocaly().addToMap(CLI_COMMANDS);
        new Firewall().addToMap(CLI_COMMANDS);
        new DeleteDroplets().addToMap(CLI_COMMANDS);
        new CreateCloudflareDns().addToMap(CLI_COMMANDS);
        new DeleteVolumes().addToMap(CLI_COMMANDS);
        new DeleteCloudflareDns().addToMap(CLI_COMMANDS);
        new DeleteSshKey().addToMap(CLI_COMMANDS);
        new DeleteCachedFile().addToMap(CLI_COMMANDS);

        // add all to COMMANDS
        for (Entry<String, CliCommand> entry : CLI_COMMANDS.entrySet()) {
            COMMANDS.put(entry.getKey(), entry.getValue().getOptionStr());
        }
    }

	void run() throws CliException, IOException, InterruptedException {
        System.out.println("Welcome to ImMan Tools!");

        try(ConsoleReader console = new ConsoleReader()){
//            console.addCompleter(completer);

        	String line = null;

        	if(cluster == null){
        		while (line == null || line.equals("")) {
        			line = console.readLine("[Set Cluster Name]> ");
        			try{
        				cluster = ImmanCluster.read(line);
        				System.out.println(line + " is loaded from a file");
        			}catch(IOException e){
        				cluster = new ImmanCluster();
        				cluster.setClusterName(line);
        				ImmanCluster.save(cluster);
        				System.out.println(line + " is created");
        			}
        		}
        	}
        	
	        while ((line = console.readLine("[" + cluster.getClusterName() + "]> ")) != null) {
	            executeLine(line, console);
	        }
        }
            
        System.exit(exitCode);
    }
	
	public void executeLine(String line, ConsoleReader reader) throws CliException, InterruptedException, IOException {
		if (!line.equals("")) {
			cl.parseCommand(line);
			history.put(commandCount, line);
			processCmd(cl, reader);
			commandCount++;
		}
	}
	    
	protected boolean processCmd(MyCommandOptions co, ConsoleReader reader) throws CliException, IOException, InterruptedException {
        boolean watch = false;
        try {
            watch = processZKCmd(co, reader);
            exitCode = 0;
        } catch (CliException ex) {
            exitCode = ex.getExitCode();
            System.err.println(ex.getMessage());
        }
        return watch;
    }
	
	protected boolean processZKCmd(MyCommandOptions co, ConsoleReader reader) throws CliException, IOException, InterruptedException {
        String[] args = co.getArgArray();
        String cmd = co.getCommand();
        if (args.length < 1) {
        	CLI_COMMANDS.get("help").exec();
            throw new MalformedCommandException("No command entered");
        }

        if (!COMMANDS.containsKey(cmd)) {
        	CLI_COMMANDS.get("help").exec();
            throw new CommandNotFoundException("Command not found " + cmd);
        }
        
        boolean watch = false;
        LOG.debug("Processing " + cmd);


        if (cmd.equals("quit")) {
//            zk.close();
            System.exit(exitCode);
        } else if (cmd.equals("redo") && args.length >= 2) {
            Integer i = Integer.decode(args[1]);
            if (commandCount <= i) { // don't allow redoing this redo
                throw new MalformedCommandException("Command index out of range");
            }
            cl.parseCommand(history.get(i));
            if (cl.getCommand().equals("redo")) {
                throw new MalformedCommandException("No redoing redos");
            }
            history.put(commandCount, history.get(i));
            processCmd(cl, reader);
        } else if (cmd.equals("history")) {
            for (int i = commandCount - 10; i <= commandCount; ++i) {
                if (i < 0) continue;
                System.out.println(i + " - " + history.get(i));
            }
        }
        
        // execute from COMMANDS
        CliCommand cliCmd = CLI_COMMANDS.get(cmd);
        if(cliCmd != null) {
            cliCmd.setCluster(cluster);
            watch = cliCmd.parse(args, reader).exec();
        } else if (!COMMANDS.containsKey(cmd)) {
             CLI_COMMANDS.get("help").exec();
        }
        return watch;
    }

	public static void main(String[] args) throws CliException, IOException, InterruptedException {
		new Main().run();
		
//		ConsoleReader reader = new ConsoleReader();
//
//        reader.setPrompt("[CLUSTER NAME]> ");
        
		
        
        
//		try (Scanner keyboard = new Scanner(System.in);){
//			
//			String clusterName = line.getOptionValue("c");
//			if(clusterName == null){
//				System.out.print("Cluster Name: ");
//				clusterName = keyboard.nextLine();
//			}else{
//				System.out.println("Cluster Name: "+clusterName);
//			}
//			
//			ImmanCluster cluster = null;
//			try{
//				cluster = ImmanCluster.read(clusterName);
//			}catch(IOException e){
//				cluster = new ImmanCluster();
//				cluster.setClusterName(clusterName);
//				cluster.setCfSubDomain(line.getOptionValue("cfd"));
//				cluster.setCfZone(line.getOptionValue("cfz"));
//				cluster.setS3Access(line.getOptionValue("s3a"));
//				cluster.setS3Secret(line.getOptionValue("s3s"));
//				cluster.setS3Bucket(line.getOptionValue("s3b"));
//				cluster.setWidths(line.getOptionValue("iw"));
//				cluster.setHeights(line.getOptionValue("ih"));
//				
//				ImmanCluster.save(cluster);
//			}
//
//			String token = line.getOptionValue("do");
//			if(token == null){
//				System.out.print("DigitalOcean Token: ");
//				token = keyboard.nextLine();
//			}
//			
//			DigitalOcean apiClient = new DigitalOceanClient(token);
//			
//			String cfEmail = line.getOptionValue("cfe");
//			if(cfEmail == null){
//				System.out.print("CloudFlair Email: ");
//				cfEmail = keyboard.nextLine();
//			}
//			
//			String cfToken = line.getOptionValue("cft");
//			if(cfToken == null){
//				System.out.print("CloudFlair Token: ");
//				cfToken = keyboard.nextLine();
//			}
//			
//			CloudFlareService cloudFlareService = new CloudFlareService(cfEmail, cfToken);
//			
//			boolean loop = true;
//			
//			while(loop){
//				System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//				System.out.println("What do you want to do next:");
//				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//				System.out.println("1: Create SSH Key and Droplets for the Cluster");
//				System.out.println("2: Check Droplet's Status");
//				System.out.println("3: Create and attach Volumes to each Droplet");
//				System.out.println("4: Setup Nodes");
//				System.out.println("5: Copy Config File");
//				System.out.println("6: Copy JAR to the nodes and set Service up");
//				System.out.println("7: Create DNS Records");
//				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//				System.out.println("41: re-Apply Firewall");
//				System.out.println("61: Stopping all Droplets");
//				System.out.println("62: Starting all Droplets");
//				System.out.println("81: Delete all cached files");
//				System.out.println("99: Delete whole Cluster");
//				System.out.println("0: Exit");
//				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
//				
//				int oper = keyboard.nextInt();
//				
//				switch(oper){
//				
//					case 1:
//						cluster = ImmanCluster.read(clusterName);
//						cluster.setClusterName(clusterName);
//						new CreateDroplets().withInput(keyboard, apiClient, cluster);
//						break;
//						
//					case 2:
//						cluster = ImmanCluster.read(clusterName);
//						new CheckDropletStatus().withInput(keyboard, apiClient, cluster);
//						break;
//						
//					case 3:
//						cluster = ImmanCluster.read(clusterName);
//						new CreateVolumes().withInput(keyboard, apiClient, cluster);
//						break;
//						
//					case 4:
//						cluster = ImmanCluster.read(clusterName);
//						new SetupNode().apply(cluster);
//						break;
//						
//					case 41:
//						cluster = ImmanCluster.read(clusterName);
//						new Firewall().apply(cluster);
//						break;
//						
//					case 5:
//						cluster = ImmanCluster.read(clusterName);
//						new ConfigFile().withInput(keyboard, cluster);
//						break;
//						
//					case 6:
//						cluster = ImmanCluster.read(clusterName);
//						keyboard.nextLine();
//						new CopyJar().withInput(keyboard, cluster);
//						break;
//					
//					case 7:
//						cluster = ImmanCluster.read(clusterName);
//						new CreateDnsRecords().withInput(keyboard, cloudFlareService, cluster);
//						break;
//						
//					case 61:
//						cluster = ImmanCluster.read(clusterName);
//						new StopDropolets().process(apiClient, cluster);
//						break;
//						
//					case 62:
//						cluster = ImmanCluster.read(clusterName);
//						new StartDropolets().process(apiClient, cluster);
//						break;
//						
//					case 81:
//						cluster = ImmanCluster.read(clusterName);
//						new CleanFiles().apply(cluster);
//						break;
//						
//					case 99:
//						cluster = ImmanCluster.read(clusterName);
//						new DeleteDropolets().withInput(keyboard, apiClient, cluster);
//						new DeleteVolumes().withInput(keyboard, apiClient, cluster);
//						new DeleteSshKey().withInput(keyboard, apiClient, cluster);
//						new DeleteDnsRecords().withInput(keyboard, cloudFlareService, cluster);
//						ImmanCluster.delete(cluster);
//						break;
//						
//					default: 
//						loop = false;
//				}
//				keyboard.nextLine();
//			}
//		} catch (ParseException | IOException exp) {
//			System.out.println(exp.getMessage());
//			printHelp(options);
//		}
	}
	
	private static class MyCommandOptions {

        private List<String> cmdArgs = null;
        private String command = null;
        public static final Pattern ARGS_PATTERN = Pattern.compile("\\s*([^\"\']\\S*|\"[^\"]*\"|'[^']*')\\s*");
        public static final Pattern QUOTED_PATTERN = Pattern.compile("^([\'\"])(.*)(\\1)$");

        public String getCommand( ) {
            return command;
        }

//        public String getCmdArgument( int index ) {
//            return cmdArgs.get(index);
//        }
//
//        public int getNumArguments( ) {
//            return cmdArgs.size();
//        }

        public String[] getArgArray() {
            return cmdArgs.toArray(new String[0]);
        }

        /**
         * Breaks a string into command + arguments.
         * @param cmdstring string of form "cmd arg1 arg2..etc"
         * @return true if parsing succeeded.
         */
        public boolean parseCommand(String cmdstring) {
            Matcher matcher = ARGS_PATTERN.matcher(cmdstring);

            List<String> args = new LinkedList<String>();
            while (matcher.find()) {
                String value = matcher.group(1);
                if (QUOTED_PATTERN.matcher(value).matches()) {
                    // Strip off the surrounding quotes
                    value = value.substring(1, value.length() - 1);
                }
                args.add(value);
            }
            
            if (args.isEmpty()){
                return false;
            }
            command = args.get(0);
            cmdArgs = args;
            return true;
        }
    }

}
