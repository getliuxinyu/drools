/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.drools.core.base.AccessorKey;
import org.drools.core.base.ReadAccessorSupplier;
import org.drools.core.impl.RuleBase;
import org.drools.core.spi.InternalReadAccessor;
import org.drools.core.util.ClassUtils;

public class DroolsObjectInputStream extends ObjectInputStream
    implements
    DroolsObjectInput {

    private ClassLoader classLoader;
    private RuleBase ruleBase;
    private InternalWorkingMemory workingMemory;
    private Package pkg;
    private ReadAccessorSupplier store;

    private Map<AccessorKey, List<Consumer<InternalReadAccessor>>> extractorBinders = new HashMap<>();
    
    private Map<String, Object> customExtensions = new HashMap<>();

    private final Map<String, Object> clonedByIdentity;

    public DroolsObjectInputStream(InputStream inputStream) throws IOException {
        this( inputStream, null );
    }

    public DroolsObjectInputStream(InputStream inputStream,
                                   ClassLoader classLoader) throws IOException {
        this( inputStream, classLoader, null );
    }

    public DroolsObjectInputStream(InputStream inputStream, ClassLoader classLoader,
                                   Map<String, Object> clonedByIdentity) throws IOException {
        super( inputStream );
        if ( classLoader == null ) {
            classLoader = Thread.currentThread().getContextClassLoader();
            if ( classLoader == null ) {
                classLoader = getClass().getClassLoader();
            }
        }

        this.classLoader = classLoader;
        this.clonedByIdentity = clonedByIdentity;
    }

    public boolean isCloning() {
        return clonedByIdentity != null;
    }

    public <T> T getCloneByKey(String key) {
        return (T) clonedByIdentity.get(key);
    }

    protected Class resolveClass(String className) throws ClassNotFoundException {
        return ClassUtils.getClassFromName( className, true, this.classLoader );
    }

    protected Class< ? > resolveClass(ObjectStreamClass desc) throws IOException,
                                                             ClassNotFoundException {
        return resolveClass( desc.getName() );
    }

    public static InvalidClassException newInvalidClassException(Class clazz, Throwable cause) {
        return newInvalidClassException(clazz.getName(), cause);
    }

    public static InvalidClassException newInvalidClassException(final String className, final Throwable cause) {
        InvalidClassException exception = new InvalidClassException(className);
        exception.initCause(cause);
        return exception;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public RuleBase getRuleBase() {
        return ruleBase;
    }

    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
        this.classLoader = this.ruleBase.getRootClassLoader();
    }

    public InternalWorkingMemory getWorkingMemory() {
        return workingMemory;
    }

    public void setWorkingMemory(InternalWorkingMemory workingMemory) {
        this.workingMemory = workingMemory;
    }

    public Package getPackage() {
        return pkg;
    }

    public void setPackage(Package pkg) {
        this.pkg = pkg;
    }

    public ClassLoader getParentClassLoader() {
        return classLoader;
    }

    public void setStore( ReadAccessorSupplier store ) {
        this.store = store;
    }

    public void readExtractor( Consumer<InternalReadAccessor> binder ) throws ClassNotFoundException, IOException {
        Object accessor = readObject();
        if (accessor instanceof AccessorKey ) {
            InternalReadAccessor reader = store != null ? store.getReader((AccessorKey) accessor) : null;
            if (reader == null) {
                // when an accessor is used in a query it may have been defined in a different package and that package
                // couldn't have been deserialized yet, so delay this binding at the end of the deserialization process
                extractorBinders.computeIfAbsent( (AccessorKey) accessor, k -> new ArrayList<>() ).add( binder );
            } else {
                binder.accept( reader );
            }
        } else {
            binder.accept( (InternalReadAccessor) accessor );
        }
    }

    public void setClassLoader( ClassLoader classLoader ) {
        if ( classLoader == null ) {
            classLoader = Thread.currentThread().getContextClassLoader();
            if ( classLoader == null ) {
                classLoader = getClass().getClassLoader();
            }
        }
        this.classLoader = classLoader;
    }


    public Map<String, Object> getCustomExtensions() {
        return customExtensions;
    }

    public void addCustomExtensions(String key, Object extension) {
        this.customExtensions.put(key, extension);
    }
}
