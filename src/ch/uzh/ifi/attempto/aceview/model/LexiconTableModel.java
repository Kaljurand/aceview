/*
 * This file is part of ACE View.
 * Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

package ch.uzh.ifi.attempto.aceview.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.AddAxiomByACEView;
import ch.uzh.ifi.attempto.aceview.RemoveAxiomByACEView;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.lexicon.FieldType;
import ch.uzh.ifi.attempto.aceview.lexicon.MorphType;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextChangeEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACETextManagerListener;
import ch.uzh.ifi.attempto.aceview.model.event.EventType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;


/**
 * <p>Lexicon table model provides viewing and editing the ACE lexicon as a table.</p>
 * <p>There are 7 columns:</p>
 * 
 * <ul>
 * <li>IRI</li>
 * <li>IRI rendering</li>
 * <li>type (CN, TV, PN)</li>
 * <li>sg</li>
 * <li>pl (in case of CN and TV)</li>
 * <li>vbg (in case of TV)</li>
 * <li>frequency</li>
 * </ul>
 * 
 * <p>Only the surface form fields
 * (<code>sg</code>, <code>pl</code>, <code>vbg</code>) are editable.</p>
 * 
 * @author Kaarel Kaljurand
 *
 */
public class LexiconTableModel extends AbstractTableModel {

	private ACEText<OWLEntity, OWLLogicalAxiom> acetext;
	private TokenMapper acelexicon;
	private Object[] entityArray;
	private static final Logger logger = Logger.getLogger(LexiconTableModel.class);

	private ACETextManagerListener aceTextManagerListener = new ACETextManagerListener() {
		public void handleChange(ACETextChangeEvent event) {			
			if (event.isType(EventType.ACELEXICON_CHANGED) ||
					event.isType(EventType.ACTIVE_ACETEXT_CHANGED)) {
				acetext = ACETextManager.getActiveACEText();
				acelexicon = acetext.getTokenMapper();
				entityArray = ACETextManager.getOWLModelManager().getActiveOntology().getReferencedEntities().toArray();
				fireTableDataChanged();
			}
		}
	};


	public enum Column implements TableColumn {
		ENTITY("Entity", null, false, IRI.class),
		ENTITY_RENDERING("Entity rendering", null, true, String.class),
		TYPE("Type", null, true, String.class),
		SG("Singular", null, true, String.class),
		PL("Plural", null, true, String.class),
		VBG("P. participle", null, true, String.class),
		FREQUENCY("Frequency", null, true, Integer.class);

		private final String name;
		private final String abbr;
		private final Class<?> dataClass;
		private final boolean isVisible;

		private Column(String name, String abbr, boolean isVisible, Class<?> dataClass) {
			this.name = name;
			this.abbr = abbr;
			this.isVisible = isVisible;
			this.dataClass = dataClass;
		}

		public String getName() {
			return name;
		}

		public String getAbbr() {
			return abbr;
		}

		public Class<?> getDataClass() {
			return dataClass;
		}

		public boolean isVisible() {
			return isVisible;
		}
	}

	public LexiconTableModel() {
		acetext = ACETextManager.getActiveACEText();
		acelexicon = acetext.getTokenMapper();
		entityArray = ACETextManager.getOWLModelManager().getActiveOntology().getReferencedEntities().toArray();
		ACETextManager.addListener(aceTextManagerListener);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return Column.values()[column].getDataClass();
	}

	public int getColumnCount() {
		return Column.values().length;
	}

	public int getRowCount() {
		if (entityArray == null) {
			return 0;
		}
		return entityArray.length;
	}

	@Override
	public String getColumnName(int column) {
		return Column.values()[column].getName();
	}

