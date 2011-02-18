package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;

import app.MatrixBuilder;

/**
 * The panel that actually draws the matrix, as well as the axes
 * @author brendan
 *
 */
public class MatrixPanel extends JPanel implements ComponentListener {

	private int numXTicks = 9;
	private int numYTicks = 5;
	
	private int topPadding = 50;
	private int rightPadding = 30;
	private int bottomPadding = 50; //Number of pixels between bottom of component and bottom of matrix
	private int leftPadding = 100; //Number of pixels between left edge of component and left edge of matrix
	
	private Color[] colors;
	
	private Color zeroColor = new Color(220, 230, 250);
	private Color oneColor = new Color(0, 100, 255);
	private int totalColors;
	
//	public void setMatrix(double[][] mat) {
//		this.matrix = mat;
//	}
	
	Shape[] rawShapes;
	Shape[] transformedShapes;
	
	boolean shapesScaled = false;
	
	String mainTitle = null;
	
	MatrixBuilder builder; //Provides values for min and max, which we use to calculate the scale..
	private DecimalFormat mantissaFormatter;
	private NumberFormat plainFormatter = new DecimalFormat("0.0###");
	private NumberFormat xFormatter = new DecimalFormat("0,000");
	private Font xLabelFont;
	private Font exponentFont;
	private int fontSize = 13;
	private Font mainTitleFont = new Font("Sans", Font.PLAIN, 16);
	
	public MatrixPanel() {
		addComponentListener(this);	
		mantissaFormatter  = new DecimalFormat("#.#");
		xLabelFont = new Font("Sans", Font.PLAIN, fontSize);
		exponentFont = new Font("Sans", Font.PLAIN, (int)Math.round((double)fontSize/1.2));
	}
	
	public void setMainTitle(String title) {
		this.mainTitle = title;
	}
	
	public void setMatrixBuilder(MatrixBuilder builder) {
		this.builder = builder;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	
		drawGridLines(g2d);
		
		drawShapes(g2d);
		
		drawXAxis(g2d);
		drawYAxis(g2d);
		
		//Draw main title
		if (mainTitle != null) {
			g2d.setFont(mainTitleFont);
			int titleWidth = g2d.getFontMetrics().stringWidth(mainTitle);
			g2d.drawString(mainTitle, getWidth()/2 - titleWidth/2, topPadding-20);
		}
	}
	
	private void drawGridLines(Graphics2D g2d) {
		g2d.setColor(Color.LIGHT_GRAY);
		
		//Vertical gridlines
		int matHeight = drawAreaHeight() + topPadding; //Y-value at which x-axis exists
		for(int i=0; i<numXTicks; i++) {
			int xPos = (int)Math.round( (double)i/(double)(numXTicks-1)*drawAreaWidth()+leftPadding);
			g2d.drawLine(xPos, topPadding, xPos, matHeight-1);
		}
		
		//Horizontal gridlines
		for(int i=0; i<numYTicks; i++) {
			int yPos = (int)Math.round( (double)i/(double)(numYTicks-1)*drawAreaHeight() +topPadding);
			g2d.drawLine(leftPadding+1, yPos, leftPadding+drawAreaWidth(), yPos);
		}
	}

	/**
	 * A series of shapes, appropriately scaled to be in 0..1, which this panel will draw
	 * @param shapes
	 */
	public void setShapes(Shape[] shapes) {
		this.rawShapes = shapes;
		shapesScaled = false;
		totalColors = shapes.length;
		createColors();
		repaint();
	}

	/**
	 * The number of pixels that the figure drawing area is wide
	 * @return
	 */
	private int drawAreaWidth() {
		return getWidth()-leftPadding-rightPadding;
	}
	
	/**
	 * Height of figure drawing area in pixels
	 * @return
	 */
	private int drawAreaHeight() {
		return getHeight()-topPadding-bottomPadding;
	}
	
	/**
	 * Transform the various shapes so they fit into the correct window size
	 */
	private void transformShapes() {
		AffineTransform transform = new AffineTransform();
		transformedShapes = new Shape[rawShapes.length];
		
		transform.setToScale( drawAreaWidth(), drawAreaHeight());
		
		for(int i=0; i<rawShapes.length; i++) {
			transformedShapes[i] = transform.createTransformedShape(rawShapes[i]);
		}
		
		transform.setToTranslation(leftPadding, topPadding);
		for(int i=0; i<rawShapes.length; i++) {
			transformedShapes[i] = transform.createTransformedShape(transformedShapes[i]);
		}
		
		shapesScaled =  true;
	}
	
	/**
	 * Construct the colors array the defines the polygons
	 */
	private void createColors() {
		colors = new Color[totalColors];
		double rStep = (oneColor.getRed()-zeroColor.getRed()) /(double)( totalColors-1.0);
		double gStep = (oneColor.getGreen()-zeroColor.getGreen()) /(double)( totalColors-1.0);
		double bStep = (oneColor.getBlue()-zeroColor.getBlue()) /(double)( totalColors-1.0);
		
		for(int i=0; i<colors.length; i++) {
			colors[i] = new Color((int)(rStep*i+zeroColor.getRed()), (int)(gStep*i+zeroColor.getGreen()), (int)(bStep*i+zeroColor.getBlue())); 
		}
		
	}

	
	private void drawShapes(Graphics2D g2d) {
		if (! shapesScaled)
			transformShapes();
		
		if (builder == null || rawShapes == null || transformedShapes == null || transformedShapes.length ==0) {
			System.out.println("Returning without drawing, things aren't initialized");
			return;
		}
				
		for(int i=0; i<transformedShapes.length; i++) {
			g2d.setColor(colors[i]);
			g2d.fill( transformedShapes[i] );
			g2d.draw( transformedShapes[i] );
		}
		
	}

