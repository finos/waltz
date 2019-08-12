/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019  Waltz open source project
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
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectDistinct;


public class LicenceIdSelectorFactory extends AbstractIdSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    public LicenceIdSelectorFactory() {
        super(EntityKind.LICENCE);
    }


    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APPLICATION:
            case SOFTWARE:
                return mkForRef(options);
            case LICENCE:
                return mkForLicence(options);
            case ACTOR:
            case APP_GROUP:
            case CHANGE_INITIATIVE:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
            case SCENARIO:
                ApplicationIdSelectionOptions appSelectionOptions = ApplicationIdSelectionOptions.mkOpts(options);
                Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(appSelectionOptions);
                return mkFromAppSelector(appSelector);
            default:
                String msg = String.format(
                        "Cannot create Licence Id selector from kind: %s",
                        options.entityReference().kind());
                throw new UnsupportedOperationException(msg);
        }
    }


    private Select<Record1<Long>> mkFromAppSelector(Select<Record1<Long>> appSelector) {
        Select<Record1<Long>> aToB = select(ENTITY_RELATIONSHIP.ID_A)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.LICENCE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.ID_B.in(appSelector));
        Select<Record1<Long>> bToA = select(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.LICENCE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.ID_A.in(appSelector));

        return aToB.unionAll(bToA);

    }


    private Select<Record1<Long>> mkForLicence(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForRef(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        Select<Record1<Long>> aToB = selectDistinct(ENTITY_RELATIONSHIP.ID_A)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.LICENCE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(ref.id()));

        Select<Record1<Long>> bToA = selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.LICENCE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()));

        return aToB.union(bToA);
    }
}
