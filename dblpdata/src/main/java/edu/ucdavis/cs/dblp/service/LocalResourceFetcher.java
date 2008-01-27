/**
 * 
 */
package edu.ucdavis.cs.dblp.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

import edu.ucdavis.cs.dblp.data.Publication;

/**
 * Fetches resources from a local store.  Assumes that the resources
 * will be under a common root directory and will be named 
 * {@link Publication#getKey()}+".html".
 * 
 * @author pfishero
 * @version $Id$
 */
public class LocalResourceFetcher implements ResourceFetcher {
	public static final Logger logger = Logger.getLogger(LocalResourceFetcher.class);
	private final File rootDir;
	
	public LocalResourceFetcher(File rootDir) {
		Preconditions.checkNotNull(rootDir);
		Preconditions.checkArgument(rootDir.isDirectory() && rootDir.canRead(), 
						"%s must be an accessible directory", rootDir);
		this.rootDir = rootDir;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ResourceFetcher#fetchElectronicEdition(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public String fetchElectronicEdition(Publication pub) {
		String contents = "";
		
		File htmlFile = new File(rootDir, pub.getKey()+".html");
		if (htmlFile.exists() && htmlFile.isFile()) {
			if (htmlFile.canRead()) {
				try {
					contents = FileUtils.readFileToString(htmlFile);
				} catch (IOException e) {
					logger.error("error while reading file "+htmlFile+": "+e);
				}
			} else {
				logger.error("unable to read file: "+htmlFile);
			}
		} else {
			logger.debug("did not find file: "+htmlFile);
		}
		
		return contents;
	}

}
