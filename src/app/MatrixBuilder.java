package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main workhorse class, given a file containing pop 
 * @author brendan
 *
 */
public class MatrixBuilder {
	
	public static final String POPSIZE_KEY = "demographic.popSize";
	public static final String INDICATOR_KEY = "demographic.indicators";
	
	protected boolean useLogScale = true;
	
	protected double maxDepth = 25000; //Maximum depth of matrix
	protected double maxSize = 1e7; //Maximum population size that appears in matrix
	protected double minSize = 10000; //Minimum size that appears in matrix = must be > 0 for log scale
	
	protected int depthBins = 200;
	protected int sizeBins = 400;
	
	protected double[][] matrix; //Stores histograms of sizes at particular depths
	
	protected int burnin = 1000; //This is in LINES READ, not MCMC states
	
	protected int popSizeOffset; //First column at which popSize value appears
	protected int indicatorOffset; //First column at which indicator appears
	int indicatorCount; //Total number of indicators
	
	BufferedReader treeBuf;
	BufferedReader traceBuf;
	
	String currentTree;
	String currentState;
	
	public MatrixBuilder(File traceFile, File treesFile) {
		try {
			traceBuf = new BufferedReader(new FileReader(traceFile));
			treeBuf = new BufferedReader(new FileReader(treesFile));

			//Must do an initial pass so we can make an educated guess at the max tree height and max population size. 
			// ..or we could just demand the user supply some values....
			
			//This seems to work!
			identifyColumns(traceBuf); //Figure out which columns contain the skyline info
			advanceToTrees(treeBuf); //Advance the tree-reading buffer to the first tree
			DemoFunction dFunc = getFunctionForState();
			
//			createMatrix();
//			
//			int max = 10000;
//			int count = 0;
//			
//			while(dFunc != null && count<max) {
////				if (count>0 && count%300 == 0) {
////					dFunc.emit();
////					addFunctionToMatrix(dFunc);
////					emitMatrix();
////					System.exit(0);
////				}
//				advanceStep(traceBuf, treeBuf);
//				if (currentState == null || currentTree==null) {
//					System.out.println("Could not read current state or tree, aborting.");
//					break;
//				}
//				if (count > burnin) {
//					dFunc = getFunctionForState(); 
//					if (dFunc != null)
//						addFunctionToMatrix(dFunc); //Add this demographic function to the matrix
//					else {
//						System.out.println("Could not read demographic function... emitting matrix and aborting");
//						emitMatrix();
//						
//					}
//				}
//				count++;
//				if (count % 100 == 0)
//					System.out.println("Counting state " + count);
//			}
//			
//			emitMatrix();
			
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not open files : " + e);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("IOError reading files: " + e);
		}
		
	}
	
	
	/**
	 * Read through the tree and trace files, computing a demographicFunction for each line / tree, and 
	 * tally the result in the matrix. This takes a long time. 
	 * @throws IOException
	 */
	public void computeRateFunctions() throws IOException {
		int max = 10000;
		int count = 0;
		
		createMatrix();
		DemoFunction dFunc = getFunctionForState();
		
		while(dFunc != null && count<max) {
//			if (count>0 && count%300 == 0) {
//				dFunc.emit();
//				addFunctionToMatrix(dFunc);
//				emitMatrix();
//				System.exit(0);
//			}
			advanceStep(traceBuf, treeBuf);
			if (currentState == null || currentTree==null) {
				System.out.println("Could not read current state or tree, aborting.");
				break;
			}
			if (count > burnin) {
				dFunc = getFunctionForState(); 
				if (dFunc != null)
					addFunctionToMatrix(dFunc); //Add this demographic function to the matrix
				else {
					System.out.println("Could not read demographic function... emitting matrix and aborting");
					emitMatrix();
					
				}
			}
			count++;
			if (count % 100 == 0)
				System.out.println("Counting state " + count);
		}
		normalizeMatrix();
	}
	
	public double[][] getMatrix() {
		return matrix;
	}
	
	
	public double meanSizeAtDepth(double time) {
		int timeBin = (int)Math.floor(time / maxDepth * depthBins);
		double sum = 0;
		for(int i=0; i<sizeBins; i++) {
			sum += matrix[timeBin][i];
		}
		
		double mean = 0;
		for(int i=0; i<sizeBins; i++) {
			mean += matrix[timeBin][i]/sum*sizeForBin(i);
		}
		return mean;
	}
	
