/* A parameter for coordinated seed management.

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.lib.colt;

import java.util.Iterator;

import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.SharedParameter;

//////////////////////////////////////////////////////////////////////////
//// ColtSeedParameter

/**
 * This parameter is shared throughout a model for coordinated management of
 * seed values for random number generation. Changing the expression of any one
 * instance of the parameter will result in all instances that are shared being
 * changed. If the value is being set to zero, then all other values will be set
 * to zero. If it is being set to anything else, then all other values are set
 * to unique numbers obtained by adding 1 to the specified value while iterating
 * through the shared parameters.
 * <p>
 * An instance elsewhere in the model (within the same top level) is shared if
 * it has the same name and its container is of the class specified in the
 * constructor (or of the container class, if no class is specified in the
 * constructor).
 * <p>
 * One exception is that if this parameter is (deeply) within an instance of
 * EntityLibrary, then the parameter is not shared. Were this not the case, then
 * opening a library containing this parameter would force expansion of all the
 * sublibraries of EntityLibrary, which would defeat the lazy instantiation of
 * EntityLibrary.
 * <p>
 * This parameter is always of type long.
 * 
 * @author Edward A. Lee
 * @version $Id: ColtSeedParameter.java,v 1.14.4.1 2006/12/30 22:19:00 cxh Exp $
 * @since Ptolemy II 4.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ColtSeedParameter extends SharedParameter {
	/**
	 * Construct a parameter with the given container and name. The container
	 * class will be used to determine which other instances of
	 * ColtSeedParameter are shared with this one.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of the parameter.
	 * @exception IllegalActionException
	 *                If the parameter is not of an acceptable class for the
	 *                container.
	 * @exception NameDuplicationException
	 *                If the name coincides with a parameter already in the
	 *                container.
	 */
	public ColtSeedParameter(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		this(container, name, null);
	}

	/**
	 * Construct a parameter with the given container, name, and container
	 * class. The specified class will be used to determine which other
	 * instances of ColtSeedParameter are shared with this one.
	 * 
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of the parameter.
	 * @param containerClass
	 *            The class used to determine shared instances.
	 * @exception IllegalActionException
	 *                If the parameter is not of an acceptable class for the
	 *                container.
	 * @exception NameDuplicationException
	 *                If the name coincides with a parameter already in the
	 *                container.
	 */
	public ColtSeedParameter(NamedObj container, String name,
			Class containerClass) throws IllegalActionException,
			NameDuplicationException {
		super(container, name, containerClass, "0L");
		setTypeEquals(BaseType.LONG);
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Override the base class to set the declared type before attempting to
	 * infer the value. This is necessary because this method is called in the
	 * constructor of the base class, before the declared type has been set.
	 * 
	 * @param defaultValue
	 *            The default parameter value to use.
	 * @exception InternalErrorException
	 *                If there are multiple shared parameters in the model, but
	 *                their values do not match.
	 */
	public void inferValueFromContext(String defaultValue) {
		try {
			setTypeEquals(BaseType.LONG);
		} catch (IllegalActionException e) {
			// This should have been caught before.
			throw new InternalErrorException(e);
		}
		super.inferValueFromContext(defaultValue);
	}

	/**
	 * Set the expression of the shared parameters.
	 */
	public void setExpression(String expression) {
		boolean previousSuppress = isSuppressingPropagation();

		try {
			setSuppressingPropagation(true);
			super.setExpression(expression);
		} finally {
			setSuppressingPropagation(previousSuppress);
		}

		// Have to evaluate the expression using getToken(),
		// rather than parsing the string. Unfortunately,
		// the Variable base class doesn't allow us to throw
		// IllegalActionException. Too bad...
		try {
			LongToken token = (LongToken) getToken();
			long value = 0L;

			if (token != null) {
				value = token.longValue();
			}

			if (value == 0L) {
				// Call again without suppression of propagation.
				super.setExpression(expression);
				// No need to record this, as it is the default value.
				setPersistent(false);
			} else {
				// Need to assign unique values.
				if (!isSuppressingPropagation()) {
					// Ensure that when the model is saved, that this
					// parameter value, and only this one, is saved.
					// The shared parameters are made non-persistent below.
					setPersistent(true);
					Iterator sharedParameters = sharedParameterSet().iterator();

					while (sharedParameters.hasNext()) {
						ColtSeedParameter sharedParameter = (ColtSeedParameter) sharedParameters
								.next();
						if (sharedParameter != this) {
							try {
								sharedParameter.setSuppressingPropagation(true);
								value++;

								String newExpression = value + "L";

								if (!sharedParameter.getExpression().equals(
										newExpression)) {
									sharedParameter
											.setExpression(newExpression);

									// Make sure the new value is not
									// persistent.
									sharedParameter.setPersistent(false);
								}
							} finally {
								sharedParameter
										.setSuppressingPropagation(previousSuppress);
							}
						}
					}
				}
			}
		} catch (IllegalActionException ex) {
			// FIXME: This is a lousy way to report a syntax error.
			throw new InternalErrorException(ex);
		}
	}

}
