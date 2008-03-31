package edu.ucdavis.cs.taxonomy;

import com.google.common.base.Predicate;

public final class Categories {
	public static final Predicate<Category> ONLY_LEAF_NODES = new Predicate<Category>() {
		@Override
		public boolean apply(Category cat) {
			boolean keep = false;
			
			if (cat.getChildren() == null || cat.getChildren().size() == 0) {
				keep = true;
			}
			
			return keep;
		}
	};
	
	public static final Predicate<Category> ONLY_LEAF_PARENT_NODES = new Predicate<Category>() {
		@Override
		public boolean apply(Category cat) {
			boolean keep = true;
			
			if (hasChildren(cat)) { // keep only if its children do not have any children
				for (Category childCat : cat.getChildren()) {
					if (hasChildren(childCat)) {
						keep = false;
					}
				}
			} else { // ignore leaf nodes
				keep = false;
			}
			
			return keep;
		}
		
		private boolean hasChildren(Category cat) {
			return (cat.getChildren() != null && cat.getChildren().size() > 0);
		}
	};
}
