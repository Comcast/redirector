/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.model.testsuite.visitor;

import com.comcast.redirector.common.annotation.Visitor;
import com.comcast.redirector.api.model.ExpressionVisitor;
import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.IVisitorState;
import com.comcast.redirector.common.util.AnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractVisitorFactory {
    private static final Logger log = LoggerFactory.getLogger(AbstractVisitorFactory.class);

    private Map<Class<?>, Class<? extends ExpressionVisitor<IVisitable>>> visitors = new HashMap<>();
    private Lock lock = new ReentrantLock();
    private final ExpressionVisitor<IVisitable> defaultVisitor;
    private Set<Class<?>> annotatedClasses;
    private Class<? extends Annotation>[] annotations;

    protected AbstractVisitorFactory(ExpressionVisitor<IVisitable> defaultVisitor,
                           Class<? extends Annotation>[] annotations,
                           String... packages) {
        this.defaultVisitor = defaultVisitor;
        this.annotatedClasses = AnnotationScanner.getAnnotatedClasses(annotations, packages);
        this.annotations = annotations;
    }

    public <T extends IVisitable> ExpressionVisitor<IVisitable> get(Class<T> expressionClass, IVisitorState visitorState) {
        ExpressionVisitor<IVisitable> result = getDefaultVisitor();
        try {
            Class<? extends ExpressionVisitor<IVisitable>> clazz = getVisitorClass(expressionClass);

            if (clazz != null) {
                result = clazz.newInstance();
                result.setVisitorState(visitorState);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("Failed to create visitor", e);
        }

        return result;
    }

    public <T extends IVisitable> ExpressionVisitor<IVisitable> get(Class<T> expressionClass) {
        ExpressionVisitor<IVisitable> result = defaultVisitor;
        try {
            Class<? extends ExpressionVisitor<IVisitable>> clazz = getVisitorClass(expressionClass);

            if (clazz != null) {
                result = clazz.newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("Failed to create visitor", e);
        }

        return result;
    }

    protected <T extends IVisitable> Class<? extends ExpressionVisitor<IVisitable>> getVisitorClass(Class<T> expressionClass)
        throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Class<? extends ExpressionVisitor<IVisitable>> clazz = null;
        try {
            lock.lock();
            if ((clazz = visitors.get(expressionClass)) == null) {
                clazz = createVisitor(expressionClass);
                visitors.put(expressionClass, clazz);
            }
        } finally {
            lock.unlock();
        }

        return clazz;
    }

    protected ExpressionVisitor<IVisitable> getDefaultVisitor() {
        return defaultVisitor;
    }

    protected Set<Class<?>> getAnnotatedClasses() {
        return annotatedClasses;
    }

    private Class<? extends ExpressionVisitor<IVisitable>> createVisitor(Class<?> expressionClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (Class<?> cl : getAnnotatedClasses()) {
            for (Class<? extends Annotation> visitorAnnotationClass : annotations) {
                Annotation annotation = cl.getAnnotation(visitorAnnotationClass);
                try {
                    if (annotation.getClass().getMethod("forClass").invoke(annotation) == expressionClass) {
                        Visitor visitorAnnotation = annotation.annotationType().getAnnotation(Visitor.class);
                        if (visitorAnnotation != null && visitorAnnotation.visitorClass() != null) {
                            return visitorAnnotation.visitorClass().getClass().cast(cl);
                        }
                    }
                } catch (InvocationTargetException | NoSuchMethodException e) {
                    log.error("Failed to create visitor", e);
                }
            }
        }
        return null;
    }
}
