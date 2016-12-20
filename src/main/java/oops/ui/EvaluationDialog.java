package oops.ui;

import java.awt.*;
import java.net.URL;

import javax.swing.*;

public class EvaluationDialog extends JPanel{

	private static final String EVALUATING_LABEL_TEXT = "OOPS! is scanning ...";
    
    private final JDialog dlg = new JDialog((JFrame) null, "", true);

    public EvaluationDialog() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JPanel holder = new JPanel(new BorderLayout());
        add(holder, BorderLayout.NORTH);
        
        JLabel evaluatingMessageLabel = new JLabel();
        evaluatingMessageLabel.setFont(evaluatingMessageLabel.getFont().deriveFont(Font.BOLD));
        evaluatingMessageLabel.setText(EVALUATING_LABEL_TEXT);
        evaluatingMessageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // add a bottom space
        holder.add(evaluatingMessageLabel, BorderLayout.NORTH);
        
        JLabel evaluatingAnimation = new JLabel();
        evaluatingAnimation.setPreferredSize(new Dimension(128, 15));
        URL url = this.getClass().getResource("/evaluating.gif");
        evaluatingAnimation.setIcon(new ImageIcon(url));
        holder.add(evaluatingAnimation, BorderLayout.SOUTH);
        
        dlg.setUndecorated(true);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        dlg.setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(asJComponent(), BorderLayout.NORTH);
    }

    public JComponent asJComponent() {
        return this;
    }
    
	/**
	 * Sets the visibility of the progress dialog. Note that the progress dialog
	 * is modal - it will block the event dispatch thread when it is shown. The
	 * dialog will be packed and positioned before it is made visible. Note that
	 * this method may be called from a thread other than the event dispatch
	 * thread. The implementation will check to see whether the calling thread
	 * is the event dispatch thread or not and, if necessary, will use
	 * SwingUtilities.invokeLater().
	 * 
	 * @param visible
	 *            true if the dialog should be made visible, or false if the
	 *            dialog should be hidden.
	 */
    public void setVisible(boolean visible) {
        Runnable r = () -> {
            if (visible) {
                dlg.pack();
                Dimension prefSize = dlg.getPreferredSize();
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension screenSize = toolkit.getScreenSize();
                dlg.setLocation(
                        (screenSize.width - prefSize.width) / 2,
                        (screenSize.height - prefSize.height) / 2);
            }
            dlg.setVisible(visible);
        };

        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        }
        else {
            SwingUtilities.invokeLater(r);
        }
    }
}
