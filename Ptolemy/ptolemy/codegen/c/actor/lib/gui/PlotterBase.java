/* Common static methods for codegen plotters
 
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// PlotterBase

/**
 * Common static methods for codegen plotters.
 * <p>
 * This class contains common code for the codegen plotters.
 * 
 * In regular interpreted Ptolemy II plotters, we extend
 * actor.lib.gui.PlotterBase. However, because in codegen we need to extend
 * CCode
 * 
 * @author Christopher Brooks, Gang Zhou
 * @version $Id: PlotterBase.java,v 1.4.2.3 2007/01/06 20:02:07 cxh Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PlotterBase extends CCodeGeneratorHelper {

	/**
	 * Constructor method for the XYPlotter helper.
	 * 
	 * @param actor
	 *            the associated actor.
	 */
	public PlotterBase(ptolemy.actor.lib.gui.PlotterBase actor) {
		super(actor);
	}

	/**
	 * Generate plot specfic fire code.
	 * 
	 * @param width
	 *            The width.
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                If the code stream encounters errors in processing the
	 *                specified code blocks.
	 */
	public String generatePlotFireCode(int width) throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		ArrayList args = new ArrayList();
		for (int i = width - 1; i >= 0; i--) {
			args.clear();
			args.add(new Integer(i));
			code.append(_generateBlockCode("plotBlock", args));
		}

		return code.toString();
	}

	/**
	 * Generate initialize code.
	 * 
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                If the code stream encounters errors in processing the
	 *                specified code blocks.
	 */
	public String generateInitializeCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
		ArrayList args = new ArrayList();
		args.add(ptIIDir);
		code.append(_generateBlockCode("createJVMBlock", args));

		// We don't have inheritance of blocks, so we use a separate block
		code.append(_generateBlockCode("plotterBaseInitBlock"));

		code.append(super.generateInitializeCode());
		ptolemy.actor.lib.gui.PlotterBase actor = (ptolemy.actor.lib.gui.PlotterBase) getComponent();

		// If the plot has not been created, we need to creat the plot
		// to get the configuration.
		if (actor.plot == null) {
			actor.initialize();
		}

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		String header = "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\""
				+ _eol
				+ "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";
		printWriter.write(header);
		printWriter.write(_eol + "<plot>" + _eol);
		actor.plot.writeFormat(printWriter);
		printWriter.write("</plot>" + _eol);

		StringBuffer result = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(
					stringWriter.toString()));
			String line = null;

			try {
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line != "") {
						// String.replace() is new in Java 1.5
						// and PtII 6.0 ships under 1.4.
						// line = line.replace("\"", "\\\\\"");
						line = StringUtilities.substitute(line, "\"", "\\\\\"");
						result.append("\"" + line + "\\\\n\"" + "\n");
					}
				}
			} catch (IOException ex) {
				throw new IllegalActionException(actor, ex,
						"Failed to create plot header.");
			}
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				throw new IllegalActionException(actor, ex,
						"Failed to close BufferedReader");
			}
		}

		args.clear();
		args.add(result.toString());
		code.append(_generateBlockCode("configureBlock", args));

		return code.toString();
	}

	/**
	 * Generate preinitialize code.
	 * 
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                If the code stream encounters errors in processing the
	 *                specified code blocks.
	 */
	public String generatePreinitializeCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		// We don't have inheritance of blocks, so we use a separate block.
		code.append(_generateBlockCode("plotterBasePreinitBlock"));

		code.append(super.generatePreinitializeCode());
		return code.toString();
	}

	/**
	 * Generate the wrapup code.
	 * 
	 * @return The generated wrapup code.
	 * @exception IllegalActionException
	 */
	public String generateWrapupCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		code.append(super.generateWrapupCode());
		// FIXME: this is a dumb way to leave the plot window open
		// when the program runs to the end. I need to figure out a
		// better way. Or is there any?
		code.append("char $actorSymbol(temp)[80];" + _eol);
		code.append("printf(\"close plot window to exit...\");" + _eol);
		code.append("scanf(\"%s\",$actorSymbol(temp));" + _eol);
		return processCode(CodeStream.indent(code.toString()));
	}

	/**
	 * Get the header files needed by the code generated for the XYPlotter
	 * actor.
	 * 
	 * @return A set of strings that are names of the header files needed by the
	 *         code generated for the XYPlotter actor.
	 * @exception IllegalActionException
	 *                Not Thrown in this subclass.
	 */
	public Set getHeaderFiles() throws IllegalActionException {
		return getJVMHeaderFiles();
	}
}
