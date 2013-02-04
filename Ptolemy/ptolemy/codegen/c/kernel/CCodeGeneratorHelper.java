/* Base class for C code generator helper.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.StringUtilities;
import ptolemy.util.StreamExec;

//////////////////////////////////////////////////////////////////////////
//// CCodeGeneratorHelper

/**
 * Base class for C code generator helper.
 * 
 * <p>
 * Actor helpers extend this class and optionally define generateFireCode(),
 * generateInitializeCode(), generatePreinitializeCode(), and
 * generateWrapupCode() methods. In derived classes, these methods, if present,
 * make actor specific changes to the corresponding code. If these methods are
 * not present, then the parent class will automatically read the corresponding
 * .c file and subsitute in the corresponding code block. For example,
 * generateInitializeCode() reads the <code>initBlock</code>, processes the
 * macros and adds the resulting code block to the output.
 * 
 * <p>
 * For a complete list of methods to define, see
 * {@link ptolemy.codegen.kernel.CodeGeneratorHelper}.
 * 
 * <p>
 * For further details, see <code>$PTII/ptolemy/codegen/README.html</code>
 * 
 * @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Ye Zhou
 * @version $Id: CCodeGeneratorHelper.java,v 1.24.2.1 2007/01/05 22:07:25 cxh
 *          Exp $
 * @since Ptolemy II 6.0 o @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class CCodeGeneratorHelper extends CodeGeneratorHelper {
	/**
	 * Create a new instance of the C code generator helper.
	 * 
	 * @param component
	 *            The actor object for this helper.
	 */
	public CCodeGeneratorHelper(NamedObj component) {
		super(component);
		_parseTreeCodeGenerator = new CParseTreeCodeGenerator();
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Return a new parse tree code generator to use with expressions.
	 * 
	 * @return the parse tree code generator to use with expressions.
	 */
	public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
		// FIXME: We need to create new ParseTreeCodeGenerator each time
		// here or else we get lots of test failures. It would be better
		// if we could use the same CParseTreeCodeGenerator over and over.
		_parseTreeCodeGenerator = new CParseTreeCodeGenerator();
		return _parseTreeCodeGenerator;
	}

	/**
	 * Generate variable declarations for inputs and outputs and parameters.
	 * Append the declarations to the given string buffer.
	 * 
	 * @return code The generated code.
	 * @exception IllegalActionException
	 *                If the helper class for the model director cannot be
	 *                found.
	 */
	public String generateVariableDeclaration() throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		// Generate variable declarations for referenced parameters.
		String referencedParameterDeclaration = _generateReferencedParameterDeclaration();
		if (referencedParameterDeclaration.length() > 1) {
			code.append(_eol
					+ _codeGenerator.comment(getComponent().getName()
							+ "'s referenced parameter declarations."));
			code.append(referencedParameterDeclaration);
		}

		// Generate variable declarations for input ports.
		String inputVariableDeclaration = _generateInputVariableDeclaration();
		if (inputVariableDeclaration.length() > 1) {
			code.append(_eol
					+ _codeGenerator.comment(getComponent().getName()
							+ "'s input variable declarations."));
			code.append(inputVariableDeclaration);
		}

		// Generate variable declarations for output ports.
		String outputVariableDeclaration = _generateOutputVariableDeclaration();
		if (outputVariableDeclaration.length() > 1) {
			code.append(_eol
					+ _codeGenerator.comment(getComponent().getName()
							+ "'s output variable declarations."));
			code.append(outputVariableDeclaration);
		}

		// Generate type convert variable declarations.
		String typeConvertVariableDeclaration = _generateTypeConvertVariableDeclaration();
		if (typeConvertVariableDeclaration.length() > 1) {
			code.append(_eol
					+ _codeGenerator.comment(getComponent().getName()
							+ "'s type convert variable declarations."));
			code.append(typeConvertVariableDeclaration);
		}

		return processCode(code.toString());
	}

	/**
	 * Get the header files needed to compile with the jvm library.
	 * 
	 * @return A set of strings that are names of the header files needed by the
	 *         code generated for jvm library
	 * @exception IllegalActionException
	 *                Not Thrown in this subclass.
	 */
	public Set getJVMHeaderFiles() throws IllegalActionException {
		if (!_printedJVMWarning) {
			// We only print this once.
			_printedJVMWarning = true;
			ExecuteCommands executeCommands = getCodeGenerator()
					.getExecuteCommands();
			String javaHome = StringUtilities.getProperty("java.home");

			if (executeCommands == null) {
				executeCommands = new StreamExec();
			}
			executeCommands.stdout(_eol + _eol
					+ "WARNING: This model uses an actor that "
					+ "links with the jvm library." + _eol
					+ "  To properly run the executable, you must have jvm.dll"
					+ " in your path." + _eol
					+ "  If you do not, then when you run the executable, "
					+ "it will immediately exit" + _eol + "  with no message!"
					+ _eol + "  For example, place " + javaHome
					+ "\\bin\\client" + _eol
					+ "  in your path.  If you are running Vergil from the "
					+ "command line as " + _eol + "  $PTII/bin/ptinvoke, "
					+ "then this has been handled for you." + _eol
					+ "  If you are running via Eclipse, then you must update "
					+ "your path by hand." + _eol + _eol + _eol);
		}

		// FIXME: This only works under windows.
		String javaHome = StringUtilities.getProperty("java.home");
		javaHome = javaHome.replace('\\', '/');
		int index = javaHome.lastIndexOf("jre");
		javaHome = javaHome.substring(0, index);
		getCodeGenerator().addInclude("-I\"" + javaHome + "include\"");
		getCodeGenerator().addInclude("-I\"" + javaHome + "include/win32\"");

		String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
		getCodeGenerator().addLibrary(
				"-L\"" + ptIIDir + "/ptolemy/codegen/c/lib/win\"");
		getCodeGenerator().addLibrary("-ljvm");

		Set files = new HashSet();
		files.add("<jni.h>");
		return files;
	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods ////

	/**
	 * Generate input variable declarations.
	 * 
	 * @return a String that declares input variables.
	 * @exception IllegalActionException
	 *                If thrown while getting port information.
	 */
	protected String _generateInputVariableDeclaration()
			throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		Iterator inputPorts = ((Actor) getComponent()).inputPortList()
				.iterator();

		while (inputPorts.hasNext()) {
			TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

			if (inputPort.getWidth() == 0) {
				continue;
			}

			code.append("static " + cType(inputPort.getType()) + " "
					+ generateName(inputPort));

			if (inputPort.isMultiport()) {
				code.append("[" + inputPort.getWidth() + "]");
			}

			int bufferSize = getBufferSize(inputPort);

			if (bufferSize > 1) {
				code.append("[" + bufferSize + "]");
			}
			code.append(";" + _eol);
		}

		return code.toString();
	}

	/**
	 * Generate output variable declarations.
	 * 
	 * @return a String that declares output variables.
	 * @exception IllegalActionException
	 *                If thrown while getting port information.
	 */
	protected String _generateOutputVariableDeclaration()
			throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		Iterator outputPorts = ((Actor) getComponent()).outputPortList()
				.iterator();

		while (outputPorts.hasNext()) {
			TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

			// If either the output port is a dangling port or
			// the output port has inside receivers.
			if (outputPort.getWidth() == 0 || outputPort.getWidthInside() != 0) {
				code.append("static " + cType(outputPort.getType()) + " "
						+ generateName(outputPort));

				if (outputPort.isMultiport()) {
					code.append("[" + outputPort.getWidthInside() + "]");
				}

				int bufferSize = getBufferSize(outputPort);

				if (bufferSize > 1) {
					code.append("[" + bufferSize + "]");
				}
				code.append(";" + _eol);
			}
		}

		return code.toString();
	}

	/**
	 * Generate referenced parameter declarations.
	 * 
	 * @return a String that declares referenced parameters.
	 * @exception IllegalActionException
	 *                If thrown while getting modified variable information.
	 */
	protected String _generateReferencedParameterDeclaration()
			throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		if (_referencedParameters != null) {
			Iterator parameters = _referencedParameters.iterator();

			while (parameters.hasNext()) {
				Parameter parameter = (Parameter) parameters.next();

				// avoid duplicate declaration.
				if (!_codeGenerator.getModifiedVariables().contains(parameter)) {
					code.append("static " + cType(parameter.getType()) + " "
							+ generateVariableName(parameter) + ";" + _eol);
				}
			}
		}

		return code.toString();
	}

	/**
	 * Generate type convert variable declarations.
	 * 
	 * @return a String that declares type convert variables.
	 * @exception IllegalActionException
	 *                If thrown while getting port information.
	 */
	protected String _generateTypeConvertVariableDeclaration()
			throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		Iterator channels = _getTypeConvertChannels().iterator();
		while (channels.hasNext()) {
			Channel channel = (Channel) channels.next();
			code.append("static ");
			code.append(cType(((TypedIOPort) channel.port).getType()));
			code.append(" " + _getTypeConvertReference(channel));

			int bufferSize = Math.max(
					DFUtilities.getTokenProductionRate(channel.port),
					DFUtilities.getTokenConsumptionRate(channel.port));

			if (bufferSize > 1) {
				code.append("[" + bufferSize + "]");
			}
			code.append(";" + _eol);
		}

		return code.toString();
	}

	// /////////////////////////////////////////////////////////////////
	// // private fields ////

	/** True if we have printed the JVM warning. */
	private boolean _printedJVMWarning;
}
