package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import ch.uzh.ifi.attempto.aceview.ACETextManager;

/**
 * 
 * @author Kaarel Kaljurand
 *
 */
public class AxiomLinkAction extends AbstractHyperlinkAction {

	private final OWLWorkspace ws;
	private final OWLLogicalAxiom axiom;
	private final OWLEntity entity;

	public AxiomLinkAction(OWLWorkspace ws, OWLEntity entity, OWLLogicalAxiom axiom) {
		this.ws = ws;
		this.axiom = axiom;
		this.entity = entity;
		super.putValue(Action.NAME, ACETextManager.getOWLModelManager().getRendering(entity));
		super.putValue(Action.SHORT_DESCRIPTION, "" + entity.getIRI());
	}


	public void actionPerformed(ActionEvent e) {
		if (entity != null && axiom != null) {
			ws.getOWLSelectionModel().setSelectedEntity(entity);
			try {
				ACETextManager.setSelectedSnippet(axiom);
				setVisited(true);
			} catch (OWLRendererException e1) {
				e1.printStackTrace();
			} catch (OWLOntologyCreationException e1) {
				e1.printStackTrace();
			} catch (OWLOntologyChangeException e1) {
				e1.printStackTrace();
			}
		}
	}
}