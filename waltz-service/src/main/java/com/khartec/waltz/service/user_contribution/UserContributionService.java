/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.user_contribution;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.changelog.ChangeLogDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.model.tally.Tally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.ListUtilities.newArrayList;

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


    public List<Tally<String>> getLeaderBoard(int limit) {
        return changeLogDao.getContributionLeaderBoard(limit);
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

        Person person = personDao.getPersonByUserId(userId);
        if (person == null) {
            return Collections.emptyList();
        }
        List<Person> directs = personDao.findDirectsByEmployeeId(person.employeeId());
        List<String> directUserIds = map(directs, p -> p.userId());
        return changeLogDao.getContributionScoresForUsers(directUserIds);
    }

}
