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

import static com.khartec.waltz.common.Checks.checkNotEmptyString;
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
        checkNotEmptyString(userId, "userId cannot be empty");

        Person person = personDao.findPersonByUserId(userId);
        if (person == null) {
            return Collections.emptyList();
        }
        List<Person> directs = personDao.findDirectsByEmployeeId(person.employeeId());
        List<String> directUserIds = map(directs, p -> p.userId());
        return changeLogDao.getContributionScoresForUsers(directUserIds);
    }

}
