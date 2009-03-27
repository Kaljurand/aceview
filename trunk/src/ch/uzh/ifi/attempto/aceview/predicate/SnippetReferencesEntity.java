package ch.uzh.ifi.attempto.aceview.predicate;

import org.semanticweb.owl.model.OWLEntity;

import com.google.common.base.Predicate;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class SnippetReferencesEntity implements Predicate {
	private final OWLEntity entity;

	public SnippetReferencesEntity(OWLEntity entity) {
		this.entity = entity;
	}


	public boolean apply(Object input) {
		if (input != null && input instanceof ACESnippet) {
			ACESnippet snippet = (ACESnippet) input;
			return snippet.containsEntityReference(entity);
		}
		return false;
	}
}