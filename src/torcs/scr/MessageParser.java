package torcs.scr;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: Feb 22, 2008 Time:
 * 6:17:32 PM
 */
public class MessageParser {
	// parses the message from the serverbot, and creates a table of
	// associated names and values of the readings

	private SensorModel model = new SensorModel();

	public MessageParser() {
	}
	
	public SensorModel parse(String line) {
		String[] items = line.trim().split("\\)\\(");
		assert items.length > 0;
		
		// strip brackets from first and last element
		items[0] = items[0].substring(1);
		items[items.length-1] = items[items.length-1].substring(0, items.length-1);
		
		for (String item : items) {
			String[] keyAndValues = item.trim().split(" ");
			assert keyAndValues.length >=2;
			
			Sensor sensor = Sensor.valueOf(keyAndValues[0]);
			sensor.parser.parse(keyAndValues, model);
		}
		return model;
	}

	@FunctionalInterface
	interface Parser {
		public void parse(String[] input, SensorModel model);
	}

	private enum Sensor {

		angle((String[] input, SensorModel model) -> {
			model.angleToTrackAxis = Double.parseDouble(input[1]);
		}),

		opponents((String[] input, SensorModel model) -> {
			for (int i = 1; i < input.length; ++i) {
				model.opponentSensors[i-1] = Double.parseDouble(input[i]);
			}
		}),

		track((String[] input, SensorModel model) -> {
			for (int i = 1; i < input.length; ++i) {
				model.trackEdgeSensors[i-1] = Double.parseDouble(input[i]);
			}
		}),

		trackPos((String[] input, SensorModel model) -> {
			model.trackPosition = Double.parseDouble(input[1]);
		}),

		curLapTime((String[] input, SensorModel model) -> {
			model.currentLapTime = Double.parseDouble(input[1]);
		}),
		
		damage((String[] input, SensorModel model) -> {
			model.damage = Integer.parseInt(input[1]);
		}),
		
		distFromStart((String[] input, SensorModel model) -> {
			model.distanceFromStartLine = Double.parseDouble(input[1]);
		}),
		
		distRaced((String[] input, SensorModel model) -> {
			model.distanceRaced = Double.parseDouble(input[1]);
		}),
		
		fuel((String[] input, SensorModel model) -> {
			model.fuelLevel = Double.parseDouble(input[1]);
		}),
		
		gear((String[] input, SensorModel model) -> {
			model.gear = Integer.parseInt(input[1]);
		}),
		
		lastLapTime((String[] input, SensorModel model) -> {
			model.lastLapTime = Double.parseDouble(input[1]);
		}),
		
		racePos((String[] input, SensorModel model) -> {
			model.racePosition = Integer.parseInt(input[1]);
		}),
		
		rpm((String[] input, SensorModel model) -> {
			model.rpm = Double.parseDouble(input[1]);
		}),

		speedX((String[] input, SensorModel model) -> {
			model.speed = Double.parseDouble(input[1]);
		}),
		
		speedY((String[] input, SensorModel model) -> {
			model.lateralSpeed = Double.parseDouble(input[1]);
		}),

		speedZ((String[] input, SensorModel model) -> {
			model.zSpeed = Double.parseDouble(input[1]);
		}),
		
		wheelSpinVel((String[] input, SensorModel model) -> {
			for (int i = 1; i < input.length; ++i) {
				model.wheelSpinVelocity[i-1] = Double.parseDouble(input[i]);
			}
		}),
		
		z((String[] input, SensorModel model) -> {
			model.zSpeed = Double.parseDouble(input[1]);
		}),
		
		focus((String[] input, SensorModel model) -> {
			for (int i = 1; i < input.length; ++i) {
				model.focusSensors[i-1] = Double.parseDouble(input[i]);
			}
		});
		
		Parser parser;

		Sensor(Parser parser) {
			this.parser = parser;
		}
	}
}