	public double stdSizeAtDepth(double time) {
		int timeBin = (int)Math.floor(time / maxDepth * depthBins);
		double mean = meanSizeAtDepth(time);
		double sum = 0;
		for(int i=0; i<sizeBins; i++) {
			sum += matrix[timeBin][i];
		}
		
		
		double sumSq = 0;
		for(int i=0; i<sizeBins; i++) {
			sumSq += matrix[timeBin][i]/sum*(mean-sizeForBin(i))*(mean-sizeForBin(i));
		}
		return Math.sqrt(sumSq);
	}
	
	public double approxLowerCPD(int timeBin, double percentage) {
		//int timeBin = (int)Math.floor(time / maxDepth * depthBins);
		double sum = 0;
				
		for(int i=0; i<sizeBins; i++) {
			sum += matrix[timeBin][i];
			if (sum > (1.0-percentage))
				return sizeForBin(i);
		}
		
		return -1; //Range end is out of bounds, we don't know where it is
	}

	
	public double approxUpperCPD(int timeBin, double percentage) {
		//int timeBin = (int)Math.floor(time / maxDepth * depthBins);
		double sum = 0;
				
		for(int i=sizeBins-1; i>=0; i--) {
			sum += matrix[timeBin][i];
			if (sum > (1.0-percentage))
				return sizeForBin(i);
		}
		
		return -1; //Range end is out of bounds, we don't know where it is
	}

	
	
	public double findMPESize(int timeBin) {
		//int timeBin = (int)Math.floor(time / maxDepth * depthBins);
		double maxDensity = 0;
		int maxIndex = -1;
				
		for(int i=0; i<sizeBins; i++) {
			if (matrix[timeBin][i]>maxDensity) {
				maxDensity = matrix[timeBin][i];
				maxIndex = i;
			}
		}
		
		return sizeForBin(maxIndex); 
	}
	
	public void writeMatrixToFile(File outputFile) throws IOException {
		BufferedWriter buf = new BufferedWriter(new FileWriter(outputFile));
		for(int i=0; i<depthBins; i++) {
			for(int j=0; j<sizeBins; j++) {
				buf.write(matrix[i][j] + "\t");
			}
			buf.write("\n");
		}
		buf.close();
	}
	
	/**
	 * Write some data about the matrix to standard out
	 */
	private void emitMatrix() {
//		System.out.print("\t");
//		for(int i=0; i<depthBins; i++) {
//			System.out.print( (double)i/(double)depthBins * maxDepth  + "\t");
//		}
//		System.out.println();
//		
//		for(int j=sizeBins-1; j>=0; j--) {
//			double size = sizeForBin(j);
//			System.out.print(size + "\t");
//			for(int i=0; i<depthBins; i++) {
//				System.out.print(matrix[i][j] + "\t");
//			}
//			System.out.println();
//		}
		
//		for(int i=0; i<depthBins; i++) {
//			for(int j=0; j<sizeBins; j++) {
//				System.out.print(matrix[i][j] + "\t");
//			}
//			System.out.println();
//		}
		
		System.out.println();
		for(int i=0; i<depthBins; i++) {
			//System.out.println((double)i/(double)depthBins*maxDepth+ "\t" + approxLowerCPD(i, 0.95) + "\t" + approxLowerCPD(i, 0.85) + "\t" + approxLowerCPD(i, 0.75) + "\t" + meanSizeAtDepth( (double)i/(double)depthBins*maxDepth) + "\t" + approxUpperCPD(i, 0.75) + "\t" + approxUpperCPD(i, 0.85) + "\t" + approxUpperCPD(i, 0.95));
			System.out.println((double)i/(double)depthBins*maxDepth+ "\t" + approxLowerCPD(i, 0.95) + "\t" + approxLowerCPD(i, 0.85) + "\t" + approxLowerCPD(i, 0.75) + "\t" + meanSizeAtDepth( (double)i/(double)depthBins*maxDepth) + "\t" + approxUpperCPD(i, 0.75) + "\t" + approxUpperCPD(i, 0.85) + "\t" + approxUpperCPD(i, 0.95));

		}
		System.out.println();
		
		
		for(int i=0; i<depthBins; i++) {
			System.out.println((double)i/(double)depthBins*maxDepth+ "\t" + meanSizeAtDepth( (double)i/(double)depthBins*maxDepth) + "\t" + stdSizeAtDepth((double)i/(double)depthBins*maxDepth) + "\t" + findMPESize(i));  
		}
	}

