# Makefile for helper classes that match ptolemy.actor.lib classes
#
# @Version: $Id: makefile,v 1.36.2.1 2007/01/06 06:31:49 cxh Exp $
# @Author: Christopher Brooks
#
# @Copyright (c) 2005-2006 The Regents of the University of California
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
# LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
# OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
# ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
# RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA  AND RESEARCH IN MOTION LIMITED
# HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		ptolemy/codegen/c/actor/lib

DIRS =		comm conversions gui hoc io javasound logic string test

# Root of the Java directory
ROOT =		../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTCLASSJAR =

PTCLASSALLJAR = $(PTPACKAGE).jar

#Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	comm/comm.jar \
	conversions/conversions.jar \
	gui/gui.jar \
	hoc/hoc.jar \
	io/io.jar \
	javasound/javasound.jar \
	logic/logic.jar \
	string/string.jar

# Keep this list alphabetized.
JSRCS = \
	AbsoluteValue.java \
	Accumulator.java \
	AddSubtract.java \
	ArrayAppend.java \
	ArrayAverage.java \
	ArrayElement.java \
	ArrayExtract.java \
	ArrayLength.java \
	ArrayLevelCrossing.java \
	ArrayMaximum.java \
	ArrayMinimum.java \
	ArrayToElements.java \
	Average.java \
	Bernoulli.java \
	BooleanMultiplexor.java \
	BooleanSelect.java \
	BooleanSwitch.java \
	Commutator.java \
	Const.java \
	Counter.java \
	Differential.java \
	Discard.java \
	DiscreteRandomSource.java \
	Distributor.java \
	ElementsToArray.java \
	Exit.java \
	Expression.java \
	Gaussian.java \
	GradientAdaptiveLattice.java \
	Limiter.java \
	LookupTable.java \
	Maximum.java \
	Minimum.java \
	MonitorValue.java \
	MultiplyDivide.java \
	Pulse.java \
	Quantizer.java \
	Ramp.java \
	RandomSource.java \
	Remainder.java \
	Rician.java \
	Scale.java \
	Sequence.java \
	Sink.java \
	Sleep.java \
	Stop.java \
	StringConst.java \
	Synchronizer.java \
	Test.java \
	ThrowException.java \
	ThrowModelError.java \
	TrigFunction.java \
	UnaryMathFunction.java \
	Uniform.java

OTHER_FILES_TO_BE_JARED = \
	AbsoluteValue.c \
	Accumulator.c \
	AddSubtract.c \
	ArrayAppend.c \
	ArrayAverage.c \
	ArrayElement.c \
	ArrayExtract.c \
	ArrayLength.c \
	ArrayLevelCrossing.c \
	ArrayMaximum.c \
	ArrayMinimum.c \
	ArrayToElements.c \
	Average.c \
	Bernoulli.c \
	BooleanMultiplexor.c \
	BooleanSelect.c \
	BooleanSwitch.c \
	Commutator.c \
	Const.c \
	Counter.c \
	Differential.c \
	DiscreteRandomSource.c \
	Distributor.c \
	ElementsToArray.c \
	ElementsToArray_new.c \
	Exit.c \
	Expression.c \
	Gaussian.c \
	GradientAdaptiveLattice.c \
	Limiter.c \
	LookupTable.c \
	Maximum.c \
	Minimum.c \
	MonitorValue.c \
	MultiplyDivide.c \
	Pulse.c \
	Quantizer.c \
	Ramp.c \
	RandomSource.c \
	Remainder.c \
	Rician.c \
	Scale.c \
	Sequence.c \
	Sink.c \
	Sleep.c \
	Stop.c \
	StringConst.c \
	Synchronizer.c \
	Test.c \
	ThrowException.c \
	ThrowModelError.c \
	TrigFunction.c \
	UnaryMathFunction.c \
	Uniform.c \
	aggregators.xml \
	array.xml \
	booleanflowcontrol.xml \
	executioncontrol.xml \
	filtering.xml \
	flowcontrol.xml \
	genericsinks.xml \
	genericsources.xml \
	math.xml \
	random.xml \
	sequencesinks.xml \
	sequencesources.xml \
	signalprocessing.xml \
	sinks.xml \
	sources.xml \
	test.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED) \
	checkGreen

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = comm conversions gui hoc io javasound logic string test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc \
	'Expression$$VariableScope.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: all jars $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
