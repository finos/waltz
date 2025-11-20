package org.finos.waltz.integration_test.inmem;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.finos.waltz.common.ExcludeFromIntegrationTesting;
import org.finos.waltz.data.DBExecutorPool;
import org.finos.waltz.data.DBExecutorPoolInterface;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jmx.support.RegistrationPolicy;

import javax.sql.DataSource;
import java.sql.SQLException;

//@Configuration
//@ComponentScan(basePackages = {
//        "org.finos.waltz.data",
//        "org.finos.waltz.service"
//
//}, excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ExcludeFromIntegrationTesting.class))
//@EnableMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)
//@PropertySource("classpath:integration-test.properties")
public class DIInMemoryTestConfiguration {

    @Bean
    public DBExecutorPoolInterface dbExecutorPool() {
        return new DBExecutorPool(2, 4);
    }



    @Bean
    public DataSource dataSource() {
        System.out.println("Setting up ds inmem");

        HikariConfig dsConfig = new HikariConfig();
        dsConfig.setJdbcUrl("jdbc:h2:mem:waltz;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=TRUE");
        dsConfig.setUsername("sa");
        dsConfig.setPassword("sa");
        dsConfig.setMaximumPoolSize(5);
        dsConfig.setMinimumIdle(2);
        return new HikariDataSource(dsConfig);
    }


    @Bean
    //@Autowired
    public DSLContext dsl(DataSource dataSource) {
        Settings dslSettings = new Settings()
                .withRenderFormatted(true)
                .withDebugInfoOnStackTrace(true)
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_QUOTED)
                .withRenderNameCase(RenderNameCase.UPPER)
                .withExecuteLogging(true);

        org.jooq.Configuration configuration = new DefaultConfiguration()
                .set(dataSource)
                .set(dslSettings)
                .set(SQLDialect.H2);

        return DSL.using(configuration);
    }


    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource, DSLContext dsl) throws SQLException {
        System.out.println("Setting up liquibase inmem");
        SpringLiquibase liquibase = new SpringLiquibase();

        liquibase.setDropFirst(true);

        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("/liquibase/db.changelog-master.xml");
        return liquibase;
    }

}
