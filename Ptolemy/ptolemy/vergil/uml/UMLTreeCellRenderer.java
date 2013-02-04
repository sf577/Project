package ptolemy.vergil.uml;

import java.awt.Component;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.Documentation;
import ptolemy.moml.EntityLibrary;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;
import ptolemy.vergil.tree.PtolemyTreeCellRenderer;

public class UMLTreeCellRenderer extends PtolemyTreeCellRenderer {

	public UMLTreeCellRenderer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		DefaultTreeCellRenderer component = (DefaultTreeCellRenderer) super
				.getTreeCellRendererComponent(tree, value, selected, expanded,
						leaf, row, hasFocus);

		if (value instanceof NamedObj) {
			NamedObj object = (NamedObj) value;

			// Fix the background colors because transparent
			// labels don't work quite right.
			if (!selected) {
				component.setBackground(tree.getBackground());
				component.setOpaque(true);
			} else {
				component.setOpaque(false);
			}

			if (object instanceof Lifeline) {
				component.setText("");
			} else if (object instanceof Settable) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(object.getName());
				buffer.append("=");
				buffer.append(((Settable) object).getExpression());
				component.setText(buffer.toString().replace('\n', ' '));
			} else {
				component.setText(object.getName());
			}

			// Render an icon, if one has been defined.
			// NOTE: Avoid asking for attributes if the entity is a library
			// because this will trigger evaluation, defeating deferred
			// evaluation.
			if (!(object instanceof EntityLibrary)) {
				// Only if an object has an icon, an icon description, or
				// a small icon description is it rendered in the tree.
				List iconList = object.attributeList(EditorIcon.class);

				if ((iconList.size() > 0)
						|| (object.getAttribute("_iconDescription") != null)
						|| (object.getAttribute("_smallIconDescription") != null)) {
					// NOTE: this code is similar to that in IconController.
					EditorIcon icon = null;

					try {
						if (iconList.size() == 0) {
							icon = new XMLIcon(object, "_icon");
							icon.setPersistent(false);
						} else {
							icon = (EditorIcon) iconList
									.get(iconList.size() - 1);
						}
					} catch (KernelException ex) {
						throw new InternalErrorException(
								"could not create icon in "
										+ object
										+ " even though one did not previously exist.");
					}

					// Wow.. this is a confusing line of code.. :)
					component.setIcon(icon.createIcon());
				}

				// FIXME: The following is not called on EntityLibrary,
				// which means no tooltip for those. Does calling it
				// force expansion of the library?
				Attribute tooltipAttribute = object.getAttribute("tooltip");

				if ((tooltipAttribute != null)
						&& tooltipAttribute instanceof Documentation) {
					// FIXME: This doesn't work with calling this
					// on either this or component.
					this.setToolTipText(((Documentation) tooltipAttribute)
							.getExpression());
				} else {
					String tip = Documentation.consolidate(object);

					if (tip != null) {
						// FIXME: This doesn't work with calling this
						// on either this or component.
						this.setToolTipText(tip);
					}
				}
			}
		}

		return component;
	}

}
