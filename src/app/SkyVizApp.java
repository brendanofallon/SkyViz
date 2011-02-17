package app;

import java.io.File;
import java.io.IOException;

import gui.FirstFrame;
import gui.SkyVizFrame;

/**
 * A mostly-static class containing a few functions for top level application startup and control. 
 * @author brendan
 *
 */
public class SkyVizApp {

	private static SkyVizFrame skyViz = null;
	
	private static void launchApplication() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	FirstFrame firstFrame = new FirstFrame();
            }
        });
	}
	
	public static void showSkyVizFrame(File traceFile, File treesFile) {
		skyViz = new SkyVizFrame(traceFile, treesFile);
	}
	
	public static SkyVizFrame getFrame() {
		return skyViz;
	}
	
	
	
	public static void main(String[] args) {
		//Parse args...?
		
		launchApplication();
		
	}
}
