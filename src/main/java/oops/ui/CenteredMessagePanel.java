package oops.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.JPanel;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * A custom panel to show the user a message with centered text
 */
public class CenteredMessagePanel extends JPanel {

    private static final Color BORDER_COLOR = new Color(220, 220, 220);

    private static final Color TEXT_COLOR = new Color(200, 200, 200);

    private static final int BORDER_MARGIN = 20;

    private static final int BORDER_THICKNESS = 8;

    private static final Font FONT = new Font("Sans-serif", Font.PLAIN, 40);
    
    private String message;

    public CenteredMessagePanel(String message) {
    	this.message = message;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Stroke currentStroke = g2.getStroke();
        
        // Border
        g2.setStroke(new BasicStroke(BORDER_THICKNESS));
        g2.setColor(BORDER_COLOR);
        int borderWidth = getWidth() - 2 * BORDER_MARGIN;
        int borderHeight = getHeight() - 2 * BORDER_MARGIN;
        g2.drawRoundRect(BORDER_MARGIN, BORDER_MARGIN, borderWidth, borderHeight, BORDER_MARGIN, BORDER_MARGIN);
        
        // Text
        g2.setStroke(currentStroke);
        g2.setColor(TEXT_COLOR);
        g2.setFont(FONT);
        FontMetrics fm = g.getFontMetrics();
        int totalWidth = (fm.stringWidth(message) * 2) + 4;
        int x = (getWidth() - totalWidth) / 2;
        int y = (getHeight() - fm.getHeight()) / 2;
        x += fm.stringWidth(message) / 2;
        y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(message, x, y);
    }
}
