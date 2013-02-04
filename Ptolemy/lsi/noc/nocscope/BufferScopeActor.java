package lsi.noc.nocscope;

import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.IOPort;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class BufferScopeActor extends NoCScopeActor {

	public BufferScopeActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		_frame = new JFrame("BufferScope");

	}

	public void fire() throws IllegalActionException {

		super.fire();

		List l = portList();
		Iterator ite = l.iterator();
		int port = 0;
		while (ite.hasNext()) {
			IOPort p = (IOPort) ite.next();
			if (p.isInput()) {
				int width = p.getWidth();
				for (int i = 0; i < width; i++) {
					if (p.hasToken(i)) {
						Token t = p.get(i);
						if (t instanceof RecordToken) {
							RecordToken record = (RecordToken) t;
							int e = ((IntToken) record.get("e")).intValue();
							int w = ((IntToken) record.get("w")).intValue();
							int n = ((IntToken) record.get("n")).intValue();
							int s = ((IntToken) record.get("s")).intValue();
							int local = ((IntToken) record.get("l")).intValue();

							((BufferScope) _nocscope).getSwitch(port, i)
									.setEast(e);
							((BufferScope) _nocscope).getSwitch(port, i)
									.setWest(w);
							((BufferScope) _nocscope).getSwitch(port, i)
									.setNorth(n);
							((BufferScope) _nocscope).getSwitch(port, i)
									.setSouth(s);
							((BufferScope) _nocscope).getSwitch(port, i)
									.setLocal(local);

						}
					}

				}

			}
			port++;
		}

		_nocscope.repaint();

	}

	public NoCScope createScope() {
		return new BufferScope(nports, maxwidth);
	}

}
