package edu.ucdavis.cs.dblp.text;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.common.base.Join;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import edu.ucdavis.cs.dblp.ServiceLocator;

/**
 * Loads a cluto/gcluto cluster solution from the file system and provides
 * clients with access to the assigned clusters from that solution.
 *  
 * @author pfishero
 */
@Component
public class ClutoClusterSolution {
	private static final Logger logger = Logger.getLogger(ClutoClusterSolution.class);
	
	private File solutionDir;
	private Map<String, Integer> clusters = Maps.newHashMap();
	
	@Autowired(required=false)
	public void setSolutionDir(@Qualifier("solutionDir")Resource solutionDirRes) {
		try {
			this.solutionDir = solutionDirRes.getFile();
		} catch (IOException e) {
			logger.error("unable to set solutionDir from resource:"+solutionDirRes);
		}
	}
	
	@PostConstruct
	public final void loadSolution() {
		if (solutionDir != null) {
			File keyFile = new File(solutionDir, "int_data_matrix/rowlabels.rlabel");
			File solutionFile = new File(solutionDir, "solution.sol");
			
			Preconditions.checkState(keyFile.canRead());
			Preconditions.checkState(solutionFile.canRead());
			
			LineIterator keyIter = null;
			LineIterator solutionIter = null;
			try {
				keyIter = FileUtils.lineIterator(keyFile, "UTF-8");
				solutionIter = FileUtils.lineIterator(solutionFile, "UTF-8");
				
				// first two lines in the solutionFile are not important
				solutionIter.next(); solutionIter.next();
				while (keyIter.hasNext() && solutionIter.hasNext()) {
					Integer clusterNum = Integer.parseInt(solutionIter.nextLine().trim());
					clusters.put(keyIter.nextLine().trim(), clusterNum);
				}
				assert !keyIter.hasNext() && !solutionIter.hasNext() : 
					"key/solution files had differing number of entries";
			} catch (IOException e) {
				logger .error("error while reading solution file. "+e);
			} finally {
				LineIterator.closeQuietly(keyIter);
				LineIterator.closeQuietly(solutionIter);
			}
		} else {
			logger.info("not loading solution - no solutionDir set");
		}
	}
	
	public Multimap<Integer, SimplePub> getClustersFor(Iterable<SimplePub> pubs) {
		// verify that a non-empty solution was loaded
		Preconditions.checkState(this.clusters.size() > 0);
		
		Multimap<Integer, SimplePub> solution = new HashMultimap<Integer, SimplePub>();
		
		for(SimplePub pub : pubs) {
			if (clusters.containsKey(convertPubKey(pub))) {
				solution.put(clusters.get(convertPubKey(pub)), pub);
			} else {
				logger.error("solution cluster not found for pub: "+pub);
			}
		}
		
		return solution;
	}
	
	public Map<SimplePub, Integer> getClusterNumbersFor(Iterable<SimplePub> pubs) {
		Multimap<Integer, SimplePub> clusters = getClustersFor(pubs);
		Map<SimplePub, Integer> pubClusterNums = Maps.newHashMap();
		
		for(Integer clusterNum : clusters.keySet()) {
			for(SimplePub pub : clusters.get(clusterNum)) {
				pubClusterNums.put(pub, clusterNum);
			}
		}
		
		return pubClusterNums ;
	}
	
	/**
	 * Converts the key from {@link SimplePub#getKey()} to the format that 
	 * the cluto clustered solution uses.
	 * 
	 * @param pub
	 * @return
	 */
	public static final String convertPubKey(SimplePub pub) {
		String key = Join.join("", pub.getKey().split("/")).toLowerCase().replaceAll("-", "");
		return key;
	}
	
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = ServiceLocator.getInstance().getAppContext();
		final ClutoClusterSolution solution = (ClutoClusterSolution) ctx.getBean("clutoClusterSolution");
		final SpatialTopics st = (SpatialTopics) ctx.getBean("spatialTopics");
		Multimap<Integer, SimplePub> clusters = solution.getClustersFor(st.getSimplePubs());
		for(Integer clusterNum : clusters.keySet()) {
			logger.info("pubs for cluster "+clusterNum+'\n'+clusters.get(clusterNum));
		}
	}
}
