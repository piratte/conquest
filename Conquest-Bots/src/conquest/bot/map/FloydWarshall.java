package conquest.bot.map;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import conquest.game.world.Region;

/**
 * Floyd-Warshall algorithm for precomputing all-possible paths within the map using {@link Region#getNeighbours()}.
 * <p><p>
 * It precomputes all the paths inside the environment using Floyd-Warshall
 * algorithm (time: O(n^3)) in the form of matrix.
 * <p><p> 
 * Use {@link FloydWarshall#isReachable(Object, Object)}, {@link FloydWarshall#getPathCost(Object, Object)}
 * and {@link FloydWarshall#getPath(Object, Object)} to obtain information about computed paths.
 * the info about the path.
 * <p><p>
 * {@link FloydWarshall#getPath(Object, Object)} is caching retrieved paths using {@link SoftReference}s.
 * <p><p>
 * If needed you may call {@link FloydWarshall#compute()} to recompute paths, this call is needed if you set new map / agent view
 * using {@link FloydWarshall#setMap(IPFKnownMap)} or {@link FloydWarshall#setMapView(IPFMapView)}.
 * 
 * @author Jimmy
 */
public class FloydWarshall  {
	
	/**
	 * Class describing cell inside the FloydWarshall matrix holding additional informations relating to the path between two
	 * Regions.
	 * <p><p>
	 * These Regions are stored under "indices" inside {@link FloydWarshall#pathMatrix}.
	 * 
	 * @author Jimmy
	 *
	 * @param <N>
	 */
	public static class PathMatrixRegion<N> {

		private int cost = Integer.MAX_VALUE;
		private Integer viaRegion = null;
		private SoftReference<List<N>> path = null;

		/**
		 * Doesn't leading anywhere
		 */
		public PathMatrixRegion() {
		}

		public PathMatrixRegion(int cost) {
			this.cost = cost;
		}

		/**
		 * Returns the cost of the path between Regions, if the path does not exist, returns {@link Integer#MAX_VALUE}.
		 * @return
		 */
		public int getPathCost() {
			return cost;
		}

		/**
		 * Sets the cost of the path between Regions.
		 * @param cost
		 */
		public void setPathCost(int cost) {
			this.cost = cost;
		}

		/**
		 * Returns the Region you have to travel through.
		 * @return
		 */
		public Integer getViaRegion() {
			return viaRegion;
		}

		/**
		 * Sets the Region you have to travel through.
		 * @param indice
		 */
		public void setViaRegion(Integer indice) {
			this.viaRegion = indice;
		}

		/**
		 * Returns the full path between Regions.
		 * <p><p>
		 * WARNING: this is cached path! Might return null even though such path exists! Use {@link FloydWarshall#getPath(Object, Object)}
		 * to obtain the result in every case.
		 * @return
		 */
		public List<N> getPath() {
			return path == null ? null : path.get();
		}

		/**
		 * Sets the full path between Regions, computed as the last step of {@link FloydWarshall#performFloydWarshall(List)}. Such path
		 * is going to be stored using {@link SoftReference} (cached) and might be freed by GC if heap runs dry.
		 * @param path
		 */
		public void setPath(List<N> path) {
			this.path = new SoftReference<List<N>>(path);
		}

	}
		
	/**
	 * Map converting Regions to their corresponding indices inside {@link FloydWarshall#pathMatrix}.
	 */
	private Map<Region, Integer> nodeIndices;

	/**
	 * Mapping indices (inside {@link FloydWarshall#pathMatrix}) to Regions.
	 */
	private Map<Integer, Region> indicesNodes;

	/**
	 * FloydWarshall's matrix of distances & paths.
	 */
	private PathMatrixRegion<Region>[][] pathMatrix;
	
	// ===========
	// CONSTRUCTOR
	// ===========

	public FloydWarshall() {
		long start = System.currentTimeMillis();				
		System.err.println("FloydWarshall initializing...");
		compute();
		cacheAllPaths();
		System.err.println("FINISHED: " + (System.currentTimeMillis() - start) + "ms");
	}
	
