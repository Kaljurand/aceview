package ch.uzh.ifi.attempto.aceview.util;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.reasoner.Node;

import ch.uzh.ifi.attempto.aceview.ACETextManager;

public class NodeComparator implements Comparator<Node<? extends OWLEntity>> {

	public int compare(Node<? extends OWLEntity> e1, Node<? extends OWLEntity> e2) {
		return getRenderingAndClass(e1.getRepresentativeElement()).compareToIgnoreCase(getRenderingAndClass(e2.getRepresentativeElement()));
	}


	private String getRenderingAndClass(OWLEntity entity) {
		return ACETextManager.getRendering(entity) + "_" + entity.getClass();
	}
}