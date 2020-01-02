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

import com.khartec.waltz.data.user.UserPreferenceDao;
import com.khartec.waltz.model.user.ImmutableUserPreference;
import com.khartec.waltz.model.user.UserPreference;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;


public class UserPreferenceHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        UserPreferenceDao userPreferenceDao = ctx.getBean(UserPreferenceDao.class);


        List<UserPreference> preferences = new ArrayList<UserPreference>() {{
            add(ImmutableUserPreference.builder()
                    .key("org-unit.section.technologies.collapsed")
                    .value("false")
                    .build());

            add(ImmutableUserPreference.builder()
                    .key("org-unit.section.indicators.collapsed")
                    .value("true")
                    .build());

            add(ImmutableUserPreference.builder()
                    .key("org-unit.section.logicalflows.collapsed")
                    .value("true")
                    .build());

        }};

//        int result = userPreferenceDao.savePreferencesForUser("admin", preferences);
//        System.out.println("result: " + result);

        int result = userPreferenceDao.savePreference("admin", ImmutableUserPreference.builder()
                .key("org-unit.section.logicalflows.hidden")
                .value("true")
                .build());
        System.out.println("result: " + result);



//        List<UserPreference> prefs = userPreferenceDao.getPreferencesForUser("admin");
//        prefs.forEach(u -> System.out.printf("user: %s, key: %s, value: %s\r\n", u.userName(), u.key(), u.value()));

//        userPreferenceDao.clearPreferencesForUser("kamran");



    }

}
