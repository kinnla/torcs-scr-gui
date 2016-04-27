package torcs.color;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import torcs.scr.Action;
import torcs.scr.Driver;
import torcs.scr.SensorModel;

/**
 * Simple controller as a starting point to develop your own one - accelerates
 * slowly - tries to maintain a constant speed (only accelerating, no braking) -
 * stays in first gear - steering follows the track and avoids to come too close
 * to the edges
 */
public abstract class ColorDriver extends Driver {

	// --------------------- parameters ------------------------------------

	static double SAFE_SPEED;
	static double MAX_SPEED;
	static int PANIC_TICKS;
	static double PANIC_DISTANCE;
	static double STUCK_ANGLE;
	static double UNSTUCK_ANGLE;
	static double MAX_OFFROAD_ACCELERATION;
	static double MAX_OFFROAD_BRAKE;
	static int GEAR_UP;
	static int GEAR_DOWN;
	static double INHIBIT_BY_STEERING;
	static double SPEEDING_COURAGE;

	// ----------------------- parameters class ------------------------

	static class DriverParameters extends Properties implements Runnable {

		private String parametersPath;

		DriverParameters(String parametersPath) {
			this.parametersPath = parametersPath;
			load();
		}

		private void load() {
			try {
				FileInputStream in = new FileInputStream(parametersPath);
				load(in);
			} catch (IOException e) {
				System.out
						.println("Error opening parameters file. Abort program.");
			}

			SAFE_SPEED = Double.parseDouble(getProperty("SAFE_SPEED"));
			System.out.println("SAFE_SPEED: " + SAFE_SPEED);
			MAX_SPEED = Double.parseDouble(getProperty("MAX_SPEED"));
			System.out.println("MAX_SPEED: " + MAX_SPEED);
			PANIC_TICKS = Integer.parseInt(getProperty("PANIC_TICKS"));
			System.out.println("PANIC_TICKS: " + PANIC_TICKS);
			PANIC_DISTANCE = Double.parseDouble(getProperty("PANIC_DISTANCE"));
			System.out.println("PANIC_DISTANCE: " + PANIC_DISTANCE);
			STUCK_ANGLE = Double.parseDouble(getProperty("STUCK_ANGLE"));
			System.out.println("STUCK_ANGLE: " + STUCK_ANGLE);
			UNSTUCK_ANGLE = Double.parseDouble(getProperty("UNSTUCK_ANGLE"));
			System.out.println("UNSTUCK_ANGLE: " + UNSTUCK_ANGLE);
			MAX_OFFROAD_ACCELERATION = Double
					.parseDouble(getProperty("MAX_OFFROAD_ACCELERATION"));
			System.out.println("MAX_OFFROAD_ACCELERATION: "
					+ MAX_OFFROAD_ACCELERATION);
			MAX_OFFROAD_BRAKE = Double
					.parseDouble(getProperty("MAX_OFFROAD_BRAKE"));
			System.out.println("MAX_OFFROAD_BRAKE: " + MAX_OFFROAD_BRAKE);
			GEAR_UP = Integer.parseInt(getProperty("GEAR_UP"));
			System.out.println("GEAR_UP: " + GEAR_UP);
			GEAR_DOWN = Integer.parseInt(getProperty("GEAR_DOWN"));
			System.out.println("GEAR_DOWN: " + GEAR_DOWN);
			INHIBIT_BY_STEERING = Double
					.parseDouble(getProperty("INHIBIT_BY_STEERING"));
			System.out.println("INHIBIT_BY_STEERING: " + INHIBIT_BY_STEERING);
			SPEEDING_COURAGE = Double
					.parseDouble(getProperty("SPEEDING_COURAGE"));
			System.out.println("SPEEDING_COURAGE: " + SPEEDING_COURAGE);
		}

		@Override
		public void run() {

		}

	}

	// --------------------- constants ------------------------------------

	static private enum Status {
		START, DRIVE, STUCKLEFT, STUCKRIGHT, PANIC
	}

	/* Gear Changing Constants */
	// static private final int[] gearUp = { 9500, 9500, 9500, 9500, 9500, 0 };
	// static private final int[] gearDown = { 0, 4000, 4200, 4500, 4500, 4500
	// };

	// ------------------------- sensor value fields -------------------------

	private double[] trackEdges;
	private double rpm;
	private double trackAngle;
	private int gear;
	private double speed;
	private double trackPosition;
	private double distanceRaced;

	// -------------------------- other fields ----------------------------

	// current state of this driver
	private Status currentState = Status.START;

	// count ticks
	private long tickCounter = 0;

	// action to be returned
	private Action action = new Action();

	// forward distance
	private double forwardDist = 200;

	// direction with the max forward distance
	private int direction = 9;

