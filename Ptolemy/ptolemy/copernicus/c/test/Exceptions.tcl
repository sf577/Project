# Tests Copernicus C Code generation for Exception functionality.
# Replaces the following tests:
# BasicException.java
# GlobalExceptions.java
# InheritedException.java
# NestedExceptions.java
#
#
# @Author: Ankush Varma
#
# @Version: $Id: Exceptions.tcl,v 1.9 2005/07/06 19:09:11 cxh Exp $
#
# @Copyright (c) 2000-2005 The Regents of the University of California.
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
#######################################################################

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more
# information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkClassPathSeparator] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#

test Exceptions-1.1 {Generate all required files for Exceptions.java} {

    set className Exceptions
    set outputDir testOutput/$className
    set output [generateC $className]
    
    # Move all extra files.
    cd ../..
    foreach i "[glob {*Exception*.[cho]}] [glob {*?Exception?*.class}]" {
        file rename -force $i $outputDir
    }

    # Turn newlines into spaces.
    regsub -all "\n" $output " " output
    regsub -all "" $output "" output
    
    # Check if the output is correct.
    set template "0 10 20 1 2 3 4 0 10 20 Caught First Exception. Caught Second Exception."
    
    # Test output
    string first $template $output
  
} {0}

