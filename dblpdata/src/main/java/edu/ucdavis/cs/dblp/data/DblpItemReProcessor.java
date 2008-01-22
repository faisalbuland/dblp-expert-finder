package edu.ucdavis.cs.dblp.data;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

import de.unitrier.dblp.Cite;


public class DblpItemReProcessor extends DblpItemProcessor {
	@Override
	protected void processItem(Object entry) {
		Publication pub = Publication.convert(entry);
			
		Publication persistedPub = dao.findById(pub.getKey());
		if (null == persistedPub) {
			// not found in the DB
			contentService.retrieveAll(pub);
			logger.info("not found in DB: "+pub.getKey() + ' ' +
					(pub.getContent() == null?"no content": "has content"));
			filterBadCites(pub);
			dao.update(pub);
		} else if (null == persistedPub.getContent()) {
			// found in the DB, but has no content
			contentService.retrieveAll(persistedPub);
			logger.info("was missing content: "+persistedPub.getKey() + ' ' +
					(persistedPub.getContent() == null?"no content": "has content"));
			filterBadCites(persistedPub);
			dao.update(persistedPub);
		} else { // found in the DB with content
			logger.info("skipping persisted pub with content "+pub.getKey());
		}
	}
	
	/**
	 * Filters out Cite entries that are empty (e.g. "..." as their value
	 * and have no label).
	 * 
	 * @param pub
	 */
	private void filterBadCites(Publication pub) {
		for (Cite cite : Sets.immutableSet(pub.getCite())) {
			if ("...".equals(cite.getContent().trim()) && 
					StringUtils.isBlank(cite.getLabel())) {
				pub.getCite().remove(cite);
				logger.info("removed empty ... cite from "+pub.getKey());
			}
		}
	}
}
