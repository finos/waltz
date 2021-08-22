package com.khartec.waltz.integration_test.inmem;

import com.khartec.waltz.data.DBExecutorPool;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@ComponentScan(basePackages = {
        "com.khartec.waltz.data",
        "com.khartec.waltz.service.entity_hierarchy",
        "com.khartec.waltz.service.person_hierarchy"
})
@EnableMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)
public class InMemoryTestConfiguration {

    @Bean
    public DBExecutorPoolInterface dbExecutorPool() {
        return new DBExecutorPool(2, 4);
    }


    @Bean
    public DataSource dataSource() {
        System.out.println("Setting up ds");

        HikariConfig dsConfig = new HikariConfig();
        dsConfig.setJdbcUrl("jdbc:h2:mem:waltz;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
        dsConfig.setUsername("sa");
        dsConfig.setPassword("sa");
        dsConfig.setMaximumPoolSize(5);
        dsConfig.setMinimumIdle(2);
        return new HikariDataSource(dsConfig);
    }


    @Bean
    @Autowired
    public DSLContext dsl(DataSource dataSource) {
        System.out.println("Setting up dsl");
        Settings dslSettings = new Settings()
                .withRenderFormatted(true)
                .withDebugInfoOnStackTrace(true)
                .withRenderQuotedNames(RenderQuotedNames.NEVER)
                .withExecuteLogging(true);

        org.jooq.Configuration configuration = new DefaultConfiguration()
                .set(dataSource)
                .set(dslSettings)
                .set(SQLDialect.H2);

        return DSL.using(configuration);
    }


    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource, DSLContext dsl) throws SQLException {
        System.out.println("Setting up liquibase");
        SpringLiquibase liquibase = new SpringLiquibase();

        // we want to drop the database if it was created before to have immutable version
        liquibase.setDropFirst(false);

        liquibase.setDataSource(dataSource);
//        liquibase.setDefaultSchema("public");
        liquibase.setChangeLog("file:../waltz-data/src/main/ddl/liquibase/db.changelog-master.xml");
        return liquibase;
    }

}
