/**
 * 
 */
package torcs.scr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

/**
 * @author Daniele Loiacono
 * 
 */
public class Client {

	public static enum Stage {
		QUALIFYING, RACE, TEST
	};

	private static int UDP_TIMEOUT = 10000;
	private static int port;
	private static String host;
	private static String clientId;
	private static boolean verbose;
	private static boolean damage;
	private static Stage stage;
	private static String trackName;

	/**
	 * @param args
	 *            is used to define all the options of the client. <port:N> is
	 *            used to specify the port for the connection (default is 3001)
	 *            <host:ADDRESS> is used to specify the address of the host
	 *            where the server is running (default is localhost)
	 *            <id:ClientID> is used to specify the ID of the client sent to
	 *            the server (default is championship2009) <verbose:on> is used
	 *            to set verbose mode on (default is off) <maxEpisodes:N> is
	 *            used to set the number of episodes (default is 1) <maxSteps:N>
	 *            is used to set the max number of steps for each episode (0 is
	 *            default value, that means unlimited number of steps) <stage:N>
	 *            is used to set the current stage: 0 is WARMUP, 1 is
	 *            QUALIFYING, 2 is RACE, others value means UNKNOWN (default is
	 *            UNKNOWN) <trackName:name> is used to set the name of current
	 *            track
	 */
	public static void main(String[] args) {

		// read parameters
		parseParameters(args);

		// set log file
//		try {
//			String logFile = args[0].substring(args[0].lastIndexOf('.') + 1)
//					+ "_log.txt";
//			System.setOut(new PrintStream(new FileOutputStream(logFile, true)));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		SocketHandler mySocket = new SocketHandler(host, port, verbose);
		MessageParser parser = new MessageParser();
		String inMsg;

		Driver driver = load(args[0]);
		driver.setStage(stage);
		driver.setTrackName(trackName);
		driver.setVerbose(verbose);
		driver.setDamage(damage);

		/* Build init string */
		float[] angles = driver.initAngles();
		String initStr = clientId + "(init";
		for (int i = 0; i < angles.length; i++) {
			initStr = initStr + " " + angles[i];
		}
		initStr = initStr + ")";

		long curEpisode = 0;
		boolean shutdownOccurred = false;
		do {

			/*
			 * Client identification
			 */

			do {
				mySocket.send(initStr);
				inMsg = mySocket.receive(UDP_TIMEOUT);
			} while (inMsg == null || inMsg.indexOf("***identified***") < 0);

			/*
			 * Start to drive
			 */
			long currStep = 0;
			while (true) {
				/*
				 * Receives from TORCS the game state
				 */
				inMsg = mySocket.receive(UDP_TIMEOUT);

				if (inMsg != null) {

					/*
					 * Check if race is ended (shutdown)
					 */
					if (inMsg.indexOf("***shutdown***") >= 0) {
						shutdownOccurred = true;
						System.out.println("Server shutdown!");
						break;
					}

					try {
						Action action = new Action();
						SensorModel model = parser.parse(inMsg);
						action = driver.control(model);

						currStep++;
						mySocket.send(action.toString());
					} catch (Throwable t) {
						t.printStackTrace();
					}
				} else
					System.out
							.println("Server did not respond within the timeout");
			}

			++curEpisode;
		} while (!shutdownOccurred);

		/*
		 * Shutdown the controller
		 */
		driver.shutdown();
		mySocket.close();
		System.out.println("Client shutdown.");
		System.out.println("Bye, bye!");
	}

	private static void parseParameters(String[] args) {
		/*
		 * Set default values for the options
		 */
		port = 3001;
		host = "localhost";
		clientId = "SCR";
		verbose = false;
		stage = Stage.QUALIFYING;
		trackName = "unknown";

		for (int i = 1; i < args.length; i++) {
			StringTokenizer st = new StringTokenizer(args[i], ":");
			String entity = st.nextToken();
			String value = st.nextToken();
			if (entity.equals("port")) {
				port = Integer.parseInt(value);
			}
			if (entity.equals("host")) {
				host = value;
			}
			if (entity.equals("id")) {
				clientId = value;
			}
			if (entity.equals("verbose")) {
				if (value.equals("on"))
					verbose = true;
				else if (value.equals("off"))
					verbose = false;
				else {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
			if (entity.equals("id")) {
				clientId = value;
			}
			if (entity.equals("stage")) {
				stage = Stage.valueOf(value);
				System.out.println("Stage: " + stage);
			}
			if (entity.equals("trackName")) {
				trackName = value;
			}
			if (entity.equals("damage")) {
				if (value.equals("on"))
					damage = true;
				else if (value.equals("off"))
					damage = false;
				else {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
		}
	}

	private static Driver load(String name) {
		Driver controller = null;
		try {
			controller = (Driver) (Object) Class.forName(name).newInstance();
		} catch (ClassNotFoundException e) {
			System.out.println(name + " is not a class name");
			System.exit(0);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return controller;
	}
}