	// stuck counter
	private long stuckCounter = 0;

	// panic counter
	private double distanceRacedMem = 0;

	// wanted angle
	private double wantedAngle = 0;

	// wanted speed
	private double wantedSpeed = 0;

	// panic action
	private Action panicAction = null;

	/* -------------------- parameters stuff --------------- */

	public abstract String getParametersPath();

	private DriverParameters parameters = new DriverParameters(
			getParametersPath());

	/* ----------------------- main control loop ----------------------- */

	public Action control(SensorModel sensors) {

		// read sensors
		trackEdges = sensors.getTrackEdgeSensors();
		rpm = sensors.getRPM();
		trackAngle = sensors.getAngleToTrackAxis();
		gear = sensors.getGear();
		speed = sensors.getSpeed();
		trackPosition = sensors.getTrackPosition();
		distanceRaced = sensors.getDistanceRaced();

		/* ----------------------- detect Status ----------------------- */

		// X --> PANIC
		// PANIC --> START
		if (tickCounter % PANIC_TICKS == PANIC_TICKS - 1) {
			if (Math.abs(distanceRaced - distanceRacedMem) < PANIC_DISTANCE) {
				currentState = Status.PANIC;
				System.out.println("PANIC !!!");
				action.accelerate = Math.random();
				action.brake = 0;
				action.steering = Math.random();
				action.gear = Math.random() > 0.5 ? -1 : 1;
				panicAction = action;
			} else if (currentState == Status.PANIC) {
				currentState = Status.START;
			}
			distanceRacedMem = distanceRaced;
		}

		// START --> DRIVE
		if (currentState == Status.START && speed > 10) {
			currentState = Status.DRIVE;
		}

		// STUCKLEFT | STUCKRIGHT --> START
		if (currentState == Status.STUCKLEFT
				|| currentState == Status.STUCKRIGHT) {
			if (Math.abs(trackAngle) < Math.PI * UNSTUCK_ANGLE / 180) {
				currentState = Status.START;
			}
		}

		// NOT (PANIC) --> STUCKLEFT | STUCKRIGHT
		if (currentState != Status.PANIC) {
			// if (Math.abs(trackAngle) > Math.PI * STUCK_ANGLE / 180) {
			if (Math.abs(trackAngle) > Math.toRadians(STUCK_ANGLE)) {
				stuckCounter++;
				if (stuckCounter > 50) {
					if (trackAngle > 0 && trackPosition < 0) {
						currentState = Status.STUCKLEFT;
					} else if (trackAngle < 0 && trackPosition > 0) {
						currentState = Status.STUCKRIGHT;
					} else {
						currentState = Status.START;
						stuckCounter = -200; // give some 5s time to get back to
												// track
					}
				}
			} else if (stuckCounter > 0) {
				stuckCounter = 0;
			}
		}

		// ------------------ compute distances -------------------

		forwardDist = 0;
		for (int i = 7; i < 12; ++i) {
			if (trackEdges[i] > forwardDist) {
				forwardDist = trackEdges[i];
				direction = 9 - i;
			}
		}

		/* -------------------- switch on current state -------------------- */

		// switch on current state
		switch (currentState) {

		case START:

			// if we are going backwards, brake first
			if (speed < -1) {
				action.brake = 1;
				action.accelerate = 0;
				break;
			}

			// set the controls
			wantedSpeed = SAFE_SPEED;
			action.gear = 1;
			controlSteering();
			controlBrakeAndAcceleration();
			break;

		case DRIVE:

			// set the controls
			computeWantedSpeed();
			controlGear();
			controlSteering();
			controlBrakeAndAcceleration();
			break;

		case STUCKLEFT:

			// steer left and go backwards
			action.gear = -1;
			action.accelerate = 1;
			action.steering = -1;
			break;

		case STUCKRIGHT:

			// steer right and go backwards
			action.gear = -1;
			action.accelerate = 1;
			action.steering = 1;
			break;

		case PANIC:

			// random behavior, as determined when entering this state
			action = panicAction;
			break;

		}// end of switch

		/* ----------------------- clean up --------------------- */

		// adjust tick counter
		tickCounter++;

		// return the action
		return action;
	} // end of control()

	/* ----------------------- control steering --------------------- */

	// control steering by wanted angle
	private void controlSteering() {
		double pidError = trackAngle - wantedAngle;
		double wantedAngle = computeWantedAngle();
		double delta = trackAngle - wantedAngle;
		action.steering = 1.8 * delta + 1.8 * pidError + 0.00
				* Math.signum(delta);

		// System.out.println("action.steering: " + action.steering);
		// System.out.println("wantedAngle: " + wantedAngle);
		// System.out.println("trackAngle: " + trackAngle);
		// System.out.println("delta: " + delta);
		// System.out.println("pidError: " + pidError);
		// normalize steering
		if (action.steering > 0) {
			action.steering = Math.min(action.steering, 1);
		} else {
			action.steering = Math.max(action.steering, -1);
		}

	}

