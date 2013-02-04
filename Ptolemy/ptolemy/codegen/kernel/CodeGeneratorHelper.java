/* Base class for code generator helper.

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
package ptolemy.codegen.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.data.ArrayToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////CodeGeneratorHelper

/**
 * Base class for code generator helper.
 * 
 * <p>
 * Subclasses should override generateFireCode(), generateInitializeCode(),
 * generatePreinitializeCode(), and generateWrapupCode() methods by appending a
 * corresponding code block.
 * 
 * <p>
 * Subclasses should be sure to properly indent the code by either using the
 * code block functionality in methods like {@link #_generateBlockCode(String)}
 * and {@link #_generateBlockCode(String, ArrayList)} or by calling
 * {@link ptolemy.codegen.kernel.CodeStream#indent(String)}, for example:
 * 
 * <pre>
 * StringBuffer code = new StringBuffer();
 * code.append(super.generateWrapupCode());
 * code.append(&quot;// Local wrapup code&quot;);
 * return processCode(CodeStream.indent(code.toString()));
 * </pre>
 * 
 * @author Ye Zhou, Gang Zhou, Edward A. Lee, Contributors: Christopher Brooks
 * @version $Id: CodeGeneratorHelper.java,v 1.181.2.2 2007/01/05 22:05:11 cxh
 *          Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGeneratorHelper extends NamedObj implements ActorCodeGenerator {

	/**
	 * Construct the code generator helper associated with the given component.
	 * 
	 * @param component
	 *            The associated component.
	 */
	public CodeGeneratorHelper(NamedObj component) {
		// FIXME: Why is this a namedObj when the analyzeActor()
		// method requires an Actor?
		_component = component;

		_parseTreeCodeGenerator = new ParseTreeCodeGenerator() {
			/**
			 * Given a string, escape special characters as necessary for the
			 * target language.
			 * 
			 * @param string
			 *            The string to escape.
			 * @return A new string with special characters replaced.
			 */
			public String escapeForTargetLanguage(String string) {
				return string;
			}

			/**
			 * Evaluate the parse tree with the specified root node using the
			 * specified scope to resolve the values of variables.
			 * 
			 * @param node
			 *            The root of the parse tree.
			 * @param scope
			 *            The scope for evaluation.
			 * @return The result of evaluation.
			 * @exception IllegalActionException
			 *                If an error occurs during evaluation.
			 */
			public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
					ParserScope scope) {
				return new Token();
			}

			/**
			 * Generate code that corresponds with the fire() method.
			 * 
			 * @return The generated code.
			 */
			public String generateFireCode() {
				return "/* ParseTreeCodeGenerator.generateFireCode() "
						+ "not implemented in codegen.kernel.CodeGenerator */";
			}
		};
	}

	/**
	 * Construct the code generator helper associated with the given component.
	 * 
	 * @param component
	 *            The associated component.
	 * @param name
	 *            The name of helper. All periods are replaced with underscores.
	 */
	public CodeGeneratorHelper(NamedObj component, String name) {
		this(component);

		try {
			setName(name.replaceAll("\\.", "_") + " helper");
		} catch (IllegalActionException ex) {
			// This should not occur.
		} catch (NameDuplicationException ex) {
			// FIXME: May not be important to handle.
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Find out each output port that needs to be converted for the actor
	 * associated with this helper. Then, mark these ports along with the sink
	 * ports (connection).
	 * 
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public void analyzeTypeConvert() throws IllegalActionException {
		// reset the previous type convert info.
		_portConversions.clear();

		Actor actor = (Actor) _component;

		ArrayList sourcePorts = new ArrayList();
		sourcePorts.addAll(actor.outputPortList());

		if (actor instanceof CompositeActor) {
			sourcePorts.addAll(actor.inputPortList());
		}

		Iterator ports = sourcePorts.iterator();

		// for each output port.
		for (int i = 0; ports.hasNext(); i++) {
			TypedIOPort sourcePort = (TypedIOPort) ports.next();

			// for each channel.
			for (int j = 0; j < sourcePort.getWidth(); j++) {
				Iterator sinks = getSinkChannels(sourcePort, j).iterator();

				// for each sink channel connected.
				while (sinks.hasNext()) {
					Channel sink = (Channel) sinks.next();
					TypedIOPort sinkPort = (TypedIOPort) sink.port;
					if (!sourcePort.getType().equals(sinkPort.getType())) {
						_markTypeConvert(new Channel(sourcePort, j), sink);
					}
				}
			}
		}
	}

	/**
	 * Get the corresponding type in code generation from the given Ptolemy
	 * type.
	 * 
	 * @param ptType
	 *            The given Ptolemy type.
	 * @return The code generation type.
	 * @exception IllegalActionException
	 *                Thrown if the given ptolemy cannot be resolved.
	 */
	public static String codeGenType(Type ptType) throws IllegalActionException {
		// FIXME: We may need to add more types.
		// FIXME: We have to create separate type for different matrix types.
		String result = ptType == BaseType.INT ? "Int"
				: ptType == BaseType.LONG ? "Long"
						: ptType == BaseType.STRING ? "String"
								: ptType == BaseType.DOUBLE ? "Double"
										: ptType == BaseType.BOOLEAN ? "Boolean"
												: ptType instanceof ArrayType ? "Array"
														: ptType instanceof MatrixType ? "Matrix"
																: "Token";

		// if (result.length() == 0) {
		// throw new IllegalActionException(
		// "Cannot resolved codegen type from Ptolemy type: " + ptType);
		// }
		return result;
	}

	/**
	 * Generate code for declaring read and write offset variables if needed.
	 * Return empty string in this base class.
	 * 
	 * @return The empty string.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String createOffsetVariablesIfNeeded() throws IllegalActionException {
		return "";
	}

	/**
	 * Get the corresponding type in C from the given Ptolemy type.
	 * 
	 * @param ptType
	 *            The given Ptolemy type.
	 * @return The C data type.
	 */
	public static String cType(Type ptType) {
		// FIXME: we may need to add more primitive types.
		return ptType == BaseType.INT ? "int"
				: ptType == BaseType.STRING ? "char*"
						: ptType == BaseType.DOUBLE ? "double"
								: ptType == BaseType.BOOLEAN ? "boolean"
										: "Token";
	}

	/**
	 * Generate the fire code. In this base class, add the name of the
	 * associated component in the comment. Subclasses may extend this method to
	 * generate the fire code of the associated component.
	 * 
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateFireCode() throws IllegalActionException {
		_codeStream.clear();

		String composite = (getComponent() instanceof CompositeActor) ? "Composite Actor: "
				: "";

		_codeStream.append(_eol
				+ CodeStream.indent(_codeGenerator.comment("Fire " + composite
						+ getComponent().getName())));

		_codeStream.appendCodeBlock(_defaultBlocks[2], true); // fireBlock
		return processCode(_codeStream.toString());
	}

	/**
	 * Generate The fire function code. This method is called when the firing
	 * code of each actor is not inlined. Each actor's firing code is in a
	 * function with the same name as that of the actor.
	 * 
	 * @return The fire function code.
	 * @exception IllegalActionException
	 *                If thrown while generating fire code.
	 */
	public String generateFireFunctionCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		code.append(_eol + generateName(getComponent()) + "() {" + _eol);
		code.append(generateFireCode());
		code.append(generateTypeConvertFireCode());
		code.append("}" + _eol);
		return code.toString();
	}

	/**
	 * Generate the initialize code. In this base class, return empty string.
	 * Subclasses may extend this method to generate initialize code of the
	 * associated component and append the code to the given string buffer.
	 * 
	 * @return The initialize code of the containing composite actor.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateInitializeCode() throws IllegalActionException {
		_codeStream.clear();
		_codeStream.appendCodeBlock(_defaultBlocks[1], true); // initBlock
		// There is no need to generate comment for empty code block.
		if (!_codeStream.isEmpty()) {
			_codeStream.insert(
					0,
					_eol
							+ CodeStream.indent(_codeGenerator
									.comment("initialize "
											+ getComponent().getName())));
		}
		return processCode(_codeStream.toString());
	}

	/**
	 * Generate the main entry point.
	 * 
	 * @return In this base class, return a comment. Subclasses should return
	 *         the definition of the main entry point for a program. In C, this
	 *         would be defining main().
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */

	// public String generateMainEntryCode() throws IllegalActionException {
	// return _codeGenerator.comment("main entry code");
	// }
	/**
	 * Generate the main entry point.
	 * 
	 * @return In this base class, return a comment. Subclasses should return
	 *         the a string that closes optionally calls exit and closes the
	 *         main() method
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	// public String generateMainExitCode() throws IllegalActionException {
	// return _codeGenerator.comment("main exit code");
	// }
	/**
	 * Generate mode transition code. The mode transition code generated in this
	 * method is executed after each global iteration, e.g., in HDF model. Do
	 * nothing in this base class.
	 * 
	 * @param code
	 *            The string buffer that the generated code is appended to.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public void generateModeTransitionCode(StringBuffer code)
			throws IllegalActionException {
	}

	/**
	 * Generate sanitized name for the given named object.
	 * 
	 * @param namedObj
	 *            The named object to generate sanitized name for.
	 * @return The sanitized name.
	 */
	public static String generateName(NamedObj namedObj) {
		return StringUtilities.sanitizeName(namedObj.getFullName());
	}

	/**
	 * Generate the expression that represents the offsite in the generated
	 * code.
	 * 
	 * @param offsetString
	 *            The specified offset from the user.
	 * @param port
	 *            The referenced port.
	 * @param channel
	 *            The referenced port channel.
	 * @param isWrite
	 *            Whether to generate the write or read offset.
	 * @return The expression that represents the offset in the generated code.
	 * @exception IllegalActionException
	 *                If there is problems getting the port buffer size or the
	 *                offset in the channel and offset map.
	 */
	public String generateOffset(String offsetString, IOPort port, int channel,
			boolean isWrite) throws IllegalActionException {

		// if the max buffer size of the port is not larger than 1,
		// offset is not needed.
		if (!(getBufferSize(port) > 1)) {
			return "";
		}

		String result = "";
		Object offsetObject;
		String temp;

		// Get the offset index.
		if (isWrite) {
			offsetObject = getWriteOffset(port, channel);
		} else {
			offsetObject = getReadOffset(port, channel);
		}

		if (!offsetString.equals("")) {
			// Specified offset.

			if (offsetObject instanceof Integer && _isInteger(offsetString)) {

				int offset = ((Integer) offsetObject).intValue()
						+ (new Integer(offsetString)).intValue();

				offset %= getBufferSize(port, channel);
				temp = new Integer(offset).toString();
				/*
				 * int divisor = getBufferSize(sinkPort, sinkChannelNumber);
				 * temp = "(" + getWriteOffset(sinkPort, sinkChannelNumber) +
				 * " + " + channelAndOffset[1] + ")%" + divisor;
				 */

			} else {
				// Note: This assumes the director helper will increase
				// the buffer size of the channel to the power of two.
				// Otherwise, use "%" instead.
				// FIXME: We haven't check if modulo is 0. But this
				// should never happen. For offsets that need to be
				// represented by string expression,
				// getBufferSize(port, channelNumber) will always
				// return a value at least 2.

				/*
				 * FIXME: The following pointer math does not give the correct
				 * result. Maybe the original author wanted to optimize by
				 * avoiding the "%" operator. int modulo = getBufferSize(port,
				 * channel) - 1; temp = "(" + offsetObject.toString() + " + " +
				 * offsetString + ")&" + modulo;
				 */
				int modulo = getBufferSize(port, channel);
				temp = "(" + offsetObject.toString() + " + " + offsetString
						+ ")%" + modulo;
			}

			result += "[" + temp + "]";

		} else {
			// Did not specify offset, so the receiver buffer
			// size is 1. This is multiple firing.

			if (offsetObject instanceof Integer) {
				int offset = ((Integer) offsetObject).intValue();

				offset %= getBufferSize(port, channel);

				temp = new Integer(offset).toString();
			} else {

				int modulo = getBufferSize(port, channel) - 1;

				temp = (String) offsetObject + "&" + modulo;
			}
			result += "[" + temp + "]";
		}
		return result;
	}

	/**
	 * Generate the preinitialize code. In this base class, return an empty
	 * string. This method generally does not generate any execution code and
	 * returns an empty string. Subclasses may generate code for variable
	 * declaration, defining constants, etc.
	 * 
	 * @return A string of the preinitialize code for the helper.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generatePreinitializeCode() throws IllegalActionException {
		_createBufferSizeAndOffsetMap();

		_codeStream.clear();
		_codeStream.appendCodeBlock(_defaultBlocks[0], true); // preinitBlock
		// There is no need to generate comment for empty code block.
		if (!_codeStream.isEmpty()) {
			_codeStream.insert(
					0,
					_eol
							+ _codeGenerator.comment("preinitialize "
									+ getComponent().getName()));
		}
		return processCode(_codeStream.toString());
	}

	/**
	 * Generate the type conversion fire code. This method is called by the
	 * Director to append necessary fire code to handle type conversion.
	 * 
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateTypeConvertFireCode() throws IllegalActionException {
		return generateTypeConvertFireCode(false);
	}

	/**
	 * Generate the type conversion fire code. This method is called by the
	 * Director to append necessary fire code to handle type conversion.
	 * 
	 * @param forComposite
	 *            True if we are generating code for a composite.
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateTypeConvertFireCode(boolean forComposite)
			throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		// Type conversion code for inter-actor port conversion.
		Iterator channels = _getTypeConvertChannels().iterator();
		while (channels.hasNext()) {
			Channel source = (Channel) channels.next();

			if (!forComposite && source.port.isOutput() || forComposite
					&& source.port.isInput()) {

				Iterator sinkChannels = _getTypeConvertSinkChannels(source)
						.iterator();

				while (sinkChannels.hasNext()) {
					Channel sink = (Channel) sinkChannels.next();
					code.append(_generateTypeConvertStatements(source, sink));
				}
			}
		}
		return code.toString();
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
		return "";
	}

	/**
	 * Generate variable initialization for the referenced parameters.
	 * 
	 * @return code The generated code.
	 * @exception IllegalActionException
	 *                If the helper class for the model director cannot be
	 *                found.
	 */
	public String generateVariableInitialization()
			throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		// Generate variable initialization for referenced parameters.
		if (!_referencedParameters.isEmpty()) {
			code.append(_eol
					+ _codeGenerator.comment(1, _component.getName()
							+ "'s parameter initialization"));

			Iterator parameters = _referencedParameters.iterator();

			while (parameters.hasNext()) {
				Parameter parameter = (Parameter) parameters.next();
				try {
					// avoid duplication.
					if (!_codeGenerator._modifiedVariables.contains(parameter)) {
						code.append(_INDENT1
								+ _codeGenerator
										.generateVariableName(parameter)
								+ " = "
								+ getParameterValue(parameter.getName(),
										_component) + ";" + _eol);
					}
				} catch (Throwable throwable) {
					throw new IllegalActionException(_component, throwable,
							"Failed to generate variable initialization for \""
									+ parameter + "\"");
				}
			}
		}
		return code.toString();
	}

	/**
	 * Generate the wrapup code. In this base class, do nothing. Subclasses may
	 * extend this method to generate the wrapup code of the associated
	 * component and append the code to the given string buffer.
	 * 
	 * @return The generated wrapup code.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateWrapupCode() throws IllegalActionException {
		_codeStream.clear();
		_codeStream.appendCodeBlock(_defaultBlocks[3], true); // wrapupBlock
		// There is no need to generate comment for empty code block.
		if (!_codeStream.isEmpty()) {
			_codeStream.insert(
					0,
					_eol
							+ CodeStream.indent(_codeGenerator
									.comment("wrapup "
											+ getComponent().getName())));
		}
		return processCode(_codeStream.toString());
	}

	/**
	 * Return the buffer size of a given port, which is the maximum of the
	 * bufferSizes of all channels of the given port.
	 * 
	 * @param port
	 *            The given port.
	 * @return The buffer size of the given port.
	 * @exception IllegalActionException
	 *                If the {@link #getBufferSize(IOPort, int)} method throws
	 *                it.
	 * @see #setBufferSize(IOPort, int, int)
	 */
	public int getBufferSize(IOPort port) throws IllegalActionException {
		int bufferSize = 1;

		if (port.getContainer() == _component) {
			int length = 0;

			if (port.isInput()) {
				length = port.getWidth();
			} else {
				length = port.getWidthInside();
			}

			for (int i = 0; i < length; i++) {
				int channelBufferSize = getBufferSize(port, i);

				if (channelBufferSize > bufferSize) {
					bufferSize = channelBufferSize;
				}
			}
		} else {
			CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(port
					.getContainer());
			bufferSize = actorHelper.getBufferSize(port);
		}

		return bufferSize;
	}

	/**
	 * Get the buffer size of the given port of this actor.
	 * 
	 * @param port
	 *            The given port.
	 * @param channelNumber
	 *            The given channel.
	 * @return The buffer size of the given port and channel.
	 * @exception IllegalActionException
	 *                If the getBufferSize() method of the actor helper class
	 *                throws it.
	 * @see #setBufferSize(IOPort, int, int)
	 */
	public int getBufferSize(IOPort port, int channelNumber)
			throws IllegalActionException {
		if (port.getContainer() == _component) {
			if (_bufferSizes == null) {
				throw new InternalErrorException(this, null,
						"_bufferSizes is null?");
			}
			if (_bufferSizes.get(port) == null) {
				throw new InternalErrorException(this, null,
						"_bufferSizes.get(" + port + ") is null?");
			}
			return ((int[]) _bufferSizes.get(port))[channelNumber];
		} else {
			CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(port
					.getContainer());
			return actorHelper.getBufferSize(port, channelNumber);
		}
	}

	/**
	 * Get the code generator associated with this helper class.
	 * 
	 * @return The code generator associated with this helper class.
	 * @see #setCodeGenerator(CodeGenerator)
	 */
	public CodeGenerator getCodeGenerator() {
		return _codeGenerator;
	}

	/**
	 * Get the component associated with this helper.
	 * 
	 * @return The associated component.
	 */
	public NamedObj getComponent() {
		return _component;
	}

	/**
	 * Return an array of strings that are regular expressions of all the code
	 * blocks that are appended automatically by default. Since the content of
	 * the array are regex, users should use matches() instead of equals() to
	 * compare their strings.
	 * 
	 * @return Array of string regular expressions of names of code blocks that
	 *         are appended by default.
	 */
	public static String[] getDefaultBlocks() {
		return _defaultBlocks;
	}

	/**
	 * Return the translated token instance function invocation string.
	 * 
	 * @param functionString
	 *            The string within the $tokenFunc() macro.
	 * @param isStatic
	 *            True if the method is static.
	 * @return The translated type function invocation string.
	 * @exception IllegalActionException
	 *                The given function string is not well-formed.
	 */
	public String getFunctionInvocation(String functionString, boolean isStatic)
			throws IllegalActionException {
		functionString = processCode(functionString);

		// i.e. "$tokenFunc(token::add(arg1, arg2, ...))"
		// this transforms to ==>
		// "functionTable[token.type][FUNC_add] (token, arg1, arg2, ...)"
		// FIXME: we need to do some more smart parsing to find the following
		// indexes.
		int commaIndex = functionString.indexOf("::");
		int openFuncParenIndex = functionString.indexOf('(', commaIndex);
		int closeFuncParenIndex = functionString.lastIndexOf(')');

		// Syntax checking.
		if ((commaIndex == -1) || (openFuncParenIndex == -1)
				|| (closeFuncParenIndex != (functionString.length() - 1))) {
			throw new IllegalActionException(
					"Bad Syntax with the $tokenFunc / $typeFunc macro. "
							+ "[i.e. -- $tokenFunc(typeOrToken::func(arg1, ...))]");
		}

		String typeOrToken = functionString.substring(0, commaIndex).trim();
		String functionName = functionString.substring(commaIndex + 2,
				openFuncParenIndex).trim();

		String argumentList = functionString.substring(openFuncParenIndex + 1)
				.trim();

		if (isStatic) {
			// Record the referenced type function in the infoTable.
			_codeGenerator._typeFuncUsed.add(functionName);

			if (argumentList.length() == 0) {
				throw new IllegalActionException(
						"Static type function requires at least one argument(s).");
			}

			return "functionTable[" + typeOrToken + "][FUNC_" + functionName
					+ "](" + argumentList;

		} else {
			// Record the referenced type function in the infoTable.
			_codeGenerator._tokenFuncUsed.add(functionName);

			// if it is more than just a closing paren
			if (argumentList.length() > 1) {
				argumentList = ", " + argumentList;
			}

			return "functionTable[" + typeOrToken + ".type][FUNC_"
					+ functionName + "](" + typeOrToken + argumentList;
		}
	}

	/**
	 * Get the files needed by the code generated from this helper class. This
	 * base class returns an empty set.
	 * 
	 * @return A set of strings that are header files needed by the code
	 *         generated from this helper class.
	 * @exception IllegalActionException
	 *                Not Thrown in this base class.
	 */
	public Set getHeaderFiles() throws IllegalActionException {
		Set files = new HashSet();
		return files;
	}

	/**
	 * Return a set of parameters that will be modified during the execution of
	 * the model. The actor gets those variables if it implements
	 * ExplicitChangeContext interface or it contains PortParameters.
	 * 
	 * @return a set of parameters that will be modified.
	 * @exception IllegalActionException
	 *                If an actor throws it while getting modified variables.
	 */
	public Set getModifiedVariables() throws IllegalActionException {
		Set set = new HashSet();
		if (_component instanceof ExplicitChangeContext) {
			set.addAll(((ExplicitChangeContext) _component)
					.getModifiedVariables());
		}

		Iterator inputPorts = ((Actor) _component).inputPortList().iterator();
		while (inputPorts.hasNext()) {
			IOPort inputPort = (IOPort) inputPorts.next();
			if (inputPort instanceof ParameterPort && inputPort.getWidth() > 0) {
				set.add(((ParameterPort) inputPort).getParameter());
			}
		}
		return set;
	}

	/**
	 * Return the translated new constructor invocation string. Keep the types
	 * referenced in the info table of this helper. The kernel will retrieve
	 * this information to determine the total number of referenced types in the
	 * model.
	 * 
	 * @param constructorString
	 *            The string within the $new() macro.
	 * @return The translated new constructor invocation string.
	 * @exception IllegalActionException
	 *                The given constructor string is not well-formed.
	 */
	public String getNewInvocation(String constructorString)
			throws IllegalActionException {
		constructorString = processCode(constructorString);

		// i.e. "$new(Array(8, 8, arg1, arg2, ...))"
		// this transforms to ==>
		// "Array_new(8, arg1, arg2, ...)"
		int openFuncParenIndex = constructorString.indexOf('(');
		int closeFuncParenIndex = constructorString.lastIndexOf(')');

		// Syntax checking.
		if ((openFuncParenIndex == -1)
				|| (closeFuncParenIndex != (constructorString.length() - 1))) {
			throw new IllegalActionException(
					"Bad Syntax with the $new() macro. "
							+ "[i.e. -- $new(Array(8, 8, arg1, arg2, ...))]");
		}

		String typeName = constructorString.substring(0, openFuncParenIndex)
				.trim();

		// Record the referenced type function in the infoTable.
		_codeGenerator._newTypesUsed.add(typeName);

		return typeName + "_new"
				+ constructorString.substring(openFuncParenIndex);
	}

	/**
	 * Return the value or an expression in the target language for the
	 * specified parameter of the associated actor. If the parameter is
	 * specified by an expression, then the expression will be parsed. If any
	 * parameter referenced in that expression is specified by another
	 * expression, the parsing continues recursively until either a parameter is
	 * directly specified by a constant or a parameter can be directly modified
	 * during execution in which case a reference to the parameter is generated.
	 * 
	 * @param name
	 *            The name of the parameter.
	 * @param container
	 *            The container to search upwards from.
	 * @return The value or expression as a string.
	 * @exception IllegalActionException
	 *                If the parameter does not exist or does not have a value.
	 */
	public String getParameterValue(String name, NamedObj container)
			throws IllegalActionException {
		name = processCode(name);

		StringTokenizer tokenizer = new StringTokenizer(name, ",");

		String attributeName = tokenizer.nextToken().trim();
		String offset = null;
		String castType = null;

		if (tokenizer.hasMoreTokens()) {
			offset = tokenizer.nextToken().trim();

			if (tokenizer.hasMoreTokens()) {
				throw new IllegalActionException(_component, name
						+ " does not have the correct format for"
						+ " accessing the parameter value.");
			}
		}

		// Get the cast type (if any), so we can add the proper convert method.
		StringTokenizer tokenizer2 = new StringTokenizer(attributeName, "()",
				false);
		if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
			throw new IllegalActionException(_component, "Invalid cast type: "
					+ attributeName);
		}

		if (tokenizer2.countTokens() == 2) {
			castType = tokenizer2.nextToken().trim();
			attributeName = tokenizer2.nextToken().trim();
		}

		Attribute attribute = ModelScope.getScopedVariable(null, container,
				attributeName);

		if (attribute == null) {
			attribute = container.getAttribute(attributeName);
			if (attribute == null) {
				throw new IllegalActionException(container,
						"No attribute named: " + name);
			}
		}

		if (offset == null) {
			if (attribute instanceof Variable) {
				// FIXME: need to ensure that the returned string
				// is correct syntax for the target language.
				Variable variable = (Variable) attribute;

				/*
				 * if (_codeGenerator._modifiedVariables.contains(variable)) {
				 * return generateVariableName(variable); } else if
				 * (variable.isStringMode()) { return "\"" +
				 * variable.getExpression() + "\""; }
				 */

				ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
				if (variable.isStringMode()) {
					return _generateTypeConvertMethod(
							"\""
									+ parseTreeCodeGenerator.escapeForTargetLanguage(variable
											.getExpression()) + "\"", castType,
							"String");
				}

				PtParser parser = new PtParser();
				ASTPtRootNode parseTree = parser.generateParseTree(variable
						.getExpression());
				parseTreeCodeGenerator.evaluateParseTree(parseTree,
						new VariableScope(variable));

				return _generateTypeConvertMethod(
						processCode(parseTreeCodeGenerator.generateFireCode()),
						castType, codeGenType(variable.getType()));

			} else /* if (attribute instanceof Settable) */{
				return ((Settable) attribute).getExpression();
			}

			// FIXME: Are there any other values that a
			// parameter might have?
			// throw new IllegalActionException(_component,
			// "Attribute does not have a value: " + name);
		} else {
			// FIXME: if offset != null, for now we assume the value of
			// the parameter is fixed during execution.
			if (attribute instanceof Parameter) {
				Token token = ((Parameter) attribute).getToken();

				if (token instanceof ArrayToken) {
					Token element = ((ArrayToken) token)
							.getElement(new Integer(offset).intValue());

					return _generateTypeConvertMethod(element.toString(),
							castType, codeGenType(element.getType()));
				}

				throw new IllegalActionException(_component, attributeName
						+ " does not contain an ArrayToken.");
			}

			throw new IllegalActionException(_component, attributeName
					+ " is not a parameter.");
		}
	}

	/**
	 * Return the parse tree to use with expressions.
	 * 
	 * @return the parse tree to use with expressions.
	 */
	public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
		return _parseTreeCodeGenerator;
	}

	/**
	 * Return the associated actor's rates for all configurations of this actor.
	 * In this base class, return null.
	 * 
	 * @return null
	 */
	public int[][] getRates() {
		return null;
	}

	/**
	 * Get the read offset in the buffer of a given channel from which a token
	 * should be read. The channel is given by its containing port and the
	 * channel number in that port.
	 * 
	 * @param inputPort
	 *            The given port.
	 * @param channelNumber
	 *            The given channel number.
	 * @return The offset in the buffer of a given channel from which a token
	 *         should be read.
	 * @exception IllegalActionException
	 *                Thrown if the helper class cannot be found.
	 * @see #setReadOffset(IOPort, int, Object)
	 */
	public Object getReadOffset(IOPort inputPort, int channelNumber)
			throws IllegalActionException {
		if (inputPort.getContainer() == _component) {
			return ((Object[]) _readOffsets.get(inputPort))[channelNumber];
		} else {
			CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
					.getContainer());
			return actorHelper.getReadOffset(inputPort, channelNumber);
		}
	}

	/**
	 * Return the reference to the specified parameter or port of the associated
	 * actor. For a parameter, the returned string is in the form
	 * "fullName_parameterName". For a port, the returned string is in the form
	 * "fullName_portName[channelNumber][offset]", if any channel number or
	 * offset is given.
	 * 
	 * FIXME: need documentation on the input string format.
	 * 
	 * @param name
	 *            The name of the parameter or port
	 * @return The reference to that parameter or port (a variable name, for
	 *         example).
	 * @exception IllegalActionException
	 *                If the parameter or port does not exist or does not have a
	 *                value.
	 */
	public String getReference(String name) throws IllegalActionException {
		boolean isWrite = false;
		return getReference(name, isWrite);
	}

	/**
	 * Return the reference to the specified parameter or port of the associated
	 * actor. For a parameter, the returned string is in the form
	 * "fullName_parameterName". For a port, the returned string is in the form
	 * "fullName_portName[channelNumber][offset]", if any channel number or
	 * offset is given.
	 * 
	 * @param name
	 *            The name of the parameter or port
	 * @param isWrite
	 *            Whether to generate the write or read offset.
	 * @return The reference to that parameter or port (a variable name, for
	 *         example).
	 * @exception IllegalActionException
	 *                If the parameter or port does not exist or does not have a
	 *                value.
	 */
	public String getReference(String name, boolean isWrite)
			throws IllegalActionException {
		name = processCode(name);

		String castType = null;
		String refType = null;

		StringBuffer result = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

		if ((tokenizer.countTokens() != 1) && (tokenizer.countTokens() != 3)
				&& (tokenizer.countTokens() != 5)) {
			throw new IllegalActionException(_component,
					"Reference not found: " + name);
		}

		// Get the referenced name.
		String refName = tokenizer.nextToken().trim();

		// Get the cast type (if any), so we can add the proper convert method.
		StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
		if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
			throw new IllegalActionException(_component, "Invalid cast type: "
					+ refName);
		}

		if (tokenizer2.countTokens() == 2) {
			castType = tokenizer2.nextToken().trim();
			refName = tokenizer2.nextToken().trim();
		}

		boolean forComposite = false;

		// Usually given the name of an input port, getReference(String name)
		// returns variable name representing the input port. Given the name
		// of an output port, getReference(String name) returns variable names
		// representing the input ports connected to the output port.
		// However, if the name of an input port starts with '@',
		// getReference(String name) returns variable names representing the
		// input ports connected to the given input port on the inside.
		// If the name of an output port starts with '@',
		// getReference(String name) returns variable name representing the
		// the given output port which has inside receivers.
		// The special use of '@' is for composite actor when
		// tokens are transferred into or out of the composite actor.
		if (refName.charAt(0) == '@') {
			forComposite = true;
			refName = refName.substring(1);
		}

		TypedIOPort port = getPort(refName);

		String[] channelAndOffset = _getChannelAndOffset(name);

		if (port != null) {
			refType = codeGenType(port.getType());

			int channelNumber = 0;
			if (!channelAndOffset[0].equals("")) {
				channelNumber = (new Integer(channelAndOffset[0])).intValue();
			}

			// To support modal model, we need to check the following condition
			// first because an output port of a modal controller should be
			// mainly treated as an output port. However, during choice action,
			// an output port of a modal controller will receive the tokens sent
			// from the same port. During commit action, an output port of a
			// modal
			// controller will NOT receive the tokens sent from the same port.
			if ((port.isOutput() && !forComposite)
					|| (port.isInput() && forComposite)) {
				Receiver[][] remoteReceivers;

				// For the same reason as above, we cannot do: if
				// (port.isInput())...
				if (port.isOutput()) {
					remoteReceivers = port.getRemoteReceivers();
				} else {
					remoteReceivers = port.deepGetReceivers();
				}

				if (remoteReceivers.length == 0) {
					// The channel of this output port doesn't have any sink.
					result.append(generateName(_component));
					result.append("_");
					result.append(port.getName());
					return _generateTypeConvertMethod(result.toString(),
							castType, refType);
				}

				Channel sourceChannel = new Channel(port, channelNumber);

				List typeConvertSinks = _getTypeConvertSinkChannels(sourceChannel);

				List sinkChannels = getSinkChannels(port, channelNumber);

				boolean hasTypeConvertReference = false;

				for (int i = 0; i < sinkChannels.size(); i++) {
					Channel channel = (Channel) sinkChannels.get(i);
					IOPort sinkPort = channel.port;
					int sinkChannelNumber = channel.channelNumber;

					// Type convert.
					if (typeConvertSinks.contains(channel)) {
						if (!hasTypeConvertReference) {
							if (i != 0) {
								result.append(" = ");
							}
							result.append(_getTypeConvertReference(sourceChannel));
							int rate = Math
									.max(DFUtilities
											.getTokenProductionRate(sourceChannel.port),
											DFUtilities
													.getTokenConsumptionRate(sourceChannel.port));
							if (rate > 1) {
								result.append("[" + channelAndOffset[1] + "]");
							}
							hasTypeConvertReference = true;
						} else {
							// We already generated reference for this sink.
							continue;
						}
					} else {
						if (i != 0) {
							result.append(" = ");
						}
						result.append(generateName(sinkPort));

						if (sinkPort.isMultiport()) {
							result.append("[" + sinkChannelNumber + "]");
						}
						result.append(generateOffset(channelAndOffset[1],
								sinkPort, sinkChannelNumber, true));
					}
				}

				return _generateTypeConvertMethod(result.toString(), castType,
						refType);
			}

			// Note that if the width is 0, then we have no connection to
			// the port but the port might be a PortParameter, in which
			// case we want the Parameter.
			// codegen/c/actor/lib/string/test/auto/StringCompare3.xml
			// tests this.

			if ((port.isInput() && !forComposite && port.getWidth() > 0)
					|| (port.isOutput() && forComposite)) {

				result.append(generateName(port));

				// if (!channelAndOffset[0].equals("")) {
				if (port.isMultiport()) {
					// Channel number specified. This must be a multiport.
					result.append("[" + channelAndOffset[0] + "]");
				}

				result.append(generateOffset(channelAndOffset[1], port,
						channelNumber, isWrite));

				return _generateTypeConvertMethod(result.toString(), castType,
						refType);
			}
		}

		// Try if the name is a parameter.
		Attribute attribute = _component.getAttribute(refName);

		if (attribute != null) {
			// FIXME: potential bug: if the attribute is not a parameter,
			// it will be referenced but not declared.
			if (attribute instanceof Parameter) {
				_referencedParameters.add(attribute);
				refType = codeGenType(((Parameter) attribute).getType());
			}

			result.append(_codeGenerator.generateVariableName(attribute));

			if (!channelAndOffset[0].equals("")) {
				throw new IllegalActionException(_component,
						"a parameter cannot have channel number.");
			}

			if (!channelAndOffset[1].equals("")) {
				// result.append("[" + channelAndOffset[1] + "]");
				result.insert(0, "Array_get(");
				result.append(" ," + channelAndOffset[1] + ")");

				Type elementType = ((ArrayType) ((Parameter) attribute)
						.getType()).getElementType();

				if (isPrimitive(elementType)) {
					result.append(".payload." + codeGenType(elementType));
				}
			}

			return _generateTypeConvertMethod(result.toString(), castType,
					refType);
		}

		throw new IllegalActionException(_component, "Reference not found: "
				+ name);
	}

	/**
	 * Get the port that has the given name.
	 * 
	 * @param refName
	 *            The given name.
	 * @return The port that has the given name.
	 */
	public TypedIOPort getPort(String refName) {
		Actor actor = (Actor) _component;

		Iterator inputPorts = actor.inputPortList().iterator();

		while (inputPorts.hasNext()) {
			TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

			// The channel is specified as $ref(port#channelNumber).
			if (inputPort.getName().equals(refName)) {
				return inputPort;
			}
		}

		Iterator outputPorts = actor.outputPortList().iterator();

		while (outputPorts.hasNext()) {
			TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

			// The channel is specified as $ref(port#channelNumber).
			if (outputPort.getName().equals(refName)) {
				return outputPort;
			}
		}

		return null;
	}

	/**
	 * Generate the shared code. This is the first generate method invoked out
	 * of all, so any initialization of variables of this helper should be done
	 * in this method. In this base class, return an empty set. Subclasses may
	 * generate code for variable declaration, defining constants, etc.
	 * 
	 * @return An empty set in this base class.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public Set getSharedCode() throws IllegalActionException {
		Set sharedCode = new HashSet();
		_codeStream.clear();
		_codeStream.appendCodeBlocks(".*shared.*");
		sharedCode.add(processCode(_codeStream.toString()));
		return sharedCode;
	}

	/**
	 * Return a list of channel objects that are the sink input ports given a
	 * port and channel. Note the returned channels are newly created objects
	 * and therefore not associated with the helper class.
	 * 
	 * @param port
	 *            The given output port.
	 * @param channelNumber
	 *            The given channel number.
	 * @return The list of channel objects that are the sink channels of the
	 *         given output channel.
	 */
	public List getSinkChannels(IOPort port, int channelNumber) {
		List sinkChannels = new LinkedList();
		Receiver[][] remoteReceivers;

		// due to reason stated in getReference(String),
		// we cannot do: if (port.isInput())...
		if (port.isOutput()) {
			remoteReceivers = port.getRemoteReceivers();
		} else {
			remoteReceivers = port.deepGetReceivers();
		}

		if (remoteReceivers.length <= channelNumber || channelNumber < 0) {
			// This is an escape method. This class will not call this
			// method if the output port does not have a remote receiver.
			return sinkChannels;
		}

		for (int i = 0; i < remoteReceivers[channelNumber].length; i++) {
			IOPort sinkPort = remoteReceivers[channelNumber][i].getContainer();
			Receiver[][] portReceivers;

			if (sinkPort.isInput()) {
				portReceivers = sinkPort.getReceivers();
			} else {
				portReceivers = sinkPort.getInsideReceivers();
			}

			for (int j = 0; j < portReceivers.length; j++) {
				for (int k = 0; k < portReceivers[j].length; k++) {
					if (remoteReceivers[channelNumber][i] == portReceivers[j][k]) {
						Channel sinkChannel = new Channel(sinkPort, j);
						sinkChannels.add(sinkChannel);
						break;
					}
				}
			}
		}

		return sinkChannels;
	}

	/**
	 * Get the size of a parameter. The size of a parameter is the length of its
	 * array if the parameter's type is array, and 1 otherwise.
	 * 
	 * @param name
	 *            The name of the parameter.
	 * @return The size of a parameter.
	 * @exception IllegalActionException
	 *                If no port or parameter of the given name is found.
	 */
	public int getSize(String name) throws IllegalActionException {

		// Try if the name is a parameter.
		Attribute attribute = _component.getAttribute(name);

		if (attribute != null) {
			// FIXME: Could it be something other than variable?
			if (attribute instanceof Variable) {
				Token token = ((Variable) attribute).getToken();

				if (token instanceof ArrayToken) {
					return ((ArrayToken) token).length();
				}

				return 1;
			}
		} else {
			IOPort port = getPort(name);
			if (port != null) {
				return port.getWidth();
			}
		}

		throw new IllegalActionException(_component, "Attribute not found: "
				+ name);
	}

	/**
	 * Given a port and channel number, create a Channel that sends data to the
	 * specified port and channel number.
	 * 
	 * @param port
	 *            The port.
	 * @param channelNumber
	 *            The channel number of the port.
	 * @return the source channel.
	 * @exception IllegalActionException
	 *                If there is a problem getting information about the
	 *                receivers or constructing the new Channel.
	 */
	public Channel getSourceChannel(IOPort port, int channelNumber)
			throws IllegalActionException {
		List sourceChannels = new LinkedList();
		Receiver[][] receivers;

		if (port.isInput()) {
			receivers = port.getReceivers();
		} else {
			receivers = port.getRemoteReceivers();
		}

		TypedIOPort sourcePort = ((TypedIOPort) port.sourcePortList().get(0));

		Channel source = new Channel(sourcePort,
				sourcePort.getChannelForReceiver(receivers[0][0]));

		return source;
	}

	/**
	 * Get the write offset in the buffer of a given channel to which a token
	 * should be put. The channel is given by its containing port and the
	 * channel number in that port.
	 * 
	 * @param inputPort
	 *            The given port.
	 * @param channelNumber
	 *            The given channel number.
	 * @return The offset in the buffer of a given channel to which a token
	 *         should be put.
	 * @exception IllegalActionException
	 *                Thrown if the helper class cannot be found.
	 * @see #setWriteOffset(IOPort, int, Object)
	 */
	public Object getWriteOffset(IOPort inputPort, int channelNumber)
			throws IllegalActionException {
		if (inputPort.getContainer() == _component) {
			return ((Object[]) _writeOffsets.get(inputPort))[channelNumber];
		} else {
			CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
					.getContainer());
			return actorHelper.getWriteOffset(inputPort, channelNumber);
		}
	}

	/**
	 * Determine if the given type is primitive.
	 * 
	 * @param ptType
	 *            The given ptolemy type.
	 * @return true if the given type is primitive, otherwise false.
	 * @exception IllegalActionException
	 *                Thrown if there is no corresponding codegen type.
	 */
	public static boolean isPrimitive(Type ptType)
			throws IllegalActionException {
		return CodeGenerator._primitiveTypes.contains(codeGenType(ptType));
	}

	/**
	 * Determine if the given type is primitive.
	 * 
	 * @param cgType
	 *            The given codegen type.
	 * @return true if the given type is primitive, otherwise false.
	 */
	public static boolean isPrimitive(String cgType) {
		return CodeGenerator._primitiveTypes.contains(cgType);
	}

	/**
	 * Process the specified code, replacing macros with their values.
	 * 
	 * @param code
	 *            The code to process.
	 * @return The processed code.
	 * @exception IllegalActionException
	 *                If illegal macro names are found.
	 */
	public String processCode(String code) throws IllegalActionException {
		StringBuffer result = new StringBuffer();
		int currentPos = code.indexOf("$");

		if (currentPos < 0) {
			// No "$" in the string
			return code;
		}

		result.append(code.substring(0, currentPos));

		while (currentPos < code.length()) {
			int openParenIndex = code.indexOf("(", currentPos + 1);
			int closeParenIndex = _findCloseParen(code, openParenIndex);

			if (closeParenIndex < 0) {
				// No matching close parenthesis is found.
				result.append(code.substring(currentPos));
				return result.toString();
			}

			int nextPos = code.indexOf("$", closeParenIndex + 1);

			if (nextPos < 0) {
				// currentPos is the last "$"
				nextPos = code.length();
			}

			String subcode = code.substring(currentPos, nextPos);

			if ((currentPos > 0) && (code.charAt(currentPos - 1) == '\\')) {
				// found "\$", do not make replacement.
				// FIXME: This is wrong. subcode may contain other macros
				// to be processed.
				// Should be result.append(processCode(subcode.substring(1)));
				result.append(subcode);
				currentPos = nextPos;
				continue;
			}

			String macro = code.substring(currentPos + 1, openParenIndex);
			macro = macro.trim();

			if (!_codeGenerator.getMacros().contains(macro)) {
				result.append(subcode.substring(0, 1));
				result.append(processCode(subcode.substring(1)));
			} else {
				String name = code.substring(openParenIndex + 1,
						closeParenIndex);
				name = name.trim();

				result.append(_replaceMacro(macro, name));

				result.append(code.substring(closeParenIndex + 1, nextPos));
			}
			currentPos = nextPos;
		}

		return result.toString();
	}

	/**
	 * Reset the offsets of all channels of all input ports of the associated
	 * actor to the default value of 0.
	 * 
	 * @return The reset code of the associated actor.
	 * @exception IllegalActionException
	 *                If thrown while getting or setting the offset.
	 */
	public String resetInputPortsOffset() throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		Iterator inputPorts = ((Actor) _component).inputPortList().iterator();

		while (inputPorts.hasNext()) {
			IOPort port = (IOPort) inputPorts.next();

			for (int i = 0; i < port.getWidth(); i++) {
				Object readOffset = getReadOffset(port, i);
				if (readOffset instanceof Integer) {
					setReadOffset(port, i, new Integer(0));
				} else {
					code.append(CodeStream.indent(((String) readOffset)
							+ " = 0;" + _eol));
				}
				Object writeOffset = getWriteOffset(port, i);
				if (writeOffset instanceof Integer) {
					setWriteOffset(port, i, new Integer(0));
				} else {
					code.append(CodeStream.indent(((String) writeOffset)
							+ " = 0;" + _eol));
				}
			}
		}
		return code.toString();
	}

	/**
	 * Set the buffer size of a given port.
	 * 
	 * @param port
	 *            The given port.
	 * @param channelNumber
	 *            The given channel.
	 * @param bufferSize
	 *            The buffer size to be set to that port and channel.
	 * @see #getBufferSize(IOPort)
	 */
	public void setBufferSize(IOPort port, int channelNumber, int bufferSize) {
		int[] bufferSizes = (int[]) _bufferSizes.get(port);
		bufferSizes[channelNumber] = bufferSize;

		// perhaps this step is redundant?
		_bufferSizes.put(port, bufferSizes);
	}

	/**
	 * Set the code generator associated with this helper class.
	 * 
	 * @param codeGenerator
	 *            The code generator associated with this helper class.
	 * @see #getCodeGenerator()
	 */
	public void setCodeGenerator(CodeGenerator codeGenerator) {
		_codeGenerator = codeGenerator;
	}

	/**
	 * Set the read offset in a buffer of a given channel from which a token
	 * should be read.
	 * 
	 * @param inputPort
	 *            The given port.
	 * @param channelNumber
	 *            The given channel.
	 * @param readOffset
	 *            The offset to be set to the buffer of that channel.
	 * @exception IllegalActionException
	 *                Thrown if the helper class cannot be found.
	 * @see #getReadOffset(IOPort, int)
	 */
	public void setReadOffset(IOPort inputPort, int channelNumber,
			Object readOffset) throws IllegalActionException {
		if (inputPort.getContainer() == _component) {
			Object[] readOffsets = (Object[]) _readOffsets.get(inputPort);
			readOffsets[channelNumber] = readOffset;
		} else {
			CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
					.getContainer());
			actorHelper.setReadOffset(inputPort, channelNumber, readOffset);
		}
	}

	/**
	 * Set the write offset in a buffer of a given channel to which a token
	 * should be put.
	 * 
	 * @param inputPort
	 *            The given port.
	 * @param channelNumber
	 *            The given channel.
	 * @param writeOffset
	 *            The offset to be set to the buffer of that channel.
	 * @exception IllegalActionException
	 *                If {@link #setWriteOffset(IOPort, int, Object)} method
	 *                throws it.
	 * @see #getWriteOffset(IOPort, int)
	 */
	public void setWriteOffset(IOPort inputPort, int channelNumber,
			Object writeOffset) throws IllegalActionException {
		if (inputPort.getContainer() == _component) {
			Object[] writeOffsets = (Object[]) _writeOffsets.get(inputPort);
			writeOffsets[channelNumber] = writeOffset;
		} else {
			CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
					.getContainer());
			actorHelper.setWriteOffset(inputPort, channelNumber, writeOffset);
		}
	}

	// ///////////////////////////////////////////////////////////////////
	// // public inner classes ////

	/**
	 * A class that defines a channel object. A channel object is specified by
	 * its port and its channel index in that port.
	 */
	public class Channel {
		/**
		 * Construct the channel with the given port and channel number.
		 * 
		 * @param portObject
		 *            The given port.
		 * @param channel
		 *            The channel number of this object in the given port.
		 */
		public Channel(IOPort portObject, int channel) {
			port = portObject;
			channelNumber = channel;
		}

		/**
		 * Whether this channel is the same as the given object.
		 * 
		 * @param object
		 *            The given object.
		 * @return True if this channel is the same reference as the given
		 *         object, otherwise false;
		 */
		public boolean equals(Object object) {
			return object instanceof Channel
					&& port.equals(((Channel) object).port)
					&& channelNumber == ((Channel) object).channelNumber;
		}

		/**
		 * Return the string representation of this channel.
		 * 
		 * @return The string representation of this channel.
		 */
		public String toString() {
			return port.getName() + "_" + channelNumber;
		}

		/**
		 * Return the hash code for this channel. Implementing this method is
		 * required for comparing the equality of channels.
		 * 
		 * @return Hash code for this channel.
		 */
		public int hashCode() {
			return port.hashCode() + channelNumber;
		}

		/**
		 * The port that contains this channel.
		 */
		public IOPort port;

		/**
		 * The channel number of this channel.
		 */
		public int channelNumber;
	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods. ////

	/**
	 * This class implements a scope, which is used to generate the parsed
	 * expressions in target language.
	 */
	protected class VariableScope extends ModelScope {
		/**
		 * Construct a scope consisting of the variables of the containing actor
		 * and its containers and their scope-extending attributes.
		 */
		public VariableScope() {
			_variable = null;
		}

		/**
		 * Construct a scope consisting of the variables of the container of the
		 * given instance of Variable and its containers and their
		 * scope-extending attributes.
		 * 
		 * @param variable
		 *            The variable whose expression is under code generation
		 *            using this scope.
		 */
		public VariableScope(Variable variable) {
			_variable = variable;
		}

		// /////////////////////////////////////////////////////////////////
		// // public methods ////

		/**
		 * Look up and return the macro or expression in the target language
		 * corresponding to the specified name in the scope.
		 * 
		 * @param name
		 *            The given name string.
		 * @return The macro or expression with the specified name in the scope.
		 * @exception IllegalActionException
		 *                If thrown while getting buffer sizes or creating
		 *                ObjectToken.
		 */
		public Token get(String name) throws IllegalActionException {

			NamedObj container = _component;
			if (_variable != null) {
				container = _variable.getContainer();
			}

			Variable result = getScopedVariable(_variable, container, name);

			if (result != null) {
				// If the variable found is a modified variable, which means
				// its vaule can be directly changed during execution
				// (e.g., in commit action of a modal controller), then this
				// variable is declared in the target language and should be
				// referenced by the name anywhere it is used.
				if (_codeGenerator._modifiedVariables.contains(result)) {
					return new ObjectToken(
							_codeGenerator.generateVariableName(result));
				} else {
					// This will lead to recursive call until a variable found
					// is either directly specified by a constant or it is a
					// modified variable.
					return new ObjectToken("("
							+ getParameterValue(name, result.getContainer())
							+ ")");
				}
			} else {
				return null;
			}
		}

		/**
		 * Look up and return the type of the attribute with the specified name
		 * in the scope. Return null if such an attribute does not exist.
		 * 
		 * @param name
		 *            The name of the attribute to look up.
		 * @return The attribute with the specified name in the scope.
		 * @exception IllegalActionException
		 *                If a value in the scope exists with the given name,
		 *                but cannot be evaluated.
		 */
		public ptolemy.data.type.Type getType(String name)
				throws IllegalActionException {
			if (_variable != null) {
				return _variable.getParserScope().getType(name);
			}
			return null;
		}

		/**
		 * Look up and return the type term for the specified name in the scope.
		 * Return null if the name is not defined in this scope, or is a
		 * constant type.
		 * 
		 * @param name
		 *            The name of the type term to look up.
		 * @return The InequalityTerm associated with the given name in the
		 *         scope.
		 * @exception IllegalActionException
		 *                If a value in the scope exists with the given name,
		 *                but cannot be evaluated.
		 */
		public ptolemy.graph.InequalityTerm getTypeTerm(String name)
				throws IllegalActionException {
			if (_variable != null) {
				return _variable.getParserScope().getTypeTerm(name);
			}
			return null;
		}

		/**
		 * Return the list of identifiers within the scope.
		 * 
		 * @return The list of variable names within the scope.
		 * @exception If
		 *                there is a problem getting the identifier set from the
		 *                variable.
		 */
		public Set identifierSet() throws IllegalActionException {
			if (_variable != null) {
				return _variable.getParserScope().identifierSet();
			}
			return null;
		}

		// /////////////////////////////////////////////////////////////////
		// // private variables ////

		/**
		 * If _variable is not null, then the helper scope created is for
		 * parsing the expression specified for this variable and generating the
		 * corresponding code in target language.
		 */
		private Variable _variable = null;
	}

	/**
	 * Create the buffer size and offset maps for each input port, which is
	 * associated with this helper object. A key of the map is an IOPort of the
	 * actor. The corresponding value is an array of channel objects. The i-th
	 * channel object corresponds to the i-th channel of that IOPort. This
	 * method is used to maintain a internal HashMap of channels of the actor.
	 * The channel objects in the map are used to keep track of the buffer sizes
	 * or offsets in their buffer.
	 * 
	 * @exception IllegalActionException
	 *                If the director helper or executive director is not found,
	 *                or if {@link #setReadOffset(IOPort, int, Object)} method
	 *                throws it, or if
	 *                {@link #setWriteOffset(IOPort, int, Object)} method throws
	 *                it.
	 * 
	 */
	protected void _createBufferSizeAndOffsetMap()
			throws IllegalActionException {

		_createInputBufferSizeAndOffsetMap();

	}

	/**
	 * Create the input buffer and offset map.
	 * 
	 * @exception IllegalActionException
	 *                If thrown while getting port information.
	 */
	protected void _createInputBufferSizeAndOffsetMap()
			throws IllegalActionException {

		// We only care about input ports where data are actually stored
		// except when an output port is not connected to any input port.
		// In that case the variable corresponding to the unconnected output
		// port always has size 1 and the assignment to this variable is
		// performed just for the side effect.
		Iterator inputPorts = ((Actor) _component).inputPortList().iterator();

		while (inputPorts.hasNext()) {
			IOPort port = (IOPort) inputPorts.next();
			int length = port.getWidth();

			// if (length == 0) {
			// length = 1;
			// }
			int[] bufferSizes = new int[length];
			_bufferSizes.put(port, bufferSizes);

			Director directorHelper = (Director) _getHelper(((Actor) _component)
					.getExecutiveDirector());

			for (int i = 0; i < port.getWidth(); i++) {
				int bufferSize = directorHelper.getBufferSize(port, i);
				setBufferSize(port, i, bufferSize);
			}

			Object[] readOffsets = new Object[length];
			_readOffsets.put(port, readOffsets);

			Object[] writeOffsets = new Object[length];
			_writeOffsets.put(port, writeOffsets);

			for (int i = 0; i < length; i++) {
				setReadOffset(port, i, new Integer(0));
				setWriteOffset(port, i, new Integer(0));
			}
		}
	}

	/**
	 * Given a block name, generate code for that block. This method is called
	 * by actors helpers that have simple blocks that do not take parameters or
	 * have widths.
	 * 
	 * @param blockName
	 *            The name of the block.
	 * @return The code for the given block.
	 * @exception IllegalActionException
	 *                If illegal macro names are found, or if there is a problem
	 *                parsing the code block from the helper .c file.
	 */
	protected String _generateBlockCode(String blockName)
			throws IllegalActionException {
		// We use this method to reduce code duplication for simple blocks.
		return _generateBlockCode(blockName, new ArrayList());
	}

	/**
	 * Given a block name, generate code for that block. This method is called
	 * by actors helpers that have simple blocks that do not take parameters or
	 * have widths.
	 * 
	 * @param blockName
	 *            The name of the block.
	 * @param args
	 *            The arguments for the block.
	 * @return The code for the given block.
	 * @exception IllegalActionException
	 *                If illegal macro names are found, or if there is a problem
	 *                parsing the code block from the helper .c file.
	 */
	protected String _generateBlockCode(String blockName, ArrayList args)
			throws IllegalActionException {
		// We use this method to reduce code duplication for simple blocks.
		_codeStream.clear();
		_codeStream.appendCodeBlock(blockName, args);
		return processCode(_codeStream.toString());
	}

	/**
	 * Generate expression that evaluates to a result of equivalent value with
	 * the cast type.
	 * 
	 * @param ref
	 *            The given variable expression.
	 * @param castType
	 *            The given cast type.
	 * @param refType
	 *            The given type of the variable.
	 * @return The variable expression that evaluates to a result of equivalent
	 *         value with the cast type.
	 * @exception IllegalActionException
	 */
	protected String _generateTypeConvertMethod(String ref, String castType,
			String refType) throws IllegalActionException {

		if (castType == null || refType == null || castType.equals(refType)) {
			return ref;
		}

		if (isPrimitive(castType)) {
			ref = refType + "to" + castType + "(" + ref + ")";
		} else if (isPrimitive(refType)) {
			ref = "$new(" + refType + "(" + ref + "))";
		}

		if (!castType.equals("Token") && !isPrimitive(castType)) {
			ref = "$typeFunc(TYPE_" + castType + "::convert(" + ref + "))";
		}

		return processCode(ref);
	}

	/**
	 * Generate the type conversion statements for the two given channels.
	 * 
	 * @param source
	 *            The given source channel.
	 * @param sink
	 *            The given sink channel.
	 * @return The type convert statement for assigning the converted source
	 *         variable to the sink variable.
	 * @exception IllegalActionException
	 *                If there is a problem getting the helpers for the ports or
	 *                if the conversion cannot be handled.
	 */
	protected String _generateTypeConvertStatements(Channel source, Channel sink)
			throws IllegalActionException {

		StringBuffer statements = new StringBuffer();

		int rate = Math.max(DFUtilities.getTokenProductionRate(source.port),
				DFUtilities.getTokenConsumptionRate(source.port));

		for (int offset = 0; offset < rate || (offset == 0 && rate == 0); offset++) {
			statements.append(CodeStream.indent(_generateTypeConvertStatement(
					source, sink, offset)));
		}
		return processCode(statements.toString());
	}

	/**
	 * Generate the type conversion statement for the particular offset of the
	 * two given channels. This assumes that the offset is the same for both
	 * channel. Advancing the offset of one has to advance the offset of the
	 * other.
	 * 
	 * @param source
	 *            The given source channel.
	 * @param sink
	 *            The given sink channel.
	 * @param offset
	 *            The given offset.
	 * @return The type convert statement for assigning the converted source
	 *         variable to the sink variable with the given offset.
	 * @exception IllegalActionException
	 *                If there is a problem getting the helpers for the ports or
	 *                if the conversion cannot be handled.
	 */
	protected String _generateTypeConvertStatement(Channel source,
			Channel sink, int offset) throws IllegalActionException {

		Type sourceType = ((TypedIOPort) source.port).getType();
		Type sinkType = ((TypedIOPort) sink.port).getType();

		// In a modal model, a refinement may have an output port which is
		// not connected inside, in this case the type of the port is
		// unknown and there is no need to generate type conversion code
		// because there is no token transferred from the port.
		if (sourceType == BaseType.UNKNOWN) {
			return "";
		}

		// The references are associated with their own helper, so we need
		// to find the associated helper.
		// String sourcePortChannel = source.port.getName() + "#"
		// + source.channelNumber + ", " + offset;
		// String sourceRef = ((CodeGeneratorHelper) _getHelper(source.port
		// .getContainer())).getReference(sourcePortChannel);
		String sourceRef = _getTypeConvertReference(source);
		int rate = Math.max(DFUtilities.getTokenProductionRate(source.port),
				DFUtilities.getTokenConsumptionRate(source.port));
		if (rate > 1) {
			sourceRef += "[" + offset + "]";
		}

		String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
				+ ", " + offset;

		// For composite actor, generate a variable corresponding to
		// the inside receiver of an output port.
		// FIXME: I think checking sink.port.isOutput() is enough here.
		if (sink.port.getContainer() instanceof CompositeActor
				&& sink.port.isOutput()) {
			sinkPortChannel = "@" + sinkPortChannel;
		}
		String sinkRef = ((CodeGeneratorHelper) _getHelper(sink.port
				.getContainer())).getReference(sinkPortChannel, true);

		// When the sink port is contained by a modal controller, it is
		// possible that the port is both input and output port. we need
		// to pay special attention. Directly calling getReference() will
		// treat it as output port and this is not correct.
		// FIXME: what about offset?
		if (sink.port.getContainer() instanceof ModalController) {
			sinkRef = generateName(sink.port);
			if (sink.port.isMultiport()) {
				sinkRef = sinkRef + "[" + sink.channelNumber + "]";
			}
		}

		String result = sourceRef;

		if (sinkType != sourceType) {
			if (isPrimitive(sinkType)) {

				result = codeGenType(sourceType) + "to" + codeGenType(sinkType)
						+ "(" + result + ")";

			} else if (isPrimitive(sourceType)) {
				result = "$new(" + codeGenType(sourceType) + "(" + result
						+ "))";
			}

			if (sinkType != BaseType.SCALAR && sinkType != BaseType.GENERAL
					&& !isPrimitive(sinkType)) {
				if (sinkType instanceof ArrayType) {
					if (isPrimitive(sourceType)) {
						result = "$new(Array(1, 1, " + result + ", TYPE_"
								+ codeGenType(sourceType) + "))";
					}
					Type elementType = ((ArrayType) sinkType).getElementType();
					if (elementType != BaseType.SCALAR) {
						result = "$typeFunc(TYPE_"
								+ codeGenType(sinkType)
								+ "::convert("
								+ result
								+ ", (int) TYPE_"
								+ codeGenType(((ArrayType) sinkType)
										.getElementType()) + "))";
					}
				} else {
					result = "$typeFunc(TYPE_" + codeGenType(sinkType)
							+ "::convert(" + result + "))";
				}
			}
		}
		return sinkRef + " = " + result + ";" + _eol;
	}

	/**
	 * Return the channel number and offset given in a string. The result is an
	 * integer array of length 2. The first element indicates the channel
	 * number, the second the offset. If either element is -1, it means that
	 * channel/offset is not specified.
	 * 
	 * @param name
	 *            The given string.
	 * @return An integer array of length 2, indicating the channel number and
	 *         offset.
	 * @exception IllegalActionException
	 *                If the channel number or offset specified in the given
	 *                string is illegal.
	 */
	protected String[] _getChannelAndOffset(String name)
			throws IllegalActionException {

		// FIXME: the comment says that this method return -1 for
		// unspecified channel or offset. However, result is initialized to
		// empty strings.
		String[] result = { "", "" };

		StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);
		tokenizer.nextToken();

		if (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			if (token.equals("#")) {
				result[0] = tokenizer.nextToken().trim();

				if (tokenizer.hasMoreTokens()) {
					if (tokenizer.nextToken().equals(",")) {
						result[1] = tokenizer.nextToken().trim();
					}
				}
			} else if (token.equals(",")) {
				result[1] = tokenizer.nextToken().trim();
			}
		}
		return result;
	}

	/**
	 * Get the code generator helper associated with the given component.
	 * 
	 * @param component
	 *            The given component.
	 * @return The code generator helper.
	 * @exception IllegalActionException
	 *                If the helper class cannot be found.
	 */
	protected ComponentCodeGenerator _getHelper(NamedObj component)
			throws IllegalActionException {
		return _codeGenerator._getHelper(component);
	}

	/**
	 * Return a number of spaces that is proportional to the argument. If the
	 * argument is negative or zero, return an empty string.
	 * 
	 * @param level
	 *            The level of indenting represented by the spaces.
	 * @return A string with zero or more spaces.
	 */
	protected static String _getIndentPrefix(int level) {
		return StringUtilities.getIndentPrefix(level);
	}

	/**
	 * Get the set of channels that need to be type converted.
	 * 
	 * @return Set of channels that need to be type converted.
	 */
	protected Set _getTypeConvertChannels() {
		return _portConversions.keySet();
	}

	/**
	 * Generate a variable reference for the given channel. This varaible
	 * reference is needed for type conversion. The source helper get this
	 * reference instead of using the sink reference directly. This method
	 * assumes the given channel is a source (output) channel.
	 * 
	 * @param channel
	 *            The given source channel.
	 * @return The variable reference for the given channel.
	 */
	protected String _getTypeConvertReference(Channel channel) {
		return generateName(channel.port) + "_" + channel.channelNumber;
	}

	/**
	 * Return the replacement string of the given macro. Subclass of
	 * CodeGenerator may overriding this method to extend or support a different
	 * set of macros.
	 * 
	 * @param macro
	 *            The given macro.
	 * @param parameter
	 *            The given parameter to the macro.
	 * @return The replacement string of the given macro.
	 * @exception IllegalActionException
	 *                Thrown if the given macro or parameter is not valid.
	 */
	protected String _replaceMacro(String macro, String parameter)
			throws IllegalActionException {
		if (macro.equals("ref")) {
			return getReference(parameter);
		} else if (macro.equals("targetType")) {
			TypedIOPort port = getPort(parameter);
			if (port == null) {
				throw new IllegalActionException(parameter
						+ " is not a port. $type macro takes in a port.");
			}
			return cType(port.getType());

		} else if (macro.equals("type") || macro.equals("cgType")) {

			TypedIOPort port = getPort(parameter);

			if (port == null) {
				throw new IllegalActionException(parameter
						+ " is not a port. $type macro takes in a port.");
			}
			String type = "";
			if (macro.equals("type")) {
				type = "TYPE_";
			}
			return type + codeGenType(port.getType());

		} else if (macro.equals("val")) {
			return getParameterValue(parameter, _component);
		} else if (macro.equals("size")) {
			return "" + getSize(parameter);
		} else if (macro.equals("actorSymbol")) {
			if (parameter.trim().length() == 0) {
				return generateVariableName(_component);
			} else {
				return generateVariableName(_component) + "_" + parameter;
			}
		} else if (macro.equals("actorClass")) {
			return _component.getClassName().replace('.', '_') + "_"
					+ parameter;
		} else if (macro.equals("new")) {
			return getNewInvocation(parameter);
		} else if (macro.equals("tokenFunc")) {
			return getFunctionInvocation(parameter, false);
		} else if (macro.equals("typeFunc")) {
			return getFunctionInvocation(parameter, true);
		} else {
			// This macro is not handled.
			throw new IllegalActionException("Macro is not handled.");
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////

	/**
	 * Generate a variable name for the NamedObj.
	 * 
	 * @param namedObj
	 *            The NamedObj to generate variable name for.
	 * @see ptolemy.codegen.kernel.CodeGenerator#generateVariableName(NamedObj)
	 */
	public String generateVariableName(NamedObj namedObj) {
		return _codeGenerator.generateVariableName(namedObj);
	}

	/**
	 * A hashmap that keeps track of the bufferSizes of each channel of the
	 * actor.
	 */
	protected HashMap _bufferSizes = new HashMap();

	/**
	 * The code generator that contains this helper class.
	 */
	protected CodeGenerator _codeGenerator;

	/**
	 * The code stream associated with this helper.
	 */
	protected CodeStream _codeStream = new CodeStream(this);

	/**
	 * End of line character. Under Unix: "\n", under Windows: "\n\r". We use a
	 * end of line charactor so that the files we generate have the proper end
	 * of line character for use by other native tools.
	 */
	protected static String _eol;
	static {
		_eol = StringUtilities.getProperty("line.separator");
	}

	/** The parse tree to use with expressions. */
	protected ParseTreeCodeGenerator _parseTreeCodeGenerator;

	/**
	 * A HashMap that contains mapping for ports and their conversion method.
	 * Ports that does not need to be converted do NOT have record in this map.
	 * The codegen kernel record this mapping during the first pass over the
	 * model. This map is used later in the code generation phase.
	 */
	protected Hashtable _portConversions = new Hashtable();

	/**
	 * A hashmap that keeps track of the read offsets of each input channel of
	 * the actor.
	 */
	protected HashMap _readOffsets = new HashMap();

	/**
	 * A hashset that keeps track of parameters that are referenced for the
	 * associated actor.
	 */
	protected HashSet _referencedParameters = new HashSet();

	/**
	 * A hashmap that keeps track of the write offsets of each input channel of
	 * the actor.
	 */
	protected HashMap _writeOffsets = new HashMap();

	/**
	 * Indent string for indent level 1.
	 * 
	 * @see ptolemy.util.StringUtilities#getIndentPrefix(int)
	 */
	protected static String _INDENT1 = StringUtilities.getIndentPrefix(1);

	/**
	 * Indent string for indent level 2.
	 * 
	 * @see ptolemy.util.StringUtilities#getIndentPrefix(int)
	 */
	protected static String _INDENT2 = StringUtilities.getIndentPrefix(2);

	/**
	 * Find the paired close parenthesis given a string and an index which is
	 * the position of an open parenthesis. Return -1 if no paired close
	 * parenthesis is found.
	 * 
	 * @param string
	 *            The given string.
	 * @param pos
	 *            The given index.
	 * @return The index which indicates the position of the paired close
	 *         parenthesis of the string.
	 * @exception IllegalActionException
	 *                If the character at the given position of the string is
	 *                not an open parenthesis.
	 */
	private int _findCloseParen(String string, int pos)
			throws IllegalActionException {
		if (string.charAt(pos) != '(') {
			throw new IllegalActionException(_component,
					"The character at index " + pos + " of string: " + string
							+ " is not a open parenthesis.");
		}

		int nextOpenParen = string.indexOf("(", pos + 1);

		if (nextOpenParen < 0) {
			nextOpenParen = string.length();
		}

		int nextCloseParen = string.indexOf(")", pos);

		if (nextCloseParen < 0) {
			return -1;
		}

		int count = 1;
		int beginIndex = pos + 1;

		while (beginIndex > 0) {
			if (nextCloseParen < nextOpenParen) {
				count--;

				if (count == 0) {
					return nextCloseParen;
				}

				beginIndex = nextCloseParen + 1;
				nextCloseParen = string.indexOf(")", beginIndex);

				if (nextCloseParen < 0) {
					return -1;
				}
			}

			if (nextOpenParen < nextCloseParen) {
				count++;
				beginIndex = nextOpenParen + 1;
				nextOpenParen = string.indexOf("(", beginIndex);

				if (nextOpenParen < 0) {
					nextOpenParen = string.length();
				}
			}
		}

		return -1;
	}

	/**
	 * Get the list of sink channels that the given source channel needs to be
	 * type converted to.
	 * 
	 * @param source
	 *            The given source channel.
	 * @return List of sink channels that the given source channel needs to be
	 *         type converted to.
	 */
	private List _getTypeConvertSinkChannels(Channel source) {
		if (_portConversions.containsKey(source)) {
			return ((List) _portConversions.get(source));
		}
		return new ArrayList();
	}

	/**
	 * Return true if the given string can be parse as an integer; otherwise,
	 * return false.
	 * 
	 * @param numberString
	 *            The given number string.
	 * @return True if the given string can be parse as an integer; otherwise,
	 *         return false.
	 */
	private boolean _isInteger(String numberString) {
		try {
			Integer.parseInt(numberString);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	/**
	 * Mark the given connection between the source and the sink channels as
	 * type conversion required.
	 * 
	 * @param source
	 *            The given source channel.
	 * @param sink
	 *            The given input channel.
	 */
	private void _markTypeConvert(Channel source, Channel sink) {
		List sinks;
		if (_portConversions.containsKey(source)) {
			sinks = (List) _portConversions.get(source);
		} else {
			sinks = new ArrayList();
			_portConversions.put(source, sinks);
		}
		sinks.add(sink);
	}

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/** The associated component. */
	private NamedObj _component;

	/**
	 * The code block table that stores the code block body (StringBuffer) with
	 * the code block name (String) as key.
	 */
	private static final String[] _defaultBlocks = { "preinitBlock",
			"initBlock", "fireBlock", "wrapupBlock" };

}
