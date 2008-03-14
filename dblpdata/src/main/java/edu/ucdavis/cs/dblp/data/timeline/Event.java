package edu.ucdavis.cs.dblp.data.timeline;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.ucdavis.cs.dblp.data.Publication;

/**
 * Event is an event in time.  Event is used by SIMILE's Timeline project
 * for displaying temporal event on a timeline.
 * 
 * @author pfishero
 */
@XStreamAlias("event")
public class Event {
	public static final Logger logger = Logger.getLogger(Event.class);
	
	@XStreamAsAttribute
	private Date start;
	@XStreamAsAttribute
	private Date end;
	@XStreamAlias("isDuration")
	@XStreamAsAttribute
	private boolean duration;
	@XStreamAsAttribute
	private String title;
	@XStreamAsAttribute
	private String description;
	private String content;
	@XStreamAsAttribute
	private String image;
	@XStreamAsAttribute
	private String icon;
	@XStreamAsAttribute
	private String link;
	
	public static Event fromPublication(Publication pub) {
		Event event = new Event();
		
		event.title = pub.getTitle();
		event.link = pub.getEe();
		if (pub.getContent() != null) {
			event.content = pub.getContent().getAbstractText();
		}
		event.duration = false;
		
		if (null != pub.getYear()) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
			
			try {
				event.start = formatter.parse(pub.getYear());
//				event.end = formatter.parse(pub.getYear());
			} catch (ParseException e) {
				logger.error("error while parsing: "+pub.getYear()+" - "+e);
			}
		}
		
		return event;
	}
	
	public static final class ContentConverter implements Converter {

		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer,
				MarshallingContext context) {
			Event event = (Event) source;
			
			writer.addAttribute("isDuration", event.duration?"true":"false");
			
			if (null != event.getStart()) {
				/*writer.addAttribute("start", DateFormat.getDateInstance(DateFormat.FULL).
												format(event.start));*/
				writer.addAttribute("start", event.getStart().toGMTString());
			}
			if (null != event.getEnd()) {
				/*writer.addAttribute("end", DateFormat.getDateInstance(DateFormat.FULL).
												format(event.end));*/
				writer.addAttribute("end", event.getEnd().toGMTString());
			}			
			if (null != event.getTitle()) {
				writer.addAttribute("title", event.title);
			}
			if (null != event.getDescription()) {
				writer.addAttribute("description", event.description);
			}			
			if (null != event.getImage()) {
				writer.addAttribute("image", event.image);
			}
			if (null != event.getIcon()) {
				writer.addAttribute("icon", event.icon);
			}
			if (null != event.getLink()) {
				writer.addAttribute("link", event.link);
			}

			if (null != event.getContent()) {
				writer.setValue(event.content);
			}
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader,
				UnmarshallingContext context) {
			return reader.getValue();
		}

		@Override
		public boolean canConvert(Class type) {
			return type.equals(Event.class);
		}
	}
	
	public static final class ContentWrapper {
		private String content;
		public ContentWrapper() {}
		public static ContentWrapper newInstance(String content) {
			ContentWrapper wrapper = new ContentWrapper();
			wrapper.setContent(content);			
			return wrapper;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public boolean isDuration() {
		return duration;
	}

	public void setDuration(boolean duration) {
		this.duration = duration;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
}
