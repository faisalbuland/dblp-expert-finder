/**
 * 
 */
package edu.cs.ucdavis.dblp.web.ui.controllers;

import java.util.Collection;

import javax.faces.component.UIComponent;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import de.unitrier.dblp.Author;

/**
 * Controller for search related actions and state.
 * 
 * @author pfishero
 */
public class SearchController {
	public static final Logger logger = Logger.getLogger(SearchController.class);
	
	private String searchText;
	private Collection<Author> authors;
	private UIComponent resultsForm;
	
	public SearchController() {
		authors = Lists.newLinkedList();
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
	
	public UIComponent getResultsForm() {
		return resultsForm;
	}

	public void setResultsForm(UIComponent resultsForm) {
		this.resultsForm = resultsForm;
	}
	
	public Collection<Author> getAuthors() {
		return authors;
	}

	public String authorSearch() {
		authors.clear();
		logger.info("searching for author with name: "+searchText);
		
/*		ResearcherDao dao = ServiceLocator.getInstance().getResearcherDao();
		Collection<Author> authorResults = dao.findByNamePrefix(searchText);
		logger.info("found "+authorResults.size()+" authors for prefix text: "+searchText);
		this.authors = authorResults;
*/
		for (String name : new String[]{"Michael Gertz", "Ian Davidson", "Test Author"}) {
			Author auth = new Author();
			auth.setContent(name);
			authors.add(auth);
		}

		resultsForm.setRendered(authors.size() > 0);
		
		return null;
	}
}
