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

package com.khartec.waltz.service.person;

import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.data.person.search.PersonSearchDao;
import com.khartec.waltz.model.person.ImmutablePerson;
import com.khartec.waltz.model.person.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.FunctionUtilities.time;

@Service
public class PersonService {

    private final PersonDao personDao;
    private final PersonSearchDao personSearchDao;


    @Autowired
    public PersonService(PersonDao personDao, PersonSearchDao personSearchDao) {
        checkNotNull(personDao, "personDao must not be null");
        checkNotNull(personSearchDao, "personSearchDao must not be null");

        this.personDao = personDao;
        this.personSearchDao = personSearchDao;
    }


    public Person getByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find person without an employeeId");
        return personDao.getByEmployeeId(employeeId);
    }


    public Person getById(long id) {
        return personDao.getById(id);
    }


    public List<Person> findDirectsByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return time("PS.findDirectsByEmployeeId", () -> personDao.findDirectsByEmployeeId(employeeId));
    }


    /**
     * Returned in order, immediate manager first
     **/
    public List<Person> findAllManagersByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return time("PS.findAllManagersByEmployeeId", () -> personDao.findAllManagersByEmployeeId(employeeId));
    }


    public List<Person> search(String query) {
        return personSearchDao.search(query);
    }


    public List<Person> all() {
        return personDao.all();
    }


    public int[] bulkSave(List<ImmutablePerson> people) {
        return personDao.bulkSave(people);
    }


    public Person findPersonByUserId(String userId) {
        return personDao.findPersonByUserId(userId);
    }
}
