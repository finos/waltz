/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import com.khartec.waltz.data.DBExecutorPool;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.model.ImmutableWaltzVersionInfo;
import com.khartec.waltz.model.WaltzVersionInfo;
import com.khartec.waltz.service.jmx.PersonMaintenance;
import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;


@Configuration
@EnableMBeanExport(defaultDomain = "${database.schema}_${database.user}_${database.schemata:schema}") // TODO: replace
@EnableScheduling
@ComponentScan(value={"com.khartec.waltz"})
@PropertySource(value = "classpath:waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.home}/.waltz/waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = false)
public class DIConfiguration {

    // -- DATABASE ---

    private static final String JOOQ_DEBUG_PROPERTY = "jooq.debug.enabled";

    @Value("${database.url}")
    private String dbUrl;

    @Value("${database.user}")
    private String dbUser;

    @Value("${database.password}")
    private String dbPassword;

    @Value("${database.driver}")
    private String dbDriver;

    @Value("${database.pool.max:10}")
    private int dbPoolMax;

    @Value("${database.pool.min:2}")
    private int dbPoolMin;

    @Value("${jooq.dialect}")
    private String dialect;

    @Value("${smtpHost:localhost}")
    private String smtpHost;

    @Value("${smtpPort:25}")
    private int smtpPort;

    // -- BUILD ---

    @Value("${build.pom}")
    private String buildPom;

    @Value("${build.date}")
    private String buildDate;

    @Value("${build.revision}")
    private String buildRevision;


    @Bean
    public DataSource dataSource() {

        HikariConfig dsConfig = new HikariConfig();
        dsConfig.setJdbcUrl(dbUrl);
        dsConfig.setUsername(dbUser);
        dsConfig.setPassword(dbPassword);
        dsConfig.setDriverClassName(dbDriver);
        dsConfig.setMaximumPoolSize(dbPoolMax);
        dsConfig.setMinimumIdle(dbPoolMin);
        return new HikariDataSource(dsConfig);
    }

    @Bean
    public DBExecutorPoolInterface dbExecutorPool() {
        return new DBExecutorPool(dbPoolMin, dbPoolMax);
    }


    @Bean
    public WaltzVersionInfo waltzBuildInfo() {
        return ImmutableWaltzVersionInfo.builder()
                .timestamp(buildDate)
                .pomVersion(buildPom)
                .revision(buildRevision)
                .build();
    }


    @Bean
    @Autowired
    public DSLContext dsl(DataSource dataSource) {
        try {
            SQLDialect.valueOf(dialect);
        } catch (IllegalArgumentException iae) {
            System.err.println("Cannot parse sql dialect: "+dialect);
            throw iae;
        }

        Settings dslSettings = null;
        if ("true".equals(System.getProperty(JOOQ_DEBUG_PROPERTY))) {
            dslSettings = new Settings()
                    .withRenderFormatted(true)
                    .withExecuteLogging(true);
        }

        return DSL.using(
                dataSource,
                SQLDialect.valueOf(dialect),
                dslSettings);
    }


    /* Required for property interpolation to work correctly */

    /**
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
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpHost);
        mailSender.setPort(smtpPort);

        return mailSender;
    }

}
