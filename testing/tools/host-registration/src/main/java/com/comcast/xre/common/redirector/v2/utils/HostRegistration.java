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

package com.comcast.xre.common.redirector.v2.utils;

import com.comcast.redirector.core.config.RedirectorCoreConfigUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class that performs registration of hosts for given stacks in zookeeper.
 * It creates same zkPath and data structure as service discovery which is used in XRE server core
 */
public class HostRegistration {

    private static final boolean OPTIONAL_ARG = true;

    private static final String DEFAULT_IPV4_BEGIN_RANGE = "10.10.10.1";

    enum InputParam {
        CONNECTION_STRING("connection", "ZkConnectionString (e.g. localhost:2181)"),
        STACK("stack", "Stack (e.g. /po/poc5)"),
        FLAVOR("flavor", "Flavor (e.g. 1.55)"),
        APP_NAME("app", "appName (e.g. xreGuide)"),
        NUMBER_OF_HOSTS("hosts", "Number of Hosts (e.g. 100)"),
        WEIGHT("weight", "Weight (e.g. 3)"),
        START_IP("ip", "Start IP (e.g. 10.10.10.5)"),
        START_IP_V6("ipv6", "Start IP (e.g. 2001:db8:11a3:9d7:1f34:8a2e:7a0:79b8)", OPTIONAL_ARG),
        RUN_TIME_IN_MINUTES("ttl", "Run Time in minutes (e.g. 20)", OPTIONAL_ARG),
        ZK_BASE_PATH("zkBasePath", "ZkBasePath (e.g. /testcases)", OPTIONAL_ARG);

        private static final String KEY_PREFIX = "-";

        private String key;
        private String description;
        private boolean optional = false;

        InputParam(String key, String description) {
            this.key = key;
            this.description = description;
        }

        InputParam(String key, String description, boolean optional) {
            this(key, description);
            this.optional = optional;
        }

        public boolean isOptional() {
            return optional;
        }

        public String toString() {
            return KEY_PREFIX + key + " " + description;
        }

        public static int numberOfRequiredArgs() {
            return maxNumberOfArgs() - numberOfOptionalArgs();
        }

        public static int maxNumberOfArgs() {
            return values().length;
        }

        public static int numberOfOptionalArgs() {
            return (int)Stream.of(values()).filter(InputParam::isOptional).count();
        }

        public static Optional<String> extractArgument(InputParam argument, String[] fromArgs) {
            for (int i = 0; i < fromArgs.length; i++) {
                String arg = fromArgs[i];
                if ((KEY_PREFIX + argument.key).equals(arg)) {
                    int valueIndex = i + 1;
                    boolean outOfBounds = valueIndex == fromArgs.length;
                    if (outOfBounds) {
                        throw new IllegalArgumentException();
                    }

                    return Optional.of(fromArgs[valueIndex]);
                }
            }

            if (argument.isOptional()) {
                return Optional.empty();
            } else {
                throw new IllegalArgumentException("Failed to extract " + argument + " from " + argsToString(fromArgs));
            }
        }

        private static String argsToString(String[] args) {
            return Stream.of(args).collect(Collectors.joining(" "));
        }
    }

    private String connectionString;
    private String zookeeperBasePath;

    private String stackName;
    private String flavor;
    private String appName;
    private String weight;

    private String numberOfHosts;
    private String startIp;
    private String startIpv6;
    private int runTime = 0;

    private HostRegister registry;

    public HostRegistration(String[] programArgs) {
        try {
            extractConfigurationFromProgramArgs(programArgs);
        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage());
            printHelp();
            exitWithError();
        }

