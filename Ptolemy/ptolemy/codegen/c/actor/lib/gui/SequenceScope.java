/* A helper class for ptolemy.actor.lib.gui.SequenceScope
 
 Copyright (c) 2006-2007 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */

package ptolemy.codegen.c.actor.lib.gui;

//////////////////////////////////////////////////////////////////////////
//// SequenceScope

/**
 * A helper class for ptolemy.actor.lib.gui.SequenceScope.
 * 
 * @author Gang Zhou
 * @version $Id: SequenceScope.java,v 1.3.2.3 2007/01/06 20:02:09 cxh Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (zgang)
 * @Pt.AcceptedRating Red (zgang)
 */
public class SequenceScope extends SequencePlotter {

	/**
	 * Constructor method for the SequenceScope helper.
	 * 
	 * @param actor
	 *            the associated actor.
	 */
	public SequenceScope(ptolemy.actor.lib.gui.SequenceScope actor) {
		super(actor);
	}
}
