# Makefile for PtolemyII TM demos
#
# @Version: $Id: makefile,v 1.5 2005/02/28 19:23:48 cxh Exp $
# @Author: Christopher Hylands (makefile only)
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

ME =		ptolemy/domains/tm/demo

# Keep these alphabetical please.
DIRS =		Controllers FFT MergedEvents SimpleTasks

# Root of Ptolemy II directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


# Used to build jar files
PTPACKAGE = 	demo
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	Controllers/Controllers.jar \
	FFT/FFT.jar \
	MergedEvents/MergedEvents.jar \
	SimpleTasks/SimpleTasks.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 	$(DIRS) \
	index.htm

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

all: jclass
	@echo "To run all the demos, run 'make demo'"

install: jclass jars

demo: subdemo

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
