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

package com.comcast.redirector.core.spring;

import com.comcast.redirector.core.spring.AppScope;
import com.comcast.redirector.core.spring.AppsContextHolder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AppScopeTest {
    private static final int N_ITERATIONS = 100;
    private static final int N_REQUESTS = 1000;
    private static final int N_BEANS_PER_REQUEST = 20;
    private static final int N_BEANS_CREATED = N_REQUESTS * N_BEANS_PER_REQUEST;

    private FakeObjectFactory objectFactory = new FakeObjectFactory();
    private CyclicBarrier requestsStarted = new CyclicBarrier(N_REQUESTS);
    private ExecutorService pool = Executors.newCachedThreadPool();

    private AppScope testee = new AppScope();

    @Test
    public void beanIsReturnedProperlyForAppSetInRequestUnderSpikeLoad() throws Exception {
        verifyInitOfBeans();

        for (int i = 0; i < N_ITERATIONS; i++) {
            verifyBeansGotFromCache();
        }
    }

    private void verifyInitOfBeans() throws Exception {
        boolean allRequestsCreatedBeans = pool.invokeAll(
            requests().map(mapHandleRequest()).collect(Collectors.toList())
        ).stream().map(this::get).allMatch(result -> result);

        Assert.assertTrue(allRequestsCreatedBeans);
        Assert.assertEquals(N_BEANS_CREATED, objectFactory.counter.get());
    }

    private void verifyBeansGotFromCache() throws Exception {
        boolean allRequestsGotBeansFromCache = pool.invokeAll(
            requests().map(mapHandleRequest()).collect(Collectors.toList())
        ).stream().map(this::get).allMatch(result -> result);

        Assert.assertTrue(allRequestsGotBeansFromCache);
        Assert.assertEquals(N_BEANS_CREATED, objectFactory.counter.get());
    }

    private Function<String, Callable<Boolean>> mapHandleRequest() {
        return request -> (Callable<Boolean>)() -> {
            try {
                requestsStarted.await();
            } catch (InterruptedException|BrokenBarrierException e) {
                e.printStackTrace();
                return false;
            }
            AppsContextHolder.setCurrentApp(request);

            nStrings(N_BEANS_PER_REQUEST, "bean").forEach(bean -> testee.get(bean, objectFactory));
            return true;
        };
    }

    private <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Stream<String> requests() {
        return nStrings(N_REQUESTS, "App");
    }

    private Stream<String> nStrings(int n, String prefix) {
        return IntStream.range(1, n + 1).boxed().map(Object::toString).map(prefix::concat);
    }

    @Test
    public void beanIsCreatedOnlyOnceForTheSameApp() throws Exception {
        AppsContextHolder.setCurrentApp("appA");

        Object bean1 = testee.get("testBean", objectFactory);
        Object bean2 = testee.get("testBean", objectFactory);

        Assert.assertSame(bean1, bean2);
    }

    @Test
    public void beanIsCreatedForEachSeparateApp() throws Exception {
        AppsContextHolder.setCurrentApp("appA");
        Object beanForAppA = testee.get("testBean", objectFactory);

        AppsContextHolder.setCurrentApp("appB");
        Object beanForAppB = testee.get("testBean", objectFactory);

        Assert.assertNotSame(beanForAppA, beanForAppB);
    }

    private static class FakeObjectFactory implements ObjectFactory<Object> {
        AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Object getObject() throws BeansException {
            counter.incrementAndGet();
            return new Object();
        }
    }
}
