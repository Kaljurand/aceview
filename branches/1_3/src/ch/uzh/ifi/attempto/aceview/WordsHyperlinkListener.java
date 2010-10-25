package ch.uzh.ifi.attempto.aceview;


import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;

public class WordsHyperlinkListener implements HyperlinkListener {
	private final OWLWorkspace ws;

	public WordsHyperlinkListener(OWLWorkspace ws) {
		this.ws = ws;
	}


	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String link = e.getDescription().substring(1);
			String decodedLink = LexiconUtils.decodeHrefLink(link);
			int sepIndex = decodedLink.indexOf(':');
			if (sepIndex != -1) {
				String typeAsString = decodedLink.substring(0, sepIndex);
				EntryType type = EntryType.valueOf(typeAsString);

				String iriAsString = decodedLink.substring(sepIndex + 1);
				IRI iri = IRI.create(iriAsString);
				OWLEntity entity = ACETextManager.findEntity(type, iri);
				if (entity != null) {
					ws.getOWLSelectionModel().setSelectedEntity(entity);
				}
			}
		}
	}
}