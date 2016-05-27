/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service;

import com.khartec.waltz.service.capability.CapabilityService;
import com.khartec.waltz.service.jmx.CapabilitiesMaintenance;
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

import javax.sql.DataSource;


@Configuration
@EnableMBeanExport(defaultDomain = "${database.schema}_${database.user}_${database.schemata:schema}") // TODO: replace
@ComponentScan(value={"com.khartec.waltz"})
@PropertySource(value = "classpath:waltz.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${user.home}/.waltz/waltz.properties", ignoreResourceNotFound = true)
public class DIConfiguration {


    @Value("${database.url}")
    private String dbUrl;

    @Value("${database.user}")
    private String dbUser;

    @Value("${database.password}")
    private String dbPassword;

    @Value("${database.driver}")
    private String dbDriver;

    @Value("${database.pool.max?:10}")
    private int dbPoolMax;

    @Value("${database.pool.min?:2}")
    private int dbPoolMin;

    @Value("${jooq.dialect}")
    private String dialect;


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
    @Autowired
    public DSLContext dsl(DataSource dataSource) {
        try {
            SQLDialect.valueOf(dialect);
        } catch (IllegalArgumentException iae) {
            System.err.println("Cannot parse sql dialect: "+dialect);
            throw iae;
        }
        return DSL.using(
                dataSource,
                SQLDialect.valueOf(dialect),
                new Settings()
                        .withRenderFormatted(false)
                        .withExecuteLogging(false));
    }


    /* Required for property interpolation to work correctly */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Autowired
    public CapabilitiesMaintenance capabilitiesMaintenance(CapabilityService capabilityService) {
        return new CapabilitiesMaintenance(capabilityService);
    }

    @Bean
    @Autowired
    public PersonMaintenance personMaintenance(PersonHierarchyService personHierarchyService) {
        return new PersonMaintenance(personHierarchyService);
    }

}
