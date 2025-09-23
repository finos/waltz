package org.finos.waltz.integration_test.inmem;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jmx.support.RegistrationPolicy;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@ComponentScan(basePackages = {
        "org.finos.waltz.data",
        "org.finos.waltz.service"

}, excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = ExcludeFromIntegrationTesting.class))
@EnableMBeanExport(registration = RegistrationPolicy.REPLACE_EXISTING)
@PropertySource("classpath:integration-test.properties")
public class DIZonkyTestConfiguration {

    final int MIN_POOL_SIZE = 2;
    final int MAX_POOL_SIZE = 6;

    private PostgreSQLContainer<?> pg = null;
    private MSSQLServerContainer<?> mssql = null;
    private EmbeddedPostgres embeddedPg = null;   // fallback when no Docker for Postgres

    @Autowired
    ApplicationContext ctx;


    @Bean
    public DBExecutorPoolInterface dbExecutorPool() {
        return new DBExecutorPool(MIN_POOL_SIZE, MAX_POOL_SIZE);
    }


    @Bean
    @Primary
    public DataSource dataSource() throws SQLException, IOException {
        System.out.println("Setting up Hikari wrapping Zonky DS");

        final String target = System.getProperty("target.db", "mssql").toLowerCase();    // postgres | mssql
        final String provider  = System.getProperty("db.provider", "embedded");                 // docker | embedded | auto
        final boolean wantDocker = "docker".equalsIgnoreCase(provider) ||
                ("auto".equalsIgnoreCase(provider) && dockerAvailable());

        System.out.println("==> target.db=" + target + "  provider=" + (wantDocker ? "docker" : "embedded/fallback"));

        HikariConfig cfg = new HikariConfig();

        switch (target) {
            case "postgres":
                if (wantDocker) {
                    System.out.println("==> Using Testcontainers: postgres:16");
                    pg = new PostgreSQLContainer<>("postgres:16");
                    pg.start();
                    cfg.setJdbcUrl(pg.getJdbcUrl());
                    cfg.setUsername(pg.getUsername());
                    cfg.setPassword(pg.getPassword());
                } else {
                    System.out.println("==> Using Embedded Postgres (Zonky native)");
                    embeddedPg = EmbeddedPostgres.builder().start();
                    String url = "jdbc:postgresql://localhost:" + embeddedPg.getPort() + "/postgres";
                    cfg.setJdbcUrl(url);
                    cfg.setUsername("postgres");
                    cfg.setPassword("postgres");
                }
                break;

            case "mssql":
                if (wantDocker) {
                    System.out.println("==> Using Testcontainers: vibs2006/sql_server_fts:latest");
                    DockerImageName sqlServerWithFTS = DockerImageName
                            .parse("vibs2006/sql_server_fts:latest")
                            .asCompatibleSubstituteFor("mcr.microsoft.com/mssql/server:2022-latest");
                    mssql = new MSSQLServerContainer<>(sqlServerWithFTS)
                            .acceptLicense() // sets ACCEPT_EULA=Y
                            .withEnv("MSSQL_PID", "EXPRESS")
                            .withStartupAttempts(100)
                            .withStartupTimeoutSeconds(120)
                            .withConnectTimeoutSeconds(120)
                            .withInitScript("init-mssql.sql");


                    try {
                        mssql.start();
                    } catch (Throwable t) {
                        System.err.println("=== MSSQL CONTAINER LOGS (startup failure) ===");
                        System.err.println(mssql.getLogs());
                        throw t;
                    }

                    String url = mssql.getJdbcUrl();
                    if (!url.contains("encrypt=")) {
                        url += ";encrypt=true;trustServerCertificate=true";
                    }

                    url +=";databaseName=waltz";

                    System.out.println("URL for SqlServer: " + url);
                    cfg.setJdbcUrl(url);
                    cfg.setUsername(mssql.getUsername()); // "sa"
                    cfg.setPassword(mssql.getPassword());
                } else {
                    System.out.println("==> No Docker: fallback to H2 in MSSQL compatibility mode");
                    cfg.setJdbcUrl("jdbc:h2:mem:waltz;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_UPPER=TRUE;DB_CLOSE_DELAY=-1");
                    cfg.setUsername("sa");
                    cfg.setPassword("sa");
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported target.db: " + target);
        }

        cfg.setMaximumPoolSize(MAX_POOL_SIZE);
        cfg.setMinimumIdle(MIN_POOL_SIZE);
        HikariDataSource ds = new HikariDataSource(cfg);

        // quick debug
        try (Connection c = DataSourceUtils.getConnection(ds)) {
            DatabaseMetaData m = c.getMetaData();
            System.out.printf("==> DB=%s %s  URL=%s%n", m.getDatabaseProductName(), m.getDatabaseProductVersion(), m.getURL());
        }
        return ds;
    }


    @PostConstruct
    public void debugDs() {
        String[] dsBeans = ctx.getBeanNamesForType(DataSource.class);
        System.out.println("DataSource beans: " + java.util.Arrays.toString(dsBeans));
        for (String name : dsBeans) {
            System.out.println(" - " + name + " -> " + ctx.getBean(name).getClass().getName());
        }
    }


    @Bean
    public DisposableBean dbShutdown() {
        return () -> {
            if (pg != null) pg.stop();
            if (mssql != null) mssql.stop();
            if (embeddedPg != null) embeddedPg.close();
        };
    }



    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        System.out.println("Setting up liquibase zonky");
        SpringLiquibase liquibase = new SpringLiquibase();

        liquibase.setDropFirst(true);

        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("/liquibase/db.changelog-master.xml");
        return liquibase;
    }


