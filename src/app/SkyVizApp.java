package app;

import java.io.File;
import java.io.IOException;

import gui.SkyVizFrame;

public class SkyVizApp {

	private static SkyVizFrame skyViz = null;
	
	private static void launchApplication() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	skyViz = new SkyVizFrame();
            }
        });
	}
	
	public static SkyVizFrame getFrame() {
		return skyViz;
	}
	
	
	
	public static void main(String[] args) {
		//Parse args...?
		launchApplication();
		
		File traceFile = new File("/Users/brendan/mito_analysis/everyone/newperu_all_priors_combo4.log");
		File treesFile = new File("/Users/brendan/mito_analysis/everyone/newperu_all_priors_combo4.trees");
	
		MatrixBuilder builder = new MatrixBuilder(traceFile, treesFile);
		try {
			builder.computeRateFunctions();
			skyViz.setMatrix(builder.getMatrix());
			//builder.emitMatrix();
			//builder.writeMatrixToFile(new File("/Users/brendan/mito_analysis/everyone/bspData.csv"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
