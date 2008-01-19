/**
 * 
 */
package edu.ucdavis.cs.taxonomy.acm;

import java.util.Collection;

import javax.xml.bind.JAXBElement;

import org.acm.taxonomy.NodeType;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.cs.dblp.data.ItemProcessor;
import edu.ucdavis.cs.taxonomy.Category;
import edu.ucdavis.cs.taxonomy.CategoryDao;

/**
 * 
 * @author pfishero
 * @version $Id$
 *
 */
public class CcsItemProcessor implements ItemProcessor<Object> {
	public static final Logger logger = Logger.getLogger(CcsItemProcessor.class);
	
	public CcsItemProcessor() { }
	
	private CategoryDao dao;
	
	public void setDao(CategoryDao dao) {
		this.dao = dao;
	}
	
	public CategoryDao getDao() {
		return dao;
	}
	
	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.ItemProcessor#process(java.lang.Object)
	 */
	@Override
	@Transactional
	public void process(Object item) {
		JAXBElement element = (JAXBElement) item;
		Category category = ACMCategoryConverter.convert((NodeType)element.getValue(), null);
		logger.debug(category);
		dao.update(category);
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.ItemProcessor#process(java.util.Collection)
	 */
	@Override
	@Transactional
	public void process(Collection<Object> items) {
			for (Object entry : items) {
				JAXBElement element = (JAXBElement) entry;
				Category category = ACMCategoryConverter.convert((NodeType)element.getValue(), null);
				logger.debug(category);
				dao.update(category);
			}
	}

}
