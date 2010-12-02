package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class FirstFrame extends JFrame {

	public FirstFrame() {
	
		setPreferredSize(new Dimension(400, 500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}
	
	private void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		mainPanel.add(new JLabel("To begin, choose input files :"));
		
		traceFileField = new JTextField("Enter trace file name");
		traceFileField.setPreferredSize(new Dimension(100, 25));
		traceButton = new JButton("Choose");
		traceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseTraceFile();
			}
		});
		mainPanel.add( makePanel(new JLabel("Trace file:"), traceFileField, traceButton));
		
		treesFileField = new JTextField("Enter tree file name");
		treesFileField.setPreferredSize(new Dimension(100, 25));
		treesButton = new JButton("Choose");
		treesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseTreeFile();
			}
		});
		mainPanel.add( makePanel(new JLabel("Trace file:"), traceFileField, traceButton));
		
		this.getContentPane().add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pack();
	}
	
	protected void chooseTreeFile() {
		// TODO Auto-generated method stub
		
	}

	protected void chooseTraceFile() {
		// TODO Auto-generated method stub
		
	}

	private JPanel makePanel(Component compOne, Component comp2) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panel.add(compOne);
		panel.add(comp2);
		return panel;
	}
	
	private JPanel makePanel(Component compOne, Component comp2, Component comp3) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panel.add(compOne);
		panel.add(comp2);
		panel.add(comp3);
		return panel;
	}
	
	private JTextField traceFileField;
	private JButton traceButton;
	private JTextField treesFileField;
	private JButton treesButton;
}

