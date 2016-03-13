package com.khartec.waltz.service.jmx;

import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(
        objectName = "Waltz:type=JMX,name=Person",
        description = "Maintenance functions for the Waltz Persons")
public class PersonMaintenance {

    private static final Logger LOG = LoggerFactory.getLogger(PersonMaintenance.class);

    private final PersonHierarchyService personHierarchyService;

    @Autowired
    public PersonMaintenance(PersonHierarchyService personHierarchyService) {
        this.personHierarchyService = personHierarchyService;
    }


    @ManagedOperation(description = "Rebuild the person hierarchy table")
    public boolean rebuildHierarchyTable() {
        LOG.warn("Rebuild person hierarchy (via jmx)");
        return personHierarchyService.build();
    }


    @ManagedAttribute
    public String getName() {
        return "Person";
    }

}
