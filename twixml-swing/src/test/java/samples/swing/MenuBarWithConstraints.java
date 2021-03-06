package samples.swing;

import java.awt.Container;

import org.twixml.TwiXML;
import org.twixml.technoproxy.swing.SwingTwiXML;

// $Id: MenuBarWithConstraints.java,v 1.1 2005/08/22 21:20:01 tichy Exp $
/**
 * Sample program to show a menubar with constraints attribute in it.
 * 
 */
public class MenuBarWithConstraints {

    /**
     * @param args
     * @throws Exception
     *             if something goes wrong
     */
    public static void main (final String [] args) throws Exception {
        final TwiXML se = new SwingTwiXML ();
        final Container container = (Container) se
                .render ("samples/swing/xml/menu-bar.xml");
        container.setVisible (true);
    }

}
