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

package com.khartec.waltz.service.person_hierarchy;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.schema.tables.records.PersonHierarchyRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static java.util.stream.Collectors.toList;

@Service
public class PersonHierarchyService {

    private static final Logger LOG = LoggerFactory.getLogger(PersonHierarchyService.class);

    private final PersonDao personDao;
    private final DSLContext dsl;


    @Autowired
    public PersonHierarchyService(PersonDao personDao, DSLContext dsl) {
        this.personDao = personDao;
        this.dsl = dsl;
    }


    public int[] build() {
        LOG.warn("Building person hierarchy");
        List<Person> all = personDao.all();

        Forest<Person, String> forest = toForest(all);

        List<PersonHierarchyRecord> records = toHierarchyRecords(forest);

        dsl.deleteFrom(PERSON_HIERARCHY).execute();
        return dsl.batchStore(records).execute();
    }


    private List<PersonHierarchyRecord> toHierarchyRecords(Forest<Person, String> forest) {
        List<PersonHierarchyRecord> records = new LinkedList<>();

        for (Node<Person, String> node : forest.getAllNodes().values()) {
            List<Person> ancestors =
                    ListUtilities.reverse(
                            HierarchyUtilities.parents(node)
                                    .stream()
                                    .map(n -> n.getData())
                                    .collect(Collectors.toList()));

            for (int i = 0; i < ancestors.size(); i++) {
                String ancestorId = ancestors.get(i).employeeId();
                String selfId = node.getData().employeeId();
                PersonHierarchyRecord record = new PersonHierarchyRecord(ancestorId, selfId, i + 1);
                records.add(record);
            }
        }
        return records;
    }


    private Forest<Person, String> toForest(List<Person> all) {
        List<FlatNode<Person, String>> allFlatNodes = all.stream()
                .map(p -> new FlatNode<>(p.employeeId(), p.managerEmployeeId(), p))
                .collect(toList());

        return HierarchyUtilities.toForest(allFlatNodes);
    }


    public int count() {
        return dsl.fetchCount(PERSON_HIERARCHY);
    }


    public double countRoots() {
        SelectConditionStep<Record1<String>> rootSelector = DSL
                .selectDistinct(PERSON_HIERARCHY.MANAGER_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.LEVEL.eq(1));

        return dsl.fetchCount(rootSelector);
    }
}
