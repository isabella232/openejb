/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.alt.config;

import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Vector;

import org.openejb.OpenEJBException;
import org.openejb.loader.SystemInstance;
import org.openejb.alt.config.ejb11.EjbJar;
import org.openejb.alt.config.rules.CheckClasses;
import org.openejb.alt.config.rules.CheckMethods;
import org.openejb.util.JarUtils;
import org.openejb.util.Messages;
import org.openejb.util.Logger;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EjbValidator {
	private static final String helpBase = "META-INF/org.openejb.cli/";

    protected static final Messages _messages = new Messages( "org.openejb.util.resources" );

    int LEVEL = 2;
    boolean PRINT_DETAILS = false;
    boolean PRINT_XML = false;
    boolean PRINT_WARNINGS = true;
    boolean PRINT_COUNT = false;

    private Vector sets = new Vector();

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public EjbValidator() throws OpenEJBException {
    	JarUtils.setHandlerSystemProperty();
    }

    public void addEjbSet(EjbSet set){
        sets.add( set );
    }

    public EjbSet[] getEjbSets(){
        EjbSet[] ejbSets = new EjbSet[sets.size()];
        sets.copyInto(ejbSets);
        return ejbSets;
    }

    public EjbSet validateJar(EjbJarUtils ejbJarUtils, ClassLoader classLoader){
        EjbSet set = null;

        try {
            set = new EjbSet(ejbJarUtils.getJarLocation(), ejbJarUtils.getEjbJar(), ejbJarUtils.getBeans(), classLoader);
            ValidationRule[] rules = getValidationRules();
            for (int i=0; i < rules.length; i++){
                rules[i].validate( set );
            }
        } catch ( Throwable e ) {
        	e.printStackTrace(System.out);
            ValidationError err = new ValidationError( "cannot.validate" );
            err.setDetails( e.getMessage() );
            set.addError( err );
        }
        return set;
    }

    protected ValidationRule[] getValidationRules(){
        ValidationRule[] rules = new ValidationRule[]{
            new CheckClasses(),
            new CheckMethods()
        };
        return rules;
    }

    //Validate all classes are present
    //Validate classes are the correct type
    //Validate ejb references
    //Validate resource references
    //Validate security references


    public void printResults(EjbSet set){
        if (!set.hasErrors() && !set.hasFailures() && (!PRINT_WARNINGS || !set.hasWarnings())){
            return;
        }
        System.out.println("------------------------------------------");
        System.out.println("JAR "+ set.getJarPath() );
        System.out.println("                                          ");

        printValidationExceptions( set.getErrors() );
        printValidationExceptions( set.getFailures() );

        if (PRINT_WARNINGS) {
            printValidationExceptions( set.getWarnings() );
        }
    }

    protected void printValidationExceptions(ValidationException[] exceptions ) {
        for (int i=0; i < exceptions.length; i++){
            System.out.print(" ");
            System.out.print(exceptions[i].getPrefix() );
            System.out.print(" ... ");
            if (!(exceptions[i] instanceof ValidationError)) {
                System.out.print(exceptions[i].getBean().getEjbName());
                System.out.print(": ");
            }
            if (LEVEL > 2) {
                System.out.println(exceptions[i].getMessage(1));
                System.out.println();
                System.out.print('\t');
                System.out.println(exceptions[i].getMessage(LEVEL));
                System.out.println();
            } else {
                System.out.println(exceptions[i].getMessage(LEVEL));
            }
        }
        if (PRINT_COUNT && exceptions.length > 0) {
            System.out.println();
            System.out.print(" "+exceptions.length+" ");
            System.out.println(exceptions[0].getCategory() );
            System.out.println();
        }

    }
    public void printResultsXML(EjbSet set){
        if (!set.hasErrors() && !set.hasFailures() && (!PRINT_WARNINGS || !set.hasWarnings())){
            return;
        }

        System.out.println("<jar>");
        System.out.print("  <path>");
        System.out.print(set.getJarPath());
        System.out.println("</path>");

        printValidationExceptionsXML( set.getErrors() );
        printValidationExceptionsXML( set.getFailures() );

        if (PRINT_WARNINGS) {
            printValidationExceptionsXML( set.getWarnings() );
        }
        System.out.println("</jar>");
    }

    protected void printValidationExceptionsXML(ValidationException[] exceptions ) {
        for (int i=0; i < exceptions.length; i++){
            System.out.print("    <");
            System.out.print(exceptions[i].getPrefix() );
            System.out.println(">");
            if (!(exceptions[i] instanceof ValidationError)) {
                System.out.print("      <ejb-name>");
                System.out.print(exceptions[i].getBean().getEjbName());
                System.out.println("</ejb-name>");
            }
            System.out.print("      <summary>");
            System.out.print(exceptions[i].getMessage(1));
            System.out.println("</summary>");
            System.out.println("      <description><![CDATA[");
            System.out.println( exceptions[i].getMessage(3) );
            System.out.println("]]></description>");
            System.out.print("    </");
            System.out.print(exceptions[i].getPrefix() );
            System.out.println(">");
        }
    }

    public void displayResults(EjbSet[] sets){
        if (PRINT_XML) {
            System.out.println("<results>");
            for (int i=0; i < sets.length; i++){
                printResultsXML( sets[i] );
            }
            System.out.println("</results>");
        } else {
            for (int i=0; i < sets.length; i++){
                printResults( sets[i] );
            }
            for (int i=0; i < sets.length; i++){
                if (sets[i].hasErrors() || sets[i].hasFailures()){
                    if (LEVEL < 3) {
                        System.out.println();
                        System.out.println("For more details, use the -vvv option");
                    }
                    i = sets.length;
                }
            }
        }

    }

    /*------------------------------------------------------*/
    /*    Static methods                                    */
    /*------------------------------------------------------*/

    private static void printVersion() {
        /*
         * Output startup message
         */
        Properties versionInfo = new Properties();

        try {
            JarUtils.setHandlerSystemProperty();
            versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
        } catch (java.io.IOException e) {
        }

        System.out.println( "OpenEJB EJB Validation Tool " + versionInfo.get( "version" )  +"    build: "+versionInfo.get( "date" )+"-"+versionInfo.get( "time" ));
        System.out.println( "" + versionInfo.get( "url" ) );
    }

    private static void printHelp() {
        String header = "OpenEJB EJB Validation Tool ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
            header += versionInfo.get( "version" );
        } catch (java.io.IOException e) {
        }

        System.out.println( header );

        // Internationalize this
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResource(helpBase + "validate.help").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write( b );
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    private static void printExamples() {
        String header = "OpenEJB EJB Validation Tool ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
            header += versionInfo.get( "version" );
        } catch (java.io.IOException e) {
        }

        System.out.println( header );

        // Internationalize this
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResource(helpBase + "validate.examples").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write( b );
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }


    public static void main(String args[]) {
        try{
            File directory = SystemInstance.get().getHome().getDirectory("lib");
            SystemInstance.get().getClassPath().addJarsToPath(directory);
            File directory1 = SystemInstance.get().getHome().getDirectory("dist");
            SystemInstance.get().getClassPath().addJarsToPath(directory1);
        } catch (Exception e){
            // ignore it
        }
        Logger.initialize( System.getProperties() );
        
        try {
            EjbValidator v = new EjbValidator();

            if (args.length == 0) {
                printHelp();
                return;
            }

            for (int i=0; i < args.length; i++){
                if (args[i].equals("-v")){
                    v.LEVEL = 1;
                } else if (args[i].equals("-vv")){
                    v.LEVEL = 2;
                } else if (args[i].equals("-vvv")){
                    v.LEVEL = 3;
                } else if (args[i].equals("-nowarn")){
                    v.PRINT_WARNINGS = false;
                } else if (args[i].equals("-xml")){
                    v.PRINT_XML = true;
                } else if (args[i].equals("--help")){
                    printHelp();
                } else if (args[i].equals("-examples")){
                    printExamples();
                } else if (args[i].equals("-version")){
                    printVersion();
                } else {
                    // We must have reached the jar list
                    for (; i < args.length; i++){
                        try{
                            EjbJarUtils ejbJarUtils = new EjbJarUtils(args[i]);
                            String jarLocation = ejbJarUtils.getJarLocation();
                            ClassLoader classLoader = null;
                            try {
                                File jarFile = new File(jarLocation);
                                URL[] classpath = new URL[]{jarFile.toURL()};
                                classLoader = new URLClassLoader(classpath, EjbValidator.class.getClassLoader());
                            } catch (MalformedURLException e) {
                                throw new OpenEJBException("Unable to create a classloader to load classes from '"+jarLocation+"'", e);
                            }
                            EjbSet set = v.validateJar( ejbJarUtils, classLoader);
                            v.addEjbSet( set );
                       } catch (Exception e){
                           e.printStackTrace();
                       }
                    }
                }
            }

            EjbSet[] sets = v.getEjbSets();
            v.displayResults(sets);

            for (int i=0; i < sets.length; i++){
                if (sets[i].hasErrors() || sets[i].hasFailures()){
                    System.exit(1);
                }
            }
        } catch ( Exception e ) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

}