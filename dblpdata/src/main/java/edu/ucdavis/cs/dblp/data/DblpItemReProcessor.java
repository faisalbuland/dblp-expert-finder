package edu.ucdavis.cs.dblp.data;

import org.apache.commons.lang.StringUtils;

public class DblpItemReProcessor extends DblpItemProcessor {
	@Override
	protected void processItem(Object entry) {
		Publication pub = Publication.convert(entry);
		logger.debug("processing pub key="+pub.getKey()+
				" cites:"+StringUtils.join(pub.getCite(), ", "));
			
		Publication persistedPub = dao.findById(pub.getKey());
		if (null != persistedPub) {
			logger.debug("persistedpub key="+pub.getKey()+
				" cites:"+StringUtils.join(pub.getCite(), ", "));
		}
		if (null == persistedPub) {
			// not found in the DB
			contentService.retrieveAll(pub);
			logger.info("not found in DB: "+pub.getKey() + ' ' +
					(pub.getContent() == null?"no content": "has content"));
			dao.update(pub);
		} else if (null == persistedPub.getContent()) {
			contentService.retrieveAll(pub);
			logger.info("was missing content: "+pub.getKey() + ' ' +
					(pub.getContent() == null?"no content": "has content"));
			dao.update(pub);
		} else {
			logger.info("skipping persisted pub with content "+pub.getKey());
		}
	}
}
