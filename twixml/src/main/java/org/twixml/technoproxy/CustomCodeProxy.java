package org.twixml.technoproxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.twixml.AppConstants;
import org.twixml.exception.ProxyCodeException;

public class CustomCodeProxy {

    private static Map<String, PlatformUnit> units = new HashMap<String, PlatformUnit> () {
                                                       /**
         * 
         */
                                                       private static final long serialVersionUID = 3339561009429231514L;

                                                       {
                                                           // To be filled with
                                                           // platform units
                                                       }
                                                   };

    /**
     * We leave the possibility to add an extra unit for the needs of the users,
     * so they can add their own facilities
     * 
     * @param pu
     *            the unit
     * @param alias
     *            the classname of the engine (should end with "TwiXML")
     */
    public static void addNewUnit (final String alias, final PlatformUnit pu) {
        CustomCodeProxy.units.put (alias, pu);
    }

    public static <T, R> R doProxy (final T source, final Object... params) {
        return CustomCodeProxy.doProxy (source, "", params);
    }

    @SuppressWarnings ("unchecked")
    public static <T, R> R doProxy (final T source, final String suffix,
            final Object... params) {
        try {

            final StackTraceElement ste = "doProxy".equals (Thread
                    .currentThread ().getStackTrace () [2].getMethodName ()) ? Thread
                    .currentThread ().getStackTrace () [3] : Thread
                    .currentThread ().getStackTrace () [2];
            final PlatformUnit unit = CustomCodeProxy.units
                    .get (CustomCodeProxy.getPlatformName ());
            final Class<?> c = unit.getProxyClasses ().get (
                    source.getClass ().getName ());

            if (c == null) {
                throw new ProxyCodeException (new ClassNotFoundException (
                        source.getClass ().getName ()));
            }

            final Method [] ms = c.getMethods ();
            Method m = null;
            int i = 0;
            final String methodName = ste.getMethodName () + suffix;
            while ( (i < ms.length) && (m == null)) {
                if (ms [i].getName ().equals (methodName)) {
                    m = ms [i];
                }
                i++;
            }
            final Constructor<ProxyCode<T>> constr = (Constructor<ProxyCode<T>>) c
                    .getConstructors () [0];
            final ProxyCode<T> pc = constr.newInstance (source);
            return (R) m.invoke (pc, params);
        } catch (final SecurityException e) {
            throw new ProxyCodeException (e);
        } catch (final InstantiationException e) {
            throw new ProxyCodeException (e);
        } catch (final IllegalAccessException e) {
            throw new ProxyCodeException (e);
        } catch (final IllegalArgumentException e) {
            throw new ProxyCodeException (e);
        } catch (final InvocationTargetException e) {
            throw new ProxyCodeException (e);
        }
    }

    private static String getPlatformName () {
        if (System.getProperty ("platform.name") != null) {
            return System.getProperty ("platform.name");
        }
        final StackTraceElement [] ste = Thread.currentThread ()
                .getStackTrace ();
        int i = 0;
        String platformName = null;
        while ( (i < ste.length) && (platformName == null)) {
            final String simpleClassName = ste [i].getClassName ()
                    .indexOf ('.') == -1 ? ste [i].getClassName () : ste [i]
                    .getClassName ().substring (
                            ste [i].getClassName ().lastIndexOf ('.') + 1);
            if (CustomCodeProxy.units.containsKey (simpleClassName)) {
                platformName = simpleClassName;
            }
            i++;
        }
        if (platformName == null) {
            if (AppConstants.SWITCH_TO_SWING_IF_PLATFORM_NULL) {
                return "SwingTwiXML";
            }
            throw new ProxyCodeException (
                    new IllegalArgumentException (
                            "[Could not know which platform the app is running on] ; Choose among these classes : "
                                    + CustomCodeProxy.units.keySet ()));
        }
        return platformName;
    }

    public static TypeAnalyser getTypeAnalyser () {
        final PlatformUnit unit = CustomCodeProxy.units.get (CustomCodeProxy
                .getPlatformName ());
        return unit.getTypeAnalyser ();
    }
}
