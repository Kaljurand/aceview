package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLAxiomAnnotationsFrame;
import org.protege.editor.owl.ui.framelist.OWLFrameList2;
import org.semanticweb.owl.model.OWLAxiom;

/**
 * <p>Based on the Protege AxiomAnnotationPanel, but does not
 * show the axiom rendering.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class AxiomAnnotationPanel extends JComponent {

	private final OWLFrameList2<OWLAxiom> axiomAnnotationComponent;

	public AxiomAnnotationPanel(OWLEditorKit eKit) {
		setLayout(new BorderLayout(6, 6));
		setPreferredSize(new Dimension(400, 300));

		axiomAnnotationComponent = new OWLFrameList2<OWLAxiom>(eKit, new OWLAxiomAnnotationsFrame(eKit));
		axiomAnnotationComponent.setAxiomSelectionSyncronized(false);

		add(new JScrollPane(axiomAnnotationComponent));

		setVisible(true);
	}


	public void setAxiom(OWLAxiom axiom) {
		axiomAnnotationComponent.setRootObject(axiom);
	}


	public void dispose() {
		axiomAnnotationComponent.dispose();
	}
}