/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.data.licence;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;

import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static com.khartec.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;
import static com.khartec.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;
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
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
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
