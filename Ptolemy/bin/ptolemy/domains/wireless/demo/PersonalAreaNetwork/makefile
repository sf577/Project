# Makefile for Ptolemy II PersonalAreaNetwork demo
#
# @Authors: Christopher Hylands Brooks (makefile only)
#
# @Version: $Id: makefile,v 1.7 2005/04/05 06:56:27 cxh Exp $
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

ME =		ptolemy/domains/wireless/demo/PersonalAreaNetwork

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	PersonalAreaNetwork
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	BluetoothChannel.java \
	BluetoothNode.java

OTHER_FILES_TO_BE_JARED = \
	Bluetooth2devices.xml \
	BluetoothNodes.xml \
	PersonalAreaNetwork.xml \
	PersonalAreaNetwork2.xml

EXTRA_SRCS = \
	$(JSRCS) \
	$(OTHER_FILES_TO_BE_JARED) \
	PersonalAreaNetwork.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make demos' to run the tests.
all: jclass $(PTCLASSJAR)
	@echo "To run all the demos, run 'make demo'"

install: all

# Run the demo in all its different formats
# Run demo_application first so that we generate PersonalAreaNetwork.xml
demo: demo_vergil

#demo_applet: $(PTCLASSJAR)
#	CLASSPATH="$(CLASSPATH)" appletviewer PersonalAreaNetwork.htm

demo_vergil:
	"$(PTII)/bin/vergil" PersonalAreaNetwork.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
