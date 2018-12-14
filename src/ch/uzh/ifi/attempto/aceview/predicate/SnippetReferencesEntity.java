package ch.uzh.ifi.attempto.aceview.predicate;

import org.semanticweb.owlapi.model.OWLEntity;

import com.google.common.base.Predicate;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class SnippetReferencesEntity implements Predicate<ACESnippet> {
	private final OWLEntity entity;

	public SnippetReferencesEntity(OWLEntity entity) {
		this.entity = entity;
	}


	public boolean apply(ACESnippet snippet) {
		if (snippet != null) {
			return snippet.containsEntityReference(entity);
		}
		return false;
	}
}