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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.user_agent_info.UserAgentInfoDao;
import com.khartec.waltz.model.user_agent_info.UserAgentInfo;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class UserAgentInfoHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        UserAgentInfoDao userLoginDao = ctx.getBean(UserAgentInfoDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        List<UserAgentInfo> infos = userLoginDao.findLoginsForUser("admin", 10);
        System.out.println(infos);


        userLoginDao.save(infos.get(0));
    }


}
