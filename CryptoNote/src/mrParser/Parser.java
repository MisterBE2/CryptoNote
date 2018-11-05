package mrParser;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Parser {

	private static List<Command> commands = new LinkedList<Command>();
	private static String delim = "<:>";
	private static boolean debug = false;

	public static Command parse(String rawCommand) {
		Command tmp = null;
		char[] rawC = rawCommand.toCharArray();
		char[] _delim = delim.toCharArray();

		if (rawCommand.length() >= 3 && rawC[0] == _delim[0] && rawC[rawC.length - 1] == _delim[2]) {
			if (debug) {
				System.out.println("Command valid - parsing...");
			}

			rawCommand = rawCommand.replace("<", "");
			rawCommand = rawCommand.replace(">", "");

			// System.out.println(rawCommand);

			List<String> data = new LinkedList<>(Arrays.asList(rawCommand.split(":")));
			
			if (data.size() == 1) {
				tmp = new Command(data.get(0), null, true);

				if (debug) {
					System.out.println(
							"Command: <" + rawCommand + ">\n parsed as: " + tmp.getCommand() + ", has 0 properties");
				}
			} else {
				String tempCom = data.get(0);
				data.remove(0);
				tmp = new Command(tempCom, data, true);

				if (debug) {
					System.out.println("Command: <" + rawCommand + ">\n parsed as: " + tmp.getCommand() + ", has "
							+ data.size() + " property(ies):");

					for (String tempString : data) {
						System.out.println("PROP: " + tempString);
					}
				}
			}
		} else {
			if (debug) {
				System.out.println("Parsed rawCommand is not a command");
			}
			
			
			
			tmp = new Command(rawCommand, new LinkedList<String>(), false);
		}
		
		commands.add(tmp);

		return tmp;
	}

	public static void setDebug(boolean _debug) {
		debug = _debug;
	}
	
	public static List<Command> getStoredCommands()
	{
		return commands;
	}
}
