/* Main for applet generation

 Copyright (c) 2001-2006 The Regents of the University of California.
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
package ptolemy.copernicus.applet;

import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.MakefileWriter;
import soot.Pack;
import soot.PackManager;

//////////////////////////////////////////////////////////////////////////
//// Main

/**
 * Read in a MoML model and generate a .html file that will run the model as an
 * applet.
 * 
 * @author Christopher Hylands
 * @version $Id: Main.java,v 1.36 2006/08/21 15:20:18 cxh Exp $
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Main extends KernelMain {
	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Add transforms to the Scene.
	 */
	public void addTransforms() {
		Pack pack = PackManager.v().getPack("wjtp");

		// Generate the makefile files in outDir
		addTransform(pack, "wjtp.makefileWriter", MakefileWriter.v(_toplevel),
				"_generatorAttributeFileName:" + _generatorAttributeFileName
						+ " targetPackage:" + _targetPackage
						+ " templateDirectory:" + _templateDirectory
						+ " outDir:" + _outputDirectory + " overwrite:false");

		// Generate the applet files in outDir
		addTransform(pack, "wjtp.appletWriter", AppletWriter.v(_toplevel),
				"targetPackage:" + _targetPackage + " modelPath:" + _modelPath
						+ " outDir:" + _outputDirectory);
	}

	/**
	 * Parse any code generator specific arguments.
	 */
	protected String[] _parseArgs(GeneratorAttribute attribute)
			throws Exception {
		_generatorAttributeFileName = attribute
				.getParameter("generatorAttributeFileName");
		_modelPath = attribute.getParameter("modelPath");
		_outputDirectory = attribute.getParameter("outputDirectory");
		_targetPackage = attribute.getParameter("targetPackage");
		_templateDirectory = attribute.getParameter("templateDirectory");
		// _watchDogTimeout = attribute.getParameter("watchDogTimeout");

		// String sootArgs = attribute.getParameter("sootArgs");
		return new String[0];
	}

	private String _generatorAttributeFileName = "unsetParameter";

	private String _modelPath = "unsetParameter";

	private String _outputDirectory = "unsetParameter";

	private String _targetPackage = "unsetParameter";

	private String _templateDirectory = "ptolemy/copernicus/java";

	// private String _watchDogTimeout = "unsetParameter";
}
