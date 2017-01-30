/**
 * Copyright 2016 Comcast Cable Communications Management, LLC 
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

package com.comcast.redirector.common.util;

import com.google.common.reflect.ClassPath;
import org.eclipse.persistence.internal.libraries.asm.AnnotationVisitor;
import org.eclipse.persistence.internal.libraries.asm.ClassReader;
import org.eclipse.persistence.internal.libraries.asm.ClassVisitor; // TODO: move to objectweb.asm
import org.eclipse.persistence.internal.libraries.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class AnnotationScanner {
    private static final Logger log = LoggerFactory.getLogger(AnnotationScanner.class);

    // TODO: do we have any benefit of all methods here being static?
    public static Set<Class<?>> getAnnotatedClasses(final Class[] annotations, final String... packages) {
        Set<String> annotationsToScan = Stream.of(annotations).map(Class::getSimpleName).collect(toSet());

        ClassLoader cl = AnnotationScanner.class.getClassLoader();
        try {
            ClassPath cp = ClassPath.from(cl);
            return Stream.of(packages)
                .flatMap(packageName -> cp.getTopLevelClassesRecursive(packageName).stream())
                .filter(isClassAnnotatedByScannedAnnotations(annotationsToScan))
                .map(ClassPath.ClassInfo::load)
                .collect(toSet());
        } catch (IOException e) {
            log.error("Failed to get annotated classes", e);
            return Collections.emptySet();
        }
    }

    private static Predicate<ClassPath.ClassInfo> isClassAnnotatedByScannedAnnotations(Set<String> annotationNames) {
        return classInfo -> {
            Set<String> annotationsForClass = getAnnotationNames(classInfo.url());
            annotationsForClass.retainAll(annotationNames);
            return ! annotationsForClass.isEmpty();
        };
    }

    private static Set<String> getAnnotationNames(URL classUrl) {
        final Set<String> annotations = new HashSet<>();
        try (InputStream classStream = classUrl.openStream()) {
            ClassReader reader = new ClassReader(classStream);
            reader.accept(new ClassVisitor(Opcodes.ASM4) { // TODO: do we need ASM5 code instead?
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (visible) {
                        annotations.add(desc.substring(desc.lastIndexOf("/") + 1, desc.lastIndexOf(";")));
                    }
                    return null;
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
        } catch (IOException e) {
            log.error("Failed to get annotations", e);
        }
        return annotations;
    }
}
