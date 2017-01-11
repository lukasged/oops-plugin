package oops.ui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * A custom JPanel to show an icon
 */
public class IconComponent extends JPanel {

    private Icon icon;

    private Dimension preferredSize = new Dimension();

    public void setIcon(Icon icon) {
        this.icon = icon;
        if (icon != null) {
            preferredSize.width = icon.getIconWidth();
            preferredSize.height = icon.getIconHeight();
        }
        else {
            preferredSize.width = 0;
            preferredSize.height = 0;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (icon != null) {
            icon.paintIcon(this, g, 0, 0);
        }
    }
}
