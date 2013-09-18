/*--
 Copyright (C) 2003-2007 Wolf Paulus.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions, and the disclaimer that follows
 these conditions in the documentation and/or other materials provided
 with the distribution.

 3. The end-user documentation included with the redistribution,
 if any, must include the following acknowledgment:
        "This product includes software developed by the
         SWIXML Project (http://www.swixml.org/)."
 Alternately, this acknowledgment may appear in the software itself,
 if and wherever such third-party acknowledgments normally appear.

 4. The name "Swixml" must not be used to endorse or promote products
 derived from this software without prior written permission. For
 written permission, please contact <info_AT_swixml_DOT_org>

 5. Products derived from this software may not be called "Swixml",
 nor may "Swixml" appear in their name, without prior written
 permission from the Swixml Project Management.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE SWIXML PROJECT OR ITS
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.
 ====================================================================

 This software consists of voluntary contributions made by many
 individuals on behalf of the Swixml Project and was originally
 created by Wolf Paulus <wolf_AT_swixml_DOT_org>. For more information
 on the Swixml Project, please see <http://www.swixml.org/>.
 */

package org.swixml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.swixml.converters.LocaleConverter;
import org.swixml.converters.PrimitiveConverter;
import org.swixml.technoproxy.CustomCodeProxy;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Singleton Parser to render XML for Swing Documents
 * <p/>
 * <img src="doc-files/swixml_1_0.png" ALIGN="center">
 * </p>
 * 
 * @author <a href="mailto:wolf@wolfpaulus.com">Wolf Paulus</a>
 * @author <a href="mailto:fm@authentidate.de">Frank Meissner</a>
 * @version $Revision: 1.5 $
 * @see org.swixml.SwingTagLibrary
 * @see org.swixml.ConverterLibrary
 * @SuppressWarnings ({ "unchecked", "deprecation" })
 */
public class Parser<Container> {

    //
    // Custom Attributes
    //

    /**
     * Additional attribute to collect layout constrain information
     */
    public static final String         ATTR_CONSTRAINTS     = "constraints";

    /**
     * Additional attribute to collect information about the PLAF implementation
     */
    public static final String         ATTR_PLAF            = "plaf";

    /**
     * Additional attribute to collect layout constrain information
     */
    public static final String         ATTR_BUNDLE          = "bundle";

    /**
     * Additional attribute to collect layout constrain information
     */
    public static final String         ATTR_LOCALE          = "locale";

    /**
     * Allows to provides swixml tags with an unique id
     */
    public static final String         ATTR_ID              = "id";

    /**
     * Allows to provides swixml tags with a reference to another tag
     */
    public static final String         ATTR_REFID           = "refid";

    /**
     * Allows to provides swixml tags with a reference to another tag
     * 
     * @see #ATTR_REFID
     * @deprecated use refid instead
     */
    @Deprecated
    public static final String         ATTR_USE             = "use";

    /**
     * Allows to provides swixml tags with an unique id
     */
    public static final String         ATTR_INCLUDE         = "include";

    /**
     * Allows to provides swixml tags with a dynamic update class
     */
    public static final String         ATTR_INITCLASS       = "initclass";

    /**
     * Allows to provides swixml tags with a dynamic update class
     */
    public static final String         ATTR_ACTION          = "action";

    /**
     * Prefix for all MAC OS X related attributes
     */
    public static final String         ATTR_MACOS_PREFIX    = "macos_";

    /**
     * Attribute name that flags an Action as the default Preferences handler on
     * a Mac
     */
    public static final String         ATTR_MACOS_PREF      = Parser.ATTR_MACOS_PREFIX
                                                                    + "preferences";

    /**
     * Attribute name that flags an Action as the default Aboutbox handler on a
     * Mac
     */
    public static final String         ATTR_MACOS_ABOUT     = Parser.ATTR_MACOS_PREFIX
                                                                    + "about";

    /**
     * Attribute name that flags an Action as the default Quit handler on a Mac
     */
    public static final String         ATTR_MACOS_QUIT      = Parser.ATTR_MACOS_PREFIX
                                                                    + "quit";

    /**
     * Attribute name that flags an Action as the default Open Application
     * handler on a Mac
     */
    public static final String         ATTR_MACOS_OPENAPP   = Parser.ATTR_MACOS_PREFIX
                                                                    + "openapp";

    /**
     * Attribute name that flags an Action as the default Open File handler on a
     * Mac
     */
    public static final String         ATTR_MACOS_OPENFILE  = Parser.ATTR_MACOS_PREFIX
                                                                    + "openfile";

    /**
     * Attribute name that flags an Action as the default Print handler on a Mac
     */
    public static final String         ATTR_MACOS_PRINT     = Parser.ATTR_MACOS_PREFIX
                                                                    + "print";

    /**
     * Attribute name that flags an Action as the default Re-Open Applicaiton
     * handler on a Mac
     */
    public static final String         ATTR_MACOS_REOPEN    = Parser.ATTR_MACOS_PREFIX
                                                                    + "reopen";

    /**
     * Method name used with initclass - if this exit, the update class will no
     * be instanced but getInstance is called
     */
    public static final String         GETINSTANCE          = "getInstance";

