/**
 * 
 */
package edu.ucdavis.cs.dblp.service;

import edu.ucdavis.cs.dblp.data.Publication;

/**
 * ResourceFetchers retrieve electronic editions (web pages) for 
 * {@link Publication}s.
 * 
 * @author pfishero
 * @version $Id$
 */
public interface ResourceFetcher {
	/**
	 * Retrieves the electronic edition of the publication <code>pub</code>
	 * as a String.  The electronic edition is considered to be the web page
	 * with abstract information (etc...) for the publication, and not the
	 * full text (e.g. pdf).
	 *  
	 * @param pub to retrieve the electronic edition for
	 * @return the electronic edition web page as a non-blank string, if the 
	 * input <code>url</code> pointed to a valid resource.  If the page was not
	 * a text/html type response, then a blank string will be returned.
	 */
	String fetchElectronicEdition(Publication pub);
}
