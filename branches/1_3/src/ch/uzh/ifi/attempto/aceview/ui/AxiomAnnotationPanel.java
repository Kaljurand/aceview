package ch.uzh.ifi.attempto.aceview.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.util.OWLAxiomInstance;

/**
 * <p>Based on the Protege AxiomAnnotationPanel, but does not
 * show the axiom rendering.</p>
 * 
 * @author Kaarel Kaljurand
 */
public class AxiomAnnotationPanel extends JComponent {

	private AxiomAnnotationsList annotationsComponent;

	public AxiomAnnotationPanel(OWLEditorKit eKit) {
		setLayout(new BorderLayout(6, 6));
		setPreferredSize(new Dimension(400, 300));		
		annotationsComponent = new AxiomAnnotationsList(eKit);
		add(new JScrollPane(annotationsComponent));
		setVisible(true);
	}


	/**
	 * <p>Annotations apply to a particular instance of an axiom, i.e.
	 * the axiom must be qualified by its containing ontology.</p>
	 * 
	 * @param axiomInstance Instance of the axiom
	 */
	public void setAxiomInstance(OWLAxiomInstance axiomInstance) {
		if (axiomInstance != null){
			annotationsComponent.setRootObject(axiomInstance);
		}
		else{
			annotationsComponent.setRootObject(null);
		}
	}


	public OWLAxiomInstance getAxiom() {
		return annotationsComponent.getRoot();
	}


	public void dispose() {
		annotationsComponent.dispose();
	}
}