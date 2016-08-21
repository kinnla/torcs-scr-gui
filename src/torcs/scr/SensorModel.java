package torcs.scr;

public class SensorModel {

	// basic information about your car and the track
	// (you probably should take care of these somehow)

	public double speed = 0;

	public double angleToTrackAxis = 0;

	public double[] trackEdgeSensors = new double[19];

	public double[] focusSensors = new double[5];

	public double trackPosition = 0;

	public int gear = 0;

	// basic information about other cars (only useful for multi-car races)

	public double[] opponentSensors = new double[36];

	public int racePosition = 0;

	// additional information (use if you need)

	public double lateralSpeed = 0;

	public double currentLapTime = 0;

	public double damage = 0;

	public double distanceFromStartLine = 0;

	public double distanceRaced = 0;

	public double fuelLevel = 0;

	public double lastLapTime = 0;

	public double rpm = 0;

	public double[] wheelSpinVelocity = new double[4];

	public double zSpeed = 0;

	public double z;
}
