package app;

/**
 * A piecewise linear demographic function specified by a number of x-y pairs, where x is time and y is pop size. 
 * @author brendan
 *
 */
public class DemoFunction {

	Double[] times;
	Double[] sizes;
	
	public DemoFunction(Double[] sizes, Double[] times) {
		this.sizes = sizes;
		this.times = times;
	}
	
	public double getSize(double time) {
		//According to the paper, population size is constant after the last pop size measurement
		if (time >= times[times.length-1]) 
			return sizes[times.length-1];
		
		
		int i=0;
		for(i=0; i<times.length-1; i++) {
			if (times[i]<=time && times[i+1]>time) {
				break;
			}
		}
		
		return sizes[i]+ (sizes[i+1]-sizes[i])*(time-times[i])/(times[i+1]-times[i]); //Straight from the paper; s/i/j/g 
	}
	
	
	public void emit() {
		double maxTime = times[times.length-1];
		for(int i=0; i<10; i++) {
			double t = (double)i * 0.1 * maxTime;
			System.out.println(t + "\t" + getSize(t));
		}
	}
}
