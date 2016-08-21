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

	// counting each time that control is called
	private int tickcounter = 0;

	public Action control(SensorModel model) {

		// adjust tick counter
		tickcounter++;

		// check, if we just started the race
		if (tickcounter == 1) {
			System.out.println("This is Simple Driver on track "
					+ getTrackName());
			System.out.println("This is a race "
					+ (damage ? "with" : "without") + " damage.");
		}

		// create new action object to send our commands to the server
		Action action = new Action();

		// ---------------- compute wanted speed ----------------------

		double maxDistance = model.trackEdgeSensors[0];
		for (double d : model.trackEdgeSensors) {
			maxDistance = Math.max(maxDistance, d);
		}
		double targetSpeed = 14.4 * Math.sqrt(maxDistance);

		/*
		 * ----------------------- control velocity --------------------
		 */

		// simply accelerate until we reach our target speed.
		if (model.speed < targetSpeed) {
			action.accelerate = Math.min((targetSpeed - model.speed) / 10, 1);
		} else {
			action.brake = Math.min((model.speed - targetSpeed) / 10, 1);
		}
		assert action.brake * action.accelerate < 0.1;

		// ------------------- control gear ------------------------

		if (action.accelerate > 0 && model.rpm > 9000
				&& model.gear < 6) {
			action.gear = model.gear + 1;
		} else if (action.brake > 0 && model.rpm < 6000
				&& model.gear > 1) {
			action.gear = model.gear - 1;
		} else if (model.rpm < 3000 && model.gear > 1) {
			action.gear = model.gear - 1;
		} else {
			action.gear = model.gear;
		}

		/*
		 * ----------------------- control steering ---------------------
		 */

		// follow the track
		action.steering = model.angleToTrackAxis * 0.75;

		// avoid to come too close to the edges
		if (model.trackEdgeSensors[0] < 3.0) {
			action.steering -= (5.0 - model.trackEdgeSensors[0]) * 0.05;
		}
		if (model.trackEdgeSensors[18] < 3.0) {
			action.steering += (5.0 - model.trackEdgeSensors[18]) * 0.05;
		}

		// return the action
		return action;
	}

	public void shutdown() {
		System.out.println("Bye bye!");
	}
}
