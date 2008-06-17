package edu.ucdavis.cs.dblp.data.mining;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.taxonomy.CategoryDao;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
DependencyInjectionTestExecutionListener.class,
TransactionalTestExecutionListener.class})
@Transactional
@ContextConfiguration(
locations={"/spring/dblpApplicationContext.xml"})
public class DocExporter {
	private static final Logger logger = Logger.getLogger(DocExporter.class); 
	
	@Resource(name="dblpPubDaoImpl")
	private DblpPubDao dao;
	@Resource
	private CategoryDao catDao;
	
    @Test
    public void exportPubs() throws Exception {
        List<Publication> pubs = dao.findByCategory(
				catDao.findByFreeTextSearch("Spatial Database").get(0));
        File outfile = new File("spatdbs.txt");
        exportData(pubs, outfile);
    }

	/**
	 * @param pubs
	 * @param outfile
	 * @throws IOException
	 */
	public static void exportData(Iterable<Publication> pubs, File outfile)
			throws IOException {
		Function<Publication, String> fnPubLines = new Function<Publication, String>() {
        	@Override
        	public String apply(Publication pub) {
        		return pub.getKey().replaceAll("[^A-Za-z0-9]", "")+" "+pub.getTitle()+" "+
        			(pub.getContent() != null && pub.getContent().getAbstractText() != null ?
        					pub.getContent().getAbstractText() : "").replaceAll("\\s+", " ");
        	}
        };
        FileUtils.writeLines(outfile, Lists.newLinkedList(Iterables.transform(pubs, fnPubLines)));
	}
}
