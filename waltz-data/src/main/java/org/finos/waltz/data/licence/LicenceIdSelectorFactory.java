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

package org.finos.waltz.data.licence;

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;
import static org.finos.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;
import static org.finos.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.val;


public class LicenceIdSelectorFactory extends AbstractIdSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    public LicenceIdSelectorFactory() {
        super(EntityKind.LICENCE);
    }


    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APPLICATION:
                return mkForApplication(options);
            case SOFTWARE:
                return mkForSoftwarePackage(options);
            case SOFTWARE_VERSION:
                return mkForSoftwareVersion(options);
            case LICENCE:
                return mkForLicence(options);
            case ACTOR:
            case ALL:
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
            case PROCESS_DIAGRAM:
            case SCENARIO:
                Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(options);
                return mkFromAppSelector(appSelector);
            default:
                String msg = String.format(
                        "Cannot create Licence Id selector from kind: %s",
                        options.entityReference().kind());
                throw new UnsupportedOperationException(msg);
        }
    }


    private Select<Record1<Long>> mkForSoftwarePackage(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        return select(SOFTWARE_VERSION_LICENCE.LICENCE_ID)
                .from(SOFTWARE_VERSION_LICENCE)
                .innerJoin(SOFTWARE_VERSION)
                    .on(SOFTWARE_VERSION.ID.eq(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID))
                .where(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.eq(ref.id()));
    }


    private Select<Record1<Long>> mkForSoftwareVersion(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        return select(SOFTWARE_VERSION_LICENCE.LICENCE_ID)
                .from(SOFTWARE_VERSION_LICENCE)
                .where(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID.eq(ref.id()));
    }


    private Select<Record1<Long>> mkForApplication(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        return select(SOFTWARE_VERSION_LICENCE.LICENCE_ID)
                .from(SOFTWARE_VERSION_LICENCE)
                .innerJoin(SOFTWARE_USAGE)
                .on(SOFTWARE_USAGE.SOFTWARE_VERSION_ID.eq(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID))
                .where(SOFTWARE_USAGE.APPLICATION_ID.eq(ref.id()));
    }


    private Select<Record1<Long>> mkFromAppSelector(Select<Record1<Long>> appSelector) {
        return select(SOFTWARE_VERSION_LICENCE.LICENCE_ID)
                .from(SOFTWARE_VERSION_LICENCE)
                .innerJoin(SOFTWARE_USAGE)
                .on(SOFTWARE_USAGE.SOFTWARE_VERSION_ID.eq(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID))
                .where(SOFTWARE_USAGE.APPLICATION_ID.in(appSelector));

    }


    private Select<Record1<Long>> mkForLicence(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return select(val(options.entityReference().id()));
    }
}
