package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * The panel that actually draws the matrix, as well as the axes
 * @author brendan
 *
 */
public class MatrixPanel extends JPanel {

	private int numXTicks = 5;
	private int numYTicks = 5;
	private double xMin = 0;
	private double xMax = 1;
	private double yMin = 0;
	private double yMax = 1;
	
	private int xPadding = 15; //Number of pixels between bottom of component and bottom of matrix
	private int yPadding = 15; //Number of pixels between left edge of component and left edge of matrix
	
	private double[][] matrix;
	private Color[] colors;
	
	public void setMatrix(double[][] mat) {
		this.matrix = mat;
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

	private void createColors() {
		colors = new Color[5];
		colors[0] = Color.black;
		colors[1] = Color.blue;
		colors[2] = Color.green;
		colors[3] = Color.orange;
		colors[4] = Color.red;
		
	}

	private void drawMatrix(Graphics2D g2d) {
		if (matrix == null)
			return;
		int matHeight = getHeight()-xPadding;
		int matWidth = getWidth()-yPadding;
		
		int xCells = matrix.length;
		int yCells = matrix[0].length;
		int cellWidth = (int)Math.round((double)matWidth / (double)xCells);
		int cellHeight = (int)Math.round((double)matHeight / (double)yCells);
		
		for(int i=0; i<xCells; i++) {
			for(int j=0; j<yCells; j++) {
				int colIndex = (int)(colors.length*matrix[i][j]); 
				g2d.setColor(colors[colIndex] );
				g2d.fillRect((int)(yPadding+(double)i/(double)xCells*matWidth), (int)((double)j/(double)yCells*matHeight), cellWidth, cellHeight);
			}
		}
		
		
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
}
