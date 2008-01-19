package edu.ucdavis.cs.dblp.data;

import edu.ucdavis.cs.dblp.ServiceLocator;

public class Loader {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DataLoader dataLoader = (DataLoader)ServiceLocator.getInstance().
								getAppContext().getBean("dataLoader");
		dataLoader.doLoad();
	}

}
