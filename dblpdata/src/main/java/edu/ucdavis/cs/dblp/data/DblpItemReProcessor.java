package edu.ucdavis.cs.dblp.data;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

import de.unitrier.dblp.Cite;


public class DblpItemReProcessor extends DblpItemProcessor {
	public static final Logger logger = Logger.getLogger(DblpItemReProcessor.class);
	
	/**
	 * PROCESS==true means re-process the data - download if needed and then save via the dao
	 * PROCESS==false means just output the state of the item (in the db or not, has content or not)
	 */
	private static final boolean PROCESS=true;
	
	@Override
	protected void processItem(Object entry) {
		Publication pub = Publication.convert(entry);
		
		try {
			Publication persistedPub = dao.findById(pub.getKey());
			
			if (null == persistedPub) {
				// not found in the DB
				if (PROCESS) {
					contentService.retrieveAll(pub);
					logger.info("not found in DB: "+pub.getKey() + ' ' +
							(pub.getContent() == null?"no content": "has content"));
					dao.update(pub);
				} else {
					logger.info("not found in DB: "+pub.getKey() + 
							" [ee]:"+pub.getEe());
					// insert without content, just to have in the DB
//					dao.update(pub);
				}
			} else if (null == persistedPub.getContent()) {
				if (PROCESS) {
					// found in the DB, but has no content
					contentService.retrieveAll(persistedPub);
					logger.info("was missing content: "+persistedPub.getKey() + ' ' +
							(persistedPub.getContent() == null?"no content": "has content"));
					dao.update(persistedPub);
				} else {
					logger.info("was missing content: " + 
							persistedPub.getKey() +" [ee]:"+persistedPub.getEe());
				}
			} else { // found in the DB with content
				logger.info("skipping persisted pub with content "+pub.getKey());
			}
		} catch (RuntimeException rex) {
			// explicitly catching runtime exception until a resolution 
			// can be found to the constraint violation problem
			logger.error("error:"+rex);
			logger.error("error pub:"+pub.getKey()+" ee:"+pub.getEe());
			logger.error("error cites:"+StringUtils.join(pub.getCite(), ", "));
		}
	}
}
