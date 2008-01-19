package edu.ucdavis.cs.dblp.data;


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
			dao.update(pub);
		} else if (null == persistedPub.getContent()) {
			// found in the DB, but has no content
			contentService.retrieveAll(persistedPub);
			logger.info("was missing content: "+persistedPub.getKey() + ' ' +
					(persistedPub.getContent() == null?"no content": "has content"));
			dao.update(persistedPub);
		} else { // found in the DB with content
			logger.info("skipping persisted pub with content "+pub.getKey());
		}
	}
}
