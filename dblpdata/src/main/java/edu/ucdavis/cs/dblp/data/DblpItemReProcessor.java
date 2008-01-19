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
			contentService.retrieveAll(pub);
			logger.debug("was missing content: "+pub.getKey() + ' ' +
					(pub.getContent() == null?"no content": "has content"));
			dao.update(pub);
		} else {
			logger.debug("skipping persisted pub with content "+pub.getKey());
		}
	}
}
