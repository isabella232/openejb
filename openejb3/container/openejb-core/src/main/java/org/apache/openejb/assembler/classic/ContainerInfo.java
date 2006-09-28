/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import java.util.Properties;

public abstract class ContainerInfo extends InfoObject {

    public static final int ENTITY_CONTAINER = 0;

    public static final int STATEFUL_SESSION_CONTAINER = 2;

    public static final int STATELESS_SESSION_CONTAINER = 3;

    public String description;
    public String displayName;
    public String containerName;
    public String codebase;
    public String className;
    public EnterpriseBeanInfo[] ejbeans;
    public Properties properties;
    public String[] constructorArgs;

    public int containerType;
}
