package ch.uzh.ifi.attempto.aceview.predicate;

import org.semanticweb.owlapi.model.OWLEntity;

import com.google.common.base.Predicate;

public class EntryReferencesEntity implements Predicate<OWLEntity> {
	private final OWLEntity entity;

	public EntryReferencesEntity(OWLEntity entity) {
		this.entity = entity;
	}

	public boolean apply(OWLEntity e) {
		return entity.equals(e);		
	}
}