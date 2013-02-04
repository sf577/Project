# Test SubscriptionAggregator
#
# @Author: Christopher Brooks
#
# @Version: $Id: SubscriptionAggregator.tcl,v 1.2.2.2 2006/12/30 22:25:11 cxh Exp $
#
# @Copyright (c) 2006 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

######################################################################
####
#
test SubscriptionAggregator-1.1 {Simple Test of SubscriptionAggregator} {
    set workspace [java::new ptolemy.kernel.util.Workspace "subaggWS"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set url [[java::new java.io.File "auto/SubscriptionAggregator3.xml"] toURL]
    $parser purgeModelRecord $url
    set model [java::cast ptolemy.actor.TypedCompositeActor \
		   [$parser {parse java.net.URL java.net.URL} \
			[java::null] $url]]
    set manager [java::new ptolemy.actor.Manager $workspace "subaggManager"]
    $model setManager $manager 
    $manager execute
} {}

test SubscriptionAggregator-1.2 {Change one actor} {
    # Uses 1.1 above
    set actor [$model getEntity SubscriptionAggregator2]
    #set channel [$actor getAttribute channel]
    set channel [getParameter $actor channel]

    # Changing the channel should not change the output
    $channel setToken [java::new ptolemy.data.StringToken "channel1"]
    $manager execute
} {}

test SubscriptionAggregator-2.0 {No Publisher} {
    set e0 [sdfModel 5]
    set subAgg [java::new ptolemy.actor.lib.SubscriptionAggregator $e0 subagg]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Subscriber $subAgg] \
		 output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    catch {[$e0 getManager] execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Subscriber has no matching Publisher.
  in .top.subagg}}

test SubscriptionAggregator-3.0 {Debugging messages} {
    set e3 [sdfModel 5]

    set const [java::new ptolemy.actor.lib.Const $e3 const]
    set publisher [java::new ptolemy.actor.lib.Publisher $e3 publisher]
    set channelP [getParameter $publisher channel]
    $channelP setExpression "channel42"
    $publisher attributeChanged $channelP

    $e3 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $const] \
	     output] \
	[java::field $publisher input]

    set subAgg [java::new ptolemy.actor.lib.SubscriptionAggregator $e3 subagg]
    set channelS [getParameter $subAgg channel]
    $channelS setExpression "channel42"
    $subAgg attributeChanged $channelS

    set rec [java::new ptolemy.actor.lib.Recorder $e3 rec]
    $e3 connect \
	[java::field [java::cast ptolemy.actor.lib.Subscriber $subAgg] \
	     output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamListener $printStream]
    $subAgg addDebugListener $listener

    [$e3 getManager] execute
    $subAgg removeDebugListener $listener
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output \
	[enumToTokenValues [$rec getRecord 0]]
} {{Called preinitialize()
Called stopFire()
Added attribute firingsPerIteration to .top.subagg
Called initialize()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called wrapup()
} {1 1 1 1 1}}
