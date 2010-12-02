package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import app.MatrixBuilder;


public class SkyVizFrame extends JFrame {

	protected File traceFile;
	protected File treesFile;
	protected MatrixBuilder matBuilder;
	
	public SkyVizFrame(File traceFile, File treesFile) {
		this.traceFile = traceFile;
		this.treesFile = treesFile;
		initComponents();
		buildMatrix();
		matrixPanel.setMatrix(matBuilder.getMatrix());
		setPreferredSize(new Dimension(400, 500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}
	
	private void buildMatrix() {
		matBuilder = new MatrixBuilder(traceFile, treesFile);
		try {
			matBuilder.computeRateFunctions();
		} catch (IOException e) {
			System.err.println("There was an error constructing the rate functions : " + e);
		}
	}
	
	public void setMatrix(double[][] mat) {
		matrixPanel.setMatrix(mat);
		matrixPanel.repaint();
	}
	
	private void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		matrixPanel = new MatrixPanel();
		mainPanel.add(matrixPanel);
		this.getContentPane().add(mainPanel);
		pack();
	}

	
	private MatrixPanel matrixPanel;
	
}
