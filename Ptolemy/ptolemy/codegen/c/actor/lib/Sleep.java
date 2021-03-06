/* A helper class for ptolemy.actor.lib.Sleep

 Copyright (c) 2006-2007 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Sleep

/**
 * A helper class for ptolemy.actor.lib.Sleep.
 * 
 * @author Gang Zhou
 * @version $Id: Sleep.java,v 1.2.2.3 2007/01/06 20:02:06 cxh Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (zgang)
 * @Pt.AcceptedRating Red (zgang)
 */
public class Sleep extends CCodeGeneratorHelper {
	/**
	 * Construct an Sleep helper.
	 * 
	 * @param actor
	 *            the associated actor
	 */
	public Sleep(ptolemy.actor.lib.Sleep actor) {
		super(actor);
	}

	/**
	 * Generate fire code.
	 * 
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                If the code stream encounters errors in processing the
	 *                specified code blocks.
	 */
	public String generateFireCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		code.append(super.generateFireCode());

		ptolemy.actor.lib.Sleep actor = (ptolemy.actor.lib.Sleep) getComponent();
		ArrayList args = new ArrayList();
		for (int i = 0; i < actor.input.getWidth(); i++) {
			if (i < actor.output.getWidth()) {
				args.clear();
				args.add(new Integer(i));
				code.append(_generateBlockCode("transferBlock", args));
			}
		}

		return code.toString();
	}

	/**
	 * Get the header files needed by the code generated for the Sleep actor.
	 * 
	 * @return A set of strings that are names of the header files needed by the
	 *         code generated for the Sleep actor.
	 * @exception IllegalActionException
	 *                Not Thrown in this subclass.
	 */
	public Set getHeaderFiles() throws IllegalActionException {
		Set files = new HashSet();
		files.add("<time.h>");
		return files;
	}
}
