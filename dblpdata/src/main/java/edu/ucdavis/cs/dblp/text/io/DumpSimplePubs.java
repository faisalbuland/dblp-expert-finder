/**
 * 
 */
package edu.ucdavis.cs.dblp.text.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.base.Join;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.text.ClutoClusterSolution;
import edu.ucdavis.cs.dblp.text.SimplePub;

/**
 * Dumps all publications for all categories into directories that will work with
 * RapidMiner's Text plugin.
 * 
 * @author pfishero
 */
@Component
public class DumpSimplePubs {
	private static final String DEFAULT_FILE_ENCODING = "UTF-8";

	public static final Logger logger = Logger.getLogger(DumpSimplePubs.class);
	
	private Set<SimplePub> simplePubs;
	
	@Autowired(required=false)
	public void setSerializedSimplePubs(@Qualifier("serSimplePubs") org.springframework.core.io.Resource serSimplePubs) {
		try {
			logger.info("loading serialized version of simplePubs");
			ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(serSimplePubs.getInputStream()));
			this.simplePubs = (Set<SimplePub>)ois.readObject();
			ois.close();
			// filter out pubs with no authors -> i.e., main conference entries
			for (Iterator<SimplePub> iter = this.simplePubs.iterator(); iter.hasNext(); ) {
				SimplePub pub = iter.next();
				if (pub.getAuthorNames().size() == 0) {
					iter.remove();
					logger.info("removed pub with no authors - key:"+pub.getKey());
				}
			}			
		} catch (IOException e) {
			logger.error("error while loading simplePubs: "+e);
		} catch (ClassNotFoundException cnfe) {
			logger.error("error while loading simplePubs: "+cnfe);
		}
	}

	public void dumpData(File baseDir) {
		logger.info("dumping data to "+baseDir.getAbsolutePath());
		StringBuilder builder = new StringBuilder();
		
		for(SimplePub pub : simplePubs) {
			builder.append(ClutoClusterSolution.convertPubKey(pub))
					.append(" ").append(pub.getTitle())
					.append(" ")
					.append(pub.getAbstractText()!= null ? 
							pub.getAbstractText().replaceAll("\n","").replaceAll("[\\s]+", " ") : 
							"")
					.append('\n');
		}
		
		File outputFile = new File(baseDir, "simpleSpatDbs.txt");
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(outputFile), DEFAULT_FILE_ENCODING);
			writer.write(builder.toString());
		} catch (IOException e) {
			logger.error("problem while writing "+outputFile, e);
			throw new RuntimeException(e);
		} finally {
			if (writer != null) { 
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("problem while closing writer for "+outputFile, e);
					throw new RuntimeException(e);
				} 
			}
		}		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File baseDir = new File("C:\\dev\\cluto-2.1.1\\doc2mat-1.0");
		baseDir.mkdirs();
		
		ApplicationContext ctx = ServiceLocator.getInstance().getAppContext();
		DumpSimplePubs dumper = (DumpSimplePubs) ctx.getBean("dumpSimplePubs");
		dumper.dumpData(baseDir);
	}

}
