package app;

import gui.SkyVizFrame;

public class SkyVizApp {

	
	private static void launchApplication() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	SkyVizFrame skyViz = new SkyVizFrame();
            }
        });
	}
	
	public static void main(String[] args) {
		//Parse args...?
		launchApplication();
	}
}
