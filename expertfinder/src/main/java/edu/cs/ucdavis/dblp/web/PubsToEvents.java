package edu.cs.ucdavis.dblp.web;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Iterators;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.timeline.EventData;
import edu.ucdavis.cs.dblp.experts.ResearcherDao;
import edu.ucdavis.cs.dblp.experts.ResearcherProfile;
import edu.ucdavis.cs.dblp.experts.ResearcherProfileImpl;

public class PubsToEvents extends HttpServlet {
	private ResearcherDao dao;
	/**
	 * Constructor of the object.
	 */
	public PubsToEvents() {
		super();
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String authorName = request.getParameter("name");
		if (StringUtils.isNotEmpty(authorName)) {
			Collection<Author> authors = dao.findByName(authorName);
			ResearcherProfile profile = new ResearcherProfileImpl(
										Iterators.getOnlyElement(authors.iterator()));
			
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/xml;charset=utf-8");
			PrintWriter out = response.getWriter();
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			out.write(EventData.fromPublications(profile.getPublications()).toXML());
			out.flush();
			out.close();
		} else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"name param must be provided");
		}
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		dao = ServiceLocator.getInstance().getResearcherDao();
	}

}