	private void drawYAxis(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.drawLine(leftPadding, topPadding, leftPadding, topPadding+drawAreaHeight());
		
		for(int i=0; i<numYTicks; i++) {
			int yPos = (int)Math.round( (double)i/(double)(numYTicks-1)*drawAreaHeight() +topPadding);
			g2d.drawLine(leftPadding-5, yPos, leftPadding, yPos);
			double yVal = builder.scaleY( 1.0-(double)i/(double)(numYTicks-1) );
			paintYLabel(g2d, leftPadding-7, yPos, yVal);
		}
	}
	
	private void paintYLabel(Graphics2D g, double xPos, double yPos, double val) {
		
		if (val != 0 &&  (Math.abs(val) > 10000 || Math.abs(val)<0.001) ) {

			String[] labels = toScientificNotation(val);
			String mantissaLabel = labels[0];
			String expLabel = labels[1];
			g.setFont(xLabelFont);
			FontMetrics fm = g.getFontMetrics();
			mantissaLabel = mantissaLabel + "x10";
			Rectangle2D mantissaRect = fm.getStringBounds(mantissaLabel, 0, mantissaLabel.length(), g);
			
			g.setFont(exponentFont);
			fm = g.getFontMetrics();
			Rectangle2D expRect = fm.getStringBounds(expLabel, 0, expLabel.length(), g);
			
			g.setFont(xLabelFont);
			g.drawString(mantissaLabel, Math.round(xPos-mantissaRect.getWidth()-expRect.getWidth()), Math.round(yPos+mantissaRect.getHeight()/2.0));
			
			g.setFont(exponentFont);
			g.drawString(expLabel, Math.round(xPos-expRect.getWidth()), Math.round(yPos-expRect.getHeight()/10.0));
			return;
		}
		else {
			g.setFont(xLabelFont);
			FontMetrics fm = g.getFontMetrics();
			String label = format(val); 
			Rectangle2D rect = fm.getStringBounds(label, 0, label.length(), g);
			g.drawString(label, Math.round(xPos-rect.getWidth()), Math.round(yPos+rect.getHeight()/3.0));
		} //number didn't need to be converted to scientific notation

	} //paintYLabel
	

	/**
	 * Convert the value to a string format with a bit of rounding
	 * @param val
	 * @return
	 */
	private String format(double val) {
		double absVal = Math.abs(val);
		
		if (absVal>1)
			plainFormatter.setMaximumFractionDigits(1);
		else {
			double log = Math.log10(absVal);
			int dig = -1*(int)Math.round(log)+1; 
			plainFormatter.setMaximumFractionDigits(dig);
		}
		
		
		return plainFormatter.format(val);
	}
	
	private void drawXAxis(Graphics2D g2d) {
		g2d.setColor(Color.black);
		int matHeight = drawAreaHeight() + topPadding; //Y-value at which x-axis exists
			
		g2d.drawLine(leftPadding, matHeight, drawAreaWidth()+leftPadding, matHeight);
		int tickDistance = (int)Math.round(1.0/(numXTicks-1)*drawAreaWidth());
		
		for(int i=0; i<numXTicks; i++) {
			int xPos = (int)Math.round( (double)i/(double)(numXTicks-1)*drawAreaWidth()+leftPadding);
			g2d.drawLine(xPos, matHeight, xPos, matHeight+5);
			
			g2d.setFont(xLabelFont);
			int xVal = (int)Math.round((double)i/(double)(numXTicks-1) * (double)builder.getMaxX());
			String xStr;
			if (xVal>9999)
				xStr = xFormatter.format(xVal);
			else
				xStr = String.valueOf(xVal);
			int width = g2d.getFontMetrics().stringWidth(xStr);
			
			g2d.drawString(xStr, xPos-width/2, matHeight+20);
			
			//Minor tick mark
			if (i>0) {
				g2d.drawLine(xPos-tickDistance/2, matHeight, xPos-tickDistance/2, matHeight+3);
			}
		}
	}
	
	
	private String[] toScientificNotation(double val) {
		int exp = 1;
		//Are we sure this couldn't be accomplish more quickly using logs?
		if ( Math.abs(val) > 10000 && val != 0) {
			while (Math.abs(val)>=10) {
				val = val/10.0;
				exp++;
			}
			exp--;
		}
		else {
			if ( Math.abs(val) < 0.001 && val != 0) {
				while (Math.abs(val)<1) {
					val *= 10.0;
					exp++;
				}
				exp--;
				exp *= -1;			
			}
			
		}
		
		String mantissaLabel = mantissaFormatter.format(val);
		String expLabel = mantissaFormatter.format(exp);
		String[] arr = {mantissaLabel, expLabel}; 
		return arr;
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
