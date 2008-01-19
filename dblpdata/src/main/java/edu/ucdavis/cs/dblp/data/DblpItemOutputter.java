/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.cs.dblp.service.ContentService;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
public class DblpItemOutputter implements ItemProcessor<Object> {
	public static final Logger logger = Logger.getLogger(DblpItemOutputter.class);
	
	private DblpPubDao dao;
	private ContentService contentService;
	private int count = 0;
	private final File outputDir;
	
	public DblpItemOutputter() {
		outputDir = new File("output");
		if (!outputDir.exists()) {
			outputDir.mkdir();
			logger.info("created serialized object output dir: "+outputDir);
		}
	}
	
	public void setDao(DblpPubDao dao) {
		this.dao = dao;
	}
	
	public DblpPubDao getDao() {
		return dao;
	}

	/**
	 * @return the contentService
	 */
	public ContentService getContentService() {
		return contentService;
	}

	/**
	 * @param contentService the contentService to set
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	@Transactional
	public void process(Object item) {
		processItem(item);
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.ItemProcessor#process(java.util.Collection)
	 */
	@Override
	@Transactional
	public void process(Collection<Object> items) {
		List<Publication> pubs = new LinkedList<Publication>();
		for (Object entry : items) {
			pubs.add(processItem(entry));
			count++;
		}
		output(pubs);
	}
	
	private final void output(List<Publication> pubs) {
		File outputFile = new File(outputDir,count+".ser.gz");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
			logger.info("writing out object set with count="+count);
			oos.writeObject(pubs);
			oos.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param entry
	 */
	private Publication processItem(Object entry) {
		Publication pub = Publication.convert(entry);
		logger.debug("skipping pub:"+pub.getKey()+
				" - this class is for output only, not processing");
		return pub;
	}

}
