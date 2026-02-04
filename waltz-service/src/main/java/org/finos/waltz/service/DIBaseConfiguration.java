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


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.finos.waltz.data.DBExecutorPool;
import org.finos.waltz.data.DBExecutorPoolInterface;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import javax.sql.DataSource;


@Configuration
@PropertySource(value = "classpath:waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.home}/.waltz/waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true)
@ComponentScan(value={"org.finos.waltz.data"})
public class DIBaseConfiguration {

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

    @Value("${database.performance.query.slow.threshold:10}")
    private int databasePerformanceQuerySlowThreshold;

    @Value("${log.connection.pool.status:false}")
    private boolean logConnectionPoolStatus;

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
    @Autowired
    public DSLContext dsl(DataSource dataSource) {
        try {
            SQLDialect.valueOf(dialect);
        } catch (IllegalArgumentException iae) {
            System.err.println("Cannot parse sql dialect: "+dialect);
            throw iae;
        }

        // TODO: remove sql server setting, see #4553
        Settings dslSettings = new Settings()
                .withRenderOutputForSQLServerReturningClause(false);

        if ("true".equals(System.getProperty(JOOQ_DEBUG_PROPERTY))) {
            dslSettings
                    .withRenderFormatted(true)
                    .withExecuteLogging(true);
        }

        org.jooq.Configuration configuration = new DefaultConfiguration()
                .set(dataSource)
                .set(SQLDialect.valueOf(dialect))
                .set(dslSettings)
                .set(
                    //new SlowDatabaseConnectionSimulator(2000),
                    new SlowQueryListener(databasePerformanceQuerySlowThreshold, dataSource, logConnectionPoolStatus),
                    new SpringExceptionTranslationExecuteListener(new SQLStateSQLExceptionTranslator()));

        return DSL.using(configuration);
    }

}
