<xsl:stylesheet
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Converts OWL 1.1 (OWL-API 2) to OWL 2 (OWL-API 3) -->
	<!-- Very preliminary. -->

	<!--
	TODO: fix namespace handling, currently the default namespace assignment
	must be deleted from the document.
	-->

	<xsl:template match="Ontology">
		<xsl:element name="Ontology">
			<xsl:attribute name="ontologyIRI">
				<xsl:value-of select="@URI"/>
			</xsl:attribute>
			<xsl:for-each select="@*">
				<xsl:if test="name() != 'URI'">
					<xsl:copy/>
				</xsl:if>
			</xsl:for-each>

			<xsl:element name="Prefix">
				<xsl:attribute name="name">ace_lexicon</xsl:attribute>
				<xsl:attribute name="IRI">http://attempto.ifi.uzh.ch/ace_lexicon#</xsl:attribute>
			</xsl:element>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>


	<!-- Rewrite ACE annotations -->
	<!-- BUG: Maybe it can be done in a shorter way. -->
	<xsl:template match="EntityAnnotation">

		<xsl:variable name="word_class">
			<xsl:choose>
				<xsl:when test="Class">
					<xsl:text>CN</xsl:text>
				</xsl:when>
				<xsl:when test="ObjectProperty|DataProperty">
					<xsl:text>TV</xsl:text>
				</xsl:when>
				<xsl:when test="Individual">
					<xsl:text>PN</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>OTHER</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="iri">
			<xsl:choose>
				<xsl:when test="Class">
					<xsl:value-of select="Class/@URI"/>
				</xsl:when>
				<xsl:when test="ObjectProperty">
					<xsl:value-of select="ObjectProperty/@URI"/>
				</xsl:when>
				<xsl:when test="DataProperty">
					<xsl:value-of select="DataProperty/@URI"/>
				</xsl:when>
				<xsl:when test="Individual">
					<xsl:value-of select="Individual/@URI"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>OTHER</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="morph_class">
			<xsl:choose>
				<xsl:when test="Annotation/@annotationURI = 'http://attempto.ifi.uzh.ch/ace_lexicon#sg'">
					<xsl:text>ace_lexicon:</xsl:text>
					<xsl:value-of select="$word_class"/>
					<xsl:text>_sg</xsl:text>
				</xsl:when>
				<xsl:when test="Annotation/@annotationURI = 'http://attempto.ifi.uzh.ch/ace_lexicon#pl'">
					<xsl:text>ace_lexicon:</xsl:text>
					<xsl:value-of select="$word_class"/>
					<xsl:text>_pl</xsl:text>
				</xsl:when>
				<xsl:when test="Annotation/@annotationURI = 'http://attempto.ifi.uzh.ch/ace_lexicon#vbg'">
					<xsl:text>ace_lexicon:</xsl:text>
					<xsl:value-of select="$word_class"/>
					<xsl:text>_vbg</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>BUG</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>


		<xsl:element name="AnnotationAssertion">
			<xsl:element name="IRI">
				<xsl:value-of select="$iri"/>
			</xsl:element>
			<xsl:element name="AnnotationProperty">
				<xsl:attribute name="abbreviatedIRI">
					<xsl:value-of select="$morph_class"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="Literal">
				<xsl:value-of select="Annotation/Constant"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>


	<!-- Any other URI -->
	<xsl:template match="@URI">
		<xsl:attribute name="IRI">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>


	<!-- Individual -->
	<xsl:template match="Individual">
		<xsl:element name="NamedIndividual">
			<xsl:apply-templates select="@*"/>
		</xsl:element>
	</xsl:template>


	<!-- InverseObjectProperty -->
	<xsl:template match="InverseObjectProperty">
		<xsl:element name="ObjectInverseOf">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>


	<!-- ObjectExistsSelf -->
	<xsl:template match="ObjectExistsSelf">
		<xsl:element name="ObjectHasSelf">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>


	<!-- SubObjectPropertyChain -->
	<xsl:template match="SubObjectPropertyChain">
		<xsl:element name="ObjectPropertyChain">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>


	<!-- otherwise just copy -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
