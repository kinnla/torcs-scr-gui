package torcs;

import javax.swing.JLabel;

class DriverClass {

	private String className;
	private JLabel label = null;

	DriverClass(String className) {
		this.className = className;
	}

	public String toString() {
		return className;
	}

	public String getName() {
		return className.substring(className.lastIndexOf('.') + 1,
				className.length() - 6);
	}

	public String getTextureFile() {
		String s = className.substring(0, className.lastIndexOf('.'));
		s = s.replace('.', '/');
		return "./src/" + s + "/car1-trb1.rgb";
	}

	public JLabel getLabel() {
		if (label == null) {
			label = new JLabel(getName());
		}
		return label;
	}
}