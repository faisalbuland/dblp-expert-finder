package edu.ucdavis.cs.dblp.data.timeline;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import edu.ucdavis.cs.dblp.data.Publication;

/**
 * EventData is a collection of {@link Event}s. 
 * @see http://simile.mit.edu/wiki/How_to_Create_Event_Source_Files
 * 
 * @author pfishero
 */
@XStreamAlias("data")
public class EventData {
	@XStreamAlias("wiki-url")
   	@XStreamAsAttribute
	private String wikiUrl;
	@XStreamAlias("wiki-section")
   	@XStreamAsAttribute
	private String wikiSection;
	@XStreamImplicit(itemFieldName="event")
	private List<Event> events;
	
	public EventData() {
		events = new LinkedList<Event>();
	}
	
	public static EventData fromPublications(Collection<Publication> pubs) {
		EventData data = new EventData();
		
		for (Publication pub : pubs) {
			data.addEvent(Event.fromPublication(pub));
		}
		
		return data;
	}
	
	public String toXML() {
		XStream xstream = new XStream();
		
		Annotations.configureAliases(xstream, Event.class);
		Annotations.configureAliases(xstream, EventData.class);

		return xstream.toXML(this);
	}	

	public List<Event> getEvents() {
		return events;
	}
	
	public void addEvent(Event event) {
		events.add(event);
	}

	public String getWikiUrl() {
		return wikiUrl;
	}

	public void setWikiUrl(String wikiUrl) {
		this.wikiUrl = wikiUrl;
	}

	public String getWikiSection() {
		return wikiSection;
	}

	public void setWikiSection(String wikiSection) {
		this.wikiSection = wikiSection;
	}
}
