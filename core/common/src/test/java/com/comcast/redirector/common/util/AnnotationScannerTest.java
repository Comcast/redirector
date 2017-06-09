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

package com.comcast.redirector.common.util;

import com.comcast.redirector.common.util.annotation.scanner.sample.annotations.Scannable;
import com.comcast.redirector.common.util.annotation.scanner.sample.classes.AnnotadedByIgnoredAnnotation;
import com.comcast.redirector.common.util.annotation.scanner.sample.classes.Annotated;
import com.comcast.redirector.common.util.annotation.scanner.sample.classes.NotAnnotated;
import com.comcast.redirector.common.util.annotation.scanner.sample.classes.OneMoreAnnotated;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class AnnotationScannerTest {
    @Test
    public void annotatedClassesAreFound() throws Exception {
        Class[] annotations = {Scannable.class};
        String[] packages = {"com.comcast.redirector.common.util.annotation.scanner.sample"};

        Set<Class<?>> result = AnnotationScanner.getAnnotatedClasses(annotations, packages);

        Assert.assertTrue(result.contains(Annotated.class));
        Assert.assertTrue(result.contains(OneMoreAnnotated.class));
        Assert.assertFalse(result.contains(NotAnnotated.class));
        Assert.assertFalse(result.contains(AnnotadedByIgnoredAnnotation.class));
    }
}
