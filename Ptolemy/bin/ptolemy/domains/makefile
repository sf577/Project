# Makefile for Java classes used to implement Ptolemy domains
#
# @Version: $Id: makefile,v 1.114.4.1 2006/10/27 16:03:40 cxh Exp $
# @Author: Christopher Hylands
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		ptolemy/domains

# Domains we do not ship
EXPERIMENTAL_DOMAINS = \
		hdf rtp 

# Domains, in alphabetical order.  If you change this, change
# PTCLASSALLJARS below
# DT has to come after SDF, sorry.
# PTPSDF_DIR is set by configure in $PTII/mk/ptII.mk to psdf if
#   $PTII/lib/mapss.jar was found
# PTJAVA3D_DIR is set by configure in $PTII/mk/ptII.mk to gr if
#   Java 3D was found
# PTTINYOS_DIR is set by configure in $PTII/mk/ptII.mk to ptinyos if
#   TinyOS is found. 
# PTCONTINUOUS_DIR is set to continuous if we want to compile that domain.
PTCONTINUOUS_DIR =
DOMAINS =	ci $(PTCONTINUOUS_DIR) csp ct dde ddf de fsm pn sdf dt \
	    giotto petrinet rendezvous \
		$(PTPSDF_DIR) $(PTTINYOS_DIR) \
		sr tm $(PTJAVA3D_DIR) wireless

DIRS =		$(DOMAINS) $(EXPERIMENTAL_DOMAINS)

# Root of Ptolemy II directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	domains
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include these jars in PTAUXALLJAR
# Don't include rtp/rtp.jar, it is not being shipped

# PTGRDOMAIN_JAR is the name of the jar file that includes the GR
# domain if Java 3D was found.  PTGRDOMAIN_JAR is set by configure.

PTAUXALLJARS = \
		ci/ci.jar \
		dde/dde.jar \
		ddf/ddf.jar \
	        dt/dt.jar \
		giotto/giotto.jar \
		$(PTGRDOMAIN_JAR) \
		hdf/hdf.jar \
		petrinet/petrinet.jar \
		tm/tm.jar \
		sr/sr.jar \
		wireless/wireless.jar

# Are we shipping these domains?
#		ptinyos/ptinyos.jar 
#		pbo/pbo.jar
#		petrinet/petrinet.jar

# Experimental domains 
PTAUXALLJAR =	experimentalDomains.jar

# Include the .class files from these jars in PTCLASSALLJAR
# If you change this, change DIRS above
PTCLASSALLJARS = \
		csp/csp.jar \
		ct/ct.jar \
		de/de.jar \
		fsm/fsm.jar \
		pn/pn.jar \
		rendezvous/rendezvous.jar \
		sdf/sdf.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	domains.xml \
	ptinyDomains.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
MISC_FILES =	$(DOMAINS) test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(EXPERIMENTAL_DOMAINS) \
	$(OTHER_FILES_TO_BE_JARED) \
	dfm \
	generic \
	gr \
	ptinyos

all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
