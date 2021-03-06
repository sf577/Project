# Tests for the CIReceiver class
#
# @Author: Based on SDFReceiver by Brian K. Vogel
#
# @Version: $Id: CIReceiver.tcl,v 1.5 2005/02/28 19:42:13 cxh Exp $
#
# @Copyright (c) 2002-2005 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# FIXME: Add constructor tests.

######################################################################
####
#
test CIReceiver-1.1 {Check constructor and initial state} {
    set d [java::new ptolemy.domains.ci.kernel.CIDirector]
    set r [java::new ptolemy.domains.ci.kernel.CIReceiver $d]
    list [$r hasRoom] [$r hasToken]
} {1 0}

######################################################################
####
#
test CIReceiver-2.1 {Check put and get tokens} {
    set e [java::new ptolemy.actor.TypedCompositeActor]
    set d [java::new ptolemy.domains.ci.kernel.CIDirector $e "d"]
    set a1 [java::new ptolemy.actor.lib.Ramp $e "ramp"]
    set a2 [java::new ptolemy.actor.lib.Discard $e "display"]
    set inp [java::cast ptolemy.actor.IOPort [$a2 getPort "input"]]
    set outp [java::cast ptolemy.actor.IOPort [$a1 getPort "output"]]
    $e connect $outp $inp
    set p [java::new ptolemy.data.expr.Parameter $inp "p"]
    $d preinitialize
    $e resolveTypes $e
    $outp broadcast [java::new ptolemy.data.IntToken 1]
    list [[$inp get 0] toString]
} {1}

