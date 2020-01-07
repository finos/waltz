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

package com.khartec.waltz.web;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spark.servlet.SparkApplication;

public class WaltzApplication implements SparkApplication {

    @Override
    public void init() {
        new Main().start();
    }


    @Override
    public void destroy() {
        AnnotationConfigApplicationContext ctx = Main.getSpringContext();
        if (ctx != null) {
            HikariDataSource dataSource = ctx.getBean(HikariDataSource.class);
            if (dataSource != null) {
                dataSource.close();
            }
            ctx.close();
        }
    }

}
