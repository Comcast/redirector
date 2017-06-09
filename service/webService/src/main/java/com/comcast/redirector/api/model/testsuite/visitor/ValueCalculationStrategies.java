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

import com.comcast.redirector.api.model.Expressions;
import com.comcast.redirector.api.model.SingleParameterExpression;
import com.comcast.redirector.api.model.TypedSingleParameterExpression;
import com.google.common.base.Joiner;
import com.google.common.net.InetAddresses;
import com.mifmif.common.regex.Generex;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public enum ValueCalculationStrategies {
    OR_EQUAL(new ValueCalculationStrategy() {
        @Override
        protected String getValueFrom(String input, Expressions.ValueType valueType) {
            return input;
        }
    }),
    GREATER(new ValueCalculationStrategy() {
        @Override
        protected String getValueFrom(String input, Expressions.ValueType valueType) {
            return ValueFactory.newValue(input, valueType).getGreater();
        }
    }),
    LESS(new ValueCalculationStrategy() {
        @Override
        protected String getValueFrom(String input, Expressions.ValueType valueType) {
            return ValueFactory.newValue(input, valueType).getLess();
        }
    }),
    MATCHES(new ValueCalculationStrategy() {
        @Override
        protected String getValueFrom(String input, Expressions.ValueType valueType) {
            String regexp = prepareRegexp(input);
            Generex generex = new Generex(regexp);
            String result = generex.random();

            return result.matches("[ -~]+") ? result : new Generex(regexp.replaceAll("(?<!\\\\)\\.", "[ -~]")).random();
        }

        private String prepareRegexp(String input) {
            String regexp = input;
            if (regexp.startsWith("^")) {
                regexp = regexp.substring(1);
            }
            if (regexp.endsWith("$")) {
                regexp = regexp.substring(0, regexp.length() - 1);
            }

            return regexp;
        }
    });

    private IValueCalculationStrategy strategy;

    ValueCalculationStrategies(IValueCalculationStrategy strategy) {
        this.strategy = strategy;
    }

    public IValueCalculationStrategy getStrategy() {
        return strategy;
    }

    private abstract static class ValueCalculationStrategy implements IValueCalculationStrategy {

        @Override
        public String getValueFrom(SingleParameterExpression item) {
            Expressions.ValueType valueType = (item instanceof TypedSingleParameterExpression)
                ? Expressions.ValueType.valueOf(((TypedSingleParameterExpression) item).getType().toUpperCase())
                : Expressions.ValueType.NONE;
            return getValueFrom(item.getValue(), valueType);
        }

        abstract protected String getValueFrom(String input, Expressions.ValueType valueType);
    }

    private static class ValueFactory {
        static IValueCalculationStrategy.Value newValue(String input, Expressions.ValueType valueType) {
            switch (valueType) {
                case NUMERIC:
                    return new NumericValue(input);
                case VERSION:
                    return new VersionValue(input);
                case IPV6:
                    return new IPv6Value(input);
                default:
                    return new StringValue(input);
            }
        }

        static class NumericValue implements IValueCalculationStrategy.Value {
            private Double number;

            public NumericValue(String number) {
                this.number = Double.valueOf(number);
            }

            @Override
            public String getGreater() {
                return String.valueOf(number.intValue() + 1);
            }

            @Override
            public String getLess() {
                return String.valueOf(number.intValue() - 1);
            }
        }

        static class VersionValue implements IValueCalculationStrategy.Value {
            private List<Integer> subVersions;

            public VersionValue(String version) {
                String[] subVersions = version.split("\\.");
                this.subVersions = new ArrayList<>(subVersions.length);
                for (String subVersion : subVersions) {
                    this.subVersions.add(Integer.valueOf(subVersion));
                }
            }

            @Override
            public String getGreater() {
                return Joiner.on(".").join(modify(getSubVersions(), +1));
            }

            @Override
            public String getLess() {
                return Joiner.on(".").join(modify(getSubVersions(), -1));
            }

            private static List<Integer> modify(List<Integer> source, int diff) {
                int index = source.size() - 1;
                source.set(index, source.get(index) + diff);
                return source;
            }

            private List<Integer> getSubVersions() {
                List<Integer> copy = new ArrayList<>(subVersions.size());
                for (Integer src : subVersions) {
                    copy.add(src);
                }
                return copy;
            }
        }

        static class IPv6Value implements IValueCalculationStrategy.Value {
            InetAddress value;

            public IPv6Value(String address) {
                this.value = InetAddresses.forString(address);
            }

            @Override
            public String getGreater() {
                return InetAddresses.increment(value).getHostAddress();
            }

            @Override
            public String getLess() {
                return InetAddresses.decrement(value).getHostAddress();
            }
        }

        static class StringValue implements IValueCalculationStrategy.Value {
            String value;

            public StringValue(String value) {
                this.value = value;
            }

            @Override
            public String getGreater() {
                byte[] bytes = value.getBytes();
                bytes[bytes.length - 1]++;
                return new String(bytes, Charset.forName("UTF-8"));
            }

            @Override
            public String getLess() {
                byte[] bytes = value.getBytes();
                bytes[bytes.length - 1]--;
                return new String(bytes, Charset.forName("UTF-8"));
            }
        }
    }
}
