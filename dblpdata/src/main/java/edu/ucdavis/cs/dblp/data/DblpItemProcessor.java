/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.cs.dblp.service.ContentService;

/**
 * 
 * @author pfishero
 * @version $Id$
 */
public class DblpItemProcessor implements ItemProcessor<Object> {
	public static final Logger logger = Logger.getLogger(DblpItemProcessor.class);
	
	protected DblpPubDao dao;
	protected ContentService contentService;
	
	public DblpItemProcessor() { }
	
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
			for (Object entry : items) {
				processItem(entry);
			}
	}

	/**
	 * @param entry
	 */
	protected void processItem(Object entry) {
		Publication pub;
		if (entry instanceof Publication) {
			pub = (Publication)entry;
		} else {
			pub = Publication.convert(entry);
			contentService.retrieveAll(pub);
		}
		logger.debug(pub.getKey());
		dao.update(pub);
	}

}
