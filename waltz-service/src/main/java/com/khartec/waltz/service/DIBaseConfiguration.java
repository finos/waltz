package com.khartec.waltz.service;


import com.khartec.waltz.data.DBExecutorPool;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

import javax.sql.DataSource;

@Configuration
@PropertySource(value = "classpath:waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.home}/.waltz/waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true)
@ComponentScan(value={"com.khartec.waltz.data"})
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
                .set(new SlowQueryListener(databasePerformanceQuerySlowThreshold));

        return DSL.using(configuration);
    }

}