    /**
     * Localiced Attributes
     */
    public static final Vector<String> LOCALIZED_ATTRIBUTES = new Vector<String> ();

    //
    // Private Members
    //

    /**
     * Adds a child component to a parent component considering many differences
     * between the Swing containers
     * 
     * @param parent
     *            <code>Component</code>
     * @param component
     *            <code>Component</code> child to be added to the parent
     * @param constrains
     *            <code>Object</code> contraints
     * @return <code>Component</code> - the passed in component
     */
    private Component addChild (Container parent, Component component,
            Object constrains) {
        if (component == null) {
            return null;
        }
        //
        // Set a JMenuBar for JFrames, JDialogs, etc.
        //
        if (CustomCodeProxy.getTypeAnalyser ().isConvenient (component.getClass (), "MenuBar")) {

            try {
                final Method m = parent.getClass ().getMethod ("setJMenuBar",
                        JMenuBar.class);
                m.invoke (parent, component);
            } catch (final NoSuchMethodException e) {
                if (constrains == null) {
                    parent.add (component);
                } else {
                    parent.add (component, constrains);
                }
            } catch (final Exception e) {
                // intentionally empty
            }

        } else if (parent instanceof RootPaneContainer) {
            //
            // add component into RootContainr
            // All Swing top-level containers contain a JRootPane as their only
            // child.
            // The content pane provided by the root pane should contain all the
            // non-menu components.
            //
            final RootPaneContainer rpc = (RootPaneContainer) parent;
            if (component instanceof LayoutManager) {
                rpc.getContentPane ().setLayout ((LayoutManager) component);
            } else {
                rpc.getContentPane ().add (component, constrains);
            }
        } else if (parent instanceof JScrollPane) {
            //
            // add component into a ScrollPane
            //
            final JScrollPane scrollPane = (JScrollPane) parent;
            scrollPane.setViewportView (component);
        } else if (parent instanceof JSplitPane) {
            //
            // add component into a SplitPane
            //
            final JSplitPane splitPane = (JSplitPane) parent;
            if (splitPane.getOrientation () == JSplitPane.HORIZONTAL_SPLIT) {
                //
                // Horizontal SplitPane
                //
                if (splitPane.getTopComponent () == null) {
                    splitPane.setTopComponent (component);
                } else {
                    splitPane.setBottomComponent (component);
                }
            } else {
                //
                // Vertical SplitPane
                //
                if (splitPane.getLeftComponent () == null) {
                    splitPane.setLeftComponent (component);
                } else {
                    splitPane.setRightComponent (component);
                }
            }
        } else if ( (parent instanceof JMenuBar)
                && (component instanceof JMenu)) {
            //
            // add Menu into a MenuBar
            //
            final JMenuBar menuBar = (JMenuBar) parent;
            menuBar.add (component, constrains);
        } else if (JSeparator.class.isAssignableFrom (component.getClass ())) {
            //
            // add Separator to a Menu, Toolbar or PopupMenu
            //
            try {
                if (JToolBar.class.isAssignableFrom (parent.getClass ())) {
                    ((JToolBar) parent).addSeparator ();
                } else if (JPopupMenu.class.isAssignableFrom (parent
                        .getClass ())) {
                    ((JPopupMenu) parent).addSeparator ();
                } else if (JMenu.class.isAssignableFrom (parent.getClass ())) {
                    ((JMenu) parent).addSeparator ();
                } else if (constrains != null) {
                    parent.add (component, constrains);
                } else {
                    parent.add (component);
                }
            } catch (final ClassCastException e) {
                parent.add (component);
            }

        } else if (parent instanceof Container) {
            //
            // add compontent into container
            //
            if (constrains == null) {
                parent.add (component);
            } else {
                parent.add (component, constrains);
            }
        }
        return component;
    }

    /**
     * Recursive element by id finder
     * 
     * @param element
     *            <code>Element</code> start node
     * @param id
     *            <code>String</code> id to look for
     * @return <code>Element</code> - with the given id in the id attribute or
     *         null if not found
     */
    private static Element find (Element element, String id) {
        Element elem = null;
        final String attrValue = Attribute.getAttributeValue (element,
                Parser.ATTR_ID);
        if ( (attrValue != null) && id.equals (attrValue.trim ())) {
            elem = element;
        } else {
            final NodeList el = element.getChildNodes ();
            for (int i = 0 ; i < el.getLength () ; i++) {
                if (! (el.item (i) instanceof Element)) {
                    continue;
                }
                elem = Parser.find ((Element) el.item (i), id.trim ());
                if (elem != null) {
                    break;
                }
            }
        }
        return elem;
    }

    protected static Element getChildByName (Element parent, String childName) {
        final NodeList nl = parent.getChildNodes ();
        for (int i = 0 ; i < nl.getLength () ; i++) {
            if (! (nl.item (i) instanceof Element)) {
                continue;
            }
            if (childName.equalsIgnoreCase (nl.item (i).getNodeName ())) {
                return (Element) nl.item (i);
            }
        }
        return null;
    }

