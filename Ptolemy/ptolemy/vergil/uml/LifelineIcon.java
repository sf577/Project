package ptolemy.vergil.uml;

import javax.swing.Icon;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.ImageAttribute;
import diva.canvas.Figure;
import diva.gui.toolbox.FigureIcon;

public class LifelineIcon extends EditorIcon {

	public ImageAttribute imgAttrib = null;

	public LifelineIcon(Workspace workspace, String name)
			throws IllegalActionException {
		super(workspace, name);
		// TODO Auto-generated constructor stub
	}

	public LifelineIcon(NamedObj container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		imgAttrib = new ImageAttribute(this, "image");
		imgAttrib.source.setExpression("$PTII/ptolemy/vergil/uml/Lifeline.gif");
		imgAttrib.source.setSource("$PTII/ptolemy/vergil/uml/Lifeline.gif");

	}

	@Override
	public Icon createIcon() {
		// In this class, we cache the rendered icon, since creating icons from
		// figures is expensive.
		if (_iconCache != null) {
			return _iconCache;
		}

		// No cached object, so rerender the icon.
		Figure figure = createBackgroundFigure();
		// no scaling, the icon will have its full size
		_iconCache = new FigureIcon(figure);

		return _iconCache;
	}

}
