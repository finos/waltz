/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service;

import com.khartec.waltz.model.ImmutableWaltzVersionInfo;
import com.khartec.waltz.model.WaltzVersionInfo;
import com.khartec.waltz.model.settings.ImmutableSetting;
import com.khartec.waltz.model.settings.Setting;
import com.khartec.waltz.service.jmx.PersonMaintenance;
import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.StringUtilities.mkSafe;


@Configuration
@Import(DIBaseConfiguration.class)
@EnableMBeanExport(defaultDomain = "waltz_${waltz.qualifier:}")
@EnableScheduling
@ComponentScan(value={"com.khartec.waltz"})
public class DIConfiguration implements SchedulingConfigurer {


    @Value("${smtpHost:#{null}}")
    private String smtpHost;

    @Value("${smtpPort:25}")
    private int smtpPort;

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
    public JavaMailSender mailSender() {
        if (smtpHost == null) {
            return null;
        } else {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(smtpHost);
            mailSender.setPort(smtpPort);
            return mailSender;
        }
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
