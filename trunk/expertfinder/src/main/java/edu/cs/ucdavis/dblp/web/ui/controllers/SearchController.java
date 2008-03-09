/**
 * 
 */
package edu.cs.ucdavis.dblp.web.ui.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.unitrier.dblp.Author;
import edu.cs.ucdavis.dblp.web.ui.data.Exporter;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.experts.ResearcherDao;
import edu.ucdavis.cs.dblp.experts.ResearcherProfile;
import edu.ucdavis.cs.dblp.experts.ResearcherProfileImpl;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * Controller for search related actions and state.
 * 
 * @author pfishero
 */
public class SearchController {
	public static final Logger logger = Logger.getLogger(SearchController.class);
	
	private String searchText;
	private String nodeSearchText;
	private String researcherName;
	private Collection<Author> authors;
	private ResearcherProfile profile;
	private Category selectedCategory;
	
	public SearchController() {
		authors = Collections.emptyList();
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
	
	public String getResearcherNameForLink() {
		StringBuilder str = new StringBuilder();
		
		if (StringUtils.isNotBlank(researcherName)) {
			try {
				str.append(URLEncoder.encode(researcherName, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("error encoding researcher name for link: "+researcherName+" - "+e);
			}
		}
		
		return str.toString();
	}

	public void setResearcherName(String researcherName) {
		this.researcherName = researcherName;
	}

	public Collection<Author> getAuthors() {
		return authors;
	}
	
	public boolean isAuthorsFound() {
		boolean authorsFound;
		
		if (authors == null || authors.size() == 0) {
			authorsFound = false;
		} else {
			authorsFound = true;
		}
		
		return authorsFound;
	}

	public String authorSearch() {
		logger.info("searching for author with name: "+searchText);
		doAuthorSearch(searchText);
		
		// TODO use a boolean to flag "don't display the profile" rather than null 
		this.profile = null;
		
		return null;
	}
	
	public String buildResearcherProfile() {
		doAuthorSearch(researcherName);
		this.searchText=researcherName;
		
		return "PEOPLE_SEARCHED";
	}
	
	private final void doAuthorSearch(String authorName) {
		ResearcherDao dao = ServiceLocator.getInstance().getResearcherDao();
		Collection<Author> authorResults = dao.findByNamePrefix(authorName);
		if (authorResults.size() > 0) {
			logger.info("found "+authorResults.size()+" authors for name: "+authorName);
			this.authors = authorResults;
	
			setProfile(new ResearcherProfileImpl(authorResults.iterator().next()));
		} else {
			this.authors = Collections.emptyList();
		}
	}
	
	public String exportResults() {
		return Exporter.exportResults(profile.getPublications());
	}
	
	public ResearcherProfile getProfile() {
		return profile;
	}

	public void setProfile(ResearcherProfile profile) {
		this.profile = profile;
	}
	
	public int getPublicationsCount() {
		return this.profile == null ? 0 : this.profile.getPublications().size();
	}
}