    /**
     * Copy the content from the source into the traget <code>Element</code>
     * 
     * @param source
     *            <code>Element</code> Content provider
     * @param target
     *            <code>Element</code> Content receiver
     */
    private static void moveContent (Element source, Element target) {
        final NodeList list = source.getChildNodes ();
        for (int i = 0 ; i < list.getLength () ; i++) {
            final Node node = target.getOwnerDocument ().importNode (
                    list.item (i), true);
            target.appendChild (node);
        }
    }

    /**
     * Recursively adds AbstractButtons into the given
     * 
     * @param obj
     *            <code>Object</code> should be an AbstractButton or JComponent
     *            containing AbstractButtons
     * @param grp
     *            <code>ButtonGroup</code>
     */
    private static void putIntoBtnGrp (Object obj, ButtonGroup grp) {
        if (AbstractButton.class.isAssignableFrom (obj.getClass ())) {
            grp.add ((AbstractButton) obj);
        } else if (JComponent.class.isAssignableFrom (obj.getClass ())) {
            final JComponent jp = (JComponent) obj;
            for (int i = 0 ; i < jp.getComponentCount () ; i++) {
                Parser.putIntoBtnGrp (jp.getComponent (i), grp);
            }
        } // otherwise just do nothing ...
    }

    /**
     * the calling engine
     */
    private final SwingEngine         engine;

    /**
     * ConverterLib, to access COnverters, converting String in all kinds of
     * things
     */
    private final ConverterLibrary    cvtlib  = ConverterLibrary.getInstance ();

    /**
     * map to store id-id components, needed to support labelFor attributes
     */
    private final Map<JLabel, String> lbl_map = new HashMap<JLabel, String> ();

    /**
     * map to store specific Mac OS actions mapping
     */
    private final Map<String, Object> mac_map = new HashMap<String, Object> ();

    /**
     * docoument, to be parsed
     */
    private Document                  jdoc;

    /**
     * Static Initializer adds Attribute Names into the LOCALIZED_ATTRIBUTES
     * Vector Needs to be inserted all lowercase.
     */
    static {
        Parser.LOCALIZED_ATTRIBUTES.add ("accelerator");
        Parser.LOCALIZED_ATTRIBUTES.add ("disabledicons");
        Parser.LOCALIZED_ATTRIBUTES.add ("displayedmnemonics");
        Parser.LOCALIZED_ATTRIBUTES.add ("icon");
        Parser.LOCALIZED_ATTRIBUTES.add ("icons");
        Parser.LOCALIZED_ATTRIBUTES.add ("iconimage");
        Parser.LOCALIZED_ATTRIBUTES.add ("label");
        Parser.LOCALIZED_ATTRIBUTES.add ("mnemonic");
        Parser.LOCALIZED_ATTRIBUTES.add ("mnemonics");
        Parser.LOCALIZED_ATTRIBUTES.add ("name");
        Parser.LOCALIZED_ATTRIBUTES.add ("text");
        Parser.LOCALIZED_ATTRIBUTES.add ("title");
        Parser.LOCALIZED_ATTRIBUTES.add ("titleat");
        Parser.LOCALIZED_ATTRIBUTES.add ("titles");
        Parser.LOCALIZED_ATTRIBUTES.add ("tooltiptext");
        Parser.LOCALIZED_ATTRIBUTES.add ("tooltiptexts");
    }

    /**
     * Constructs a new SwixMl Parser for the provided engine.
     * 
     * @param engine
     *            <code>SwingEngine</code>
     */
    public Parser (SwingEngine engine) {
        this.engine = engine;
    }

