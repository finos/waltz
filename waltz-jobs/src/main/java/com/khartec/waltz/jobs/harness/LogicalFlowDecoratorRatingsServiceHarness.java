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

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.hierarchy.FlatNode;
import org.finos.waltz.common.hierarchy.Forest;
import org.finos.waltz.common.hierarchy.HierarchyUtilities;
import org.finos.waltz.common.hierarchy.Node;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.flow_classification_rule.FlowClassificationRule;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import com.khartec.waltz.service.flow_classification_rule.FlowClassificationRuleService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

public class LogicalFlowDecoratorRatingsServiceHarness {

    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        FlowClassificationRuleService authoritativeSourceService = ctx.getBean(FlowClassificationRuleService.class);

        List<FlowClassificationRule> authSources = authoritativeSourceService.findAll();

        OrganisationalUnitService organisationalUnitService = ctx.getBean(OrganisationalUnitService.class);
        OrganisationalUnitDao organisationalUnitDao = ctx.getBean(OrganisationalUnitDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        dsl.select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT)
                .fetch(organisationalUnitDao.TO_DOMAIN_MAPPER);

        EntityHierarchyService hierarchyService = ctx.getBean(EntityHierarchyService.class);

        List<OrganisationalUnit> allOrgUnits = organisationalUnitService.findAll();
        List<FlatNode<OrganisationalUnit, Long>> ouNodes = ListUtilities.map(allOrgUnits, ou -> new FlatNode<>(ou.id().get(), ou.parentId(), ou));
        Forest<OrganisationalUnit, Long> ouForest = HierarchyUtilities.toForest(ouNodes);
        Map<Long, Node<OrganisationalUnit, Long>> nodeMap = ouForest.getAllNodes();





    }

}
