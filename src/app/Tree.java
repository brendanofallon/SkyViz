package app;


import java.util.ArrayList;
import java.util.List;

public class Tree {

		private Node root = null;
		
		private boolean heightKnown = false;
		private double height = -1;
		
		public Tree() {
			root = new Node("root");
		}
		
		public Tree(Node rootNode) {
			root = rootNode;
		}
		
		public Tree(String treeStr) {
			root = buildTreeFromNewick(treeStr);
		}
		
		public int getTotalNodes() {
			if (root==null)
				return 0;
			else 
				return subTreeSize(root);
		}
		
		public int getNumLeaves() {
			if (root==null)
				return 0;
			else 
				return numDescendentLeaves(root);		
		}
		
		public String getNewick() {
			StringBuffer str = new StringBuffer("(");
			int i;
			for(i=0; i<(root.numChildren()-1); i++)
				str.append( getNewickSubtree(root.getChild(i)) + ", ");
			str.append( getNewickSubtree(root.getChild(i)) );
			
			str.append(");");
			return str.toString();
		}
		
		public void printTree() {
			if (root==null) {
				System.out.println("Tree has no root.");
			}
			else {
				printDescendents(root, 0);
			}
		}
		
		public Node getRoot() {
			return root;
		}
		
		public ArrayList getAllTips() {
			if (root==null)
				return new ArrayList<Node>();
			else {
				ArrayList<Node> tips = new ArrayList<Node>();
				int i;
				for(i=0; i<root.numChildren(); i++)
					tips.addAll( getTips(root.getChild(i)));
					
				return tips;
			}
				
		}
		
		public ArrayList<Node> getAllNodes() {
			ArrayList<Node> nodes = getNonTips(root);
			nodes.addAll( getTips(root) );
			return nodes;
		}
		
		public ArrayList<Node> getInternalNodes() {
			ArrayList<Node> iNodes = getNonTips(root);
			return iNodes;
		}
		
		public double getMaxHeight() {
			if (heightKnown) 
				return height;
			else {
				ArrayList<Node> tips = this.getAllTips();
				double maxHeight = 0;
				int i;
				for(i=0; i<tips.size(); i++)
					if ( getDistToRoot( tips.get(i) ) > maxHeight )
						maxHeight = getDistToRoot( tips.get(i) );

				height = maxHeight;
				heightKnown = true;
				return height;
			}
		}

		//Returns the maximum 'time back from present' (i.e. distance to current time, or tips)
		//amongst all the leaves descendent of this node
		public static double getMaxDescendentHeight(Node n, Tree tree) {
			double totalDepth = getMaxDescendentDepth( tree.getRoot() );
			double minDepth = getMinDescendentDepth(n);
			if (totalDepth-minDepth < 0) {
				System.err.println("Uh-oh, total tree depth is : " + totalDepth + " but min. depth from this node is : " + minDepth);
			}
			
			return totalDepth - minDepth;
		}
		
		//t is measured in units of height, that is, starting at tips and counting toward root
		public int lineageCountAtTime(double t) {
			double totalDepth = getMaxDescendentDepth( root );
			double tDepth = totalDepth - t;
			//time asked for is greater than TMRCA
			if (tDepth < 0) 
				return 1;
			else {
				return lineageCountSubtree(root, tDepth);
			}
		}
		
		
		//Returns the minimum distance to 'current' time amongst all the leaves
		//descendent of this node
		public static double getMinDescendentHeight(Node n, Tree tree) {
			double totalDepth = getMaxDescendentDepth( tree.getRoot() );
			double maxDepth = getMaxDescendentDepth(n);
			if (totalDepth-maxDepth < 0) {
				System.err.println("Uh-oh, total tree depth is : " + totalDepth + " but max. depth from this node is : " + maxDepth);
			}
			
			return totalDepth - maxDepth;
		}
		
		
		private static String stripSpaces(String str) {
			str = str.replaceAll(" ", "");
			str = str.replaceAll("\n", "");
			str = str.replaceAll("\t", "");
			return str;
		}
		
		//returns the root
		public static Node buildTreeFromNewick(String treeStr) {
			Node root = new Node("root");
			treeStr = treeStr.replaceAll("\\[&[^\\]]+\\]", ""); //Used to strip off beast-style annotations, 

			int first = treeStr.indexOf("(");
			int last = treeStr.lastIndexOf(");");
			
			treeStr = treeStr.substring(first, last+1);
			treeStr = stripSpaces(treeStr);
			
			buildSubtreeFromNewick(root, treeStr);
			
			//Sometimes we'll get an extra node at the root, so strip it off
			//Will we ever want to preserve these?
			while (root.numChildren()==1) {
				root = root.getChild(0);
				root.setDistToParent(0);
				root.setParent(null);
			}
			return root;
		}
		