    /**
     * Creates an object and sets properties based on the XML tag's attributes
     * 
     * @param obj
     *            <code>Object</code> object representing a tag found in the
     *            SWIXML descriptor document
     * @param factory
     *            <code>Factory</code> factory to instantiate a new object
     * @param attributes
     *            <code>List</code> attribute list
     * @return <code>List</code> - list of attributes that could not be applied.
     * @throws Exception
     *             <p/>
     *             <ol>
     *             <li>For every attribute, createContainer() 1st tries to find
     *             a setter in the given factory.<br>
     *             if a setter can be found and converter exists to convert the
     *             parameter string into a type that fits the setter method, the
     *             setter gets invoked.</li>
     *             <li>Otherwise, createContainer() looks for a public field
     *             with a matching name.</li>
     *             </ol>
     *             </p>
     *             <p>
     *             <b>Example:</b><br>
     *             <br>
     *             1.) try to create a parameter obj using the ParameterFactory:
     *             i.e. <br>
     *             background="FFC9AA" = container.setBackground ( new
     *             Color(attr.value) ) <br>
     *             2.) try to find a simple setter taking a primitive or String:
     *             i.e. <br>
     *             width="25" container.setWidth( new Interger( attr.
     *             getIntValue() ) ) <br>
     *             3.) try to find a public field, <br>
     *             container.BOTTOM_ALIGNMENT
     *             </p>
     */
    private List<Attribute> applyAttributes (Object obj, Factory factory,
            List<Attribute> attributes) throws Exception {
        //
        // pass 1: Make an 'action' the 1st attribute to be processed -
        // otherwise the action would override already applied attributes like
        // text etc.
        //

        for (int i = 0 ; i < attributes.size () ; i++) {
            final Attribute attr = attributes.get (i);
            if (Parser.ATTR_ACTION.equalsIgnoreCase (attr.getName ())) {
                attributes.remove (i);
                attributes.add (0, attr);
                break;
            }
        }

        //
        // pass 2: process the attributes
        //

        final Iterator<Attribute> it = attributes.iterator ();
        final List<Attribute> list = new ArrayList<Attribute> (); // remember
                                                                  // not applied
                                                                  // attributes
        Action action = null; // used to insert an action into the macmap

        while ( (it != null) && it.hasNext ()) { // loop through all available
                                                 // attributes
            final Attribute attr = it.next ();

            if (Parser.ATTR_ID.equals (attr.getName ())) {
                continue;
            }
            if (Parser.ATTR_REFID.equals (attr.getName ())) {
                continue;
            }
            if (Parser.ATTR_USE.equals (attr.getName ())) {
                continue;
            }

            if ( (action != null)
                    && attr.getName ().startsWith (Parser.ATTR_MACOS_PREFIX)) {
                this.mac_map.put (attr.getName (), action);
                continue;
            }

            if (JLabel.class.isAssignableFrom (obj.getClass ())
                    && attr.getName ().equalsIgnoreCase ("LabelFor")) {
                this.lbl_map.put ((JLabel) obj, attr.getValue ());
                continue;
            }

            // using guessSetter allow to not have to care about case when
            // looking at the attribute name.
            final Method method = factory.guessSetter (attr.getName ());
            if (method != null) {
                //
                // A setter method has successfully been identified.
                //
                final Class<?> paraType = method.getParameterTypes () [0];
                final Converter converter = this.cvtlib.getConverter (paraType);

                if (converter != null) { // call setter with a newly instanced
                                         // parameter
                    Object para = null;
                    try {
                        //
                        // Actions are provided in the engine's member
                        // variables.
                        // a getClass().getFields lookup has to be done to find
                        // the correct fields.
                        //
                        if (Action.class.equals (paraType)) {
                            try {
                                para = this.engine.getClient ().getClass ()
                                        .getField (attr.getValue ())
                                        .get (this.engine.getClient ());
                            } catch (final NoSuchFieldException e) {
                                //
                                // At this point we know that a action attribute
                                // was put into an XML tag but the client call
                                // doesn't
                                // seem to have an Action member varible with a
                                // matching name.
                                // Now we look for a public method that could be
                                // wrapped into an generated AbstrtactAction
                                // instead
                                //
                                try {
                                    para = new XAction (
                                            this.engine.getClient (),
                                            attr.getValue ());
                                } catch (final NoSuchMethodException e1) {
                                    para = null;
                                }
                            }
                            action = (Action) para;
                        } else {
                            para = converter.convert (paraType, attr,
                                    this.engine.getLocalizer ());
                        }

                        method.invoke (obj, para); // ATTR SET

                    } catch (final NoSuchFieldException e) {
                        if (AppConstants.DEBUG_MODE) {
                            System.err.println ("Method: " + method.getName ()
                                    + " Attribute " + attr.getName () + " : "
                                    + attr.getValue () + "' not set. ");
                        }
                    } catch (final InvocationTargetException e) {
                        //
                        // The JFrame class is slightly incompatible with Frame.
                        // Like all other JFC/Swing top-level containers, a
                        // JFrame contains a JRootPane as its only child.
                        // The content pane provided by the root pane should, as
                        // a rule, contain all the non-menu components
                        // displayed by the JFrame. The JFrame class is slightly
                        // incompatible with Frame.
                        //
                        if (obj instanceof RootPaneContainer) {
                            final Container rootpane = ((RootPaneContainer) obj)
                                    .getContentPane ();
                            final Factory f = this.engine.getTaglib ()
                                    .getFactory (rootpane.getClass ());
                            final Method m = f.guessSetter (attr.getName ());
                            try {
                                m.invoke (rootpane, para); // ATTR SET
                            } catch (final Exception ex) {
                                list.add (attr);
                            }
                        } else {
                            list.add (attr);
                        }
                    } catch (final Exception e) {
                        throw new Exception (e + ":" + method.getName () + ":"
                                + para, e);
                    }
                    continue;
                }

                //
                // try this: call the setter with an Object.class Type
                //
                if (paraType.equals (Object.class)) {
                    try {
                        String s = attr.getValue ();
                        if (Parser.LOCALIZED_ATTRIBUTES.contains (attr
                                .getName ().toLowerCase ())) {
                            s = this.engine.getLocalizer ().getString (s);
                        }
                        method.invoke (obj, s); // ATTR SET
                    } catch (final Exception e) {
                        list.add (attr);
                    }
                    continue;
                }

                //
                // try this: call the setter with a primitive
                //
                if (paraType.isPrimitive ()) {
                    try {
                        method.invoke (obj, PrimitiveConverter.conv (paraType,
                                attr, this.engine.getLocalizer ())); // ATTR SET
                    } catch (final Exception e) {
                        list.add (attr);
                    }
                    continue;
                }
                //
                // try again later
                //
                list.add (attr);
            } else {
                //
                // Search for a public field in the obj.class that matches the
                // attribute name
                //
                try {
                    final Field field = obj.getClass ().getField (
                            attr.getName ());
                    if (field != null) {
                        final Converter converter = this.cvtlib
                                .getConverter (field.getType ());
                        if (converter != null) {
                            //
                            // Localize Strings
                            //
                            Object fieldValue = converter.convert (
                                    field.getType (), attr, null);
                            if (String.class.equals (converter.convertsTo ())) {
                                fieldValue = this.engine.getLocalizer ()
                                        .getString ((String) fieldValue);
                            }
                            field.set (obj, fieldValue); // ATTR SET
                        } else {
                            list.add (attr);
                        }
                    } else {
                        list.add (attr);
                    }
                } catch (final Exception e) {
                    list.add (attr);
                }
            }
        } // end_while
        return list;
    }

