package edu.ucdavis.cs.taxonomy.acm;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.DataLoader;

public class CcsLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DataLoader dataLoader = (DataLoader)ServiceLocator.getInstance().
								getAppContext().getBean("ccsDataLoader");
		dataLoader.doLoad();
	}

}