	/**
	 * Force FloydWarshall to refresh its path matrix, useful if you modify the map or view using {@link FloydWarshall#setMap(IPFKnownMap)}
	 * or {@link FloydWarshall#setMapView(IPFKnownMapView)}.
	 * <p><p>
	 * Called automatically from constructors!
	 */
	public synchronized void compute() {
		
		List<Region> nodes = new ArrayList<Region>();
		
		for (Region region : Region.values()) {
			nodes.add(region);
		}
		
		performFloydWarshall(nodes);		
	}
	
	protected synchronized void cacheAllPaths() {
		int size = pathMatrix.length;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (pathMatrix[i][j].getPathCost() == Integer.MAX_VALUE) {
					// NON-REACHABLE
				} else {
					// path exists ... retrieve it
					pathMatrix[i][j].setPath(retrievePath(i, j));
				}
			}
		}
	}
	
	//////////////////////////////////////
	// FloydWarshall algorithms & variable 
	//////////////////////////////////////

	
	/**
	 * Perform FloydWarshall over the list of Regions initializing {@link FloydWarshall#nodeIndices}, {@link FloydWarshall#pathMatrix}.
	 * @param nodes
	 */
	@SuppressWarnings("unchecked")
	private void performFloydWarshall(List<Region> nodes) {
		long start = System.currentTimeMillis();

		// prepares data structures
		int size = nodes.size();
		nodeIndices = new HashMap<Region, Integer>(size);
		indicesNodes = new HashMap<Integer, Region>(size);
		pathMatrix = new PathMatrixRegion[size][size];

		// Initialize navPoint indices mapping.
		for (int i = 0; i < nodes.size(); ++i) {
			nodeIndices.put(nodes.get(i), i);
			indicesNodes.put(i, nodes.get(i));
		}

		// Initialize distance matrix.
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				pathMatrix[i][j] = new PathMatrixRegion<Region>((i == j) ? 0 : Integer.MAX_VALUE);
			}
		}

		// Set initial arc costs into distance matrix.
		for (int i = 0; i < size; i++) {
			Region node1 = nodes.get(i);
			
			Collection<Region> neighbors = node1.getNeighbours();
			
			Iterator<Region> iterator = neighbors.iterator();
			
			while(iterator.hasNext()) {
				Region node2 = iterator.next();
				int j = nodeIndices.get(node2);
				
				int arcCost = 1;
				int nodeCost = 0;
				
				pathMatrix[i][j].setPathCost(arcCost+nodeCost);
			}
		}

		// Perform Floyd-Warshall.
		for (int k = 0; k < size; k++) {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					int newLen =
						pathMatrix[i][k].getPathCost() == Integer.MAX_VALUE ?
								Integer.MAX_VALUE
							:	(pathMatrix[k][j].getPathCost() == Integer.MAX_VALUE) ? 
									Integer.MAX_VALUE
								:	pathMatrix[i][k].getPathCost() + pathMatrix[k][j].getPathCost();
					if (pathMatrix[i][j].getPathCost() > newLen) {
						pathMatrix[i][j].setPathCost(newLen);
						pathMatrix[i][j].setViaRegion(k);
					}
				}
			}
		}

		// Check reachability...
		int count = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (pathMatrix[i][j].getPathCost() == Integer.MAX_VALUE) {
					// WE'RE PURPOSEFULLY TESTING "FINER" LEVEL HERE!
					System.out.println("[!!!] Unreachable path from " + nodes.get(i) + " -> " + nodes.get(j));
					count++;
				}
			}
		}
	}

	/**
	 * Sub-routine of {@link FloydWarshall#retrievePath(Integer, Integer)} - do not use! ... Well you may, it returns
	 * path without 'from', 'to' or null if such path dosn't exist.
	 * <p><p>
	 * DO NOT USE OUTSIDE {@link FloydWarshall#performFloydWarshall(List)} (relies on indicesNavPoints).
	 * <p><p>
	 * Uses recursion.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	private List<Region> retrievePathInner(Integer from, Integer to) {
		PathMatrixRegion Region = pathMatrix[from][to];
		if (Region.getPathCost() == Integer.MAX_VALUE)
			return null;
		if (Region.getViaRegion() == null) {
			return new ArrayList<Region>(0);
		}
		if (Region.getViaRegion() == null)
			return new ArrayList<Region>(0);

		List<Region> path = new ArrayList<Region>();
		path.addAll(retrievePathInner(from, Region.getViaRegion()));
		path.add(indicesNodes.get(Region.getViaRegion()));
		path.addAll(retrievePathInner(Region.getViaRegion(), to));

		return path;
	}

	/**
	 * Returns path between from-to or null if path doesn't exist. Path begins
	 * with 'from' and ends with 'to'.
	 * <p><p>
	 * DO NOT USE OUTSIDE CONSTRUCTOR (relies on indicesNavPoints).
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	private List<Region> retrievePath(Integer from, Integer to) {
		List<Region> path = new ArrayList<Region>();
		path.add(indicesNodes.get(from));
		path.addAll(retrievePathInner(from, to));
		path.add(indicesNodes.get(to));
		return path;
	}

	/**
	 * Returns {@link PathMatrixRegion<Region>} describing path from "RegionFrom" to "RegionTo". 
	 * @param RegionFrom
	 * @param RegionTo
	 * @return
	 */
	protected PathMatrixRegion<Region> getPathMatrixRegion(Region RegionFrom, Region RegionTo) {
		Integer from = nodeIndices.get(RegionFrom);
		Integer to = nodeIndices.get(RegionTo);
		if (from == null || to == null) return null;
		return pathMatrix[from][to];
	}

	/**
	 * Whether Region 'to' is reachable (path exists) from the Region 'from'.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean isReachable(Region from, Region to) {
		if ((from == null) || (to == null)) return false;
		PathMatrixRegion matrixRegion = getPathMatrixRegion(from, to);
		if (matrixRegion == null) return false;
		return matrixRegion.getPathCost() != Integer.MAX_VALUE;
	}

	/**
	 * Calculate's distance between two nav points (using pathfinding).
	 * <p><p>
	 * Throws exception if object is disabled, see {@link FloydWarshallMap#setEnabled(boolean)}. Note that the object
	 * is enabled by default.
	 * 
	 * @return Distance or {@link Integer#MAX_VALUE} if there's no path.
	 */
	public int getPathCost(Region from, Region to) {
		if ((from == null) || (to == null))
			return Integer.MAX_VALUE;
		PathMatrixRegion matrixRegion = getPathMatrixRegion(from, to);
		if (matrixRegion == null) return Integer.MAX_VALUE;
		return matrixRegion.getPathCost();
	}

	/**
	 * Returns path between navpoints 'from' -> 'to'. The path begins with
	 * 'from' and ends with 'to'. If such path doesn't exist, returns null.
	 * <p><p>
	 * Path is automatically cached using {@link SoftReference}.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public List<Region> getPath(Region from, Region to) {
		if ((from == null) || (to == null))
			return null;
		PathMatrixRegion matrixRegion = getPathMatrixRegion(from, to);
		if (matrixRegion == null) return null;
		if (matrixRegion.getPathCost() == Integer.MAX_VALUE) return null;
		List<Region> path = matrixRegion.getPath();
		if (path == null) {
			// was not cached or JVM has GC()ed it
			path = retrievePathInner(nodeIndices.get(from), nodeIndices.get(to));
			// cache the path
			matrixRegion.setPath(path);
		}
		return path;
	}
	
	/**
	 * Returns matrix of Regions as computed by FloydWarshall algorithm. You should not alter it by hand!
	 * @return
	 */
	protected PathMatrixRegion<Region>[][] getMatrix() {
		return pathMatrix;
	}
	
	/**
	 * Returns index of the Region inside {@link FloydWarshall#getMatrix()}. Note that if Region that is not inside the matrix is passed,
	 * this will return null! 
	 * @param Region
	 * @return
	 */
	protected Integer getRegionIndex(Region Region) {
		return nodeIndices.get(Region);
	}

	@Override
	public String toString() {
		return "FloydWarshall";
	}
	
	public static void main(String[] args) {
		// SIMPLE TEST...
		FloydWarshall fw = new FloydWarshall();
		
		for (Region from : Region.values()) {
			for (Region to : Region.values()) {
				
				if (from == to) continue;
				
				List<Region> path = fw.getPath(from, to);

				System.out.print(from + " --> " + to + " <><><> ");
				
				for (int i = 0; i < path.size(); ++i) {
					if (i > 0) System.out.print(" -> ");
					System.out.print(path.get(i));
				}

				System.out.println();
			}
		}
		
	}

}
