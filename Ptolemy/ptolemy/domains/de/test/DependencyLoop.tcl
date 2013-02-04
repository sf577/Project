# Tests for finding dependency loops
#
# @Author: Steve Neuendorffer and Haiyang Zheng
#
# @Version: $Id: DependencyLoop.tcl,v 1.3 2006/08/21 23:14:52 cxh Exp $
#
# @Copyright (c) 2004-2006 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test DependencyLoop-1.0 {} {
    catch {createAndExecute "auto/knownFailedTests/dependencyLoop.xml"} foo
    list $foo
} {{ptolemy.kernel.util.IllegalActionException: Found zero delay loop including: .dependencyLoop.original, .dependencyLoop.original, .dependencyLoop.opposite, .dependencyLoop.opposite, .dependencyLoop.Merge, .dependencyLoop.Merge
  in .dependencyLoop}}

test DependencyLoop-2.0 {} {
    catch {createAndExecute "auto/knownFailedTests/dependencyLoop2.xml"} foo
    list $foo
} {{ptolemy.kernel.util.IllegalActionException: Found zero delay loop including: .dependencyLoop2.original, .dependencyLoop2.original, .dependencyLoop2.opposite, .dependencyLoop2.opposite, .dependencyLoop2.Merge, .dependencyLoop2.Merge
  in .dependencyLoop2}}
