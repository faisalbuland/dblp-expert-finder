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
}
