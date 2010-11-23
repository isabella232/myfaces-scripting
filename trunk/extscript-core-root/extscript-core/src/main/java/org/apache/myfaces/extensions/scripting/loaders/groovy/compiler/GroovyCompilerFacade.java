/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.extensions.scripting.loaders.groovy.compiler;

import org.apache.myfaces.extensions.scripting.api.CompilationResult;
import org.apache.myfaces.extensions.scripting.api.DynamicCompiler;
import org.apache.myfaces.extensions.scripting.api.ScriptingConst;
import org.apache.myfaces.extensions.scripting.core.util.ClassUtils;
import org.apache.myfaces.extensions.scripting.core.util.FileUtils;
import org.apache.myfaces.extensions.scripting.core.util.WeavingContext;
import org.apache.myfaces.extensions.scripting.loaders.groovy.GroovyRecompiledClassloader;
import org.apache.myfaces.extensions.scripting.sandbox.compiler.GroovyCompiler;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 *          <p/>
 *          Custom compiler call for jdk5
 *          we can call javac directly
 */

public class GroovyCompilerFacade implements DynamicCompiler {

    Logger _log = Logger.getLogger(this.getClass().getName());
    GroovyCompiler compiler;

    static final PrivilegedExceptionAction<GroovyRecompiledClassloader> LOADER_ACTION = new PrivilegedExceptionAction<GroovyRecompiledClassloader>() {
        public GroovyRecompiledClassloader run() {
            return new GroovyRecompiledClassloader(ClassUtils.getContextClassLoader(), ScriptingConst.ENGINE_TYPE_JSF_GROOVY, ".groovy");
        }
    };

    public GroovyCompilerFacade() {
        super();

        compiler = new GroovyCompiler();
    }

    public Class loadClass(String sourceRoot, String classPath, String filePath) throws ClassNotFoundException {

        String separator = FileUtils.getFileSeparatorForRegex();
        String className = filePath.replaceAll(separator, ".");
        className = ClassUtils.relativeFileToClassName(className);

        GroovyRecompiledClassloader classLoader;
        try {
            classLoader = AccessController.doPrivileged(LOADER_ACTION);
        } catch (PrivilegedActionException e) {
            _log.log(Level.SEVERE, "", e);
            return null;
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            classLoader.setSourceRoot(sourceRoot);
            Thread.currentThread().setContextClassLoader(classLoader);

            return classLoader.loadClass(className);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }


    public File compileFile(String sourceRoot, String classPath, String filePath)  {
        //TODO do a full compile and block the compile for the rest of the request
        //so that we do not run into endless compile cycles

        /*
        * privilege block to allow custom classloading only
        * in case of having the privileges,
        * this was proposed by the checkstyle plugin
        */
        GroovyRecompiledClassloader classLoader;
        try {
            classLoader = AccessController.doPrivileged(LOADER_ACTION);
        } catch (PrivilegedActionException e) {
            _log.log(Level.SEVERE, "", e);
            return null;
        }


        classLoader.setSourceRoot(sourceRoot);
        CompilationResult result = compiler.compile(new File(sourceRoot), WeavingContext.getConfiguration().getCompileTarget(), new File(sourceRoot + File.separator + filePath), classLoader);

        displayMessages(result);

        return new File(WeavingContext.getConfiguration().getCompileTarget() + File.separator + filePath.substring(0, filePath.lastIndexOf('.')) + ".class");
    }

    

    /**
     * compiles all files
     *
     * @param sourceRoot the source root
     * @param classPath  the class path
     * @return the root target path for the classes which are compiled
     *         so that they later can be picked up by the classloader
     */
    public File compileAllFiles(String sourceRoot, String classPath) {
        GroovyRecompiledClassloader classLoader;
        try {
            classLoader = AccessController.doPrivileged(LOADER_ACTION);
        } catch (PrivilegedActionException e) {
            _log.log(Level.SEVERE, "", e);
            return null;
        }

        classLoader.setSourceRoot(sourceRoot);
        CompilationResult result = compiler.compile(new File(sourceRoot), WeavingContext.getConfiguration().getCompileTarget(), classLoader);

        displayMessages(result);
        return WeavingContext.getConfiguration().getCompileTarget();
    }

    private void displayMessages(CompilationResult result) {
        for (CompilationResult.CompilationMessage error : result.getErrors()) {
            _log.log(Level.WARNING, "[EXT-SCRIPTING] Groovy compiler error: {0} - {1}", new String[]{Long.toString(error.getLineNumber()), error.getMessage()});
        }
        for (CompilationResult.CompilationMessage error : result.getWarnings()) {
            _log.log(Level.WARNING, "[EXT-SCRIPTING] Groovy compiler warning: {0}", error.getMessage());
        }
        WeavingContext.setCompilationResult(ScriptingConst.ENGINE_TYPE_JSF_GROOVY, result);
    }
}
