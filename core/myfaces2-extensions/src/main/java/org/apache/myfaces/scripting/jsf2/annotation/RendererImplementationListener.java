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
package org.apache.myfaces.scripting.jsf2.annotation;

import com.thoughtworks.qdox.model.JavaClass;

import java.util.Map;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */

public class RendererImplementationListener implements SourceClassAnnotationListener {
    public boolean supportsAnnotation(Class annotation) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void register(JavaClass clazz, String annotationName, Map<String, String> params) {
    }

    public void register(Class clazz, String annotationName, Map<String, String> params) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
