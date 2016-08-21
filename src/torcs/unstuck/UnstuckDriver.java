package torcs.unstuck;

import torcs.scr.Action;
import torcs.scr.Driver;
import torcs.scr.SensorModel;

/**
 * Simple controller as a starting point to develop your own one - accelerates
 * slowly - tries to maintain a constant speed (only accelerating, no braking) -
 * stays in first gear - steering follows the track and avoids to come too close
 * to the edges
 */
public class UnstuckDriver extends Driver {

	// ------------------ constants

	// rpm threshold to gear up
	static final int GEAR_UP = 8000;

	// rpm threshold to gear down
	static final int GEAR_DOWN = 5000;

	// minimum angle to trigger an unstuck
	static final double STUCK_ANGLE = Math.toRadians(30);

	// angle that the car aims to have when driving back to the track
	static final double OFFTRACK_ANGLE = Math.toRadians(20);

	// threshold for a low speed
	static final int LOW_SPEED = 5;

	// ------------------ private fields

	// sensor model to be updated every 20ms
	SensorModel model = null;

	Status status = Status.DRIVE;

	// temporary data
	TempData temp = new TempData();

	// action object to send our commands to the server
	private Action action = new Action();

	// ------------------------- Constructor
	public UnstuckDriver() {
		System.out.println("This is UnstuckDriver on track " + getTrackName());
		System.out.println("This is a race " + (damage ? "with" : "without") + " damage.");
	}

	// ------------------ public methods

	// main control loop
	public Action control(SensorModel m) {

		// update sensor model
		this.model = m;

		// update status and temp values
		status.updateStatus(this);
		temp.update(m);

		// compute and return action
		status.computeAction(action, this);
		return action;
	}

	// Sets the next gear based on rpm and current gear
	int controlGear() {

		// if gear is 0 (N) or -1 (R) just return 1
		if (model.gear < 1) {
			return 1;
		}

		// check if the RPM value of car is greater than the one suggested
		// to shift up the gear from the current one
		else if (model.gear < 6 && model.rpm >= GEAR_UP) {
			return model.gear + 1;
		}

		// check if the RPM value of car is lower than the one suggested
		// to shift down the gear from the current one
		else if (model.gear > 1 && model.rpm <= GEAR_DOWN) {
			return model.gear - 1;
		}

		// otherwhise keep current gear
		else {
			return model.gear;
		}
	}

	// encapsulates temporary values that help us to compute the driver's
	// behavior
	class TempData {

		// indicating whethere the car is on track
		boolean onTrack = true;

		// indicates whether the car has a sharp angle related to track
		// direction
		boolean sharpAngle = false;

		// indicates whether the car's speed (both x and y) is low
		boolean lowSpeed = true;

		// indicates whether the car's direction is towards the middle of the
		// track
		boolean towardsTrackCenter = true;

		// updates the temporary values.
		// to be called once per tick
		void update(SensorModel m) {
			onTrack = Math.abs(m.trackPosition) < 1;
			sharpAngle = Math.abs(m.angleToTrackAxis) > Math.toRadians(STUCK_ANGLE);
			lowSpeed = Math.abs(m.speed) + Math.abs(m.lateralSpeed) < LOW_SPEED;
			towardsTrackCenter = m.angleToTrackAxis > 0 && m.trackPosition < 0;
		}
	}

	private enum Status {

		// standard driving behaviour
		DRIVE {
			@Override
			void updateStatus(UnstuckDriver driver) {

				// check, if stuck and we shall go backwards
				if (driver.temp.sharpAngle && driver.temp.lowSpeed && !driver.temp.towardsTrackCenter) {
					driver.status = BACK;
				}

				// check if off road
				if (!driver.temp.onTrack) {
					driver.status = OFF_TRACK;
				}
			}

			@Override
			void computeAction(Action a, UnstuckDriver d) {
				a.gear = d.controlGear();
				a.steering = 0; // TODO
				a.accelerate = 1; //TODO
				a.brake = 0; //TODO
			}
		},

		// driving offroad (head back towards track, no full acceleration)
		OFF_TRACK {
			@Override
			void updateStatus(UnstuckDriver driver) {

				// check, if stuck and we shall go backwards
				if (driver.temp.sharpAngle && driver.temp.lowSpeed && !driver.temp.towardsTrackCenter) {
					driver.status = BACK;
				}

				// check if off road
				if (!driver.temp.onTrack) {
					driver.status = OFF_TRACK;
				}
			}

			@Override
			void computeAction(Action a, UnstuckDriver d) {
				a.gear = d.controlGear();
				a.steering = OFFTRACK_ANGLE * Math.signum(d.model.trackPosition) + d.model.angleToTrackAxis;
				a.accelerate = 0.5;
				a.brake = 0;
			}
		},

		// heading backwars in order to unstuck
		BACK {
			@Override
			void updateStatus(UnstuckDriver driver) {

				// check if direction is ok again
				if (driver.temp.towardsTrackCenter) {
					driver.status = HALT;
				}
			}

			@Override
			void computeAction(Action a, UnstuckDriver d) {
				a.gear = -1;
				a.steering = Math.signum(d.model.trackPosition);
				a.accelerate = 0.5;
				a.brake = 0;
			}
		},

		// car shall halt
		HALT {
			@Override
			void updateStatus(UnstuckDriver driver) {

				// if speed is low, switch to drive
				if (driver.temp.lowSpeed) {
					driver.status = DRIVE;
				}
			}

			@Override
			void computeAction(Action a, UnstuckDriver d) {
				a.gear = 0;
				a.steering = 0;
				a.accelerate = 0;
				a.brake = 1;
			}
		};

		// updates the current status 
		abstract void updateStatus(UnstuckDriver driver);

		// computes the action
		abstract void computeAction(Action a, UnstuckDriver d);
	}

}