        try {
            validateConfiguration();
        } catch (IllegalArgumentException e) {
            printErrorMessage(e.getMessage());
            exitWithError();
        }
    }

    private void extractConfigurationFromProgramArgs(String[] programArgs) {
        connectionString = InputParam.extractArgument(InputParam.CONNECTION_STRING, programArgs).get();
        stackName = InputParam.extractArgument(InputParam.STACK, programArgs).get();
        flavor = InputParam.extractArgument(InputParam.FLAVOR, programArgs).get();
        appName = InputParam.extractArgument(InputParam.APP_NAME, programArgs).get();
        numberOfHosts = InputParam.extractArgument(InputParam.NUMBER_OF_HOSTS, programArgs).get();
        weight = InputParam.extractArgument(InputParam.WEIGHT, programArgs).get();
        startIp = InputParam.extractArgument(InputParam.START_IP, programArgs).get();
        startIpv6 = InputParam.extractArgument(InputParam.START_IP_V6, programArgs).orElse(null);
        zookeeperBasePath = InputParam.extractArgument(InputParam.ZK_BASE_PATH, programArgs).orElse("");
        runTime = Integer.parseInt(InputParam.extractArgument(InputParam.RUN_TIME_IN_MINUTES, programArgs).orElse("0"));
    }

    private void printHelp() {
        String argsDescription = Stream.of(InputParam.values()).sorted(Comparator.comparing(InputParam::isOptional))
            .map(inputParam ->
                ((inputParam.isOptional()) ? "And *optional* " : "") + inputParam.toString()
            ).collect(Collectors.joining("\n"));

        System.out.println("at least " + InputParam.numberOfRequiredArgs() + " arguments are needed:\n" + argsDescription);
    }

    private void exitWithError() {
        throw new ProgramFailedException();
    }

    private void validateConfiguration() {
        String connectionStringValidation = RedirectorCoreConfigUtil.validateZooKeeperConnectionString(connectionString);
        if (connectionStringValidation != null) {
            throw new IllegalArgumentException(connectionStringValidation);
        }

        if (Integer.valueOf(numberOfHosts) < 1) {
            throw new IllegalArgumentException("NumberOfHosts should be in range 1 of higher ");
        }

        if (StringUtils.isNotBlank(zookeeperBasePath) && ! zookeeperBasePath.startsWith("/")) {
            throw new IllegalArgumentException("ZkBasePath should start with / ");
        }
    }

    private void printErrorMessage(String message) {
        System.out.println(message);
    }

    public void start() {
        registerHosts();

        if (isRunTimeLimited()) {
            shutdownAfterTimeout();
        }
    }

    private void registerHosts() {
        registry = new HostRegister(connectionString, zookeeperBasePath);
        registry.registerHosts(stackName, flavor, appName, getHosts(startIp, startIpv6, Integer.valueOf(numberOfHosts)), weight);
    }

    private boolean isRunTimeLimited() {
        return runTime > 0;
    }

    private void shutdownAfterTimeout() {
        try {
            TimeUnit.MINUTES.sleep(runTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        registry.deRegisterAllHosts();
    }

    /**
     * This method is used only for demonstration of usage of EnvReg util
     * @param args
     */
    public static void main(String[] args) {
        try {
            new HostRegistration(args).start();
        } catch (ProgramFailedException e) {
            System.exit(1);
        }
    }

    public static List<Host> getHosts(String startIp, String startIpv6, Integer numberOfHosts) {

        Function<Integer, String> IPV6Generator = hostCounter -> null;
        Function<Integer, String> IPV4Generator;

        if(startIp == null) {
            IPV4Generator = hostCounter -> IPv4Address.parse(DEFAULT_IPV4_BEGIN_RANGE).add(hostCounter).toString();
        } else {
            IPV4Generator = hostCounter -> IPv4Address.parse(startIp).add(hostCounter).toString();
        }

        if(startIpv6 != null) {
            IPV6Generator = hostCounter -> IPv6Address.parse(startIpv6).add(hostCounter).toString();
        }

        return buildHosts(numberOfHosts, IPV4Generator, IPV6Generator);
    }

    private static List<Host> buildHosts(Integer numberOfHosts, final Function<Integer, String> finalIPV4Generator, final Function<Integer, String> finalIPV6Generator) {
        return IntStream.rangeClosed(0, numberOfHosts - 1)
                .mapToObj(host -> new Host(finalIPV4Generator.apply(host), finalIPV6Generator.apply(host)))
                .collect(Collectors.toList());
    }

    static class ProgramFailedException extends RuntimeException {

    }
}
