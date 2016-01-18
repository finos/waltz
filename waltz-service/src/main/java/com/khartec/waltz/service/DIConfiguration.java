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

import com.khartec.waltz.common.HomeConfigFile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
@ComponentScan(value={"com.khartec.waltz"})
public class DIConfiguration {

    @Bean
    public DataSource dataSource() {
        HomeConfigFile props = new HomeConfigFile(".waltz", "waltz.properties");
        HikariConfig dsConfig = new HikariConfig();
        dsConfig.setJdbcUrl(props.get("database.url"));
        dsConfig.setUsername(props.get("database.user"));
        dsConfig.setPassword(props.get("database.password"));
        dsConfig.setDriverClassName(props.get("database.driver"));
        dsConfig.setMaximumPoolSize(props.getInt("database.pool.max", 10));
        dsConfig.setMinimumIdle(props.getInt("database.pool.min", 2));
        return new HikariDataSource(dsConfig);
    }

    @Bean
    @Autowired
    public DSLContext dsl(DataSource dataSource) {
        return DSL.using(
                dataSource,
                SQLDialect.MARIADB,
                new Settings().withRenderFormatted(true));
    }

}
