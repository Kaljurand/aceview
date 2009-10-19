package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.util.OWLAxiomInstance;
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
	private final OWLAxiomAnnotationsFrame annotationsFrame;

	public AxiomAnnotationPanel(OWLEditorKit eKit) {
		setLayout(new BorderLayout(6, 6));
		setPreferredSize(new Dimension(400, 300));

		annotationsFrame = new OWLAxiomAnnotationsFrame(eKit);
		axiomAnnotationComponent = new OWLFrameList2<OWLAxiom>(eKit, annotationsFrame);
		axiomAnnotationComponent.setAxiomSelectionSyncronized(false);

		add(new JScrollPane(axiomAnnotationComponent));
		setVisible(true);
	}


	/**
	 * <p>Annotations apply to a particular instance of an axiom, i.e.
	 * the axiom must be qualified by its containing ontology.</p>
	 * 
	 * @param axiomInstance Instance of the axiom
	 */
	public void setAxiom(OWLAxiomInstance axiomInstance) {
		if (axiomInstance != null) {
			axiomAnnotationComponent.setRootObject(axiomInstance.getAxiom());
			annotationsFrame.setContainingOntology(axiomInstance.getOntology());
		}
		else{
			axiomAnnotationComponent.setRootObject(null);
		}
	}


	public void dispose() {
		axiomAnnotationComponent.dispose();
	}
}