    /**
     * Copies attributes that element doesn't have yet form element[id]
     * 
     * @param target
     *            <code>Element</code> target to receive more attributes
     */
    private void cloneAttributes (Element target) {
        Element source = null;
        if (Attribute.getAttributeValue (target, Parser.ATTR_REFID) != null) {
            source = Parser.find (this.jdoc.getDocumentElement (), Attribute
                    .getAttributeValue (target, Parser.ATTR_REFID).trim ());
        } else if (Attribute.getAttributeValue (target, Parser.ATTR_USE) != null) {
            source = Parser.find (this.jdoc.getDocumentElement (), Attribute
                    .getAttributeValue (target, Parser.ATTR_USE).trim ());
        }
        if (source != null) {
            final NamedNodeMap it = source.getAttributes ();
            for (int i = 0 ; i < it.getLength () ; i++) {
                final Attr attr = (Attr) it.item (i);
                final String name = attr.getName ().trim ();
                //
                // copy but don't overwrite an attr.
                // also, don't copy the id attr.
                //
                if (!Parser.ATTR_ID.equals (name)
                        && (Attribute.getAttributeValue (target, name) == null)) {
                    target.setAttribute (attr.getName (), attr.getValue ());
                }
            } // end while
        }
    }

    /**
     * Retrieve all atributes into list.
     * 
     * @param elem
     *            element for extract attributes
     * @return attributes list
     */
    protected List<Attribute> getAttributes (Element elem) {
        final NamedNodeMap map = elem.getAttributes ();
        final List<Attribute> result = new ArrayList<Attribute> (
                map.getLength ());
        for (int i = 0 ; i < map.getLength () ; i++) {
            final Node a = map.item (i);
            result.add (new Attribute (a.getNodeName (), a.getNodeValue ()));
        }
        return result;
    }