    @Bean
    @DependsOn("liquibase")
    public DSLContext dsl(DataSource dataSource) throws SQLException {

        SQLDialect dialect = SQLDialect.DEFAULT;

        try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
            DatabaseMetaData meta = conn.getMetaData();
            dialect = mkSqlDialect(meta.getDatabaseProductName());

            System.out.println("===========================================================");
            System.out.println("DB Product: " + meta.getDatabaseProductName());
            System.out.println("DB Version: " + meta.getDatabaseProductVersion());
            System.out.println("DB URL: " + meta.getURL());
            System.out.println("Driver: " + meta.getDriverName() + " " + meta.getDriverVersion());
            System.out.println("SQL Dialect: " + dialect);
            System.out.println("Mode: " + (pg == null && mssql == null ? "EMBEDDED" : "CONTAINER"));
            System.out.println("===========================================================");
        }

        Settings dslSettings = new Settings()
                .withRenderFormatted(true)
                .withDebugInfoOnStackTrace(true)
                .withRenderOutputForSQLServerReturningClause(false) //note: only for sql server
                .withExecuteLogging(true);

        if(SQLDialect.H2 == dialect) {
            dslSettings
                    .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_QUOTED)
                    .withRenderNameCase(RenderNameCase.UPPER);
        }

        org.jooq.Configuration configuration = new DefaultConfiguration()
                .set(dataSource)
                .set(dslSettings)
                .set(dialect);

        return DSL.using(configuration);
    }


    private SQLDialect mkSqlDialect(String databaseProductName) {
        String lowerCase = databaseProductName.toLowerCase();
        if(lowerCase.contains("h2")) {
            return SQLDialect.H2;
        } else if(lowerCase.contains("mysql")) {
            return SQLDialect.MYSQL;
        } else if(lowerCase.contains("mariadb")) {
            return SQLDialect.MARIADB;
        } else if(lowerCase.contains("postgresql")) {
            return SQLDialect.POSTGRES;
        } else if(lowerCase.contains("sql server")) {
            try {
                // Only present in jOOQ Pro/Enterprise
                return SQLDialect.valueOf("SQLSERVER");
            } catch (IllegalArgumentException e) {
                // OSS edition: constant not present
                return SQLDialect.DEFAULT;
            }
        } else {
            return SQLDialect.DEFAULT;
        }
    }


    private static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

}
