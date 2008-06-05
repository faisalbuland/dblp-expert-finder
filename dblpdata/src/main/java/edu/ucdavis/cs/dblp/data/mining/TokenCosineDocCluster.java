package edu.ucdavis.cs.dblp.data.mining;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.util.Counter;
import com.aliasi.util.Distance;
import com.aliasi.util.ObjectToCounterMap;
import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.Iterables;

import edu.ucdavis.cs.dblp.data.DblpPubDao;
import edu.ucdavis.cs.dblp.data.Publication;
import edu.ucdavis.cs.dblp.data.keywords.KeywordRecognizer;
import edu.ucdavis.cs.taxonomy.CategoryDao;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
DependencyInjectionTestExecutionListener.class,
TransactionalTestExecutionListener.class})
@Transactional
@ContextConfiguration(
locations={"/spring/dblpApplicationContext.xml"})
public class TokenCosineDocCluster {
	private static final Logger logger = Logger.getLogger(TokenCosineDocCluster.class); 
	
	@Resource(name="dblpPubDaoImpl")
	private DblpPubDao dao;
	@Resource
	private CategoryDao catDao;
	@Resource
	private KeywordRecognizer recognizer;

    public static void main(String[] args) throws Exception {
    	
    }
	
    @Test
    public void runClustering() throws Exception {
        List<Publication> pubs = dao.findByCategory(
				catDao.findByFreeTextSearch("Spatial Database").get(0));
        Set<Document> docSet = new HashSet<Document>();
        for (Publication pub : pubs) {
        	Document doc = new Document(pub);
        	docSet.add(doc);
        }

        // eval clusterers
        HierarchicalClusterer<Document> clClusterer
            = new CompleteLinkClusterer<Document>(COSINE_DISTANCE);
        Dendrogram<Document> completeLinkDendrogram
            = clClusterer.hierarchicalCluster(docSet);

//        HierarchicalClusterer<Document> slClusterer
//            = new SingleLinkClusterer<Document>(COSINE_DISTANCE);
//        Dendrogram<Document> singleLinkDendrogram
//            = slClusterer.hierarchicalCluster(docSet);


        for (int k = 1; k <= docSet.size()/5; ++k) {
            Set<Set<Document>> clResponsePartition
                = completeLinkDendrogram.partitionK(k);
//            Set<Set<Document>> slResponsePartition
//                = singleLinkDendrogram.partitionK(k);

            logger.info("cl clusters - k="+k);
            int i=0; 
            for (Set<Document> clusterDocs : clResponsePartition) {
            	recognizer.produceControlledVocabulary(Iterables.transform(clusterDocs, FN_DOCS_TO_PUBS));
            	logger.info(k+"."+i);
            	logger.info(Join.join("\n", clusterDocs));
            	i++;
            }
        }
    }
    
    public static final Function<Document, Publication> FN_DOCS_TO_PUBS = new Function<Document, Publication>() {
    	@Override
    	public Publication apply(Document doc) {
    		return doc.pub;
    	}
    };

    static class Document {
    	final Publication pub;
        final char[] mText; // don't really need to store
        final ObjectToCounterMap<String> mTokenCounter
            = new ObjectToCounterMap<String>();
        final double mLength;
        Document(Publication pub) throws IOException {
            this.pub = pub;
            if (pub.getContent() != null) {
            	mText = (pub.getTitle()+" "+pub.getContent().getAbstractText()).toCharArray();
            } else {
            	mText = pub.getTitle().toCharArray();
            }
            Tokenizer tokenizer = createTokenizer(mText);
            String token;
            while ((token = tokenizer.nextToken()) != null)
                mTokenCounter.increment(token.toLowerCase());
            mLength = length(mTokenCounter);
        }
        double cosine(Document thatDoc) {
            return product(thatDoc) / (mLength * thatDoc.mLength);
        }
        double product(Document thatDoc) {
            double sum = 0.0;
            for (String token : mTokenCounter.keySet()) {
                int count = thatDoc.mTokenCounter.getCount(token);
                if (count == 0) continue;
                // tf = sqrt(count); sum += tf1 * tf2
                sum += Math.sqrt(count * mTokenCounter.getCount(token));
            }
            return sum;
        }
        public String toString() {
            return pub.getKey()+" -> "+pub.getTitle()+"\n\t"+pub.getContent().getKeywords();
        }
        static double length(ObjectToCounterMap<String> otc) {
            double sum = 0.0;
            for (Counter counter : otc.values()) {
                double count = counter.doubleValue();
                sum += count;  // tf =sqrt(count); sum += tf * tf
            }
            return Math.sqrt(sum);
        }
        static Tokenizer createTokenizer(char[] cs) {
            Tokenizer tokenizer
                = IndoEuropeanTokenizerFactory
                .FACTORY.tokenizer(cs,0,cs.length);
            // tokenizer = new LowerCaseFilterTokenizer(tokenizer);
            // tokenizer = new EnglishStopListFilterTokenizer(tokenizer);
            // tokenizer = new PorterStemmerFilterTokenizer(tokenizer);
            return tokenizer;
        }
    }

    static Distance<Document> COSINE_DISTANCE
        = new Distance<Document>() {
            public double distance(Document doc1, Document doc2) {
                return 1.0 - doc1.cosine(doc2);
            }
        };

}