	private void addFunctionToMatrix(DemoFunction dFunc) {
		for(int i=0; i<depthBins; i++) {
			double t = (double)i/(double)depthBins * maxDepth;
			double size = dFunc.getSize(t);
			int sizeBin = binForSize(size);
			//System.out.println("Time: " + t + " size: " + size + " i: " + i + " j:" + sizeBin);
			if (sizeBin>=0 && sizeBin < sizeBins) {
				matrix[i][sizeBin]++;
			}
		}
	}
	
	/**
	 * Divide all cells in the matrix by the maximum column (over sizes at a single time) sum
	 */
	private void normalizeMatrix() {
		double max = 0;
		for(int i=0; i<depthBins; i++) {
			double sum = 0;
			for(int j=0; j<sizeBins; j++) {
				sum += matrix[i][j];
			}
			if (sum>max)
				max = sum;
		}
		
		for(int i=0; i<depthBins; i++) {
			double sum = 0;
			for(int j=0; j<sizeBins; j++) {
				matrix[i][j] /= max;
			}
		}		
		
	}

	/**
	 * A function that returns the bin number that the given size corresponds to. This may return a number greater than
	 *  sizeBins, so do bounds checking elsewhere. 
	 * @param size
	 * @return
	 */
	private int binForSize(double size) {
		if (useLogScale) {
			if (size<= minSize) {
				return -1;
			}
			return (int)Math.floor((Math.log(size) - Math.log(minSize))/(Math.log(maxSize)-Math.log(minSize))*sizeBins);
		}
		else
			return (int)Math.floor((size-minSize) /(maxSize-minSize)*sizeBins);
	}
	
	/**
	 * Returns the (minimum) size that a bin corresponds to.  
	 * @param bin
	 * @return
	 */
	private double sizeForBin(int bin) {	
		if (useLogScale) {
			return Math.exp( (double)bin/(double)sizeBins*(Math.log(maxSize)-Math.log(minSize)) + Math.log(minSize));
		}
		else
			return (double)bin / (double)sizeBins * (maxSize-minSize)+minSize;
	}
	
	
	
	private void createMatrix() {
		matrix = new double[depthBins][sizeBins];
	}

	/**
	 * Advance the reader to the first line containing the trees
	 * @throws IOException 
	 */
	private void advanceToTrees(BufferedReader treeBuf) throws IOException {
		String line = treeBuf.readLine();
		while(line != null && (! line.toUpperCase().contains("TREE STATE"))) {
			line = treeBuf.readLine();
		}

		currentTree = line;
	}

	/**
	 * Finds that columns that are the population sizes and indictors and sets the corresponding fields appropriately
	 * @param traceBuf
	 * @throws IOException 
	 */
	private void identifyColumns(BufferedReader traceBuf) throws IOException {
		String line = traceBuf.readLine();
		while(line != null && ((line.trim().length()==0) || (line.startsWith("#")))) {
			line = traceBuf.readLine();
		}
		
		String[] toks = line.split("\t");
		int firstPopSize = -1;
		int lastPopSize = -1;
		int firstIndicator = -1;
		int lastIndicator = -1;
		for(int i=0; i<toks.length; i++) {
			if (firstPopSize>=0 && lastPopSize<0 && toks[i-1].startsWith(POPSIZE_KEY) && (! toks[i].startsWith(POPSIZE_KEY))) 
				lastPopSize = i-1;
				
			if (firstPopSize<0 && toks[i].startsWith(POPSIZE_KEY))
				firstPopSize = i;
			
			if (firstIndicator>=0 && lastIndicator<0 && toks[i-1].startsWith(INDICATOR_KEY) && (! toks[i].startsWith(INDICATOR_KEY)) )
				lastIndicator = i-1;
			
			if (firstIndicator<0 && toks[i].startsWith(INDICATOR_KEY))
				firstIndicator = i;
			
		}
		
		int numPopSizes = lastPopSize - firstPopSize+1;
		int numIndicators = lastIndicator - firstIndicator+1;
		System.out.println("Found " + numPopSizes + " population size params, starting at column " + firstPopSize);
		System.out.println("Found " + numIndicators + " indicator params, starting at column " + firstIndicator);
		
		System.out.println("First popSize column: " + toks[firstPopSize]);
		System.out.println("Last popSize columns: " + toks[lastPopSize]);
		System.out.println("First indicator column: " + toks[firstIndicator]);
		System.out.println("Last indicator column: " + toks[lastIndicator]);
		
		this.popSizeOffset = firstPopSize;
		this.indicatorOffset = firstIndicator;
		this.indicatorCount = numIndicators;
		currentState = traceBuf.readLine();
	}

