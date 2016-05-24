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

package com.khartec.waltz.service.involvement;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.person.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class InvolvementService {


    private final InvolvementDao dao;


    @Autowired
    public InvolvementService(InvolvementDao dao) {
        checkNotNull(dao, "dao must not be null");

        this.dao = dao;
    }


    public List<Involvement> findByEntityReference(EntityReference ref) {
        return dao.findByEntityReference(ref);
    }


    public List<Application> findDirectApplicationsByEmployeeId(String employeeId) {
        return dao.findDirectApplicationsByEmployeeId(employeeId);
    }


    public List<Application> findAllApplicationsByEmployeeId(String employeeId) {
        return dao.findAllApplicationsByEmployeeId(employeeId);
    }


    public List<Involvement> findByEmployeeId(String employeeId) {
        return dao.findByEmployeeId(employeeId);
    }


    public List<Person> findPeopleByEntityReference(EntityReference ref) {
        return dao.findPeopleByEntityReference(ref);
    }

    public Collection<ChangeInitiative> findDirectChangeInitiativesByEmployeeId(String employeeId) {
        Checks.checkNotEmptyString(employeeId, "employeeId cannot be empty");
        return dao.findDirectChangeInitiativesByEmployeeId(employeeId);
    }
}