    /**
     * Recursively converts <code>org.jdom.Element</code>s into
     * <code>javax.swing</code> or <code>java.awt</code> objects
     * 
     * @param element
     *            <code>org.jdom.Element</code> XML tag
     * @param obj
     *            <code>Object</code> if not null, only this elements children
     *            will be processed, not the element itself
     * @return <code>java.awt.Container</code> representing the GUI
     *         impementation of the XML tag.
     * @throws Exception
     *             - if parsing fails
     * @SuppressWarnings ({ "RedundantArrayCreation",
     *                   "NullArgumentToVariableArgMethod" })
     */
    Object getSwing (Element element, Object obj) throws Exception {

        final Factory factory = this.engine.getTaglib ().getFactory (
                element.getNodeName ());
        // look for <id> attribute value
        final String id = Attribute.getAttributeValue (element, Parser.ATTR_ID) != null ? Attribute
                .getAttributeValue (element, Parser.ATTR_ID).trim () : null;
        // either there is no id or the id is not user so far
        final boolean unique = !this.engine.getIdMap ().containsKey (id);
        boolean constructed = false;

        if (!unique) {
            throw new IllegalStateException ("id already in use: " + id + " : "
                    + this.engine.getIdMap ().get (id).getClass ().getName ());
        }
        if (factory == null) {
            throw new Exception (
                    "Unknown TAG, implementation class not defined: "
                            + element.getNodeName ());
        }

        //
        // XInclude
        //

        if (Attribute.getAttributeValue (element, Parser.ATTR_INCLUDE) != null) {
            final StringTokenizer st = new StringTokenizer (
                    Attribute.getAttributeValue (element, Parser.ATTR_INCLUDE),
                    "#");
            element.removeAttribute (Parser.ATTR_INCLUDE);

            final DocumentBuilder builder = DocumentBuilderFactory
                    .newInstance ().newDocumentBuilder ();
            final Document doc = builder.parse (this.engine.getClassLoader ()
                    .getResourceAsStream (st.nextToken ()));

            final Element xelem = Parser.find (doc.getDocumentElement (),
                    st.nextToken ());
            if (xelem != null) {
                Parser.moveContent (xelem, element);
            }
        }

        //
        // clone attribute if <em>refid</em> attribute is available
        //

        if (Attribute.getAttributeValue (element, Parser.ATTR_REFID) != null) {
            this.cloneAttributes (element);
            element.removeAttribute (Parser.ATTR_REFID);
        } else if (Attribute.getAttributeValue (element, Parser.ATTR_USE) != null) {
            this.cloneAttributes (element);
            element.removeAttribute (Parser.ATTR_USE);
        }
        //
        // let the factory instantiate a new object
        //

        final List<Attribute> attributes = this.getAttributes (element);
        if (obj == null) {
            Object initParameter = null;

            if (Attribute.getAttributeValue (element, Parser.ATTR_INITCLASS) != null) {
                final StringTokenizer st = new StringTokenizer (
                        Attribute.getAttributeValue (element,
                                Parser.ATTR_INITCLASS), "( )");
                element.removeAttribute (Parser.ATTR_INITCLASS);
                // try {
                try {
                    if (st.hasMoreTokens ()) {
                        final Class<?> initClass = Class.forName (st
                                .nextToken ()); // load
                        // update
                        // type
                        try { // look for a getInstance() methode
                            final Method factoryMethod = initClass
                                    .getMethod (Parser.GETINSTANCE);
                            if (Modifier.isStatic (factoryMethod
                                    .getModifiers ())) {
                                initParameter = factoryMethod.invoke (null);
                            }
                        } catch (final NoSuchMethodException nsme) {
                            // really nothing to do here
                        }
                        if ( (initParameter == null) && st.hasMoreTokens ()) { // now
                                                                               // try
                                                                               // to
                                                                               // instantiate
                                                                               // with
                                                                               // String
                                                                               // taking
                                                                               // ctor
                            try {
                                final Constructor<?> ctor = initClass
                                        .getConstructor (new Class<?> [] { String.class });
                                final String pattern = st.nextToken ();
                                initParameter = ctor
                                        .newInstance (new Object [] { pattern });
                            } catch (final NoSuchMethodException e) { // intentionally
                                // empty
                            } catch (final SecurityException e) { // intentionally
                                // empty
                            } catch (final InstantiationException e) { // intentionally
                                // empty
                            } catch (final IllegalAccessException e) { // intentionally
                                // empty
                            } catch (final IllegalArgumentException e) { // intentionally
                                // empty
                            } catch (final InvocationTargetException e) { // intentionally
                                // empty
                            }
                        }
                        if (initParameter == null) { // now try to instantiate
                                                     // with default ctor
                            initParameter = initClass.newInstance ();
                        }
                        if (Action.class.isInstance (initParameter)) {
                            for (int i = 0, n = attributes.size () ; i < n ; i++) {
                                final Attribute attrib = attributes.get (i);
                                final String attribName = attrib.getName ();
                                if ( (attribName != null)
                                        && attribName
                                                .startsWith (Parser.ATTR_MACOS_PREFIX)) {
                                    this.mac_map
                                            .put (attribName, initParameter);
                                }
                            }
                        }
                    }
                } catch (final ClassNotFoundException e) {
                    System.err.println (Parser.ATTR_INITCLASS
                            + " not instantiated : " + e.getLocalizedMessage ()
                            + e);
                } catch (final SecurityException e) {
                    System.err.println (Parser.ATTR_INITCLASS
                            + " not instantiated : " + e.getLocalizedMessage ()
                            + e);
                } catch (final IllegalAccessException e) {
                    System.err.println (Parser.ATTR_INITCLASS
                            + " not instantiated : " + e.getLocalizedMessage ()
                            + e);
                } catch (final IllegalArgumentException e) {
                    System.err.println (Parser.ATTR_INITCLASS
                            + " not instantiated : " + e.getLocalizedMessage ()
                            + e);
                } catch (final InvocationTargetException e) {
                    System.err.println (Parser.ATTR_INITCLASS
                            + " not instantiated : " + e.getLocalizedMessage ()
                            + e);
                } catch (final InstantiationException e) {
                    System.err.println (Parser.ATTR_INITCLASS
                            + " not instantiated : " + e.getLocalizedMessage ()
                            + e);
                } catch (final RuntimeException re) {
                    throw re;
                } catch (final Exception e) {
                    throw new Exception (
                            Parser.ATTR_INITCLASS + " not instantiated : "
                                    + e.getLocalizedMessage (), e);
                }
            }

            obj = initParameter != null ? factory
                    .newInstance (new Object [] { initParameter }) : factory
                    .newInstance ();
            constructed = true;
            //
            // put newly created object in the map if it has an <id> attribute
            // (uniqueness is given att this point)
            //
            if (id != null) {
                this.engine.getIdMap ().put (id, obj);
            }
        }

        //
        // handle "layout" element or attribute
        //
        if (obj != null &&
                CustomCodeProxy.getTypeAnalyser ().isConvenient (obj.getClass (), "Container")) {
            LayoutManager lm = null;
            final Element layoutElement = Parser.getChildByName (element,
                    "layout");
            if (layoutElement != null) {
                element.removeChild (layoutElement);

                final String layoutType = Attribute.getAttributeValue (
                        layoutElement, "type");
                final LayoutConverter layoutConverter = LayoutConverterLibrary
                        .getInstance ().getLayoutConverterByID (layoutType);
                if (layoutConverter != null) {
                    lm = layoutConverter.convertLayoutElement (layoutElement);
                }
            }

            if (lm == null) {
                // search for case-insensitive "layout" attribute to ensure
                // compatibility
                Attribute layoutAttr = null;
                for (int i = 0 ; i < attributes.size () ; i++) {
                    final Attribute attr = attributes.get (i);
                    if ("layout".equalsIgnoreCase (attr.getName ())) {
                        attributes.remove (i);
                        layoutAttr = attr;
                        break;
                    }
                }

                if (layoutAttr != null) {
                    element.removeAttribute (layoutAttr.getName ());

                    String layoutType = layoutAttr.getValue ();
                    final int index = layoutType.indexOf ('(');
                    if (index > 0) {
                        layoutType = layoutType.substring (0, index); // strip
                    }
                    // parameters
                    final LayoutConverter layoutConverter = LayoutConverterLibrary
                            .getInstance ().getLayoutConverterByID (layoutType);
                    if (layoutConverter != null) {
                        lm = layoutConverter
                                .convertLayoutAttribute (layoutAttr);
                    }
                }
            }

            if (lm != null) {
                ((Container) obj).setLayout (lm);
            }
        }

        //
        // 1st attempt to apply attributes (call setters on the objects)
        // put an action attribute at the beginning of the attribute list
        final Attribute actionAttr = Attribute.getAttribute (element, "Action");
        if (actionAttr != null) {
            element.removeAttribute (actionAttr.getName ());
            attributes.add (0, actionAttr);
        }
        //
        // put Tag's Text content into Text Attribute
        //
        if (Attribute.getAttributeValue (element, "Text") == null) {
            final String v = element.getNodeValue ();
            if ( (v != null) && (v.trim ().length () > 0)) {
                attributes.add (new Attribute ("Text", v.trim ()));
            }
        }
        List<Attribute> remainingAttrs = this.applyAttributes (obj, factory,
                attributes);
        //
        // process child tags
        //

        final LayoutManager layoutMgr = obj instanceof Container ? ((Container) obj)
                .getLayout () : null;

        final NodeList nl = element.getChildNodes ();
        for (int i = 0 ; i < nl.getLength () ; i++) {
            if (! (nl.item (i) instanceof Element)) {
                continue;
            }
            final Element child = (Element) nl.item (i);
            //
            // Prepare for possible grouping through BottonGroup Tag
            //
            if ("buttongroup".equalsIgnoreCase (child.getNodeName ())) {

                int k = JMenu.class.isAssignableFrom (obj.getClass ()) ? ((JMenu) obj)
                        .getItemCount () : ((Container) obj)
                        .getComponentCount ();
                this.getSwing (child, obj);
                final int n = JMenu.class.isAssignableFrom (obj.getClass ()) ? ((JMenu) obj)
                        .getItemCount () : ((Container) obj)
                        .getComponentCount ();
                //
                // add the recently add container entries into the btngroup
                //
                final ButtonGroup btnGroup = new ButtonGroup ();
                // incase the button group was given an id attr. it should also
                // be put into the idmap.
                if (null != Attribute.getAttributeValue (child, Parser.ATTR_ID)) {
                    this.engine.getIdMap ()
                            .put (Attribute.getAttributeValue (child,
                                    Parser.ATTR_ID), btnGroup);
                }
                while (k < n) {
                    Parser.putIntoBtnGrp (JMenu.class.isAssignableFrom (obj
                            .getClass ()) ? ((JMenu) obj).getItem (k++)
                            : ((Container) obj).getComponent (k++), btnGroup);
                }
                continue;
            }

            //
            // A CONSTRAINTS attribute is removed from the childtag but used to
            // add the child into the currrent obj
            //
            final Attribute constrnAttr = Attribute.getAttribute (child,
                    Parser.ATTR_CONSTRAINTS);
            Object constrains = null;
            if ( (constrnAttr != null) && (layoutMgr != null)) {
                child.removeAttribute (Parser.ATTR_CONSTRAINTS); // therefore it
                                                                 // won't be
                                                                 // used in
                                                                 // getSwing(child)
                final LayoutConverter layoutConverter = LayoutConverterLibrary
                        .getInstance ().getLayoutConverter (
                                layoutMgr.getClass ());
                if (layoutConverter != null) {
                    constrains = layoutConverter
                            .convertConstraintsAttribute (constrnAttr);
                }
            }

            //
            // A CONSTRAINTS element is used to add the child into the currrent
            // obj
            //
            final Element cce = Parser.getChildByName (child, "constraints");
            if ( (cce != null) && (layoutMgr != null)) {
                final LayoutConverter layoutConverter = LayoutConverterLibrary
                        .getInstance ().getLayoutConverter (
                                layoutMgr.getClass ());
                if (layoutConverter != null) {
                    constrains = layoutConverter
                            .convertConstraintsElement (cce);
                }
            }

            //
            // A constraints or GridBagConstraints grand-childtag is not added
            // at all ..
            // .. but used to add the child into this container
            //
            final Element grandchild = Parser.getChildByName (child,
                    "gridbagconstraints");
            if (grandchild != null) {
                this.addChild ((Container) obj,
                        (Component) this.getSwing (child, null),
                        this.getSwing (grandchild, null));
            } else if (!child.getNodeName ().equals ("constraints")
                    && !child.getNodeName ().equals ("gridbagconstraints")) {
                this.addChild ((Container) obj,
                        (Component) this.getSwing (child, null), constrains);
            }
        }

        //
        // 2nd attempt to apply attributes (call setters on the objects)
        //
        if ( (remainingAttrs != null) && (0 < remainingAttrs.size ())) {
            remainingAttrs = this
                    .applyAttributes (obj, factory, remainingAttrs);
            if (remainingAttrs != null) {
                final Iterator<Attribute> it = remainingAttrs.iterator ();
                while ( (it != null) && it.hasNext ()) {
                    final Attribute attr = it.next ();
                    if (JComponent.class.isAssignableFrom (obj.getClass ())) {
                        ((JComponent) obj).putClientProperty (attr.getName (),
                                attr.getValue ());
                        if (AppConstants.DEBUG_MODE) {
                            System.out.println ("ClientProperty put: "
                                    + obj.getClass ().getName () + "(" + id
                                    + "): " + attr.getName () + "="
                                    + attr.getValue ());
                        }
                    } else {
                        if (AppConstants.DEBUG_MODE) {
                            System.err.println (attr.getName ()
                                    + " not applied for tag: <"
                                    + element.getNodeName () + ">");
                        }
                    }
                }
            }
        }

        return (constructed ? obj : null);
    }