	public Object getValueAt(int row, int column) {
		if (row >= 0 && row < entityArray.length) {
			OWLEntity entity = (OWLEntity) entityArray[row];
			IRI entityIRI = entity.getIRI();
			EntryType entryType = LexiconUtils.getLexiconEntryType(entity);

			switch (Column.values()[column]) {
			case ENTITY:
				return entity;
			case ENTITY_RENDERING:
				return ACETextManager.getOWLModelManager().getRendering(entity);
			case TYPE:
				if (entryType == null) {
					return "";
				}
				return entryType;
			case SG:
				return process(entityIRI, entryType, FieldType.SG);
			case PL:
				return process(entityIRI, entryType, FieldType.PL);
			case VBG:
				return process(entityIRI, entryType, FieldType.VBG);
			case FREQUENCY:
				return acetext.getSnippetCount(entity);
			default:
				throw new RuntimeException("Programmer error: missed a case for: " + column);
			}
		}
		// TODO: throw exception instead
		return "NULL";
	}


	/**
	 * The delete/add is a single change i.e. one UNDO would suffice to restore
	 * the original state.
	 * 
	 * TODO: This code is buggy as it accesses the LexiconField enum
	 */
	@Override
	public void setValueAt(Object newValue, int row, int column) {
		String newValueAsString = (String) newValue;
		String oldValueAsString = (String) getValueAt(row, column);
		if (newValueAsString.equals(oldValueAsString)) {
			logger.info("No change");
		}
		else {
			logger.info("Changing: " + oldValueAsString + " -> " + newValueAsString);
			OWLEntity entity = (OWLEntity) entityArray[row];
			if (entity != null) {
				OWLModelManager mm = ACETextManager.getOWLModelManager();
				OWLOntology ont = mm.getActiveOntology();
				// TODO: BUG: This way of finding the URI is waiting to be broken.
				FieldType fieldType = FieldType.values()[column - 3];
				List<OWLAxiomChange> changes = Lists.newArrayList();

				OWLDataFactory df = mm.getOWLDataFactory();

				EntryType entryType = LexiconUtils.getLexiconEntryType(entity);
				MorphType morphType = MorphType.getMorphType(entryType, fieldType);

				if (entryType != null && morphType != null) {

					// Remove the respective annotation (if present)
					// TODO: we construct a new axiom just to use it to match an axiom
					// to be removed, is is smart? Maybe we should search for it in the ontology?
					OWLAnnotationAssertionAxiom oldAnnot = OntologyUtils.createEntityAnnotationAxiom(df, morphType.getIRI(), entity, oldValueAsString);
					changes.add(new RemoveAxiomByACEView(ont, oldAnnot));

					// We add a new annotation only if the modification of the table cell is
					// a non-empty string.
					if (newValueAsString.length() > 0) {
						OWLAnnotationAssertionAxiom newAnnot = OntologyUtils.createEntityAnnotationAxiom(df, morphType.getIRI(), entity, newValueAsString);
						changes.add(new AddAxiomByACEView(ont, newAnnot));
					}

					OntologyUtils.changeOntology(mm.getOWLOntologyManager(), changes);
					fireTableCellUpdated(row, column);
				}
			}
		}
	}


	/**
	 * TODO: improve the readability, e.g. try to use the enum for column testing
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		EntryType entryType = LexiconUtils.getLexiconEntryType((OWLEntity) entityArray[row]);
		if (entryType == null) {
			return false;
		}
		switch (entryType) {
		case CN:
			return (column == 3 || column == 4);
		case TV:
			return (column == 3 || column == 4 || column == 5);
		case PN:
			return (column == 3);
		default:
			return false;
		}
	}


	public OWLEntity getEntity(int row) {
		return (OWLEntity) entityArray[row];
	}


	public void dispose() {
		ACETextManager.removeListener(aceTextManagerListener);
	}


	private String process(IRI entityIRI, EntryType entryType, FieldType fieldType) {
		// logger.info("Table cell: " + entityIRI + " -> " + entryType + " -> " + fieldType);
		MorphType morphType = MorphType.getMorphType(entryType, fieldType);
		if (morphType == null) {
			return "";
		}
		String wordfrom = acelexicon.getWordform(entityIRI, morphType.getIRI());
		if (wordfrom == null) {
			return "";
		}
		return wordfrom;
	}
}