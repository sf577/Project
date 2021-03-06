/*
 @Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib.javasound;

import java.util.ArrayList;

import ptolemy.codegen.c.actor.lib.io.FileReader;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.javasound.AudioReader.
 * 
 * @author Man-Kit Leung
 * @version $Id: AudioReader.java,v 1.40 2006/04/07 18:07:39 cxh Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (mankit)
 * @Pt.AcceptedRating Yellow (mankit)
 */
public class AudioReader extends AudioSDLActor {
	/**
	 * Constructor method for the AudioReader helper.
	 * 
	 * @param actor
	 *            the associated actor.
	 */
	public AudioReader(ptolemy.actor.lib.javasound.AudioReader actor) {
		super(actor);
	}

	/**
	 * Generate initialization code. Get the file path from the actor's
	 * fileOrURL parameter. Read the <code>initBlock</code> from AudioReader.c
	 * and pass the file path string as an argument to code block. Replace
	 * macros with their values and return the processed code string.
	 * 
	 * @return The processed code string.
	 * @exception IllegalActionException
	 *                If the file path parameter is invalid or the code stream
	 *                encounters an error in processing the specified code
	 *                block(s).
	 */
	public String generateInitializeCode() throws IllegalActionException {
		super.generateInitializeCode();

		ptolemy.actor.lib.javasound.AudioReader actor = (ptolemy.actor.lib.javasound.AudioReader) getComponent();
		String fileNameString = FileReader.getFileName(actor.fileOrURL);
		ArrayList args = new ArrayList();
		args.add(fileNameString);
		_codeStream.appendCodeBlock("initBlock", args);
		return processCode(_codeStream.toString());
	}
}