	private void advanceStep(BufferedReader traceBuf, BufferedReader treeBuf) throws IOException {
		currentState = traceBuf.readLine();
		currentTree = treeBuf.readLine();
	}
	
	/**
	 * Using the currentTree and currentState fields, construct and return a new DemoFunction representing the skyline
	 * @return a DemoFunction representing the skyline given by the currentState and currentTree fields, or null if currentState or currentTree not valid
	 * @throws IOException
	 */
	private DemoFunction getFunctionForState() throws IOException {	
	
		if (currentState == null || currentTree == null)
			return null;
		
		String[] traceTokens = currentState.split("\t");
		Tree tree = new Tree(currentTree);
		if (tree.getRoot()==null) {
			return null;
		}
		
		//TODO: verify that the tree and the trace file have the same state?
		
		List<Double> sizeList = new ArrayList<Double>();
		List<Double> timeList = new ArrayList<Double>();
		
		timeList.add(0.0);
		sizeList.add(Double.parseDouble( traceTokens[popSizeOffset]) );
		
		Double[] nodeHeights = getNodeTimes(tree);
		int countedIndicators = 0;
		for(int i=0; i<indicatorCount; i++) {

			boolean indicator = Double.parseDouble( traceTokens[indicatorOffset+i])>0.5;
			if (indicator) {
				sizeList.add( Double.parseDouble(traceTokens[popSizeOffset+i+1])); //Indicator from position 0 indexes population size 1, since we always take pop size 0 at time 0
				timeList.add( nodeHeights[i]);
				countedIndicators++;
			}
		}
		
//		System.out.println("Read following demographic points:");
//		for(int i=0; i<timeList.size(); i++) {
//			System.out.println(timeList.get(i) + "\t" + sizeList.get(i));
//		}
		
		return new DemoFunction(sizeList.toArray(new Double[1]), timeList.toArray(new Double[1]));
	}
	
	private static Double[] getNodeTimes(Tree tree) {
		List<Node> nodes = tree.getInternalNodes();
		if (nodes.size() >= tree.getNumLeaves()) {
			System.out.println("Uh, we somehow have more internal nodes than leaves ");
			System.out.println(" internal nodes : " + nodes.size());
			System.out.println(" leaves : " + tree.getNumLeaves());
		}
		Double[] times = new Double[nodes.size()];
		int next = 0;
		for(Node n : nodes) {
			for(int i=0; i<(n.numChildren()-1); i++) {   //Nodes with two offspring are added once, nodes with three offspring are added twice...
				times[next] = tree.getNodeHeight(n);  //..ensures we always get n-1 coalescent times if there are n tips
				next++;
			}
		}
		
		Arrays.sort(times);
		return times;
	}
	
	public static void main(String[] args) {
//		File traceFile = new File(args[0]);
//		File treesFile = new File(args[1]);
		
		File traceFile = new File("/Users/brendan/mito_analysis/everyone/newperu_all_priors_combo4.log");
		File treesFile = new File("/Users/brendan/mito_analysis/everyone/newperu_all_priors_combo4.trees");
	
		MatrixBuilder sv = new MatrixBuilder(traceFile, treesFile);
		try {
			sv.computeRateFunctions();
			sv.emitMatrix();
			sv.writeMatrixToFile(new File("/Users/brendan/mito_analysis/everyone/bspData.csv"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
