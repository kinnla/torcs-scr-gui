package torcs.simple;

import torcs.scr.Action;
import torcs.scr.Driver;
import torcs.scr.SensorModel;

/**
 * Simple controller as a starting point to develop your own one - accelerates
 * slowly - tries to maintain a constant speed (only accelerating, no braking) -
 * stays in first gear - steering follows the track and avoids to come too close
 * to the edges
 */
public class SimpleDriver extends Driver {

	// counting each time that control is called
	private int tickcounter = 0;

	public Action control(SensorModel sensorModel) {

		// adjust tick counter
		tickcounter++;

		// check, if we just started the race
		if (tickcounter == 1) {
			System.out.println("This is Simple Driver on track "
					+ getTrackName());
			System.out.println("This is a race "
					+ (damage ? "with" : "without") + " damage.");
		}

		double[] trackedgeSensors = sensorModel.getTrackEdgeSensors();

		// create new action object to send our commands to the server
		Action action = new Action();

		// ---------------- compute wanted speed ----------------------

		double maxDistance = trackedgeSensors[0];
		for (double d : trackedgeSensors) {
			maxDistance = Math.max(maxDistance, d);
		}
		double targetSpeed = 14.4 * Math.sqrt(maxDistance);

		/*
		 * ----------------------- control velocity --------------------
		 */

		// simply accelerate until we reach our target speed.
		double currentSpeed = sensorModel.getSpeed();
		if (currentSpeed < targetSpeed) {
			action.accelerate = Math.min((targetSpeed - currentSpeed) / 10, 1);
		} else {
			action.brake = Math.min((currentSpeed - targetSpeed) / 10, 1);
		}
		assert action.brake * action.accelerate < 0.1;

		// ------------------- control gear ------------------------

		int currentGear = sensorModel.getGear();
		if (action.accelerate > 0 && sensorModel.getRPM() > 9000
				&& currentGear < 6) {
			action.gear = currentGear + 1;
		} else if (action.brake > 0 && sensorModel.getRPM() < 6000
				&& currentGear > 1) {
			action.gear = currentGear - 1;
		} else if (sensorModel.getRPM() < 3000 && currentGear > 1) {
			action.gear = currentGear - 1;
		} else {
			action.gear = currentGear;
		}

		/*
		 * ----------------------- control steering ---------------------
		 */

		double trackAngle = sensorModel.getAngleToTrackAxis();
		double distanceLeft = trackedgeSensors[0];
		double distanceRight = trackedgeSensors[18];
		// System.out.println("trackAngle" + trackAngle);
		// System.out.println(Arrays.toString(trackedgeSensors));

		// follow the track
		action.steering = trackAngle * 0.75;

		// avoid to come too close to the edges
		if (distanceLeft < 3.0) {
			action.steering -= (5.0 - distanceLeft) * 0.05;
		}
		if (distanceRight < 3.0) {
			action.steering += (5.0 - distanceRight) * 0.05;
		}

		// return the action
		return action;
	}

	public void shutdown() {
		System.out.println("Bye bye!");
	}
}