    /**
     * Helper Method to Link Labels to InputFields etc.
     */
    private void linkLabels () {
        final Iterator<JLabel> it = this.lbl_map.keySet ().iterator ();
        while ( (it != null) && it.hasNext ()) {
            final JLabel lbl = (JLabel) it.next ();
            final String id = this.lbl_map.get (lbl).toString ();
            try {
                lbl.setLabelFor ((Component) this.engine.getIdMap ().get (id));
            } catch (final ClassCastException e) {
                // intent. empty.
            }
        }
    }

    /**
     * Converts XML into a javax.swing object tree.
     * 
     * <pre>
     *    Reads XML from the provied <code>Reader</code> and builds an intermediate jdom document.
     *    Tags and their attributes are getting converted into swing objects.
     * </pre>
     * 
     * @param jdoc
     *            <code>Document</code> providing the XML document
     * @return <code>java.awt.Container</code> root object for the swing object
     *         tree
     * @throws Exception
     *             if parsing fails
     */
    public Object parse (Document jdoc) throws Exception {
        this.jdoc = jdoc;
        this.lbl_map.clear ();
        final Object obj = this
                .getSwing (this.processCustomAttributes (jdoc
                        .getDocumentElement ()), null);

        this.linkLabels ();
        this.supportMacOS ();

        this.lbl_map.clear ();
        this.mac_map.clear ();
        return obj;
    }

