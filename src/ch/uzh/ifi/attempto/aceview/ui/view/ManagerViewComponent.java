package ch.uzh.ifi.attempto.aceview.ui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owl.model.OWLOntology;

import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;


public class ManagerViewComponent extends AbstractOWLViewComponent {

	private JTextArea textarea;

	@Override
	protected void disposeOWLView() {
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		JButton button = ComponentFactory.makeButton("Refresh");

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				refresh();
			}
		});

		textarea = new JTextArea();

		setLayout(new BorderLayout());
		add(new JScrollPane(textarea), BorderLayout.CENTER);
		add(button, BorderLayout.SOUTH);
		refresh();
	}


	private void refresh() {
		OWLModelManager mm = getOWLModelManager();

		textarea.setText("");
		for (OWLOntology ont : mm.getOntologies()) {
			textarea.append(ont.toString());
			textarea.append("\n");
		}
	}
}