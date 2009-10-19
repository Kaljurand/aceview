package ch.uzh.ifi.attempto.aceview.util;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLEntity;

import ch.uzh.ifi.attempto.aceview.ACETextManager;

public class EntityComparator implements Comparator<OWLEntity> {

	public int compare(OWLEntity e1, OWLEntity e2) {
		return getRenderingAndClass(e1).compareToIgnoreCase(getRenderingAndClass(e2));
	}


	private String getRenderingAndClass(OWLEntity entity) {
		return ACETextManager.getRendering(entity) + "_" + entity.getClass();
	}
}