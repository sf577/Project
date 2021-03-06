# Tests for the HSFSMDirector class
#
# @Author: Xiaojun Liu and Haiyang Zheng
#
# @Version: $Id: HSFSMDirector.tcl,v 1.2 2006/08/21 23:14:49 cxh Exp $
#
# @Copyright (c) 2000-2006 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test HSFSMDirector-1.1 {test constructors} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set dir1 [java::new ptolemy.domains.ct.kernel.HSFSMDirector]
    set dir2 [java::new ptolemy.domains.ct.kernel.HSFSMDirector $w]
    set e0 [java::new ptolemy.actor.CompositeActor]
    set dir3 [java::new ptolemy.domains.ct.kernel.HSFSMDirector $e0 dir]
    list [$dir1 getFullName] [$dir2 getFullName] [$dir3 getFullName]
} {. . ..dir}
