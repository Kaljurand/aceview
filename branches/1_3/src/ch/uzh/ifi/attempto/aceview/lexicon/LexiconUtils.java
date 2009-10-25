package ch.uzh.ifi.attempto.aceview.lexicon;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class LexiconUtils {

	public static EntryType getLexiconEntryType(OWLEntity entity) {
		if (entity instanceof OWLClass) {
			return EntryType.CN;
		}
		else if (entity instanceof OWLObjectProperty || entity instanceof OWLDataProperty) {
			return EntryType.TV;
		}
		else if (entity instanceof OWLNamedIndividual) {
			return EntryType.PN;
		}
		return null;
	}


	/**
	 * <p>Returns an identifier that is constructed from the lexicon type and
	 * the name of the entity.
	 * This identifier makes a difference between punned entities.
	 * It is intended for the a-element in the HTML views
	 * where entities are used as links.</p>
	 * 
	 * TODO: instead of lexicon entry type, use the entity type (class, object property, ...)
	 * TODO: instead of toString() use getURI() to get a true identifier
	 * 
	 * @param entity OWL entity
	 * @return Identifier constructed from the entity type and entity name
	 */
	public static String getHrefId(OWLEntity entity) {
		EntryType type = LexiconUtils.getLexiconEntryType(entity);
		return type + ":" + entity.toString();
	}
}