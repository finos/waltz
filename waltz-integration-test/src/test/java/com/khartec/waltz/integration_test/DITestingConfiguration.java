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

package com.khartec.waltz.integration_test;

import com.khartec.waltz.data.DBExecutorPool;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;

import static java.lang.String.format;

@Configuration
@ComponentScan(basePackages = "com.khartec.waltz.data")
public class DITestingConfiguration {

    static PostgreSQLContainer postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer(
            "postgres:10.3")
            .withDatabaseName("test")
            .withUsername("user")
            .withPassword("pass")
            .withStartupTimeout(Duration.ofSeconds(600));


    @Bean
    public DBExecutorPoolInterface dbExecutorPool() {
        return new DBExecutorPool(2, 4);
    }


    @Bean
    public DataSource dataSource() {
        HikariConfig dsConfig = new HikariConfig();
        postgreSQLContainer.start();
        dsConfig.setJdbcUrl(format("jdbc:postgresql://%s:%s/%s",
                postgreSQLContainer.getContainerIpAddress(),
                postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgreSQLContainer.getDatabaseName()));
        dsConfig.setUsername(postgreSQLContainer.getUsername());
        dsConfig.setPassword(postgreSQLContainer.getPassword());
        dsConfig.setSchema("test");
        dsConfig.setDriverClassName("org.postgresql.Driver");
        dsConfig.setMaximumPoolSize(5);
        dsConfig.setMinimumIdle(2);
        return new HikariDataSource(dsConfig);
    }


    @Bean
    @Autowired
    public DSLContext dsl(DataSource dataSource) {
        org.jooq.Configuration configuration = new DefaultConfiguration()
                .set(dataSource)
                .set(SQLDialect.POSTGRES);
        return DSL.using(configuration);
    }


    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource, DSLContext dsl) throws SQLException {
        dsl.createSchemaIfNotExists("test").execute();
        SpringLiquibase liquibase = new SpringLiquibase();

        // we want to drop the database if it was created before to have immutable version
        liquibase.setDropFirst(true);

        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("test");
        liquibase.setChangeLog("file:../waltz-data/src/main/ddl/liquibase/db.changelog-master.xml");
        return liquibase;
    }

}