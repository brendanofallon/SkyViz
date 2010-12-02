package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import app.SkyVizApp;

/**
 * A small frame that users can use to specify which trace and trees file we should read the data from. 
 * @author brendan
 *
 */
public class FirstFrame extends JFrame {
	
	public FirstFrame() {
		
        try {
        	String plaf = UIManager.getSystemLookAndFeelClassName();
        	String gtkLookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        	//Attempt to avoid metal look and feel
        	if (plaf.contains("metal")) {

        		UIManager.setLookAndFeel(gtkLookAndFeel);
        	}

        	UIManager.setLookAndFeel( plaf );
        }
        catch (Exception e) {
            System.err.println("Could not set look and feel, exception : " + e.toString());
        }
        
		initComponents();
		setPreferredSize(new Dimension(400, 400));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}
	
	private void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		mainPanel.add(new JLabel("<html><strong>To begin, choose input files :<strong></html>"));
		
		traceFileField = new JTextField("Enter trace file name");
		traceFileField.setPreferredSize(new Dimension(200, 25));
		traceFileField.setHorizontalAlignment(JTextField.RIGHT);
		traceButton = new JButton("Choose");
		traceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseTraceFile();
			}
		});
		mainPanel.add( makePanel(new JLabel("Trace file:"), traceFileField, traceButton));
		
		treesFileField = new JTextField("Enter tree file name");
		treesFileField.setPreferredSize(new Dimension(200, 25));
		treesFileField.setHorizontalAlignment(JTextField.RIGHT);
		treesButton = new JButton("Choose");
		treesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseTreeFile();
			}
		});
		mainPanel.add( makePanel(new JLabel("Trees file:"), treesFileField, treesButton));
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		
		
		JButton done = new JButton("Done");
		done.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				done();
			}
		});
		
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(cancel);
		bottom.add(Box.createGlue());
		bottom.add(done);
		
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(bottom);
		this.getContentPane().add(mainPanel);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pack();
	}
	
	/**
	 * Called when done button is pressed, we just read whatever's in the appropriate fields and have skyVizApp show the main frame
	 */
	protected void done() {
		File traceFile = new File(traceFileField.getText());
		File treesFile = new File(treesFileField.getText());
		SkyVizApp.showSkyVizFrame(traceFile, treesFile);
		this.dispose();
	}

	/**
	 * Cancel everything and dispose of this frame
	 */
	protected void cancel() {
		this.dispose();
	}

	protected void chooseTreeFile() {
		if (fileChooser==null) {
			fileChooser = new JFileChooser();
		}
		int n = fileChooser.showOpenDialog(this);
		if (n == fileChooser.APPROVE_OPTION) {
			File treeFile = fileChooser.getSelectedFile();
			treesFileField.setText(treeFile.getAbsolutePath());
		}
	}

	protected void chooseTraceFile() {
		if (fileChooser==null) {
			fileChooser = new JFileChooser();
		}
		int n = fileChooser.showOpenDialog(this);
		if (n == fileChooser.APPROVE_OPTION) {
			File traceFile = fileChooser.getSelectedFile();
			traceFileField.setText(traceFile.getAbsolutePath());
		}
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
	
	private JFileChooser fileChooser;
	private JTextField traceFileField;
	private JButton traceButton;
	private JTextField treesFileField;
	private JButton treesButton;
}

