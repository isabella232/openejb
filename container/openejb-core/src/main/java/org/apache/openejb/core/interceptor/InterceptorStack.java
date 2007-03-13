/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.interceptor;

import org.apache.openejb.core.Operation;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorStack {
    private final Object beanInstance;
    private final List<Interceptor> interceptors;
    private final Method targetMethod;

    public InterceptorStack(Object beanInstance, Method targetMethod, Operation operation, List<InterceptorData> interceptorDatas, Map<String, Object> interceptorInstances) {
        if (interceptorDatas == null) throw new NullPointerException("interceptorDatas is null");
        if (interceptorInstances == null) throw new NullPointerException("interceptorInstances is null");
        this.beanInstance = beanInstance;
        this.targetMethod = targetMethod;

        interceptors = new ArrayList<Interceptor>(interceptorDatas.size());
        for (InterceptorData interceptorData : interceptorDatas) {
            Class interceptorClass = interceptorData.getInterceptorClass();
            Object interceptorInstance = interceptorInstances.get(interceptorClass.getName());
            if (interceptorInstance == null) {
                throw new IllegalArgumentException("No interceptor of type " + interceptorClass.getName());
            }

            List<Method> methods = interceptorData.getMethods(operation);
            for (Method method : methods) {
                Interceptor interceptor = new Interceptor(interceptorInstance, method);
                interceptors.add(interceptor);
            }
        }
    }

    public InterceptorStack(Object beanInstance, Method method, List<Interceptor> interceptors) {
        this.beanInstance = beanInstance;
        this.targetMethod = method;
        this.interceptors = interceptors;
    }

    public InvocationContext createInvocationContext() {
        InvocationContext invocationContext = new ReflectionInvocationContext(interceptors, beanInstance, targetMethod);
        return invocationContext;
    }

    public Object invoke(Object... parameters) throws Exception {
        InvocationContext invocationContext = createInvocationContext();
        invocationContext.setParameters(parameters);
        Object value = invocationContext.proceed();
        return value;
    }
}