    /**
     * Converts XML into a javax.swing object tree.
     * 
     * <pre>
     * Note: This parse method does not return a swing object but converts all <b>sub</b> nodes
     * of the xml documents root into seing objects and adds those into the provided container.
     * This is useful when a JApplet for instance already exists and need to get some gui inserted.
     * </pre>
     * 
     * @param jdoc
     *            <code>Document</code> providing the XML document
     * @param container
     *            <code>Container</code> container for the XML root's children
     * @throws Exception
     *             if parsing fails
     */
    public void parse (Document jdoc, Container container) throws Exception {
        this.jdoc = jdoc;
        this.lbl_map.clear ();
        this.mac_map.clear ();
        this.getSwing (
                this.processCustomAttributes (jdoc.getDocumentElement ()),
                container);

        this.linkLabels ();
        this.supportMacOS ();

        this.lbl_map.clear ();
        this.mac_map.clear ();
    }

    /**
     * Looks for custom attributes to be proccessed.
     * 
     * @param element
     *            <code>Element</code> custom attr. tag are looked for in this
     *            jdoc element
     * @return <code>Element</code> - passed in (and maybe modified) element <br />
     *         <b>Note:</b> <br />
     *         Successfully proccessed custom attributes will be removed from
     *         the jdoc element.
     * @throws Exception
     *             if parsing fails
     */
    private Element processCustomAttributes (Element element) throws Exception {
        //
        // set Locale
        //
        final String locale = Attribute.getAttributeValue (element,
                Parser.ATTR_LOCALE);
        if (locale != null) {
            this.engine.setLocale (LocaleConverter.conv (locale));
            element.removeAttribute (Parser.ATTR_LOCALE);
        }
        //
        // set ResourceBundle
        //
        final String bundle = Attribute.getAttributeValue (element,
                Parser.ATTR_BUNDLE);
        if (bundle != null) {
            this.engine.getLocalizer ().setResourceBundle (bundle);
            element.removeAttribute (Parser.ATTR_BUNDLE);
        }
        //
        // Set Look and Feel based on ATTR_PLAF
        //
        final String plaf = Attribute.getAttributeValue (element,
                Parser.ATTR_PLAF);
        if ( (plaf != null) && (0 < plaf.length ())) {
            try {
                UIManager.setLookAndFeel (plaf);
            } catch (final Exception e) {
                if (AppConstants.DEBUG_MODE) {
                    System.err.println (e);
                }
            }
            element.removeAttribute (Parser.ATTR_PLAF);
        }
        return element;
    }

    /**
     * Link actions with the MacOS' system menu bar
     */
    private void supportMacOS () {
        if (AppConstants.isMacOSXSupported () && AppConstants.isMacOSX ()) {
            try {
                MacApp.getInstance ().update (this.mac_map);
            } catch (final Throwable t) {
                // intentionally empty
            }
        }
    }
}
