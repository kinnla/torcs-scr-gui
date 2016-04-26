package torcs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import torcs.scr.Client;

public class Main {

	// path to torcs
	static String TORCS_FOLDER = "./torcs-exe";
	static String DRIVERS_FOLDER = "./bin/torcs/";

	private Writer writer;
	private BufferedReader reader;
	private RaceConfig raceConfig = new RaceConfig();

	// singleton
	private static Main main = null;

	public static Main getInstance() {
		if (main == null) {
			main = new Main();
		}
		return main;
	}

	private void initCMD() throws IOException {
		// start command shell and create a writer to interact with
		// processes
		ProcessBuilder builder = new ProcessBuilder("cmd.exe");
		Process p = builder.start();
		writer = new OutputStreamWriter(p.getOutputStream());
		reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	}

	private void killOldProcesses() throws IOException {
		writer.write("taskkill /f /im wtorcs.exe\n");
		writer.write("taskkill /f /im java.exe\n");
		writer.flush();
	}

	public void performAction() {

		// remove unselected drivers
		Iterator<DriverClass> iter = raceConfig.getDrivers().iterator();
		while (iter.hasNext()) {
			DriverClass driver = iter.next();
			if (driver.getLabel().getX() < 200) {
				iter.remove();
			}
		}

		// check, if any driver selected
		if (raceConfig.getDrivers().isEmpty()) {
			System.err.println("No Driver selected. System exit.");
			System.exit(0);
		}

		// check, if too many drivers are selected
		while (raceConfig.getDrivers().size() > 10) {
			System.err.println("Warning: too many drivers selected. Skipping "
					+ raceConfig.getDrivers().remove(10).getName() + ".");
		}

		// sort drivers
		boolean swapped = true;
		while (swapped == true) {
			swapped = false;
			for (int i = 1; i < raceConfig.getDrivers().size(); ++i) {
				if (raceConfig.getDrivers().get(i).getLabel().getY() < raceConfig
						.getDrivers().get(i - 1).getLabel().getY()) {
					Collections.swap(raceConfig.getDrivers(), i, i - 1);
					swapped = true;
				}
			}
		}

		// check, if we are starting a test run
		if (raceConfig.getStage() == Client.Stage.TEST) {

			// Test run. Make sure that we have only 1 driver selected
			while (raceConfig.getDrivers().size() > 1) {
				System.err.println("Warning: too many drivers selected for test run. Skipping "
						+ raceConfig.getDrivers().remove(1).getName() + ".");
			}
			
			// Add simple driver at the start
			raceConfig.getDrivers().add(0, new DriverClass("torcs.simple.SimpleDriver"));
			
			// add another 7 drivers in the middle 
			DriverClass d = raceConfig.getDriver(1);
			for (int i=0; i<7;++i){
				raceConfig.addDriver(d);
			}
			
			// add dahlem boys at the end
			raceConfig.addDriver(new DriverClass("torcs.misto.MistoDriver"));			
		}

		
		try {
			// write xml files
			writeQuickRace();
			writeDriverNames();

			// write car textures
			replaceTextures();

			// start executables
			// drivers first, as we don't change the working dir
			startDrivers();
			startTorcs();

			// read from the command shell
			String line;
			DriverClass lastDriver = raceConfig.getDrivers().get(
					raceConfig.getDrivers().size() - 1);
			while ((line = reader.readLine()) != null) {
				System.out.println(line);

				if (line.contains(lastDriver.toString())) {
					System.out.println("TERMINATED");
					System.exit(0);
				}
			}
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	private void readDrivers() throws IOException {
				File driversFolder = new File(DRIVERS_FOLDER);
		
		// check if drivers directory exists
		String[] packages = driversFolder.list();
		if (packages == null) {
			System.err
			.println("Error: Drivers Folder does not exist: " + DRIVERS_FOLDER);
			System.exit(1);
		}
		
		// iterate on packages and init drivers
		for (String p : packages) {
			File f = new File(driversFolder + "/" + p);
			if (!p.equals("scr") && f.isDirectory()) {
				String[] classes = f.list();
				for (String cls : classes) {
					if (cls.contains("Driver.class")) {
						String driver = "torcs." + p + "."
								+ cls.substring(0, cls.lastIndexOf('.'));
						raceConfig.addDriver(new DriverClass(driver));
						System.out.println("Finding driver: " + driver);
					}
				}
			}
		}
	}

	private void replaceTextures() {

		// iterate on the drivers
		for (int i = 0; i < raceConfig.getNumerOfDrivers(); ++i) {

			// check, if source file exists
			File source = new File(raceConfig.getDriver(i).getTextureFile());
			if (!source.exists()) {
				System.err
						.println("Warning: Texture File not found: " + source);

				// look for default texture file
				source = new File("./src/torcs/car1-trb1.rgb");
				if (!source.exists()) {
					continue;
				}
				System.out.println("Using default texture file.");
			}

			// replace file
			File target = new File(TORCS_FOLDER + "\\drivers\\scr_server\\" + i
					+ "\\car1-trb1.rgb");
			try {
				Files.copy(source.toPath(), target.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				System.out.println("texture file copied to: " + target);
			} catch (IOException e) {
				System.err.println("Warning: Can not replace texture file: "
						+ target);
			}
		}
	}

	private void writeDriverNames() {

		// delete old scr_server config
		File scrServerConfig = new File(TORCS_FOLDER
				+ "/drivers/scr_server/scr_server.xml");
		scrServerConfig.delete();

		// create reader and writer
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(Main.class
					.getClassLoader().getResourceAsStream(
							"torcs/scr_server.xml")));
			bw = new BufferedWriter(new FileWriter(scrServerConfig));
			String line;
			int lineCounter = 0;
			while ((line = br.readLine()) != null) {
				if (lineCounter % 11 == 6 && lineCounter > 11
						&& lineCounter / 11 <= raceConfig.getNumerOfDrivers()) {
					String s = raceConfig.getDriver(lineCounter / 11 - 1)
							.getName();
					bw.write("      <attstr name=\"name\" val=\"" + s
							+ "\"></attstr>\n");
				} else {
					bw.write(line + "\n");
				}
				lineCounter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeQuickRace() {

		// delete old quick race config
		File quickRaceConfig = new File(TORCS_FOLDER
				+ "\\config\\raceman\\quickrace.xml");
		quickRaceConfig.delete();

		// create reader and writer
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(Main.class
					.getClassLoader()
					.getResourceAsStream("torcs/quickrace.xml")));
			bw = new BufferedWriter(new FileWriter(quickRaceConfig));
			String line;
			int lineCounter = 0;
			while ((line = br.readLine()) != null) {
				if (lineCounter == 15) {
					bw.write("      <attstr name=\"name\" val=\""
							+ raceConfig.getTrackName() + "\"/>\n");
					bw.write("      <attstr name=\"category\" val=\""
							+ raceConfig.getTrackCategory() + "\"/>\n");
				} else if (lineCounter == 31) {
					bw.write("    <attnum name=\"laps\" val=\""
							+ raceConfig.getNumberOfLaps() + "\"/>\n");
				} else if (lineCounter == 46) {
					for (int i = 0; i < raceConfig.getNumerOfDrivers(); ++i) {
						bw.write("      <section name=\"" + (i + 1) + "\">\n");
						bw.write("        <attnum name=\"idx\" val=\"" + i
								+ "\"/>\n");
						bw.write("        <attstr name=\"module\" val=\"scr_server\"/>\n");
						bw.write("      </section>\n");
					}
				}
				bw.write(line + "\n");
				lineCounter++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void startTorcs() throws IOException {

		// check, if path is absolute
		String path = TORCS_FOLDER;
		if ((new File(path)).isAbsolute()) {
			writer.write(path.substring(0, 2) + "\n");
			path = (path.substring(2, path.length()));
		}

		writer.write("cd " + path + "\n");
		writer.write("start \"torcs\" \"wtorcs.exe\" -nofuel");
		if (!raceConfig.isDamage()) {
			writer.write(" -nodamage");
		}
		writer.write("\n");
		writer.flush();
	}

	private void startDrivers() throws IOException {
		
		// initial values for port and id
		int port = 3001;
		int id = 1;

		// iterate on the drivers
		for (DriverClass driver : raceConfig.getDrivers()) {
			// start each driver in its own process
			writer.write("start /MIN \"" + driver + "\" \"java\" ");
			writer.write("-cp \"" + System.getProperty("java.class.path")
					+ "\" -Xmx256m ");
			writer.write("torcs.scr.Client ");
			writer.write(driver + " host:127.0.0.1 port:" + (port++)
					+ " id:SCR" + (id++) + " damage:"
					+ (raceConfig.isDamage() ? "on" : "off") + " verbose:"
					+ (raceConfig.isVerbose() ? "on" : "off") + " track:"
					+ raceConfig.getTrackName() + " stage:"
					+ raceConfig.getStage());
			writer.write("\n");
			writer.flush();
			System.out.println("starting driver " + driver);
		} // end of for
	}

	public static void main(String[] args) throws IOException {
		
		// read parameters from input stream if any
		for (String arg : args) {
			if (arg.startsWith("TORCS_FOLDER")) {
				TORCS_FOLDER = arg.substring(arg.indexOf('=') + 1);
			} else if (arg.startsWith("DRIVERS_FOLDER")) {
				DRIVERS_FOLDER = arg.substring(arg.indexOf('=') + 1);
			} 				
		}
		
		// set log file
		System.setOut(new PrintStream(new FileOutputStream("log_main.txt",true)));
		
		// init
		getInstance().initCMD();
		getInstance().killOldProcesses();
		getInstance().readDrivers();

		// create panel
		TorcsPanel panel = new TorcsPanel(getInstance().raceConfig);
		panel.init();

		// create frame
		JFrame frame = panel.createFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocation(100, 100);
		frame.setVisible(true);
	}

}
