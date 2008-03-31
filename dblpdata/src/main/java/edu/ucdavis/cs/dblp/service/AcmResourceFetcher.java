/**
 * 
 */
package edu.ucdavis.cs.dblp.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.ucdavis.cs.dblp.data.Publication;

/**
 * Decorator for {@link ResourceFetcher}s to restrict fetching to ACM Digital
 * Library pages only.
 * 
 * @author pfishero
 * @version $Id$
 */
public class AcmResourceFetcher implements ResourceFetcher {
	public static final Logger logger = Logger.getLogger(AcmResourceFetcher.class);
	private final ResourceFetcher wrappedFetcher;

	public AcmResourceFetcher(ResourceFetcher fetcher) {
		this.wrappedFetcher = fetcher;
	}

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.dblp.service.ResourceFetcher#fetchElectronicEdition(edu.ucdavis.cs.dblp.data.Publication)
	 */
	@Override
	public String fetchElectronicEdition(Publication pub) {
		String responseBody = "";

		if (hasAcmEe(pub)) {
			responseBody = wrappedFetcher.fetchElectronicEdition(pub);
			if (responseBody.indexOf("ACM Digital Library") < 0) {
				// non-ACM DL link - reject
				logger.info("ignoring non-ACM Digital Library page: "
						+ pub.getEe());
				responseBody = "";
			}
		} else {
			logger.debug("skipping non-ACM URL: " + pub.getEe());
		}

		return responseBody;
	}

	private static final List<String> NON_ACM_SUFFIXES = ImmutableList.of(
			"pdf", "pdf.gz", "ps", "ps.gz", "doc");
	private static final Predicate<Publication> ACM_SUFFIX_CHECKER = 
		new Predicate<Publication>() {
		
		@Override
		public boolean apply(Publication pub) {
			boolean allowed = true;

			for (String str : NON_ACM_SUFFIXES) {
				if (pub.getEe().toLowerCase().endsWith(str)) {
					allowed = false;
				}
			}

			return allowed;
		}
	};
	private static final List<String> NON_ACM_URL_PARTS = ImmutableList.of(
			"doi.ieeecomputersociety.org",
			"ieee",
			"ippserv.ugent.be",
			"springer",
			"computer.org",
			"www.graphicsinterface.org",
			"www.sigda.org",
			"decsai.ugr.es",
			"dx.doi.org/10.1007",
			"dx.doi.org/10.1109",
			"dx.doi.org/10.1117",
			"dx.doi.org/10.1093",
			"dx.doi.org/10.2991",
			"dx.doi.org/10.1016",
			"dx.doi.org/10.1002",
			"dx.doi.org/10.1504",
			"dx.doi.org/10.1023",
			"dx.doi.org/10.1137",
			"www.usenix.org",
			"www.zpid.de",
			"www.supercomp.org",
			"www.stringology.org",
			"psc.felk.cvut.cz",
			"www2003.org",
			"SunSITE.Informatik.RWTH-Aachen.DE".toLowerCase(),
			"arxiv.org",
			"www.igi-pub.com",
			"www.fujipress.jp",
			"jair.org",
			"www.cs.washington.edu",
			"www.acmqueue.org"
		);
	private static final Predicate<Publication> ACM_PARTS_CHECKER = 
		new Predicate<Publication>() {
		
		@Override
		public boolean apply(Publication pub) {
			boolean allowed = true;

			for (String str : NON_ACM_URL_PARTS) {
				if (pub.getEe().toLowerCase().indexOf(str) >= 0) {
					allowed = false;
				}
			}

			return allowed;
		}
	};

	public static boolean hasAcmEe(Publication pub) {
		boolean acmEe = false;

		if (!ACM_SUFFIX_CHECKER.apply(pub)) {
			// it ends with a non-ACM-DL suffix
			acmEe = false;
		} else if (!ACM_PARTS_CHECKER.apply(pub)) {
			// it was a non-ACM DL URL
			acmEe = false;
		} else if (pub.getEe().indexOf("acm.org") < 0 && 
					pub.getEe().indexOf("doi") < 0) {
			acmEe = false;
		} else {
			acmEe = true;
		}

		return acmEe;
	}

}
