# Makefile for the Java classes used to test the polymorphic actors
#
# @Authors: Christopher Hylands
#
# @Version: $Id: makefile,v 1.59.4.1 2007/01/11 23:40:20 cxh Exp $
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

# Location of this directory, relative to the Ptolemy II directory
ME =		ptolemy/actor/lib/test

DIRS =		performance 

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS = \
	SleepFireTwice.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	AddSubtract.tcl \
	AbsoluteValue.tcl \
	Average.tcl \
	Bernoulli.tcl \
	Clock.tcl \
	Commutator.tcl \
	Const.tcl \
	CurrentTime.tcl \
	DiscreteRandomSource.tcl \
	Distributor.tcl \
	DB.tcl \
	Exit.tcl \
	Expression.tcl \
	Gaussian.tcl \
	Interpolator.tcl \
	MathFunction.tcl \
	Maximum.tcl \
	Minimum.tcl \
	MultiplyDivide.tcl \
	OrderedMerge.tcl \
	Pulse.tcl \
	Ramp.tcl \
	RecordAssembler.tcl \
	RecordDisassembler.tcl \
	Sinewave.tcl \
	Scale.tcl \
	Test.tcl \
	VariableClock.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) \
	Exit.xml \
	FileWriter.xml \
	subaggtest.xml \
	#$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl \
	auto \
	performance

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl \
		cleanFileWriter cleanRunCompositeActor test_auto
	$(JTCLSH) alljtests.tcl

# auto/FileWriter1.xml creates this file, so we remove it before running
# the tests
# auto/FileWriter1.xml creates this file, so we remove it before running
# the tests
cleanFileWriter:
	echo 'if [catch {file delete -force [file join [java::call System getProperty java.io.tmpdir] FileWriter1Output.txt]} errMsg ] {puts $$errMsg}; exit ' | $(JTCLSH)

cleanRunCompositeActor:
	echo 'if [catch {file delete -force [file join [java::call System getProperty java.io.tmpdir] RunCompositeActor.txt]} errMsg ] {puts $$errMsg}; exit ' | $(JTCLSH)

# 'make clean' removes $(KRUFT)
KRUFT = Const.txt ReadFile1Output.txt

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
