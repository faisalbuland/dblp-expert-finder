package edu.ucdavis.cs.dblp.data;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableList;

import edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer;


public class DblpItemKeywordProcessor extends DblpItemProcessor {
	public static final Logger logger = Logger.getLogger(DblpItemKeywordProcessor.class);
	
	/**
	 * PROCESS==true means extract keywords when processing the data 
	 * PROCESS==false means just output the state of the item (in the db or not, has content or not)
	 */
	private static final boolean PROCESS=true;
	
	private KeywordRecognizer recognizer;
	
	/**
	 * @return the recognizer
	 */
	public KeywordRecognizer getRecognizer() {
		return recognizer;
	}
	/**
	 * @param recognizer the recognizer to set
	 */
	@Required
	public void setRecognizer(KeywordRecognizer recognizer) {
		this.recognizer = recognizer;
	}

	@Override
	protected void processItem(Object entry) {
		Publication pub;
		if (entry instanceof Publication) {
			pub = (Publication) entry;
		} else {
			pub = Publication.convert(entry);
		}
		logger.info(pub.getKey());

		try {
			Publication persistedPub = dao.findById(pub.getKey());
			assert persistedPub.getKey().equals(pub.getKey()) : "keys didn't match "+persistedPub.getKey()+" "+pub.getKey();
			
			if (null == persistedPub) {
				// not found in the DB
				if (PROCESS) {
					contentService.retrieveAll(pub);
					recognizer.produceControlledVocabulary(ImmutableList.of(pub));
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
					recognizer.produceControlledVocabulary(ImmutableList.of(persistedPub));
					logger.info("was missing content: "+persistedPub.getKey() + ' ' +
							(persistedPub.getContent() == null?"no content": "has content"));
					dao.update(persistedPub);
				} else {
					logger.info("was missing content: " + 
							persistedPub.getKey() +" [ee]:"+persistedPub.getEe());
				}
			} else if (StringUtils.isBlank(persistedPub.getContent().getAbstractText()) ||
					null == persistedPub.getContent().getKeywords() || 
					persistedPub.getContent().getKeywords().size() == 0) {
				contentService.retrieveAll(persistedPub);
				// found in the DB with no content (but maybe some keywords)
				recognizer.produceControlledVocabulary(ImmutableList.of(persistedPub));
				logger.info("updating keywords on persisted pub with content "+persistedPub.getKey());
				dao.update(persistedPub);
			} else {
				String origAbstractText = persistedPub.getContent().getAbstractText();
				contentService.retrieveAll(persistedPub);
				if (!origAbstractText.equals(persistedPub.getContent().getAbstractText())
						&& StringUtils.isNotBlank(persistedPub.getContent().getAbstractText())) {
					// reprocess
					recognizer.produceControlledVocabulary(ImmutableList.of(persistedPub));
					logger.info("updating keywords on persisted pub with existing content "+persistedPub.getKey());
					dao.update(persistedPub);
				} else {
					logger.info("skipping persisted pub with content and keywords "+persistedPub.getKey());
				}
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
