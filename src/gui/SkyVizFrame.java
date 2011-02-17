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
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import app.MatrixBuilder;


public class SkyVizFrame extends JFrame implements PropertyChangeListener {

	protected File traceFile;
	protected File treesFile;
	protected MatrixBuilder matBuilder;
	
	public SkyVizFrame(File traceFile, File treesFile) {
		this.traceFile = traceFile;
		this.treesFile = treesFile;
		initComponents();
		MatrixBuilder builder = buildMatrix();
		
		
		//matrixPanel.setMatrix(matBuilder.getMatrix());
		Path2D conf95 = builder.generateConfPolygon(0.95);
				
		AffineTransform trans = new AffineTransform();
		
		trans.setToTranslation(0, -builder.getMinY());
		conf95 = (Path2D) trans.createTransformedShape(conf95);
		
		trans.setToScale(1.0/builder.getMaxX(), 1.0/(builder.getMaxY()));
		conf95.transform(trans);
		
		//Now all coordinates are scaled to be in (0, 1)
		
		trans.setToScale(1, -1);
		conf95.transform(trans);
		
		//Shape is now upside down, so that lower values appear toward the bottom of the figure
		
		trans.setToTranslation(0, 1);
		conf95.transform(trans);
		
		matrixPanel.setShapes(new Shape[]{conf95});
		
		setPreferredSize(new Dimension(400, 500));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
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
