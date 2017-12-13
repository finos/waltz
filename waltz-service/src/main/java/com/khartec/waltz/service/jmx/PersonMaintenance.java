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

package com.khartec.waltz.service.jmx;

import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description = "Maintenance functions for the Waltz Persons")
public class PersonMaintenance {

    private static final Logger LOG = LoggerFactory.getLogger(PersonMaintenance.class);

    private final PersonHierarchyService personHierarchyService;

    @Autowired
    public PersonMaintenance(PersonHierarchyService personHierarchyService) {
        this.personHierarchyService = personHierarchyService;
    }


    @ManagedOperation(description = "Rebuild the person hierarchy table")
    public int rebuildHierarchyTable() {
        LOG.warn("Rebuild person hierarchy (via jmx)");
        return personHierarchyService.build().length;
    }


    @ManagedAttribute
    public String getName() {
        return "Person";
    }

}