		private static boolean hasKids(String subtree) {
			if (subtree.indexOf(",")<0)
				return false;
			else
				return true;
		}
		private static ArrayList<String> getChildStrings(int startIndex, String treeStr) {
			ArrayList<String> children = new ArrayList<String>();
			treeStr = treeStr.trim();
			int lastParen = matchingParenIndex(startIndex, treeStr);
			
			//Scan along subtree string, create new strings separated by commas *at 'highest level'*
			//not all commas, of course
			int i=startIndex+1;
			StringBuffer cstr = new StringBuffer();
			
			int count = 0;
			for(i=startIndex+1; i<lastParen; i++) {	
				if (treeStr.charAt(i)=='(')
					count++;
				if (treeStr.charAt(i)==')')
					count--;
				if (treeStr.charAt(i)==',' && count==0) {
					children.add(cstr.toString());
					cstr = new StringBuffer();
				}
				else	
					cstr.append( treeStr.charAt(i));
				
			}
			
			children.add(cstr.toString());
			
			return children;
		}
		
		//Returns the entire subtree string, don't try it on the whole tree string thoughh (index==0 will error) 
		private static String extractSubtreeString(int startIndex, String treeStr) {	
			int lastParen = matchingParenIndex(startIndex, treeStr);
			int lastPos = Math.min( treeStr.indexOf(",", lastParen),  treeStr.indexOf(")", lastParen+1) );
			System.out.println("Last paren index : " + lastParen);
			System.out.println("First , or ) after lastParen : " + lastPos);
			String subtree = treeStr.substring(startIndex, lastPos);
			return subtree;
		}
		
		//Returns the distance of the subtree from a parent (just the number following the final colon) 
		private static double subTreeDistFromParent(String subtree) {
			int lastColon = subtree.lastIndexOf(":");
			int lastParen = subtree.lastIndexOf(")");
			if (lastParen<lastColon)
				lastParen=subtree.length();
			if (lastColon > lastParen)
				System.err.println("Uh-oh, found bad values for branch length positions on this substring : " + subtree);
			String branchLengthStr = subtree.substring(lastColon+1, lastParen);
			double branchLength = Double.valueOf(branchLengthStr);
			return branchLength;
		}
		
		private static int matchingParenIndex(int firstPos, String str) {
			int i = firstPos;
			if (str.charAt(i) != '(') {
				System.err.println("Got a non-paren for first index in find matching paren");
				System.err.println(" Char is : |" + str.charAt(i) +"|");
				return -1;
			}
			int count = 0;
			for(i=firstPos; i<str.length(); i++) {
				if (str.charAt(i) == '(')
					count++;
				if (str.charAt(i) == ')')
					count--;
				if (count==0)
					return i;
			}
			
			System.err.println("Couldn't find matching paren for this string : |" + str + "|");
			System.err.println(" First paren index : " + firstPos);
			return -1;
		}
		
		private static void buildSubtreeFromNewick(Node parent, String subTreeStr) {
			ArrayList<String> kids = getChildStrings(0, subTreeStr);
			for (String kidStr : kids) {
				stripSpaces(kidStr);
				Node kid = new Node();
				if (hasKids(kidStr))
					buildSubtreeFromNewick(kid, kidStr);
				else {
					kid.setId(kidStr.substring(0, kidStr.indexOf(":")) );
				}
				parent.addChild(kid);
				kid.setDistToParent(subTreeDistFromParent(kidStr ));
				kid.setParent(parent);
//				System.out.println("dist from parent is : " + kid.getDistToParent());
//				System.out.println("dist to root is : " + getDistToRoot(kid));
			}
		}
		
		
		/**
		 * This should handle non-contemporaneous samples OK
		 * @param n
		 * @param tree
		 * @return
		 */
		public double getNodeHeight(Node n) {
			double totalDepth = getMaxHeight();
			double nodeDepth = getDistToRoot(n);
			return totalDepth - nodeDepth;
		}
		
		
		//Returns depth of leaf that is 'farthest' (toward tips) fom n
		public static double getMaxDescendentDepth(Node n) {
			ArrayList<Double> depths = getDescendentDepthList(n);
			int i;
			double maxDepth = 0;
			//System.out.println("Found depths : ");
			for(i=0; i<depths.size(); i++) {
				//System.out.println( depths.get(i));
				if (depths.get(i) > maxDepth)
					maxDepth = depths.get(i);
			}
			//System.out.println(" Returning max dist from root : " + maxDepth);
			return maxDepth;
		}
		
		
		//Returns the depth of the leaf node that is 'closest' (toward root) to n	
		public static double getMinDescendentDepth(Node n) {
			ArrayList<Double> depths = getDescendentDepthList(n);
			int i;
			double minDepth = Double.MAX_VALUE;
			//System.out.println("Found depths : ");
			for(i=0; i<depths.size(); i++) {
				//System.out.println( depths.get(i));
				if (depths.get(i) < minDepth)
					minDepth = depths.get(i);
			}
			//System.out.println(" Returning min dist from root : " + minDepth);
			return minDepth;
		}
		
