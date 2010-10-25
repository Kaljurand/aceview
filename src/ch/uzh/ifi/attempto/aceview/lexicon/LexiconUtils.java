package ch.uzh.ifi.attempto.aceview.lexicon;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.semanticweb.owlapi.model.IRI;
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
	 * 
	 * @param entity OWL entity
	 * @return Identifier constructed from the entity type and entity name
	 */
	public static String getHrefId(OWLEntity entity) {
		EntryType type = LexiconUtils.getLexiconEntryType(entity);
		return getHrefId(type, entity.getIRI());
	}


	/**
	 * 
	 * @param type
	 * @param iri
	 * @return
	 */
	public static String getHrefId(EntryType type, IRI iri) {
		try {
			return URLEncoder.encode(type + ":" + iri, "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		// This can never happen
		return null;
	}


	public static String decodeHrefLink(String link) {
		try {
			return URLDecoder.decode(link, "UTF-8");
		} catch (UnsupportedEncodingException exception) {}
		// This can never happen
		return null;
	}
}