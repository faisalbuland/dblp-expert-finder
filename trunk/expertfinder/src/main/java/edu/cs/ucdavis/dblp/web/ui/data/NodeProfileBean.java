/**
 * 
 */
package edu.cs.ucdavis.dblp.web.ui.data;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.experts.NullAuthor;
import edu.ucdavis.cs.dblp.experts.ResearcherProfile;
import edu.ucdavis.cs.dblp.experts.ResearcherProfileImpl;
import edu.ucdavis.cs.taxonomy.Categories;
import edu.ucdavis.cs.taxonomy.Category;
import edu.ucdavis.cs.taxonomy.CategoryDao;

/**
 * @author pfishero
 */
public class NodeProfileBean implements Serializable {
	public static final Logger logger = Logger.getLogger(NodeProfileBean.class);
	
	private String nodeKey;
	private ResearcherProfile profile;
	private Category selectedCategory;

	public NodeProfileBean() { }
	
	// ---- Action handlers ----
	public String buildProfile() {
		CategoryDao catDao = ServiceLocator.getInstance().getCategoryDao();
		DblpPubDao pubDao = ServiceLocator.getInstance().getDblpPubDao();
		Author noOpAuthor = new NullAuthor();
		logger.info("building profile for cat="+nodeKey);
		Category cat = catDao.findByKey(nodeKey);
		
		List<Publication> pubs = pubDao.findByCategory(cat);
		if (pubs != null && pubs.size() > 0) {
			logger.info("found "+pubs.size()+" pubs for category:"+cat);
			ResearcherProfile profile = new ResearcherProfileImpl(noOpAuthor, pubs);
			setProfile(profile);
			setSelectedCategory(cat);
		}
		
		return "NODE_RESULTS_FOUND";
	}
	
	public String exportResults() {
		return Exporter.exportResults(profile.getPublications());
	}
	
	// ---- Getters/Setters ----
	public String getNodeKey() {
		return nodeKey;
	}

	public void setNodeKey(String nodeKey) {
		this.nodeKey = nodeKey;
	}

	public ResearcherProfile getProfile() {
		return profile;
	}

	public void setProfile(ResearcherProfile profile) {
		this.profile = profile;
	}
	
	public int getPublicationsCount() {
		return (profile == null) ? 0 : profile.getPublications().size();
	}

	public Category getSelectedCategory() {
		return selectedCategory;
	}

	public void setSelectedCategory(Category selectedCategory) {
		this.selectedCategory = selectedCategory;
	}
}
