package ch.uzh.ifi.attempto.aceview;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owl.model.OWLEntity;

import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;

public class WordsHyperlinkListener implements HyperlinkListener {
	private static final Logger logger = Logger.getLogger(WordsHyperlinkListener.class);
	private final OWLWorkspace ws;

	public WordsHyperlinkListener(OWLWorkspace ws) {
		this.ws = ws;
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String link = e.getDescription().substring(1);
			int sepIndex = link.indexOf(':');
			if (sepIndex != -1) {
				String typeAsString = link.substring(0, sepIndex);
				String lemma = link.substring(sepIndex + 1);
				EntryType type = EntryType.valueOf(typeAsString);
				logger.info("link = " + type + " " + lemma);
				OWLEntity entity = ACETextManager.findEntity(type, lemma);
				if (entity != null) {
					ws.getOWLSelectionModel().setSelectedEntity(entity);
				}
			}
		}
	}
}