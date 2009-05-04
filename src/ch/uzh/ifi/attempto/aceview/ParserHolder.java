/*
 * This file is part of ACE View.
 * Copyright 2008, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

package ch.uzh.ifi.attempto.aceview;

import ch.uzh.ifi.attempto.ape.ACEParser;
import ch.uzh.ifi.attempto.ape.APELocal;
import ch.uzh.ifi.attempto.ape.APESocket;
import ch.uzh.ifi.attempto.ape.APEWebservice;

public enum ParserHolder {

	INSTANCE;

	private ACEParser aceParser;


	/**
	 * <p>Returns the concrete ACE parser
	 * (either <code>APELocal</code>, <code>APESocket</code>, or <code>APEWebservice</code>)
	 * that the user has selected in the Protege Preferences-menu.</p>
	 * 
	 * @return ACE parser that is selected in preferences
	 */
	public static ACEParser getACEParser() {
		if (INSTANCE.aceParser == null) {
			updateACEParser(ACEViewPreferences.getInstance());
		}
		return INSTANCE.aceParser;
	}


	/**
	 * <p>Updates the ACE parser based on the given ACE preferences.</p>
	 * 
	 * @param prefs ACE preferences
	 */
	public static void updateACEParser(ACEViewPreferences prefs) {
		String serviceType = prefs.getAceToOwl();
		if (serviceType.equals("APE Local")) {
			// BUG: experimental. If ACE Parser has been instantiated as APE Local then we won't
			// create a new parser, as we would get a runtime exception.
			if (APELocal.isInitialized()) {
				// TODO: Inform the user that we keep using the old APELocal parser
			}
			else {
				//INSTANCE.aceParser = new APELocal(prefs.getSwiPath(), prefs.getApePath());
				APELocal.init(prefs.getApePath());
				INSTANCE.aceParser = APELocal.getInstance();
			}
		}
		else if (serviceType.equals("APE Socket")) {
			INSTANCE.aceParser = new APESocket(prefs.getAceToOwlSocketHost(), prefs.getAceToOwlSocketPort());
		}
		else if (serviceType.equals("APE Webservice")) {
			INSTANCE.aceParser = new APEWebservice(prefs.getAceToOwlWebserviceUrl());
		}

		INSTANCE.aceParser.setGuessingEnabled(prefs.isGuessingEnabled());
		INSTANCE.aceParser.setClexEnabled(prefs.isClexEnabled());
	}
}