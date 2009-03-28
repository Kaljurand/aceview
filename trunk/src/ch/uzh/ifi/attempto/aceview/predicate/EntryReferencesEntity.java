package ch.uzh.ifi.attempto.aceview.predicate;

import org.semanticweb.owl.model.OWLEntity;

import com.google.common.base.Predicate;

public class EntryReferencesEntity implements Predicate {
	private final OWLEntity entity;

	public EntryReferencesEntity(OWLEntity entity) {
		this.entity = entity;
	}

	public boolean apply(Object input) {
		return entity.equals(input);			
	}
}