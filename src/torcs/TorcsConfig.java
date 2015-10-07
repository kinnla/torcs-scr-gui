package torcs;

import java.util.ArrayList;
import java.util.List;

import torcs.scr.Client;

public class TorcsConfig {

	// track name
	private String trackName = "wheel-1";
	// static final String TRACK_NAME = "g-track-3";

	// track category
	private String trackCategory = "road";

	// verbose flag
	// True: all data send to and received from the server will be logged to
	// std.out
	private boolean verbose = false;

	// damage flag.
	// True: damage counts and cars can be demolished. Default is false
	private boolean damage = false;

	// the stage. Either QUALIFYING or RACE
	private Client.Stage stage = Client.Stage.QUALIFYING;

	// number of laps
	private int numberOfLaps = 1;

	// drivers
	private List<Driver> drivers = new ArrayList<>();

	// -------------------- drivers -------------------------

	public List<Driver> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<Driver> drivers) {
		this.drivers = drivers;
	}

	public void addDriver(Driver d) {
		drivers.add(d);
	}

	public int getNumerOfDrivers() {
		return drivers.size();
	}

	public Driver getDriver(int index) {
		return drivers.get(index);
	}

	// ---------------------- getters and setters --------------------------

	public void setNumberOfLaps(int n) {
		numberOfLaps = n;
	}

	public int getNumberOfLaps() {
		return numberOfLaps;
	}

	public String getTrackName() {
		return trackName;
	}

	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}

	public String getTrackCategory() {
		return trackCategory;
	}

	public void setTrackCategory(String trackCategory) {
		this.trackCategory = trackCategory;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isDamage() {
		return damage;
	}

	public void setDamage(boolean damage) {
		this.damage = damage;
	}

	public Client.Stage getStage() {
		return stage;
	}

	public void setStage(Client.Stage stage) {
		this.stage = stage;
	}

}
