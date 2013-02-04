/* Base class for code generators.

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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// CodeGenerator

/**
 * Base class for code generator.
 * 
 * @author Edward A. Lee, Gang Zhou, Ye Zhou, Contributors: Christopher Brooks
 * @version $Id: CodeGenerator.java,v 1.147.2.2 2007/01/05 22:06:10 cxh Exp $
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGenerator extends Attribute implements ComponentCodeGenerator {
	/**
	 * Create a new instance of the code generator.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of the code generator.
	 * @exception IllegalActionException
	 *                If the super class throws the exception or error occurs
	 *                when setting the file path.
	 * @exception NameDuplicationException
	 *                If the super class throws the exception or an error occurs
	 *                when setting the file path.
	 */
	public CodeGenerator(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);

		codeDirectory = new FileParameter(this, "codeDirectory");
		codeDirectory.setExpression("$HOME/codegen/");

		// FIXME: This should not be necessary, but if we don't
		// do it, then getBaseDirectory() thinks we are in the current dir.
		codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());
		new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
		new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

		compile = new Parameter(this, "compile");
		compile.setTypeEquals(BaseType.BOOLEAN);
		compile.setExpression("true");

		generatorPackage = new StringParameter(this, "generatorPackage");

		generateComment = new Parameter(this, "generateComment");
		generateComment.setTypeEquals(BaseType.BOOLEAN);
		generateComment.setExpression("true");

		inline = new Parameter(this, "inline");
		inline.setTypeEquals(BaseType.BOOLEAN);
		inline.setExpression("true");

		overwriteFiles = new Parameter(this, "overwriteFiles");
		overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
		overwriteFiles.setExpression("true");

		run = new Parameter(this, "run");
		run.setTypeEquals(BaseType.BOOLEAN);
		run.setExpression("true");

		_attachText("_iconDescription", "<svg>\n"
				+ "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
				+ "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
				+ "style=\"font-size:12; font-family:SansSerif; fill:white\">"
				+ "Double click to\ngenerate code.</text></svg>");

		_model = (CompositeEntity) getContainer();

		// FIXME: We may not want this GUI dependency here...
		// This attribute could be put in the MoML in the library instead
		// of here in the Java code.
		new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");
	}

	// /////////////////////////////////////////////////////////////////
	// // parameters ////

	/**
	 * The directory in which to put the generated code. This is a file
	 * parameter that must specify a directory. The default is $HOME/codegen.
	 */
	public FileParameter codeDirectory;

	/**
	 * If true, then compile the generated code. The default value is a
	 * parameter with the value true.
	 */
	public Parameter compile;

	/**
	 * If true, generate comments in the output code; otherwise, no comments is
	 * generated. The default value is a parameter with the value true.
	 */
	public Parameter generateComment;

	/**
	 * The name of the package in which to look for helper class code
	 * generators. This is a string that defaults to "ptolemy.codegen.c".
	 */
	public StringParameter generatorPackage;

	/**
	 * If true, generate file with no functions. If false, generate file with
	 * functions. The default value is a parameter with the value true.
	 */
	public Parameter inline;

	/**
	 * If true, overwrite preexisting files. The default value is a parameter
	 * with the value true.
	 */
	public Parameter overwriteFiles;

	/**
	 * If true, then run the generated code. The default value is a parameter
	 * with the value true.
	 */
	public Parameter run;

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Analyze the model to find out what connections need to be type converted.
	 * This should be called before all the generate methods.
	 * 
	 * @exception IllegalActionException
	 *                Thrown if the helper of the top composite actor is
	 *                unavailable.
	 */
	public void analyzeTypeConvert() throws IllegalActionException {
		((CodeGeneratorHelper) _getHelper(getContainer())).analyzeTypeConvert();
	}

	/**
	 * Add an include command line argument the compile command.
	 * 
	 * @param includeCommand
	 *            The include command, for example "-I/usr/local/include".
	 */
	public void addInclude(String includeCommand) {
		_includes.add(includeCommand);
	}

	/**
	 * Add a library command line argument the compile command.
	 * 
	 * @param libraryCommand
	 *            The library command, for example "-L/usr/local/lib".
	 */
	public void addLibrary(String libraryCommand) {
		_libraries.add(libraryCommand);
	}

	/**
	 * Return a formatted comment containing the specified string. In this base
	 * class, the comments is a C-style comment, which begins with
	 * "\/*" and ends with "*\/".
	 * 
	 * @param comment
	 *            The string to put in the comment.
	 * @return A formatted comment.
	 */
	public String comment(String comment) {
		return formatComment(comment);
	}

	/**
	 * Return a formatted comment containing the specified string with a
	 * specified indent level.
	 * 
	 * @param comment
	 *            The string to put in the comment.
	 * @param indentLevel
	 *            The indentation level.
	 * @return A formatted comment.
	 */
	public String comment(int indentLevel, String comment) {
		if (generateComment.getExpression().equals("true")) {
			return StringUtilities.getIndentPrefix(indentLevel)
					+ formatComment(comment);
		} else {
			return "";
		}
	}

	/**
	 * Return a formatted comment containing the specified string. In this base
	 * class, the comments is a C-style comment, which begins with
	 * "\/*" and ends with "*\/" followed by the platform dependent end of line
	 * character(s): under Unix: "\n", under Windows: "\n\r". Subclasses may
	 * override this produce comments that match the code generation language.
	 * 
	 * @param comment
	 *            The string to put in the comment.
	 * @return A formatted comment.
	 */
	public String formatComment(String comment) {
		return "/* " + comment + " */" + _eol;
	}

	/**
	 * Generate the body code that lies between initialize and wrapup. In this
	 * base class, nothing is generated.
	 * 
	 * @return The empty string.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateBodyCode() throws IllegalActionException {

		return "";
	}

	/**
	 * Generate code and write it to the file specified by the
	 * <i>codeDirectory</i> parameter.
	 * 
	 * @exception KernelException
	 *                If the target file cannot be overwritten or write-to-file
	 *                throw any exception.
	 */
	public void generateCode() throws KernelException {
		generateCode(new StringBuffer());
	}

	/**
	 * Generate code and append it to the given string buffer.
	 * 
	 * Write the code to the directory specified by the codeDirectory parameter.
	 * The file name is a sanitized version of the model name with a suffix that
	 * is based on last package name of the <i>generatorPackage</i> parameter.
	 * Thus if the <i>codeDirectory</i> is <code>$HOME</code>, the name of the
	 * model is <code>Foo</code> and the <i>generatorPackage</i> is
	 * <code>ptolemy.codegen.c</code>, then the file that is written will be
	 * <code>$HOME/Foo.c</code> This method is the main entry point.
	 * 
	 * @param code
	 *            The given string buffer.
	 * @return The return value of the last subprocess that was executed. or -1
	 *         if no commands were executed.
	 * @exception KernelException
	 *                If the target file cannot be overwritten or write-to-file
	 *                throw any exception.
	 */
	public int generateCode(StringBuffer code) throws KernelException {

		// List actors = get all actors
		// for each actor in actors
		// actor._analyzeActor();

		_codeFileName = null;
		_sanitizedModelName = StringUtilities.sanitizeName(_model.getName());
		boolean inline = ((BooleanToken) this.inline.getToken()).booleanValue();

		// We separate the generation and the appending into 2 phases.
		// This would be convenience for making addition passes, and
		// for adding additional code into different sections.
		analyzeTypeConvert();

		// FIXME: these should be in the order they are used unless
		// otherwise necessary. If it is necessary, it should be noted.

		String sharedCode = generateSharedCode();
		String includeFiles = generateIncludeFiles();
		String preinitializeCode = generatePreinitializeCode();
		CodeStream.setIndentLevel(1);
		String initializeCode = generateInitializeCode();
		CodeStream.setIndentLevel(2);
		String bodyCode = generateBodyCode();
		CodeStream.setIndentLevel(0);
		String mainEntryCode = generateMainEntryCode();
		String mainExitCode = generateMainExitCode();
		String initializeEntryCode = generateInitializeEntryCode();
		String initializeExitCode = generateInitializeExitCode();
		String initializeProcedureName = generateInitializeProcedureName();
		String wrapupEntryCode = generateWrapupEntryCode();
		String wrapupExitCode = generateWrapupExitCode();
		String wrapupProcedureName = generateWrapupProcedureName();

		String fireFunctionCode = null;
		if (!inline) {
			CodeStream.setIndentLevel(1);
			fireFunctionCode = generateFireFunctionCode();
			CodeStream.setIndentLevel(0);
		}
		CodeStream.setIndentLevel(1);
		String wrapupCode = generateWrapupCode();
		CodeStream.setIndentLevel(0);

		// Generating variable declarations needs to happen after buffer
		// sizes are set(?).
		String variableDeclareCode = generateVariableDeclaration();
		String variableInitCode = generateVariableInitialization();
		// generate type resolution code has to be after
		// fire(), wrapup(), preinit(), init()...
		String typeResolutionCode = generateTypeConvertCode();

		// The appending phase.
		code.append(includeFiles);
		code.append(typeResolutionCode);
		code.append(sharedCode);
		code.append(variableDeclareCode);
		code.append(preinitializeCode);

		if (!inline) {
			code.append(fireFunctionCode);
		}

		code.append(initializeEntryCode);
		code.append(variableInitCode);
		code.append(initializeCode);
		code.append(initializeExitCode);

		code.append(wrapupEntryCode);
		code.append(wrapupCode);
		code.append(wrapupExitCode);

		code.append(mainEntryCode);

		// If the container is in the top level, we are generating code
		// for the whole model.
		if (isTopLevel()) {
			code.append(initializeProcedureName);
		}

		code.append(bodyCode);

		// If the container is in the top level, we are generating code
		// for the whole model.
		if (isTopLevel()) {
			code.append(wrapupProcedureName);
		}

		code.append(mainExitCode);

		if (_executeCommands == null) {
			_executeCommands = new StreamExec();
		}

		_codeFileName = _writeCode(code);
		_writeMakefile();
		return _executeCommands();
	}

	/**
	 * Generate code for a model.
	 * 
	 * @param args
	 *            An array of Strings, each element names a MoML file containing
	 *            a model.
	 * @return The return value of the last subprocess that was run to compile
	 *         or run the model. Return -1 if called with no arguments. Return
	 *         -2 if no CodeGenerator was created.
	 * @exception Exception
	 *                If any error occurs.
	 */
	public static int generateCode(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("Usage: java -classpath $PTII "
					+ "ptolemy.codegen.kernel.CodeGenerator model.xml "
					+ "[model.xml . . .]" + _eol
					+ "  The arguments name MoML files containing models");
			return -1;
		}

		CodeGenerator codeGenerator = null;

		// See MoMLSimpleApplication for similar code
		MoMLParser parser = new MoMLParser();
		MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
		MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

		for (int i = 0; i < args.length; i++) {
			// Note: the code below uses explicit try catch blocks
			// so we can provide very clear error messages about what
			// failed to the end user. The alternative is to wrap the
			// entire body in one try/catch block and say
			// "Code generation failed for foo", which is not clear.
			URL modelURL;

			try {
				modelURL = new File(args[i]).toURL();
			} catch (Exception ex) {
				throw new Exception("Could not open \"" + args[i] + "\"", ex);
			}

			CompositeActor toplevel = null;

			try {
				try {
					toplevel = (CompositeActor) parser.parse(null, modelURL);
				} catch (Exception ex) {
					throw new Exception("Failed to parse \"" + args[i] + "\"",
							ex);
				}

				// Get all instances of this class contained in the model
				List codeGenerators = toplevel
						.attributeList(CodeGenerator.class);

				if (codeGenerators.size() == 0) {
					// Add a codeGenerator
					codeGenerator = new StaticSchedulingCodeGenerator(toplevel,
							"CodeGenerator_AutoAdded");
				} else {
					// Get the last CodeGenerator in the list, maybe
					// it was added last?
					codeGenerator = (CodeGenerator) codeGenerators
							.get(codeGenerators.size() - 1);
				}

				try {
					codeGenerator.generateCode();
				} catch (KernelException ex) {
					throw new Exception("Failed to generate code for \""
							+ args[i] + "\"", ex);
				}
			} finally {
				// Destroy the top level so that we avoid
				// problems with running the model after generating code
				if (toplevel != null) {
					toplevel.setContainer(null);
				}
			}
		}
		if (codeGenerator != null) {
			return codeGenerator.getExecuteCommands()
					.getLastSubprocessReturnCode();
		}
		return -2;
	}

	/**
	 * Generate The fire function code. This method is called when the firing
	 * code of each actor is not inlined. Each actor's firing code is in a
	 * function with the same name as that of the actor.
	 * 
	 * @return The fire function code of the containing composite actor.
	 * @exception IllegalActionException
	 *                If thrown while generating fire code.
	 */
	public String generateFireFunctionCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
		code.append(compositeActorHelper.generateFireFunctionCode());
		return code.toString();
	}

	/**
	 * Generate the function table. In this base class return the empty string.
	 * 
	 * @param types
	 *            An array of types.
	 * @param functions
	 *            An array of functions.
	 * @return The code that declares functions.
	 */
	public Object generateFunctionTable(Object[] types, Object[] functions) {

		return "";
	}

	/**
	 * Generate include files.
	 * 
	 * @return The include files.
	 * @exception IllegalActionException
	 *                If the helper class for some actor cannot be found.
	 */
	public String generateIncludeFiles() throws IllegalActionException {

		return "";
	}

	/**
	 * Return the code associated with initialization of the containing
	 * composite actor. This method calls the generateInitializeCode() method of
	 * the code generator helper associated with the model director.
	 * 
	 * @return The initialize code of the containing composite actor.
	 * @exception IllegalActionException
	 *                If the helper class for the model director cannot be found
	 *                or if an error occurs when the director helper generates
	 *                initialize code.
	 */
	public String generateInitializeCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		// code.append(comment("Initialize " + getContainer().getFullName()));

		ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
		code.append(compositeActorHelper.generateInitializeCode());
		return code.toString();
	}

	/**
	 * Generate the initialization procedure entry point.
	 * 
	 * @return a string for the initialization procedure entry point.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateInitializeEntryCode() throws IllegalActionException {

		return comment("initialization entry code");
	}

	/**
	 * Generate the initialization procedure exit point.
	 * 
	 * @return a string for the initialization procedure exit point.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateInitializeExitCode() throws IllegalActionException {

		return comment("initialization exit code");
	}

	/**
	 * Generate the initialization procedure name.
	 * 
	 * @return a string for the initialization procedure name.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateInitializeProcedureName()
			throws IllegalActionException {

		return "";
	}

	/**
	 * Generate the main entry point.
	 * 
	 * @return Return the definition of the main entry point for a program.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateMainEntryCode() throws IllegalActionException {

		return comment("main entry code");
	}

	/**
	 * Generate the main exit point.
	 * 
	 * @return Return a string that declares the end of the main() function.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateMainExitCode() throws IllegalActionException {

		return comment("main exit code");
	}

	/**
	 * Generate preinitialize code (if there is any). This method calls the
	 * generatePreinitializeCode() method of the code generator helper
	 * associated with the model director
	 * 
	 * @return The preinitialize code of the containing composite actor.
	 * @exception IllegalActionException
	 *                If the helper class for the model director cannot be
	 *                found, or if an error occurs when the director helper
	 *                generates preinitialize code.
	 */
	public String generatePreinitializeCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());

		try {
			_modifiedVariables = compositeActorHelper.getModifiedVariables();

			code.append(compositeActorHelper.generatePreinitializeCode());

			code.append(compositeActorHelper.createOffsetVariablesIfNeeded());
		} catch (Throwable throwable) {
			throw new IllegalActionException(
					compositeActorHelper.getComponent(), throwable,
					"Failed to generate preinitialize code");
		}
		return code.toString();
	}

	/**
	 * Generate code shared by helper actors, including globally defined data
	 * struct types and static methods or variables shared by multiple instances
	 * of the same helper actor type.
	 * 
	 * @return The shared code of the containing composite actor.
	 * @exception IllegalActionException
	 *                If an error occurrs when generating the globally shared
	 *                code, or if the helper class for the model director cannot
	 *                be found, or if an error occurs when the helper actor
	 *                generates the shared code.
	 */
	public String generateSharedCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();

		ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
		Set sharedCodeBlocks = compositeActorHelper.getSharedCode();
		Iterator blocks = sharedCodeBlocks.iterator();
		while (blocks.hasNext()) {
			String block = (String) blocks.next();
			code.append(block);
		}

		if (code.length() > 0) {
			code.insert(0, _eol
					+ comment("Generate shared code for "
							+ getContainer().getName()));
			code.append(comment("Finished generating shared code for "
					+ getContainer().getName()));
		}

		return code.toString();
	}

	/**
	 * Generate type conversion code.
	 * 
	 * @return The type conversion code.
	 * @exception IllegalActionException
	 *                If an error occurrs when generating the type conversion
	 *                code, or if the helper class for the model director cannot
	 *                be found, or if an error occurs when the helper actor
	 *                generates the type conversion code.
	 */
	public String generateTypeConvertCode() throws IllegalActionException {

		return "";
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
		// code.append(_eol + _eol);
		// code.append(comment(0, "Variable Declarations "
		// + getContainer().getFullName()));

		ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
		code.append(compositeActorHelper.generateVariableDeclaration());
		return code.toString();
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
		// code.append(_eol + _eol);
		// code.append(comment(1, "Variable initialization "
		// + getContainer().getFullName()));

		ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());

		code.append(compositeActorHelper.generateVariableInitialization());
		return code.toString();
	}

	/**
	 * Generate variable name for the given attribute. The reason to append
	 * underscore is to avoid conflict with the names of other objects. For
	 * example, the paired PortParameter and ParameterPort have the same name.
	 * 
	 * @param attribute
	 *            The attribute to generate variable name for.
	 * @return The generated variable name.
	 */
	public String generateVariableName(NamedObj attribute) {
		return CodeGeneratorHelper.generateName(attribute) + "_";
	}

	/**
	 * Generate into the specified code stream the code associated with wrapping
	 * up the container composite actor. This method calls the
	 * generateWrapupCode() method of the code generator helper associated with
	 * the director of this container.
	 * 
	 * @return The wrapup code of the containing composite actor.
	 * @exception IllegalActionException
	 *                If the helper class for the model director cannot be
	 *                found.
	 */
	public String generateWrapupCode() throws IllegalActionException {
		StringBuffer code = new StringBuffer();
		// code.append(comment(1, "Wrapup " + getContainer().getFullName()));

		ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
		code.append(compositeActorHelper.generateWrapupCode());
		return code.toString();
	}

	/**
	 * Generate the wrapup procedure entry point.
	 * 
	 * @return a string for the wrapup procedure entry point.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateWrapupEntryCode() throws IllegalActionException {

		return comment("wrapup entry code");
	}

	/**
	 * Generate the wrapup procedure exit point.
	 * 
	 * @return a string for the wrapup procedure exit point.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateWrapupExitCode() throws IllegalActionException {

		return comment("wrapup exit code");
	}

	/**
	 * Generate the wrapup procedure name.
	 * 
	 * @return a string for the wrapup procedure name.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public String generateWrapupProcedureName() throws IllegalActionException {

		return "";
	}

	/**
	 * Return the associated component, which is always the container.
	 * 
	 * @return The helper to generate code.
	 */
	public NamedObj getComponent() {
		return getContainer();
	}

	/**
	 * Get the command executor, which can be either non-graphical or graphical.
	 * The initial default is non-graphical, which means that stderr and stdout
	 * from subcommands is written to the console.
	 * 
	 * @return executeCommands The subprocess command executor.
	 * @see #setExecuteCommands(ExecuteCommands)
	 */
	public ExecuteCommands getExecuteCommands() {
		return _executeCommands;
	}

	/**
	 * Return the name of the code file that was written, if any. If no file was
	 * written, then return null.
	 * 
	 * @return The name of the file that was written.
	 */
	public String getCodeFileName() {
		return _codeFileName;
	}

	/**
	 * Return a list of macros this code generator supports.
	 * 
	 * @return Returns the _macros.
	 */
	public List getMacros() {
		return _macros;
	}

	/**
	 * Return the set of modified variables.
	 * 
	 * @return The set of modified variables.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	public Set getModifiedVariables() throws IllegalActionException {
		return _modifiedVariables;
	}

	/**
	 * Test if the containing actor is in the top level.
	 * 
	 * @return true if the containing actor is in the top level.
	 */
	public boolean isTopLevel() {
		return getContainer().getContainer() == null;
	}

	/**
	 * Generate code for a model.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 *  java -classpath $PTII ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
	 * </pre>
	 * 
	 * or
	 * 
	 * <pre>
	 *  $PTII/bin/ptinvoke ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
	 * </pre>
	 * 
	 * @param args
	 *            An array of Strings, each element names a MoML file containing
	 *            a model.
	 * @exception Exception
	 *                If any error occurs.
	 */
	public static void main(String[] args) throws Exception {
		generateCode(args);
	}

	/**
	 * This method is used to set the code generator for a helper class. Since
	 * this is not a helper class for a component, this method does nothing.
	 * 
	 * @param codeGenerator
	 *            The given code generator.
	 */
	public void setCodeGenerator(CodeGenerator codeGenerator) {
	}

	/**
	 * Set the command executor, which can be either non-graphical or graphical.
	 * The initial default is non-graphical, which means that stderr and stdout
	 * from subcommands is written to the console.
	 * 
	 * @param executeCommands
	 *            The subprocess command executor.
	 * @see #getExecuteCommands()
	 */
	public void setExecuteCommands(ExecuteCommands executeCommands) {
		_executeCommands = executeCommands;
	}

	/**
	 * Set the container of this object to be the given container.
	 * 
	 * @param container
	 *            The given container.
	 * @exception IllegalActionException
	 *                If the given container is not null and not an instance of
	 *                CompositeEntity.
	 * @exception NameDuplicationException
	 *                If there already exists a container with the same name.
	 */
	public void setContainer(NamedObj container) throws IllegalActionException,
			NameDuplicationException {
		if ((container != null) && !(container instanceof CompositeEntity)) {
			throw new IllegalActionException(this, container,
					"CodeGenerator can only be contained"
							+ " by CompositeEntity");
		}

		super.setContainer(container);
	}

	// /////////////////////////////////////////////////////////////////
	// // protected methods ////

	/**
	 * Execute the compile and run commands in the <i>codeDirectory</i>
	 * directory. In this base class, 0 is returned by default.
	 * 
	 * @return The result of the execution.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	protected int _executeCommands() throws IllegalActionException {
		return 0;
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
	protected ActorCodeGenerator _getHelper(NamedObj component)
			throws IllegalActionException {
		if (_helperStore.containsKey(component)) {
			return (ActorCodeGenerator) _helperStore.get(component);
		}

		String packageName = generatorPackage.stringValue();

		String componentClassName = component.getClass().getName();
		String helperClassName = componentClassName.replaceFirst("ptolemy",
				packageName);

		Class helperClass = null;

		try {
			helperClass = Class.forName(helperClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalActionException(this, e,
					"Cannot find helper class " + helperClassName);
		}

		Constructor constructor = null;

		try {
			constructor = helperClass.getConstructor(new Class[] { component
					.getClass() });
		} catch (NoSuchMethodException e) {
			throw new IllegalActionException(this, e,
					"There is no constructor in " + helperClassName
							+ " which accepts an instance of "
							+ componentClassName + " as the argument.");
		}

		Object helperObject = null;

		try {
			helperObject = constructor.newInstance(new Object[] { component });
		} catch (Exception ex) {
			throw new IllegalActionException(component, ex,
					"Failed to create helper class code generator.");
		}

		if (!(helperObject instanceof ActorCodeGenerator)) {
			throw new IllegalActionException(this,
					"Cannot generate code for this component: " + component
							+ ". Its helper class does not"
							+ " implement ActorodeGenerator.");
		}

		ActorCodeGenerator castHelperObject = (ActorCodeGenerator) helperObject;

		castHelperObject.setCodeGenerator(this);

		_helperStore.put(component, helperObject);

		return castHelperObject;
	}

	/**
	 * Write the code to a directory named by the codeDirectory parameter, with
	 * a file name that is a sanitized version of the model name, and an
	 * extension that is the last package of the generatorPackage.
	 * 
	 * @param code
	 *            The StringBuffer containing the code.
	 * @return The name of the file that was written.
	 * @exception IllegalActionException
	 *                If there is a problem reading a parameter, if there is a
	 *                problem creating the codeDirectory directory or if there
	 *                is a problem writing the code to a file.
	 */
	protected String _writeCode(StringBuffer code)
			throws IllegalActionException {
		// This method is private so that the body of the caller shorter.

		String extension = generatorPackage.stringValue().substring(
				generatorPackage.stringValue().lastIndexOf("."));

		String codeFileName = _sanitizedModelName + extension;

		// Write the code to a file with the same name as the model into
		// the directory named by the codeDirectory parameter.
		try {
			File codeDirectoryFile = codeDirectory.asFile();
			if (codeDirectoryFile.isFile()) {
				throw new IOException("Error: " + codeDirectory.stringValue()
						+ " is a file, " + "it should be a directory.");
			}
			if (!codeDirectoryFile.isDirectory() && !codeDirectoryFile.mkdirs()) {
				throw new IOException("Failed to make the \""
						+ codeDirectory.stringValue() + "\" directory.");
			}

			// FIXME: Note that we need to make the directory before calling
			// getBaseDirectory()
			codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());

			_executeCommands.stdout("Writing " + codeFileName + " in "
					+ codeDirectory.getBaseDirectory());

			// Check if needs to overwrite.
			if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
					&& codeDirectory.asFile().exists()) {
				// FIXME: It is totally bogus to ask a yes/no question
				// like this, since it makes it impossible to call
				// this method from a script. If the question is
				// asked, the build will hang.
				if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
						+ " exists. OK to overwrite?")) {
					throw new IllegalActionException(this,
							"Please select another file name.");
				}
			}

			Writer writer = null;
			try {
				writer = FileUtilities.openForWriting(codeFileName,
						codeDirectory.getBaseDirectory(), false);
				writer.write(code.toString());
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
			return FileUtilities.nameToFile(codeFileName,
					codeDirectory.getBaseDirectory()).getCanonicalPath();
		} catch (Throwable ex) {
			throw new IllegalActionException(this, ex, "Failed to write \""
					+ codeFileName + "\" in "
					+ codeDirectory.getBaseDirectory());
		}
	}

	/**
	 * Create a make file to compile the generated code file(s). In this base
	 * class, it does nothing.
	 * 
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	protected void _writeMakefile() throws IllegalActionException {
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////

	/**
	 * The name of the file that was written. If no file was written, then the
	 * value is null.
	 */
	protected String _codeFileName = null;

	/**
	 * End of line character. Under Unix: "\n", under Windows: "\n\r". We use a
	 * end of line charactor so that the files we generate have the proper end
	 * of line character for use by other native tools.
	 */
	protected static String _eol;
	static {
		_eol = StringUtilities.getProperty("line.separator");
	}

	/**
	 * Set of execute commands to run the generated code.
	 */
	protected ExecuteCommands _executeCommands;

	/**
	 * Set of library command line arguments where each element is a string, for
	 * example "-L/usr/local/lib".
	 */
	protected Set _libraries = new HashSet();

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
	 * Set of include command line arguments where each element is a string, for
	 * example "-I/usr/local/include".
	 */
	protected Set _includes = new HashSet();

	/** The model we for which we are generating code. */
	protected CompositeEntity _model;

	/**
	 * A set that contains all variables in the model whose values can be
	 * changed during execution.
	 */
	protected Set _modifiedVariables;

	/**
	 * A HashSet that contains all codegen types referenced in the model. When
	 * the codegen kernel processes a $new() macro, it would add the codegen
	 * type to this set. Codegen types are supported by the code generator
	 * package. (e.g. Int, Double, Array, and etc.)
	 */
	protected HashSet _newTypesUsed = new HashSet();

	/**
	 * A static list of all macros supported by the code generator.
	 */
	protected List _macros = new ArrayList(Arrays.asList(new String[] { "ref",
			"val", "size", "type", "targetType", "cgType", "tokenFunc",
			"typeFunc", "actorSymbol", "actorClass", "new" }));

	/**
	 * A static list of all primitive types supported by the code generator.
	 */
	protected static final List _primitiveTypes = Arrays.asList(new String[] {
			"Int", "Double", "String", "Long", "Boolean" });

	/** The sanitized model name. */
	protected String _sanitizedModelName;

	/**
	 * A HashSet that contains all token functions referenced in the model. When
	 * the codegen kernel processes a $tokenFunc() macro, it would add the type
	 * function to this set.
	 */
	protected HashSet _tokenFuncUsed = new HashSet();

	/**
	 * A HashSet that contains all type functions referenced in the model. When
	 * the codegen kernel processes a $tokenFunc() macro, it would add the type
	 * function to this set.
	 */
	protected HashSet _typeFuncUsed = new HashSet();

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/**
	 * A hash map that stores the code generator helpers associated with the
	 * actors.
	 */
	private HashMap _helperStore = new HashMap();

}
