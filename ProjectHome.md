# Overview #
An Expert Finder System for [DBLP](http://www.informatik.uni-trier.de/~ley/db/).  Extends the [ACM Computing Classification System](http://www.acm.org/class/1998/) to build a hierarchy of expertise areas.  Associates experts with areas of expertise in this extended taxonomy.

This work is a part of [Philip Fisher-Ogden](http://code.google.com/u/pfishero/)'s Master's Thesis at UC Davis.  Professors [Michael Gertz](http://www.cs.ucdavis.edu/people/faculty/gertz.html) and [Ian Davidson](http://www.cs.ucdavis.edu/people/faculty/davidson.html) are the thesis advisors for this research.


## Primary Goals ##
  * **Expertise areas**: synthesize associations between topics and experts, enabling expertise identification for a given topic or a given researcher.
  * **Taxonomy**: extend an existing taxonomy to produce an exploratory interface to browse for experts in.

## Contributions ##
1. We augment the information in DBLP with enough additional data to be able to make a judgment on expertise areas and experts for a given area.

2. We address the problem of topic extraction by leveraging existing author-generated metadata to build a light weight controlled vocabulary.

3. We propose an exploratory interface for finding experts that utilizes an extended form of a well known taxonomy combined with spatio-temporal visualizations of each node's document sets.

## Milestones ##
Building an expert finder system for DBLP requires reaching the following milestones:
  * Create an object/domain model for the DBLP data and load the DBLP XML data into a database.  (Leverage JAXB and JPA technologies to do this)
  * Create an object model for the ACM Computing Classification System taxonomy and load the XML version of that taxonomy into a database.  (Leverage JAXB and JPA technologies to do this)
  * Extract classification information, general terms, keywords, and abstracts for each DBLP entry and associate that information with each DBLP entry in a database.
  * Associate topics in the extended taxonomy with researchers that have published on each topic.
  * Provide an interface to allow users to identify experts for a given topic, where the topic is an element in the extended taxonomy.
  * Identify communities of expertise.
  * Evaluate various spatio-temporal visualizations for the exploratory interface.

## Related Work ##
  * [Faceted DBLP](http://dblp.l3s.de/) allows users to browse the DBLP data using keyword searches and faceted navigation.  It also allows users to automatically generate categories from their search results by using the Semantic GrowBag algorithm

## Links ##
For a current list of links related to this effort, see [my thesis links on delicious](http://del.icio.us/philfish/thesis).