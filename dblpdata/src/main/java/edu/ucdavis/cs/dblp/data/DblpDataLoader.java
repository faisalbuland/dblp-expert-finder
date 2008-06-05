/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import edu.ucdavis.cs.dblp.ServiceLocator;

/**
 * DataLoader for DBLP data dumps.
 * 
 * @author pfishero
 * @version $Id$
 */
public class DblpDataLoader implements DataLoader {
	public static final Logger logger = Logger.getLogger(DblpDataLoader.class);

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
		
		ExecutorService execService = null;
		try {
			provider.open();
			execService = Executors.newFixedThreadPool(8);
			final Semaphore lock = new Semaphore(8);
//			execService = Executors.newSingleThreadExecutor();
//			final Semaphore lock = new Semaphore(1);
			
			
			if (0 == batchSize) { // load one at a time
				for (Object item : provider) {
					processor.process(item);
				}
			} else { // do a bulk load
				List<Object> items = new ArrayList<Object>(batchSize);
				int i=0;
				for (Object item : provider) {
					items.add(item);
					if ((i > 0) && (i % (batchSize/10) == 0)) {
						logger.info("processing " + i);
					}
					if ((i > 0) && (i % batchSize == 0)) {
						execService.execute(new Runner(Lists.newLinkedList(items), lock));
						lock.acquire();
						items.clear();
					}
					i++;
				}
				execService.execute(new Runner(Lists.newLinkedList(items), lock));
				lock.acquire();
				logger.info(i + " processed");
			}
		} finally {
			provider.close();
			if (execService != null) {
				execService.shutdown();
			}
		}
	}
	private class Runner implements Runnable {
		private List<Object> items;
		private ItemProcessor<Object> processor;
		private Semaphore lock;

		public Runner(List<Object> items, Semaphore lock) {
			super();
			this.items = items;
			this.processor = (ItemProcessor<Object>) ServiceLocator.getInstance().
								getAppContext().getBean("kwItemProcessorBean");
			this.lock = lock;
		}
		@Override
		public void run() {
			processor.process(items);
			lock.release();
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
