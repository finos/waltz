/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service;

import org.finos.waltz.model.ImmutableWaltzVersionInfo;
import org.finos.waltz.model.WaltzVersionInfo;
import org.finos.waltz.model.settings.ImmutableSetting;
import org.finos.waltz.model.settings.Setting;
import org.finos.waltz.service.jmx.PersonMaintenance;
import org.finos.waltz.service.person_hierarchy.PersonHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.StringUtilities.mkSafe;


@Configuration
@Import(DIBaseConfiguration.class)
@EnableMBeanExport(defaultDomain = "waltz_${waltz.qualifier:}")
@EnableScheduling
@ComponentScan(value={"org.finos.waltz"})
public class DIConfiguration implements SchedulingConfigurer {


    // -- BUILD ---

    @Value("${build.pom:dev}")
    private String buildPom;

    @Value("${build.date:dev}")
    private String buildDate;

    @Value("${build.revision:dev}")
    private String buildRevision;

    @Value("${settings.override:#{null}}")
    private String settingsOverrideStr;

    @Bean
    public WaltzVersionInfo waltzBuildInfo() {
        return ImmutableWaltzVersionInfo.builder()
                .timestamp(buildDate)
                .pomVersion(buildPom)
                .revision(buildRevision)
                .build();
    }


    @Bean
    public Collection<Setting> settingOverrides() {
        return Stream.of(mkSafe(settingsOverrideStr).split(";"))
                .map(setting -> setting.split("="))
                .filter(nv -> nv.length == 2)
                .map(nv -> ImmutableSetting.builder()
                        .name(nv[0])
                        .value(nv[1])
                        .build())
                .collect(Collectors.toList());
    }


    /**
     * Required for property interpolation to work correctly
     * @see <a href="http://stackoverflow.com/a/41760877/2311919">Explanation</a>
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment env) {
        PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        JndiPropertySource jndiPropertySource = new JndiPropertySource("java:comp");
        env.getPropertySources().addFirst(jndiPropertySource);

        return placeholderConfigurer;
    }


    @Bean
    @Autowired
    public PersonMaintenance personMaintenance(PersonHierarchyService personHierarchyService) {
        return new PersonMaintenance(personHierarchyService);
    }


    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setDaemon(true);
        return scheduler;
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setTaskScheduler(taskScheduler());
    }
}
