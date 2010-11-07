package ch.uzh.ifi.attempto.aceview.predicate;

import org.semanticweb.owlapi.model.OWLEntity;

import ch.uzh.ifi.attempto.aceview.ACEAnswer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;

import com.google.common.base.Predicate;

public class AnswerReferencesEntity implements Predicate<ACESnippet> {
	private final OWLEntity entity;

	public AnswerReferencesEntity(OWLEntity entity) {
		this.entity = entity;
	}

	public boolean apply(ACESnippet snippet) {
		if (snippet != null && snippet.isQuestion()) {
			ACEAnswer answer = ACETextManager.getActiveACEText().getAnswer(snippet);
			if (answer != null) {
				return answer.containsEntity(entity);
			}
		}
		return false;
	}
}