	// get steering
	private double computeWantedAngle() {

		// if left outside the track, steer right
		if (trackPosition > 1) {
			return Math.toRadians(20);
		}
		// if right outside the track, steer left
		if (trackPosition < -1) {
			return Math.toRadians(-20);
		}

		// close to the left edge
		if (trackEdges[0] < 3.0 && Math.abs(Math.toDegrees(trackAngle)) < 40) {
			return Math.toRadians((3 - trackEdges[0]) * 8);
		}

		// close to the right edge
		if (trackEdges[18] < 3.0 && Math.abs(Math.toDegrees(trackAngle)) < 40) {
			return Math.toRadians((3 - trackEdges[18]) * -8);
		}

		// otherwise get angle by direction and trackPosition
		if (direction == 0) {
			return 0.1 * (trackPosition - 0.5);
		}
		if (direction == 1) {
			return 1.1 * (trackPosition - 0.7);
		}
		if (direction == -1) {
			return 1.1 * (trackPosition - 0.3);
		}
		if (direction == 2) {
			return 1.1 * (trackPosition - 0.8);
		}
		if (direction == -2) {
			return 1.1 * (trackPosition - 0.2);
		}

		return 0;
	}

	/**
	 * Computes the wanted speed (for state DRIVE only)
	 * 
	 */
	private void computeWantedSpeed() {
		// evaluate trackedgesensors to determine wanted speed
		if (forwardDist > 199) {
			wantedSpeed = MAX_SPEED;
		} else {
			wantedSpeed = Math.max(SAFE_SPEED, Math.sqrt(forwardDist) * SPEEDING_COURAGE);
			wantedSpeed = Math.min(MAX_SPEED, wantedSpeed);
		}
	}

	/**
	 * Sets the next gear based on rpm and current gear
	 */
	private void controlGear() {

		// if gear is 0 (N) or -1 (R) just return 1
		if (gear < 1) {
			action.gear = 1;
		}

		// check if the RPM value of car is greater than the one suggested
		// to shift up the gear from the current one
		else if (gear < 6 && rpm >= GEAR_UP) {
			action.gear = gear + 1;
		}

		// check if the RPM value of car is lower than the one suggested
		// to shift down the gear from the current one
		else if (gear > 1 && rpm <= GEAR_DOWN) {
			action.gear = gear - 1;
		}

		// otherwhise keep current gear
		else {
			action.gear = gear;
		}
	}

	/**
	 * control the brake and acceleration
	 */
	private void controlBrakeAndAcceleration() {

		// PANIC: don't control anything
		if (currentState == Status.PANIC) {
			return;
		}

		// gear 0: no acceleration, full break
		if (action.gear == 0) {
			action.accelerate = 0;
			action.brake = 1;
			return;
		}

		if (action.gear == -1) {

			// we want to go backwards
			action.accelerate = 1;
			action.brake = 0;
		} else {

			// calculate speed and brake
			if (wantedSpeed > speed + 5) {

				// full speed
				action.accelerate = 1;
				action.brake = 0;
			} else if (wantedSpeed < speed - 5) {

				// full brake
				action.accelerate = 0;
				action.brake = 1;
			} else if (wantedSpeed > speed + 2) {

				// limited acceleration (linear ramp)
				action.accelerate = (wantedSpeed - speed) / 3;
				action.brake = 0;
			} else if (wantedSpeed < speed - 2) {

				// limited brake (linear ramp)
				action.accelerate = 0;
				action.brake = (speed - wantedSpeed) / 3;
			}
		}

		// off road: limit by offroad coefficient and steering
		if (Math.abs(trackPosition) > 1) {
			action.accelerate = Math.min(MAX_OFFROAD_ACCELERATION
					* (1 - Math.abs(action.steering) * INHIBIT_BY_STEERING),
					action.accelerate);
			action.brake = Math
					.min(MAX_OFFROAD_BRAKE
							* (1 - Math.abs(action.steering) * INHIBIT_BY_STEERING),
							action.brake);
		}

		// on road: limit by steering
		else {

			action.accelerate = Math.min(action.accelerate, 1 - Math.abs(action.steering)
					* INHIBIT_BY_STEERING);
			action.brake = Math.min(action.brake, 1 - Math.abs(action.steering)
					* INHIBIT_BY_STEERING);
		}
	}

	public void reset() {
		System.out.println("Restarting the race!");
	}

	public void shutdown() {
		System.out.println("Bye bye!");
	}
}
