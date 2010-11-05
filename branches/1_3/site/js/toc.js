/**
* This code does two things:
* 1. numbers the sections (i.e. puts a number in front of the content of the elements h1, h2, ..., h6)
* 2. generates a table of contents into an element with an ID "toc"
*
* @author Kaarel Kaljurand
* @version 2009-09-30
*
* Changelog:
*
* 2009-09-30: Some bug fixes
* 2008-03-12: TOC now contains links to section headings
* 2007-01-17: Minor fixes and comments changed into English
* 2006-07-07: TOC support added
* 2002-07-09: First version
*/

function toc(firstNumber, makeToc) {
	var tased = new Array("H2", "H3", "H4", "H5", "H6");
	var pead = new Array(5);

	pead[0] = firstNumber - 1;
	pead[1] = pead[2] = pead[3] = pead[4] = 0;

	tased["H2"] = 0;
	tased["H3"] = 1;
	tased["H4"] = 2;
	tased["H5"] = 3;
	tased["H6"] = 4;

	// we start the search from "body"
	var somebody = document.getElementsByTagName("body").item(0);
	if (somebody == null) {
		alert("error: nobody found");
		exit;
	}

	if (makeToc) {
		var tocHolder = document.getElementById("toc");

		if (tocHolder == null) {
			tocHolder = document.createElement("pre");
			tocHolder.setAttribute("id", "toc");
			var position = somebody.getElementsByTagName("h2").item(0);
			if (position == null) {
				position = somebody.firstChild;
			}
			somebody.insertBefore(tocHolder, position);
		}
	}

	var tocContent = "";
	// we only consider the direct childern of "body"
	for(var i = 0; i < somebody.childNodes.length; i++) {

		//this doesn't work in Mozilla???: var t = somebody.childNodes[i].tagName;
		var t = somebody.childNodes[i].nodeName;

		// BUG: just in case browsers differ (at least they used to in 2002)
		var s = t.toUpperCase();

		if(s == "H2" || s == "H3" || s == "H4" || s == "H5" || s == "H6") {
			tase = tased[s];
			pead[tase]++; // increase the counter

			// set all the next levels to zero
			for(j = tase + 1; j < pead.length; j++) {
				pead[j] = 0;
			}

			// create a string
			number = pead[0];

			for(j = 1; (pead[j] != 0) && (j < pead.length); j++) {
				number += "." + pead[j];
			}

			// We use "\r\n" to make IE happy.
			if (makeToc) {
				var tocEntry = somebody.childNodes[i].innerHTML;
				tocContent += number + "  " + "<a href='#" + number + "'>" + tocEntry + "</a><br/>";
			}

			var title = somebody.childNodes[i].innerHTML;
			somebody.childNodes[i].innerHTML = number + " <a name='" + number + "'>" + title + "</a>";
		}
	}

	if (makeToc) {
		tocHolder.innerHTML = tocContent;
	}
}
