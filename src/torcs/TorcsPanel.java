package torcs;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import torcs.scr.Client;

public class TorcsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// integer fields for the point we clicked on an item
	int dragX;
	int dragY;

	// original location of the label when we pressed the mouse
	int origX;
	int origY;

	TorcsConfig torcsConfig;

	public TorcsPanel(TorcsConfig config) {
		super(null);
		this.torcsConfig = config;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		g.drawLine(200, 0, 200, 800);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(400, 800);
	}

	public void init() {
		for (int i = 0; i < torcsConfig.getDrivers().size(); ++i) {
			JLabel label = torcsConfig.getDrivers().get(i).getLabel();
			label.setBounds(0, i * 20, 200, 20);
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					dragX = e.getXOnScreen();
					dragY = e.getYOnScreen();
					origX = e.getComponent().getX();
					origY = e.getComponent().getY();
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					Point p = e.getComponent().getLocation();
					if (p.x > 150) {
						p.x = 200;
						e.getComponent().setLocation(p);
					}
				}
			});
			label.addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					int x = e.getXOnScreen();
					int y = e.getYOnScreen();
					e.getComponent().setLocation(x - dragX + origX,
							y - dragY + origY);
				}
			});
			add(label);
		}

		// check box for damage
		final JCheckBox damageBox = new JCheckBox("Damage");
		damageBox.setSelected(torcsConfig.isDamage());
		damageBox.setBounds(201, 560, 199, 30);
		add(damageBox);
		
		// check box for verbose
		final JCheckBox verboseBox = new JCheckBox("Verbose");
		verboseBox.setSelected(torcsConfig.isVerbose());
		verboseBox.setBounds(201, 590, 199, 30);
		add(verboseBox);
		
		// combo box to select the track
		final JComboBox<String> trackBox = new JComboBox<>();
		trackBox.addItem("wheel-1");
		trackBox.addItem("forza");
		trackBox.addItem("alpine-1");
		trackBox.addItem("spring");
		trackBox.setEditable(true);
		trackBox.setBounds(201, 620, 199, 30);
		add(trackBox);

		// radiobuttons to select the stage
		final JRadioButton stageQualifyingButton = new JRadioButton("Qualifying",
				torcsConfig.getStage() == Client.Stage.QUALIFYING);
		final JRadioButton stageRaceButton = new JRadioButton("Race",
				torcsConfig.getStage() == Client.Stage.RACE);
		final JRadioButton stageTestButton = new JRadioButton("Test",
				torcsConfig.getStage() == Client.Stage.TEST);
		add(stageQualifyingButton);
		add(stageRaceButton);
		add(stageTestButton);
		stageQualifyingButton.setBounds(201, 650, 199, 30);
		stageRaceButton.setBounds(201, 680, 199, 30);
		stageTestButton.setBounds(201, 710, 199, 30);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(stageQualifyingButton);
		buttonGroup.add(stageRaceButton);
		buttonGroup.add(stageTestButton);
		stageQualifyingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stageQualifyingButton.isSelected()){
					torcsConfig.setStage(Client.Stage.QUALIFYING);
				}
			}
		});
		stageRaceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stageRaceButton.isSelected()){
					torcsConfig.setStage(Client.Stage.RACE);
				}
			}
		});
		stageTestButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stageTestButton.isSelected()){
					torcsConfig.setStage(Client.Stage.TEST);
				}
			}
		});

		// number of laps label
		final JLabel numberOfLapsLabel = new JLabel("laps: "
				+ torcsConfig.getNumberOfLaps());
		numberOfLapsLabel.setBounds(201, 740, 99, 30);
		add(numberOfLapsLabel);

		// plusButton
		JButton plusButton = new JButton("+");
		plusButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				torcsConfig.setNumberOfLaps(torcsConfig.getNumberOfLaps() + 1);
				numberOfLapsLabel.setText("laps: "
						+ torcsConfig.getNumberOfLaps());
			}
		});
		plusButton.setBounds(300, 740, 50, 30);
		add(plusButton);

		// minus button
		JButton minusButton = new JButton("-");
		minusButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (torcsConfig.getNumberOfLaps() > 1) {
					torcsConfig.setNumberOfLaps(torcsConfig.getNumberOfLaps() - 1);
				}
				numberOfLapsLabel.setText("laps: "
						+ torcsConfig.getNumberOfLaps());
			}
		});
		minusButton.setBounds(350, 740, 50, 30);
		add(minusButton);

		// startbutton to start the race or qualifying
		JButton startButton = new JButton("start");
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				torcsConfig.setTrackName((String) trackBox.getSelectedItem());
				torcsConfig.setDamage(damageBox.isSelected());
				torcsConfig.setVerbose(verboseBox.isSelected());
				Main.getInstance().performAction();
			}
		});
		add(startButton);
		startButton.setBounds(201, 770, 199, 30);

	}

	public JFrame createFrame() {
		JFrame frame = new JFrame("Select Drivers");
		frame.setLayout(null);
		frame.setContentPane(this);
		return frame;
	}

}
