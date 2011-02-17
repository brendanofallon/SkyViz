package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import javax.swing.JPanel;

/**
 * The panel that actually draws the matrix, as well as the axes
 * @author brendan
 *
 */
public class MatrixPanel extends JPanel implements ComponentListener {

	private int numXTicks = 5;
	private int numYTicks = 5;
	private double xMin = 0;
	private double xMax = 25000;
	private double yMin = 0;
	private double yMax = 1e7;
	
	private int xPadding = 15; //Number of pixels between bottom of component and bottom of matrix
	private int yPadding = 15; //Number of pixels between left edge of component and left edge of matrix
	
	private double[][] matrix;
	private Color[] colors;
	
	private Color zeroColor = new Color(200, 200, 200);
	private Color oneColor = new Color(255, 0, 200);
	private int totalColors = 5;
	
//	public void setMatrix(double[][] mat) {
//		this.matrix = mat;
//	}
	
	Shape[] rawShapes;
	Shape[] transformedShapes;
	
	boolean shapesScaled = false;
	
	public MatrixPanel() {
		addComponentListener(this);
	}
	
	public void paintComponent(Graphics g) {
		if (colors == null) {
			createColors();
		}
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	
		drawMatrix(g2d);
		
		drawXAxis(g2d);
		drawYAxis(g2d);
	}
	
	/**
	 * A series of shapes, appropriately scaled to be in 0..1, which this panel will draw
	 * @param shapes
	 */
	public void setShapes(Shape[] shapes) {
		this.rawShapes = shapes;
		shapesScaled = false;
		repaint();
	}

	private void transformShapes() {
		AffineTransform transform = new AffineTransform();
		transformedShapes = new Shape[rawShapes.length];
		
		transform.setToScale( (double)this.getWidth()-xPadding, (double)this.getHeight()-yPadding);
		
		for(int i=0; i<rawShapes.length; i++) {
			transformedShapes[i] = transform.createTransformedShape(rawShapes[i]);
		}
		
		transform.setToTranslation(xPadding, 0);
		for(int i=0; i<rawShapes.length; i++) {
			transformedShapes[i] = transform.createTransformedShape(transformedShapes[i]);
		}
		
		shapesScaled =  true;
	}
	
	private void createColors() {
		colors = new Color[totalColors];
		double rStep = (oneColor.getRed()-zeroColor.getRed()) /(double)( totalColors-1.0);
		double gStep = (oneColor.getGreen()-zeroColor.getGreen()) /(double)( totalColors-1.0);
		double bStep = (oneColor.getBlue()-zeroColor.getBlue()) /(double)( totalColors-1.0);
		
		for(int i=0; i<colors.length; i++) {
			colors[i] = new Color((int)(rStep*i+zeroColor.getRed()), (int)(gStep*i+zeroColor.getGreen()), (int)(bStep*i+zeroColor.getBlue())); 
		}
		
	}

	
	private void drawMatrix(Graphics2D g2d) {
		if (rawShapes == null || transformedShapes == null || transformedShapes.length ==0)
			return;
			
		if (! shapesScaled)
			transformShapes();
		
		for(int i=0; i<transformedShapes.length; i++) {
			g2d.setColor(Color.red);
			g2d.fill( transformedShapes[i] );
			g2d.draw( transformedShapes[i] );
//			System.out.println("Shape bounds : " + transformedShapes[i].getBounds());
//			PathIterator pit = transformedShapes[i].getPathIterator(null);
//			for(int j=0; j<5; j++) {
//				double[] coords = new double[2];
//				pit.currentSegment(coords);
//				System.out.println(j + " x : " + coords[0] + ",  " + coords[1]);
//				pit.next();
//			}
		}
		
		
//		int matHeight = getHeight()-xPadding;
//		int matWidth = getWidth()-yPadding;
//		
//		int xCells = matrix.length;
//		int yCells = matrix[0].length;
//		int cellWidth = (int)Math.round((double)matWidth / (double)xCells)+1;
//		int cellHeight = (int)Math.round((double)matHeight / (double)yCells)+1;
		
//		for(int i=0; i<xCells; i++) {
//			for(int j=0; j<yCells; j++) {
//				int colIndex = (int)(colors.length*matrix[i][yCells-j-1]*50); 
//				if (colIndex >= colors.length)
//					colIndex = colors.length-1;
//				if (colIndex>=0 && colIndex < colors.length){ 
//					g2d.setColor(colors[colIndex] );
//					g2d.fillRect((int)(yPadding+(double)i/(double)xCells*matWidth), (int)((double)j/(double)yCells*matHeight), cellWidth, cellHeight);
//				}
//			}
//		}
		
		
	}

	private void drawYAxis(Graphics2D g2d) {
		g2d.setColor(Color.black);
		int matHeight = getHeight()-xPadding;
		g2d.drawLine(yPadding, 0, yPadding, matHeight);
		
		for(int i=0; i<numYTicks; i++) {
			int yPos = (int)Math.round( (double)i/(double)(numYTicks-1)*matHeight );
			g2d.drawLine(yPadding-5, yPos, yPadding, yPos);
		}
	}

	private void drawXAxis(Graphics2D g2d) {
		g2d.setColor(Color.black);
		int matHeight = getHeight()-xPadding;
		int matWidth = getWidth()-yPadding;
		
		g2d.drawLine(yPadding, matHeight, getWidth(), matHeight);
		
		for(int i=0; i<numXTicks; i++) {
			int xPos = (int)Math.round( (double)i/(double)(numXTicks-1)*matWidth+yPadding);
			g2d.drawLine(xPos, matHeight, xPos, matHeight+5);
		}
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		shapesScaled = false;
		
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		shapesScaled = false;
	}
	
	
	
	
}
