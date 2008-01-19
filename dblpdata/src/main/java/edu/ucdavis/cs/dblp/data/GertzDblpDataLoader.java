/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.unitrier.dblp.Author;

/**
 * DataLoader for DBLP data dumps.
 * 
 * @author pfishero
 * @version $Id$
 */
public class GertzDblpDataLoader implements DataLoader {
	public static final Logger logger = Logger.getLogger(GertzDblpDataLoader.class);

	private InputSourceItemProvider<Object> provider;
	private ItemProcessor<Object> processor;
	
	private int batchSize;
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public int getBatchSize() {
		return batchSize;
	}
	
	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.DataLoader#doLoad()
	 */
	@Override
	public void doLoad() throws Exception {
		if (null == provider) {
			throw new IllegalStateException(
					"provider must be set before calling doLoad()");
		} else if (null == processor) {
			throw new IllegalStateException(
					"processor must be set before calling doLoad()");
		}

		try {
			provider.open();
			
			if (0 == batchSize) { // load one at a time
				for (Object item : provider) {
					processor.process(item);
				}
			} else { // do a filtered bulk load
				List<Object> items = new ArrayList<Object>(batchSize);
				int i=0;
				for (Object item : provider) {
					Publication pub = Publication.convert(item);
					boolean containsGertz = false;
					
					for (Author author : pub.getAuthor()) {
						if (StringUtils.isNotBlank(author.getContent())
								&& author.getContent().toUpperCase().indexOf("GERTZ") >= 0) {
							containsGertz = true;
							break;
						}
					}
					
					if (containsGertz) {
						items.add(item);
						if ((i > 0) && (i % (batchSize/10) == 0)) {
							logger.info("processing " + i);
						}
						if ((i > 0) && (i % batchSize == 0)) {
							processor.process(items);
							logger.info("committed after "+ i + " processed");
							items.clear();
						}
						i++;
					}
				}
				processor.process(items);
				logger.info(i + " processed");
			}
		} finally {
			provider.close();
		}
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.DataLoader#setInputSourceItemProvider(edu.ucdavis.cs.wikipedia.data.InputSourceItemProvider)
	 */
	@Override
	public void setInputSourceItemProvider(
			InputSourceItemProvider<Object> provider) {
		this.provider = provider;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.DataLoader#setItemProcessor(edu.ucdavis.cs.wikipedia.data.ItemProcessor)
	 */
	@Override
	public void setItemProcessor(ItemProcessor<Object> processor) {
		this.processor = processor;
	}

}
