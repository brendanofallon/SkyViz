package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import app.MatrixBuilder;


public class SkyVizFrame extends JFrame implements PropertyChangeListener {

	protected File traceFile;
	protected File treesFile;
	protected MatrixBuilder matBuilder;
	
	AffineTransform trans = new AffineTransform();
	
	public SkyVizFrame(File traceFile, File treesFile) {
		this.traceFile = traceFile;
		this.treesFile = treesFile;
		initComponents();
		MatrixBuilder builder = buildMatrix();
		
		Path2D conf95 = builder.generateScaledPolygon(0.95);
		Path2D conf90 = builder.generateScaledPolygon(0.9);
		Path2D conf85 = builder.generateScaledPolygon(0.85);
		Path2D conf80 = builder.generateScaledPolygon(0.8);
		Path2D conf75 = builder.generateScaledPolygon(0.75);
		Path2D conf70 = builder.generateScaledPolygon(0.7);
		Path2D conf65 = builder.generateScaledPolygon(0.65);
		Path2D conf60 = builder.generateScaledPolygon(0.60);

		flipVertically(conf95);
		flipVertically(conf90);
		flipVertically(conf85);
		flipVertically(conf80);
		flipVertically(conf75);
		flipVertically(conf70);
		flipVertically(conf65);
		flipVertically(conf60);
		
		
		matrixPanel.setShapes(new Shape[]{conf95, conf90, conf85, conf80, conf75, conf70, conf65, conf60});
		
		setPreferredSize(new Dimension(600, 500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}
	
	private void flipVertically(Path2D path) {

		trans.setToScale(1, -1);
		path.transform(trans);
		
		trans.setToTranslation(0, 1);
		path.transform(trans);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent propEvent) {
		if (propEvent.getPropertyName().equals("progress")) {
			
		}	
	}
	
	/**
	 * Construct a new MatrixBuilder and use it to compute the matrix in the background. 
	 */
	private MatrixBuilder buildMatrix() {
		matBuilder = new MatrixBuilder(traceFile, treesFile);
		matBuilder.addPropertyChangeListener(this);
		matrixPanel.setMatrixBuilder(matBuilder);
		
		String fileTitle = traceFile.getName();
		int rPos = fileTitle.lastIndexOf(".");
		if (rPos > 0) {
			fileTitle = fileTitle.substring(0, rPos);
		}
		matrixPanel.setMainTitle(fileTitle);
		
		//matBuilder.beginRateFunctionComputation(this);
		try {
			matBuilder.computeRateFunctions();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //For debugging, run not in background
		return matBuilder;
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
