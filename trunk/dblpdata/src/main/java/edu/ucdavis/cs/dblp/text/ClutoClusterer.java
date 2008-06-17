package edu.ucdavis.cs.dblp.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import com.google.common.base.Join;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.data.mining.DocExporter;

/**
 * Interfaces with cluto to provide clustering of {@link Publication}s.
 *  
 * @author pfishero
 */
public class ClutoClusterer {
	private static final Logger logger = Logger.getLogger(ClutoClusterer.class);
	
	public static Multimap<Integer, Publication> loadSolution(
						Iterable<Publication> pubs, File matrixFile, int numClusters) {
		Multimap<Integer, Publication> solution = new HashMultimap<Integer, Publication>();
		Map<String, Publication> keysToPubs = Maps.newHashMap();
		for (Publication pub : pubs) {
			keysToPubs.put(convertPubKey(pub), pub);
		}
		
		File keyFile = new File(matrixFile.getParent(), matrixFile.getName()+".rlabel");
		File solutionFile = new File(matrixFile.getParent(), 
									matrixFile.getName()+".clustering."+numClusters);
		Preconditions.checkState(keyFile.canRead());
		Preconditions.checkState(solutionFile.canRead());
		
		LineIterator keyIter = null;
		LineIterator solutionIter = null;
		try {
			keyIter = FileUtils.lineIterator(keyFile, "UTF-8");
			solutionIter = FileUtils.lineIterator(solutionFile, "UTF-8");
			
			while (keyIter.hasNext() && solutionIter.hasNext()) {
				Integer clusterNum = Integer.parseInt(solutionIter.nextLine().trim());
				String key = keyIter.nextLine().trim();
				assert clusterNum != -1 : key+" was not put in a cluster"; // -1 means it wasn't put into a cluster
				assert keysToPubs.containsKey(key) : key+" not found in pubs"; // all keys should match one in pubs
				solution.put(clusterNum, keysToPubs.get(key));
			}
			assert !keyIter.hasNext() && !solutionIter.hasNext() : 
				"key/solution files had differing number of entries";
		} catch (IOException e) {
			logger .error("error while reading solution file. "+e);
		} finally {
			LineIterator.closeQuietly(keyIter);
			LineIterator.closeQuietly(solutionIter);
		}
		
		return solution;
	}
	
	public static Multimap<Integer, Publication> getClustersFor(Iterable<Publication> pubs, int numClusters) {		
		File exportFile = new File("/dev/cluto-2.1.1/doc2mat-1.0", "testPubs.txt");
		File matrixFile = new File("/dev/cluto-2.1.1/doc2mat-1.0", "testPubs.mat");
		
		try {
			DocExporter.exportData(pubs, exportFile);
			String cwd = "c:\\dev\\cluto-2.1.1\\doc2mat-1.0\\";
			String perlExe = "/Perl/bin/perl.exe";
			String doc2mat = cwd+"doc2mat.pl";
			String command1 = perlExe +' '+
					doc2mat+" -nlskip 1 " +
					"-tokfile "+exportFile+' '+matrixFile;
			logger.info("running cmd: "+command1);
			Process p1 = Runtime.getRuntime().exec(command1);

			MyReader out = new MyReader(p1.getInputStream());
	        MyReader err = new MyReader(p1.getErrorStream());
	        out.start(); err.start();
			int retcode1 = p1.waitFor();
			logger.info("p1 return code: "+retcode1);
			out.interrupt(); err.interrupt();
			
			String vcluster = "/dev/cluto-2.1.1/Win32/vcluster.exe ";
			String command2 = vcluster+" -clmethod=rbr -showfeatures -showsummaries=itemsets "+
								matrixFile+' '+numClusters;
			
			logger.info("running cmd: "+command2);
			Process p2 = Runtime.getRuntime().exec(command2);
			out = new MyReader(p2.getInputStream());
	        err = new MyReader(p2.getErrorStream());
	        out.start();
	        err.start();
			int retcode2 = p2.waitFor();
			logger.info("p2 return code: "+retcode2);
			out.interrupt(); err.interrupt();
		} catch (IOException e) {
			logger.error(e);
		} catch (InterruptedException e) {
			logger.error(e);
		}
		
		return loadSolution(pubs, matrixFile, numClusters);
	}
	
	public static class MyReader extends Thread {
		  BufferedReader  reader = null;
		  StringBuffer    buff = new StringBuffer();

		  public MyReader(InputStream ins) {
		    reader = new BufferedReader(new InputStreamReader(ins));
		  }

		  public void run() {
		    String str = null;

		    try {
		      while((str = reader.readLine()) != null) {
		        buff.append(str);
		        buff.append('\n');
		      }
		    }
		    catch(IOException x) {
		      logger.error(x);
		    }
		    finally {
		        logger.info(buff);
		      try {
		        reader.close();
		      } catch(IOException x) {
		      }
		    }
		  }
	};

		
	/**
	 * Converts the key from {@link Publication#getKey()} to the format that 
	 * the cluto clustered solution uses.
	 * 
	 * @param pub
	 * @return
	 */
	public static final String convertPubKey(Publication pub) {
		String key = Join.join("", pub.getKey().split("/")).toLowerCase().replaceAll("-", "");
		return key;
	}
}
