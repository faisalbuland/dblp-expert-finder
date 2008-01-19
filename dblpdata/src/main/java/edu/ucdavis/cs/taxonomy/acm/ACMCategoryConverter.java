package edu.ucdavis.cs.taxonomy.acm;

import org.acm.taxonomy.IsComposedByType;
import org.acm.taxonomy.IsRelatedToType;
import org.acm.taxonomy.NodeType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ucdavis.cs.taxonomy.Category;

public final class ACMCategoryConverter {
	public static final Logger logger = Logger.getLogger(ACMCategoryConverter.class);
	
	private ACMCategoryConverter() { }
	
	public static Category convert(NodeType node, Category parent) {
		Category nodeCat = new Category(node.getId(), node.getLabel(), parent);

		if (node.getHasNote() != null
				&& StringUtils.isNotBlank(node.getHasNote().getValue())) {
			nodeCat.setActive(false);
		}
		
		IsRelatedToType related = node.getIsRelatedTo();
		if (related != null && related.getNode() != null) {
			for (NodeType relCatNode : related.getNode()) {
				// related category entries only have their id set (in the xml)
				Category relCategory = new Category();
				relCategory.setId(relCatNode.getId());
				nodeCat.addRelatedCategory(relCategory);
			}
		}
		// generate twice - once now, to ensure that child nodes 
		// inherit the right key (e.g. nodes with subjects wouldn't have 
		// related cat info from their parent's key if the key wasn't 
		// generated here.)
		nodeCat.generateKey();
		
		IsComposedByType children = node.getIsComposedBy();
		if (children != null && children.getNode() != null) {
			for (NodeType child : children.getNode()) {
				Category childCat = convert(child, nodeCat);
				if (childCat != null) {
					if (child.getHasNote() != null && 
						StringUtils.isNotBlank(child.getHasNote().getValue())) {
						childCat.setActive(false);
					}
					nodeCat.addChild(childCat);
				}
			}
		}
		
		nodeCat.generateKey();
		logger.trace("converted to Category with key="+nodeCat.getKey());
		
		return nodeCat;
	}
}
