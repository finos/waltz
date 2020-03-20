package com.khartec.waltz.data.actor;

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
            .withPassword("pass").withStartupTimeout(Duration.ofSeconds(600));


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
        dsl.createSchemaIfNotExists("test");
        SpringLiquibase liquibase = new SpringLiquibase();

        // we want to drop the database if it was created before to have immutable version
        liquibase.setDropFirst(true);

        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("test");
        liquibase.setChangeLog("file:src/main/ddl/liquibase/db.changelog-master.xml");
        return liquibase;
    }

}
