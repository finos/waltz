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

package com.khartec.waltz.service.user_contribution;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.changelog.ChangeLogDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.tally.OrderedTally;
import com.khartec.waltz.model.tally.Tally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.service.user_contribution.UserContributionUtilities.getOrderedListOf10;

@Service
public class UserContributionService {

    private final ChangeLogDao changeLogDao;
    private final PersonDao personDao;

    @Autowired
    public UserContributionService(ChangeLogDao changeLogDao,
                                   PersonDao personDao) {
        checkNotNull(changeLogDao, "changeLogDao cannot be null");
        checkNotNull(personDao, "personDao cannot be null");

        this.changeLogDao = changeLogDao;
        this.personDao = personDao;
    }


    public List<OrderedTally<String>> getLeaderBoard(int limit) {
        return changeLogDao.getContributionLeaderBoard(limit);
    }

    public List<OrderedTally<String>> getLeaderBoardLastMonth(int limit) {
        return changeLogDao.getContributionLeaderBoardLastMonth(limit);
    }

    public List<OrderedTally<String>> getRankedLeaderBoard(String userId) {

        List<OrderedTally<String>> contributors = changeLogDao.getRankingOfContributors();
        List<OrderedTally<String>> orderedListContributors = getOrderedListOf10(contributors, userId);

        return orderedListContributors;
    }


    public double getScoreForUser(String userId) {
        List<String> userIds = newArrayList(userId);
        return ListUtilities.applyToFirst(
                    changeLogDao.getContributionScoresForUsers(userIds),
                    c -> c.count())
                .orElse(0.0);
    }


    public List<Tally<String>> findScoresForDirectReports(String userId) {
        checkNotEmpty(userId, "userId cannot be empty");

        Person person = personDao.getByUserEmail(userId);
        if (person == null) {
            return Collections.emptyList();
        }
        List<Person> directs = personDao.findDirectsByEmployeeId(person.employeeId());
        List<String> directUserIds = map(directs, p -> p.userId());
        return changeLogDao.getContributionScoresForUsers(directUserIds);
    }

}
