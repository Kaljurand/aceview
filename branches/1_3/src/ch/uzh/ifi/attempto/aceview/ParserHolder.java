/*
 * This file is part of ACE View.
 * Copyright 2008-2011, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
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

	private String apePath;


	/**
	 * <p>Returns the concrete ACE parser
	 * (either <code>APELocal</code>, <code>APESocket</code>, or <code>APEWebservice</code>)
	 * that the user has selected in the Protege Preferences-menu. In case
	 * the parser has not been selected then <code>null</code> is returned.</p>
	 * 
	 * @return ACE parser that is selected in preferences
	 */
	public static ACEParser getACEParser() {
		return INSTANCE.aceParser;
	}


	/**
	 * <p>Updates the ACE parser based on the given ACE preferences.</p>
	 * 
	 * <p>Note that we cannot reinitialize APE Local, as we would get a runtime exception.
	 * If the user changes the path to APE, then a restart is required.</p>
	 * 
	 * @param prefs ACE preferences
	 * @throws Exception if Protege needs to be restarted for the update to take effect
	 */
	public static void updateACEParser(ACEViewPreferences prefs) throws Exception {
		String serviceType = prefs.getAceToOwl();
		if (serviceType.equals("APE Local")) {
			String apePath = prefs.getApePath();
			if (INSTANCE.apePath == null || ! INSTANCE.apePath.equals(apePath)) {
				if (APELocal.isInitialized()) {
					throw new Exception("Restart Protege for this change to take effect.");
				}
				INSTANCE.apePath = apePath;
				APELocal.init(apePath);
				INSTANCE.aceParser = APELocal.getInstance();
			}
		}
		else if (serviceType.equals("APE Socket")) {
			INSTANCE.aceParser = new APESocket(prefs.getAceToOwlSocketHost(), prefs.getAceToOwlSocketPort());
		}
		else if (serviceType.equals("APE Webservice")) {
			INSTANCE.aceParser = new APEWebservice(prefs.getAceToOwlWebserviceUrl());
		}

		INSTANCE.aceParser.setGuessingEnabled(false);
		INSTANCE.aceParser.setClexEnabled(false);
	}
}