/**
 * 
 */
package edu.cs.ucdavis.dblp.web.ui.controllers;

import java.util.Collection;
import java.util.Collections;

import javax.faces.component.UIComponent;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.experts.ResearcherDao;
import edu.ucdavis.cs.dblp.experts.ResearcherProfile;
import edu.ucdavis.cs.dblp.experts.ResearcherProfileImpl;

/**
 * Controller for search related actions and state.
 * 
 * @author pfishero
 */
public class SearchController {
	public static final Logger logger = Logger.getLogger(SearchController.class);
	
	private String searchText;
	private String researcherName;
	private Collection<Author> authors;
	private ResearcherProfile profile;
	private UIComponent authorSelector;
	private UIComponent researcherInfo;
	private UIComponent publicationsList;
	
	public SearchController() {
		authors = Lists.newLinkedList();
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
	
	public String getResearcherName() {
		return researcherName;
	}

	public void setResearcherName(String researcherName) {
		this.researcherName = researcherName;
	}

	public Collection<Author> getAuthors() {
		return authors;
	}

	public String authorSearch() {
		logger.info("searching for author with name: "+searchText);
		doAuthorSearch(searchText);
		
		authorSelector.setRendered(true);
		researcherInfo.setRendered(false);
		publicationsList.setRendered(false);
		
		return null;
	}
	
	public String buildResearcherProfile() {
		doAuthorSearch(researcherName);
		this.searchText=researcherName;
		
		authorSelector.setRendered(false);
		researcherInfo.setRendered(true);
		publicationsList.setRendered(true);
		
		return null;
	}
	
	private final void doAuthorSearch(String authorName) {
		ResearcherDao dao = ServiceLocator.getInstance().getResearcherDao();
		Collection<Author> authorResults = dao.findByNamePrefix(authorName);
		logger.info("found "+authorResults.size()+" authors for name: "+authorName);
		this.authors = authorResults;

		setProfile(new ResearcherProfileImpl(authorResults.iterator().next()));		
	}
	
	public ResearcherProfile getProfile() {
		return profile;
	}

	public void setProfile(ResearcherProfile profile) {
		this.profile = profile;
	}
	
	public UIComponent getAuthorSelector() {
		return authorSelector;
	}

	public void setAuthorSelector(UIComponent authorSelector) {
		this.authorSelector = authorSelector;
	}

	public UIComponent getResearcherInfo() {
		return researcherInfo;
	}

	public void setResearcherInfo(UIComponent researcherInfo) {
		this.researcherInfo = researcherInfo;
	}

	public UIComponent getPublicationsList() {
		return publicationsList;
	}

	public void setPublicationsList(UIComponent publicationsList) {
		this.publicationsList = publicationsList;
	}
	
	public int getPublicationsCount() {
		return this.profile == null ? 0 : this.profile.getPublications().size();
	}
}
