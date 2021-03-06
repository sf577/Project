<?xml version="1.0"?>
<!-- 	
 Copyright (c) 2003-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
                                        
@ProposedRating Red (hyzheng)
@AcceptedRating Red (cxh)
	
This file strips away the redundant inputs or outputs generated by the global
and local variable preprocessors. 

Due to the algorithm in global variables and local variables preprocessors,
there are quite a few redundant input and output ports generated. See 
GlobalVariablePreprocessor.xsl and LocalVariablePreprocessor.xsl.

@author Haiyang Zheng
@version $Id: SlimPreprocessor.xsl,v 1.13 2006/08/21 23:16:39 cxh Exp $ 
@since HyVisual 2.2
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
			     xmlns:xalan="http://xml.apache.org/xslt" version="1.0">

    <xsl:output doctype-system="HSIF.dtd"/>
    <xsl:output method="xml" indent="yes"/>

    <!-- features of the XSLT 2.0 language -->
    <xsl:decimal-format name="comma" decimal-separator="," grouping-separator="."/>

    <!-- time function -->
    <xsl:variable name="now" xmlns:Date="/java.util.Date">
        <xsl:value-of select="Date:toString(Date:new())"/>
    </xsl:variable>

    <!-- configuration -->
    <xsl:param name="author">Ptolemy II</xsl:param>
    <xsl:preserve-space elements="*"/>

    <!-- ==========================================================
          root element
          ========================================================== -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- General-Copy which copies everything. -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
            </xsl:for-each>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- This template strips away redundant variables. -->
    <xsl:template match="IntegerVariable|RealVariable|BooleanVariable">
        <xsl:variable name="name" select="@name"/>
        <xsl:variable name="kind" select="@kind"/>
        <xsl:if test="not(preceding-sibling::IntegerVariable[@name=$name and @kind=$kind]|
                                  preceding-sibling::RealVariable[@name=$name and @kind=$kind]|
                                  preceding-sibling::BooleanVariable[@name=$name and @kind=$kind])">
            <xsl:copy>
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <!-- This template strips away redundant parameters. -->
    <xsl:template match="IntegerParameter|RealParameter|BooleanParameter">
        <xsl:variable name="name" select="@name"/>
        <xsl:if test="not(preceding-sibling::IntegerParameter[@name=$name]|
                            preceding-sibling::RealParameter[@name=$name]|
                            preceding-sibling::BooleanParameter[@name=$name])">
            <xsl:copy>
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name()}"><xsl:value-of select="."/></xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
