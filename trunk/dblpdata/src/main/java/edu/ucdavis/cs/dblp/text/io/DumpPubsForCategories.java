/**
 * 
 */
package edu.ucdavis.cs.dblp.text.io;

import static edu.ucdavis.cs.taxonomy.Categories.ONLY_LEAF_NODES;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import edu.ucdavis.cs.dblp.ServiceLocator;
import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.Category;

/**
 * Dumps all publications for all categories into directories that will work with
 * RapidMiner's Text plugin.
 * 
 * @author pfishero
 */
public class DumpPubsForCategories {
	private static final String DEFAULT_FILE_ENCODING = "UTF-8";

	public static final Logger logger = Logger.getLogger(DumpPubsForCategories.class);

	private final DblpPubDao dao;
	private final Iterable<Publication> allPubData;
	private final List<Category> categories;
	
	public DumpPubsForCategories(List<Category> categories) {		
		this.dao = ServiceLocator.getInstance().getDblpPubDao();
		this.categories = categories;
		
		Iterable<List<Publication>> pubsForCats = Iterables.transform(categories, 
				new Function<Category, List<Publication>>() {
					@Override
					public List<Publication> apply(Category cat) {
						logger.info("Finding pubs for cat: "+cat);
						return dao.findByCategory(cat);
					}
				});
		allPubData = Iterables.concat(Iterables.concat(pubsForCats));
		
		logger.info("created data dumper for "+categories.size()+" categories");
	}
	
	public void dumpData(File baseDir) {
		Multiset<String> catPubCounts = Multisets.newHashMultiset();
		logger.info("dumping data to "+baseDir.getAbsolutePath());
		int count=0;
		
		for(Publication pub : allPubData) {
			count++;
			
			for (Category cat : 
					Iterables.filter(pub.getContent().getCategories(), ONLY_LEAF_NODES)) {
				catPubCounts.add(cat.getKey());
				String categoryId = cat.getKey();
				String catDirName = categoryId.replaceAll("[\\/:*?\"<>|]", "_");
				StringBuilder fileIdBuilder = new StringBuilder();
				fileIdBuilder.append(catDirName).
								append('^').
								append(pub.getKey().replaceAll("[\\/:*?\"<>|]", "_")).
								append(".txt");
				
				StringBuilder fileContents = new StringBuilder();
				fileContents.append(pub.getTitle()).
							append('\n').
							append(Join.join(" ", pub.getContent().getKeywords())).
							append('\n').
							append(pub.getContent().getAbstractText());
				
				File catDir = new File(baseDir, catDirName);
				File outputFile = new File(catDir, fileIdBuilder.toString());
				try {
					FileUtils.writeStringToFile(
							outputFile, fileContents.toString(), DEFAULT_FILE_ENCODING);
				} catch (IOException e) {
					logger.error("problem while writing "+outputFile, e);
					throw new RuntimeException(e);
				}
			}
			
			if (count % 500 == 0) {
				System.out.println("\n\n\n>=1");
				outputResults(baseDir, catPubCounts, 1);
				System.out.println("\n\n\n>=30");
				outputResults(baseDir, catPubCounts, 30);
				System.out.println("\n\n\n>=70");
				outputResults(baseDir, catPubCounts, 70);
			}
		}
		
		System.out.println("\n\n\n>=1");
		outputResults(baseDir, catPubCounts, 1);
		System.out.println("\n\n\n>=30");
		outputResults(baseDir, catPubCounts, 30);
		System.out.println("\n\n\n>=70");
		outputResults(baseDir, catPubCounts, 70);
	}

	/**
	 * Prints out <param> tags that point to the results, subject to the 
	 * minimum count threshold specified in {code}minRequiredPubs{count}.
	 * 
	 * NOTE: this method uses System.out instead of log4j to make 
	 * copy+paste-ing to RapidMiner easy. 
	 * 
	 * @param baseDir
	 * @param catPubCounts
	 * @param minRequiredPubs
	 */
	private void outputResults(File baseDir, Multiset<String> catPubCounts, int minRequiredPubs) {
		for (Category cat : Iterables.filter(categories, ONLY_LEAF_NODES)) {
			if (catPubCounts.count(cat.getKey()) >= minRequiredPubs) {
				String categoryId = cat.getKey();
				String catDirName = categoryId.replaceAll("[\\/:*?\"<>|]", "_");
				File catDir = new File(baseDir, catDirName);
				System.out.println("<parameter key=\""+categoryId.replace("\"", "\\\"")+"\" "+
						"value=\""+catDir.getAbsolutePath().replace("\"", "\\\"")+"\"/> " +
								"<!-- "+catPubCounts.count(cat.getKey())+" items -->");
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File baseDir = new File("C:\\dev\\data\\texts\\");
		baseDir.mkdirs();
		
		List<Category> cats = ServiceLocator.getInstance().getCategoryDao().findLeafNodes();
		DumpPubsForCategories dumper = new DumpPubsForCategories(cats);
		dumper.dumpData(baseDir);
	}

}
