/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

/**
 * 
 * @author pfishero
 * @version $Id$
 *
 */
public interface DataLoader {
	void setInputSourceItemProvider(InputSourceItemProvider<Object> provider);
	void setItemProcessor(ItemProcessor<Object> processor);
	void doLoad() throws Exception;
}
