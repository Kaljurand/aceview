package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owl.model.OWLEntity;

import ch.uzh.ifi.attempto.aceview.ACETextManager;

public class EntityLinkAction extends AbstractHyperlinkAction {

	private final OWLWorkspace ws;
	private final OWLEntity entity;

	public EntityLinkAction(OWLWorkspace ws, OWLEntity entity) {
		this.ws = ws;
		this.entity = entity;
		super.putValue(Action.NAME, ACETextManager.getOWLModelManager().getRendering(entity));
		super.putValue(Action.SHORT_DESCRIPTION, "" + entity.getURI());
	}

	public void actionPerformed(ActionEvent e) {
		if (entity != null) {
			ws.getOWLSelectionModel().setSelectedEntity(entity);
			setVisited(true);
		}
	}
}