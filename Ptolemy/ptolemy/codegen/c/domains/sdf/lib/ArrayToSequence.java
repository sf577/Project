/* A code generation helper class for sdf ArrayToSequence

 @Copyright (c) 2006-2007 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.domains.sdf.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.domains.sdf.lib.ArrayToSequence.
 * 
 * @author Christopher Brooks
 * @version $Id: ArrayToSequence.java,v 1.2.2.3 2007/01/06 20:02:14 cxh Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ArrayToSequence extends CCodeGeneratorHelper {
	/**
	 * Construct a ArrayToSequence helper.
	 * 
	 * @param actor
	 *            The associated actor.
	 */
	public ArrayToSequence(ptolemy.domains.sdf.lib.ArrayToSequence actor) {
		super(actor);
	}

	/**
	 * Generate fire code. Read the <code>fireBlock</code> from
	 * ArrayToSequence.c replace macros with their values and return the
	 * processed code block.
	 * 
	 * @return The processed code string.
	 * @exception IllegalActionException
	 *                If the code stream encounters an error in processing the
	 *                specified code block(s).
	 */
	public String generateFireCode() throws IllegalActionException {
		super.generateFireCode();

		ptolemy.domains.sdf.lib.ArrayToSequence actor = (ptolemy.domains.sdf.lib.ArrayToSequence) getComponent();

		ArrayList args = new ArrayList();

		if (actor.input.getType() instanceof ArrayType) {
			args.add(codeGenType(((ArrayType) actor.input.getType())
					.getElementType()));
		} else {
			throw new IllegalActionException("Unhandled type for input: ("
					+ actor.input.getType() + ")");
		}

		_codeStream.appendCodeBlock("fireBlock", args);

		return processCode(_codeStream.toString());
	}

}
