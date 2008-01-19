/**
 * 
 */
package edu.ucdavis.cs.dblp.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

/**
 * 
 * @author pfishero
 * @version $Id$
 *
 */
public class DblpInputSourceItemProvider implements InputSourceItemProvider<Object> {
	public static final Logger logger = Logger.getLogger(DblpInputSourceItemProvider.class);
	
	private InputStream source;
	
	private XMLInputFactory xmlif;
	private XMLEventReader xmler;
	private XMLEventReader xmlfer;
	private JAXBContext context;

	/* (non-Javadoc)
	 * @see edu.ucdavis.cs.wikipedia.data.InputSourceItemProvider#setInputSource(java.io.InputStream)
	 */
	@Override
	public void setInputSource(Resource source) {
		try {
			if (source.getFilename().endsWith("gz")) {
				this.source = new GZIPInputStream(source.getInputStream());
			} else if (source.getFilename().endsWith("xml")) {
				this.source = source.getInputStream();
			} else {
				String msg = "unknown file extension on input source: "+source;
				throw new IllegalArgumentException(msg);
			}
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void open() throws Exception {
		// Parse the data, filtering out the start elements
		xmlif = XMLInputFactory.newInstance();

		xmler = xmlif.createXMLEventReader(source, "UTF-8");
		EventFilter filter = new EventFilter() {
			public boolean accept(XMLEvent event) {
				return event.isStartElement();
			}
		};
		xmlfer = xmlif.createFilteredReader(xmler, filter);
		// Jump to the first element in the document, the enclosing dblp
		StartElement e = (StartElement) xmlfer.nextEvent();
		assert "dblp".equals(e.getName().getLocalPart()) : "unexpected start element";

		context = JAXBContext.newInstance("de.unitrier.dblp");
	}
	
	@Override
	public void close() throws Exception{
		if (null != xmlfer) xmlfer.close();
		if (null != xmler) xmler.close();
		if (null != source) source.close();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Object> iterator() {
		return new InternalIterator<Object>();
	}
	
	private final class InternalIterator<Object> implements Iterator<Object> {
		private Unmarshaller um;
		
		public InternalIterator() {
			try {
				um = context.createUnmarshaller();
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public boolean hasNext() {
			boolean hasNext = false;
			try {
				hasNext = (xmlfer.peek() != null); 
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return hasNext;
		}
		@Override
		public Object next() {
			Object item = null;
			
			try {
				item = (Object)um.unmarshal(xmler);
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			}
			
			return item;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}

}
