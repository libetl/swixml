package samples.swing;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.twixml.technoproxy.swing.SwingTwiXML;

/**
 * The InitClass class demonstrates how to use the initclass attribute. Date:
 * Mar 10, 2003
 * 
 * @author <a href="mailto:wolf@paulus.com">Wolf Paulus</a>
 * @version $Revision: 1.1 $
 * @since swixml 0.76
 */

public class InitClass extends WindowAdapter {
    public static void main (final String [] args) {
        try {
            new InitClass ();
        } catch (final Exception e) {
            e.printStackTrace ();
        }
    }

    public Action DO_SELECT = new AbstractAction () {
                                /**
		 * 
		 */
                                private static final long serialVersionUID = 4291115453689257148L;

                                @Override
                                public void actionPerformed (final ActionEvent e) {
                                    System.out.println ( ((JComboBox<?>) e
                                            .getSource ()).getSelectedItem ()
                                            .toString ());
                                }
                            };

    private InitClass () throws Exception {
        ((JFrame) new SwingTwiXML (this)
                .render ("samples/swing/xml/initclass.xml")).setVisible (true);
    }
}
