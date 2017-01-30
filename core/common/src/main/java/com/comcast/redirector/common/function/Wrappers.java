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

package com.comcast.redirector.common.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class Wrappers {
    private static final Logger log = LoggerFactory.getLogger(Wrappers.class);

    @FunctionalInterface
    public interface ConsumerCheckException<T>{
        void accept(T elem) throws Exception;
    }

    @FunctionalInterface
    public interface RunnableCheckException {
        void run() throws Exception;
    }

    public static <T> Consumer<T> unchecked(ConsumerCheckException<T> wrapped) {
        return t -> {
            try {
                wrapped.accept(t);
            } catch (Exception e) {
                log.error("Consumer Execution failure", e);
                throw new RuntimeException(e);
            }
        };
    }

    public static Runnable unchecked(RunnableCheckException wrapped) {
        return () -> {
            try {
                wrapped.run();
            } catch (Exception e) {
                log.error("Runnable Execution failure", e);
                throw new RuntimeException(e);
            }
        };
    }
}
