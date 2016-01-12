package conquest.bot.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import conquest.bot.map.RegionBFS.BFSNode;
import conquest.game.GameMap;
import conquest.game.world.Region;

/**
 * BFS over {@link Region}s using visitor pattern via {@link BFSVisitor}.
 * 
 * Your visitor needs to have access to {@link GameMap} in order to be useful.
 * 
 * @author Jimmy
 *
 * @param <NODE>
 */
public class RegionBFS<NODE extends BFSNode> {
	
	public static class BFSNode {
		
		/**
		 * Will be auto-filled...
		 */
		public Region region;
		
		/**
		 * Will be auto-filled...
		 */
		public int level;
		
		/**
		 * Will be auto-filled...
		 */
		public List<Region> parents = new ArrayList<Region>();
		
		public void addParent(Region region) {
			this.parents.add(region);
		}
		
	}
	
	public static enum BFSVisitResultType {
		
		ADD,
		IGNORE,
		TERMINATE
		
	}
	
	public static class BFSVisitResult<NODE extends BFSNode> {
		
		/**
		 * Return if you want this region to be ignored (not added into map of nodes + not added to BFS queue for further expansion). 
		 * BFS will continue with a next node from the queue.
		 */
		public static final BFSVisitResult IGNORE = new BFSVisitResult(BFSVisitResultType.IGNORE, null);
		
		/**
		 * Return if you want to terminate BFS right away.
		 */
		public static final BFSVisitResult TERMINATE = new BFSVisitResult(BFSVisitResultType.TERMINATE, null);
		
		public BFSVisitResultType type;
		
		public NODE node;
		
		public BFSVisitResult(NODE node) {
			this.type = BFSVisitResultType.ADD;
			this.node = node;
		}
		
		public BFSVisitResult(BFSVisitResultType type, NODE node) {
			this.type = type;
			this.node = node;
		}
		
	}
	
	public static interface BFSVisitor<NODE extends BFSNode> {
		
		/**
		 * 
		 * @param region region we have touched
		 * @param level how deep (far) this region is from the start
		 * @param parent from where we have reached the region (guaranteed to be "level-1" parent)
		 * @param thisNode this is non-null if we already visited the region in the past
		 * @return
		 */
		public BFSVisitResult<NODE> visit(Region region, int level, NODE parent, NODE thisNode);
		
	}
	
	public RegionBFS() {		
	}
	
	private Map<Region, NODE> nodes = new HashMap<Region, NODE>();
	
	private Region start;
	
	public void run(Region start, BFSVisitor visitor) {
		
		reset();
		
		this.start = start; 
		
		BFSVisitResult<NODE> firstVisit = visitor.visit(start, 0, null, null);
		
		if (firstVisit.type == BFSVisitResultType.IGNORE || firstVisit.type == BFSVisitResultType.TERMINATE) return;
		
		firstVisit.node.region = start;
		firstVisit.node.level = 0;
		
		nodes.put(start, firstVisit.node);
		
		LinkedList<NODE> queue = new LinkedList<NODE>();
		
		queue.add(firstVisit.node);
		
		while (queue.size() > 0) {
			NODE parent = queue.removeFirst();
			
			for (Region region : parent.region.getNeighbours()) {
				
				NODE node = nodes.get(region);
				
				boolean newNode = node == null;
				
				if (!newNode && node.level <= parent.level) continue;
				
				BFSVisitResult<NODE> result = visitor.visit(region, parent.level+1, parent, node);
				
				if (result.type == BFSVisitResultType.IGNORE) {
					continue;						
				}
				
				if (node == null) {
					node = result.node;
					if (node != null) {
						node.region = region;
						node.level = parent.level+1;							
						nodes.put(region, result.node);
					}
				}
				if (node != null) {
					node.addParent(parent.region);
				}
				
				if (newNode && node != null) queue.addLast(node);
				
				if (result.type == BFSVisitResultType.TERMINATE) {
					return;
				}
			}
		}		
	}
	
	public List<List<Region>> getAllPaths(Region to) {
		List<List<Region>> result = new ArrayList<List<Region>>();
		
		NODE node = getNode(to);
		
		if (node == null) return result;
		
		List<Region> firstPath = new ArrayList<Region>();
		firstPath.add(to);
		result.add(firstPath);
		
		generatePath(result, firstPath, to);
		
		for (List<Region> path : result) {
			Collections.reverse(path);
		}
		
		return result;
	}
	
	private void generatePath(List<List<Region>> result, List<Region> currentPath, Region nodeRegion) {
		NODE node = getNode(nodeRegion);
		
		if (node.parents.size() == 0) {
			return;
		}
		
		boolean first = true;
		
		int pathLen = currentPath.size();
		
		for (Region parent : node.parents) {
			List<Region> myPath = null;
			if (first) {
				myPath = currentPath;
			} else {
				myPath = new ArrayList<Region>(currentPath.subList(0, pathLen));
				result.add(myPath);
			}
			myPath.add(parent);
			generatePath(result, myPath, parent);			
			first = false;
		}		
	}

	public Map<Region, NODE> getNodes() {
		return nodes;
	}
	
	public NODE getNode(Region region) {
		return nodes.get(region);
	}

	private void reset() {
		nodes.clear();
	}
	
	public static void main(String[] args) {
		// SIMPLE TEST FOR FINDING "all shortest path" BETWEEN SOME REGIONS
		
		BFSVisitor<BFSNode> visitor = new BFSVisitor<BFSNode>() {

			@Override
			public BFSVisitResult<BFSNode> visit(Region region, int level, BFSNode parent, BFSNode thisNode) {
				System.out.println((parent == null ? "START" : parent.level + ":" + parent.region) + " --> " + level + ":" + region);
				// WE CAN IGNORE THE REGION
				//return BFSVisitResult.IGNORE;
				
				// WE CAN TERMINATE PREMATURALY
				//return BFSVisitResult.TERMINATE;
				
				return new BFSVisitResult<BFSNode>(thisNode == null ? new BFSNode() : thisNode);
			}

		};
		
		RegionBFS<BFSNode> bfs = new RegionBFS<BFSNode>();
		
		Region from = Region.Eastern_Australia;
		
		System.out.println("BFS from " + from);
		
		bfs.run(from, visitor);
		
		System.out.println();
		
		// GET ALL SHORTEST PATH FROM REGION 'from' TO A FEW RANDOM OTHER REGIONS
		for (int i = 0; i < 5; ++i) {
			Region to = Region.values()[new Random().nextInt(Region.values().length)];
			while (from == to) {
				to = Region.values()[new Random().nextInt(Region.values().length)];
			}
			
			List<List<Region>> allPaths = bfs.getAllPaths(to);
			
			System.out.println("PATHS " + from + " --> " + to + ":");
			
			int j = 0;
			for (List<Region> path : allPaths) {
				++j;
				System.out.print("  [" + path.size() + "] " + j + ". ");
				boolean first = true;
				for (Region region : path) {
					if (first) first = false;
					else System.out.print(" --> ");
					System.out.print(region);
				}
				System.out.println();
			}
			
		}
		
	}

}
