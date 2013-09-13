package samples;

import java.awt.event.WindowAdapter;

import org.swixml.SwingEngine;

/**
 * The XInclude class shows in simple way how to use xml includes.
 * <code>XInclude</code> extends the WindowAdapter and uses a SwingEngine to
 * renders the GUI.
 * 
 * @author <a href="mailto:wolf@paulus.com">Wolf Paulus</a>
 * @version $Revision: 1.1 $
 * 
 * @since swixml 0.95
 */
public class XInclude extends WindowAdapter {
    public static void main (String [] args) {
        new XInclude ();
    }

    private final SwingEngine swix = new SwingEngine (this);

    private XInclude () {
        try {
            this.swix.render ("xml/xinclude.xml").setVisible (true);
        } catch (final Exception e) {
            e.printStackTrace ();
        }
    }
}
