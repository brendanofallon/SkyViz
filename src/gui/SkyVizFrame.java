package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


public class SkyVizFrame extends JFrame {

	
	public SkyVizFrame() {
		
		initComponents();
		double[][] mat = new double[5][5];
		for(int i=0; i<mat.length; i++) {
			for(int j=0; j<mat[0].length; j++) {
				mat[i][j] = i*j/(25.0);
			}
		}
		matrixPanel.setMatrix(mat);
		matrixPanel.repaint();
		
		setPreferredSize(new Dimension(400, 500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
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
