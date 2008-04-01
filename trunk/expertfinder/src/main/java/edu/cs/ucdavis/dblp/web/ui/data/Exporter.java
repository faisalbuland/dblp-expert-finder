package edu.cs.ucdavis.dblp.web.ui.data;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.unitrier.dblp.Author;
import edu.ucdavis.cs.dblp.data.Keyword;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Categories;
import edu.ucdavis.cs.taxonomy.Category;

public final class Exporter {
	public static final Logger logger = Logger.getLogger(Exporter.class);

	/**
	 * Non-instantiable as this is a utility class.
	 */
	private Exporter() { }
	
	public static String exportResults(Collection<Publication> publications) {
		String filename = "export.csv"; // Filename suggested in browser Save As dialog
		String contentType = "application/csv"; // For dialog, try application/x-download
		byte[] data = Exporter.exportPubsToCsv(publications);

		FacesContext fc = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse)fc.getExternalContext().getResponse();
		response.setHeader("Content-disposition", "attachment; filename=" + filename);
		response.setContentLength(data.length);
		response.setContentType(contentType);
		response.setCharacterEncoding("UTF-8");
		ServletOutputStream out;
		try {
			out = response.getOutputStream();
			out.write(data);
		} catch (IOException e) {
			// TODO report error back to web UI
			logger.error(e);
		}
		fc.responseComplete();

		return null;
	}
	
	public static byte[] exportPubsToCsv(Collection<Publication> publications) {
		byte[] data = null;
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CSVWriter writer = new CSVWriter(new BufferedWriter(new OutputStreamWriter(baos, "UTF-8")));
			// write the header
			writer.writeNext(new String[]{"title", "keywords", "abstract", "authors", "citation", "categories",});
			// write the data
			List<String[]> entries = Lists.transform(
		    		 ImmutableList.copyOf(publications), 
		    		 new Function<Publication, String[]>() {
			    	 	@Override
			    	 	public String[] apply(Publication pub) {
			    	 		String[] values = new String[]{
			    	 			pub.getTitle(),
			    	 			pub.getContent() != null && pub.getContent().getKeywords() != null
			    	 				? Join.join(", ", 
			    	 						Iterables.transform(pub.getContent().getKeywords(), 
			    	 								new Function<Keyword, String>() { 
							    	 					@Override
							    	 					public String apply(Keyword keyword) {
							    	 						return keyword.getKeyword();
							    	 					}
			    	 				}))
			    	 				: "",
			    	 			pub.getContent() != null && pub.getContent().getAbstractText() != null
			    	 				? pub.getContent().getAbstractText() : "",
			    	 			Join.join(", ", 
			    	 					Iterables.transform(pub.getAuthor(), 
			    	 								new Function<Author, String>() {
							    	 					public String apply(Author author) {
							    	 						return author.getContent();
							    	 					};
			    	 				})),
			    	 			pub.getCitationString(),
			    	 			pub.getContent() != null && pub.getContent().getCategories() != null
			    	 				? Join.join(", ", 
			    	 						Iterables.transform(
			    	 								Iterables.filter(pub.getContent().getCategories(), 
			    	 										Categories.ONLY_LEAF_NODES),
			    	 								new Function<Category, String>() {
				    	 								@Override
				    	 								public String apply(Category cat) {
				    	 									return cat.getKey();
				    	 								}
			    	 					}))
			    	 				: "",
			    	 		};
			    	 		return values;
			    	 	}
		    		 });
		     writer.writeAll(entries);
		     writer.close();
		     data = baos.toByteArray();
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
		return data;
	}
}
