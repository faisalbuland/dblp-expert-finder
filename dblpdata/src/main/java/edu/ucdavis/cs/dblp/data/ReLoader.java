package edu.ucdavis.cs.dblp.data;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import edu.ucdavis.cs.dblp.ServiceLocator;


/**
 * ReLoader loads serialized object outputs and persists them by using the DAO.
 * 
 * @author pfishero
 * @version $Id$
 */
public class ReLoader {
	private static final Logger logger = Logger.getLogger(ReLoader.class);
	
	/**
	 * @param args
	 */
	public static void main(String ... args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			File inputDir = new File(args[i]);
			int count = 0;

			if (!inputDir.exists() || !inputDir.canRead() || 
					!inputDir.isDirectory()) {
				throw new RuntimeException("error with input file: " + inputDir);
			}

			for (File file : inputDir.listFiles()) {
				logger.info("reading " + file.getName());
				List<Publication> pubs;
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new GZIPInputStream(new FileInputStream(file)));
					pubs = (List<Publication>) ois.readObject();
					ois.close();
				} catch (EOFException eofex) {
					logger.error("error file reading "+file+'-'+eofex);
					continue;
				}

				DblpPubDao dao = ServiceLocator.getInstance().getDblpPubDao();
				ItemProcessor itemProc = (ItemProcessor) ServiceLocator
						.getInstance().getAppContext().getBean(
								"itemProcessorBean");

				for (Publication pub : pubs) {
					if (null != pub.getEe()) {
						// logger.info("found ee: "+pub.getEe());
						if (null != pub.getContent()) {
							logger.info("found content for: " + pub.getEe());
							count++;
						}
					}
				}

				itemProc.process(pubs);
				logger.info("published " + pubs.size() + " entries. " +
						"total="+count);
			}

			logger.info("found " + count + " entries with content");
		}
	}

}
