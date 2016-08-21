package torcs.scr;

public abstract class Driver {

	protected String trackName;
	protected boolean verbose;
	protected boolean damage;
	protected Client.Stage stage;

	public float[] initAngles() {
		float[] angles = new float[19];
		for (int i = 0; i < 19; ++i)
			angles[i] = -90 + i * 10;
		return angles;
	}

	public Client.Stage getStage() {
		return stage;
	}

	public void setStage(Client.Stage stage) {
		this.stage = stage;
	}

	public String getTrackName() {
		return trackName;
	}

	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}

	public abstract Action control(SensorModel sensors);

	public void shutdown() {
		System.out.println("shutdown");
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setDamage(boolean damage) {
		this.damage = damage;
	}

}