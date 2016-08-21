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
		assert line.trim().equals(line);

		String[] items = line.split("\\)\\(");
		assert items.length > 0;
		
		// strip brackets from first and last element
		items[0] = items[0].substring(1);
		items[items.length-1] = items[items.length-1].substring(0, items.length-1);
		
		for (String item : items) {
			assert item.trim().equals(item);

			String[] keyAndValues = item.split(" ");
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
				model.opponentSensors[i] = Double.parseDouble(input[i]);
			}
		}),

		track((String[] input, SensorModel model) -> {
			for (int i = 1; i < input.length; ++i) {
				model.trackEdgeSensors[i] = Double.parseDouble(input[i]);
			}
		}),

		trackPos((String[] input, SensorModel model) -> {
			model.trackPosition = Double.parseDouble(input[1]);
		});

		private Parser parser;

		Sensor(Parser parser) {
			this.parser = parser;
		}
	}
}
