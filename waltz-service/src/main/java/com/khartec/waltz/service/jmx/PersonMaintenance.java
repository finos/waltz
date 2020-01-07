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
