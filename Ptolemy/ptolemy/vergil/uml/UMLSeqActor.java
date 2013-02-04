package ptolemy.vergil.uml;

import java.util.TreeSet;
import java.util.List;
import java.util.Hashtable;
import java.util.Vector;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class UMLSeqActor extends TypedCompositeActor {

	public UMLSeqActor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UMLSeqActor(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		// TODO Auto-generated constructor stub

	}

	public UMLSeqActor(Workspace workspace) {
		super(workspace);
		// TODO Auto-generated constructor stub
	}

	public TreeSet getOrderedMessages() {
		return new TreeSet(this.attributeList(Message.class));
	}

	public Vector getUMLSeqActor() {
		return new Vector(this.attributeList(Message.class));
	}

}