		public static double getDistToRoot(Node n) {
			double dist = 0;
			Node p = n;
			int steps = 0;
			//System.out.println("Computing dist to root :");
			while( p != null) {
				dist += p.getDistToParent();
				steps++;
				//System.out.println("Step " + steps + " dist to parent : " + p.getDistToParent() + " total : " + dist);
				p = p.getParent();
			}
			
			return dist;
		}

		private static ArrayList getNonTips(Node n) {
			ArrayList<Node> nonTips = new ArrayList<Node>();
			int i;
			if (n.numChildren()>0) {
				nonTips.add(n);
				if (n.numChildren()==1) {
					System.out.println("Huh, this guy has exactly one offspring. Weird. \n");
				}
				for(i=0; i<n.numChildren(); i++) 
					nonTips.addAll( getNonTips(n.getChild(i)));
			}
			
			return nonTips;
		}
		
		
		private static int lineageCountSubtree(Node n, double tDepth) {
			double thisDepth = getDistToRoot(n);
			double parentDepth = getDistToRoot( n.getParent() );
			if (thisDepth >= tDepth && parentDepth <= tDepth)
				return 1;
			
			if (thisDepth < tDepth) {
				int count = 0;
				int i;
				for(i=0; i<n.numChildren(); i++)
					count += lineageCountSubtree(n.getChild(i), tDepth);
				return count;
			}
			else {
				return 0;
			}
		}
		
		private static List getTips(Node n) {
			ArrayList<Node> tips = new ArrayList<Node>();
			int i;
			if (n.numChildren()==0) {
				tips.add(n);
			}
			else {
				for(i=0; i<n.numChildren(); i++) {
					tips.addAll( getTips(n.getChild(i)));
				}
			}
			return tips;
		}
		
		private static String getNewickSubtree(Node n) {
			if (n.numChildren()==0) {
				return new String(n.getId() + ":" + n.getDistToParent() );
			}
			else {
				StringBuffer str = new StringBuffer("(");
				int i;
				for(i=0; i<(n.numChildren()-1); i++)
					str.append( getNewickSubtree(n.getChild(i)) + ", ");
				str.append( getNewickSubtree(n.getChild(i)) );
				str.append("):" + n.getDistToParent());
				return str.toString();
			}
			
		}	
		
		//Returns a list of the distances to th root of all the tips that descend 
		//from this node
		public static ArrayList<Double> getDescendentDepthList(Node n) {
			ArrayList<Double> depths = new ArrayList<Double>();
			if (n.numChildren()==0) {
				depths.add( getDistToRoot(n) );
			}
			else {
				int i;
				for(i=0; i<n.numChildren(); i++) 
					depths.addAll( getDescendentDepthList(n.getChild(i)) );
			}
			return depths;
		}
		
		
		private static void printDescendents(Node n, int padding) {
			int i;
			for(i=0; i<padding; i++)
				System.out.print("  ");
			System.out.println("Node id : " + n.getId() + " children : " + n.numChildren() );
			for(i=0; i<n.numChildren(); i++ ) {
				printDescendents(n.getChild(i), padding+2);
			}
			
		}
		
		public static int getNumTips(Node n) {
			return numDescendentLeaves(n);
		}

		private static int numDescendentLeaves(Node n) {
			int count = 0;
			int i;
			int k = n.numChildren();
			if (k==0)
				return 1;
			else {
				for(i=0; i<k; i++)
					count += numDescendentLeaves(n.getChild(i));
				return count;
			}
			
		}
		
		private static int subTreeSize(Node n) {
			int count = 1;
			int i;
			for(i=0; i<n.numChildren(); i++)
				count += subTreeSize(n.getChild(i));
			
			return count;
		}
		
		
	}
