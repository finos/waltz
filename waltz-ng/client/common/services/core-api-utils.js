/*
 *
 *  * Waltz - Enterprise Architecture
 *  * Copyright (C) 2017  Khartec Ltd.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import {ApplicationStore_API as ApplicationStore} from "../../applications/services/application-store";
import {BookmarkStore_API as BookmarkStore} from "../../bookmarks/services/bookmark-store";
import {ChangeInitiativeStore_API as ChangeInitiativeStore} from "../../change-initiative/services/change-initiative-store";
import {ChangeLogStore_API as ChangeLogStore} from "../../change-log/services/change-log-store";
import {DataTypeStore_API as DataTypeStore} from "../../data-types/services/data-type-store";
import {EndUserAppStore_API as EndUserAppStore} from "../../end-user-apps/services/end-user-app-store";
import {EntityNamedNoteStore_API as EntityNamedNoteStore} from "../../entity-named-note/services/entity-named-note-store";
import {EntityNamedNoteTypeStore_API as EntityNamedNoteTypeStore} from "../../entity-named-note/services/entity-named-note-type-store";
import {EntityStatisticStore_API as EntityStatisticStore} from "../../entity-statistics/services/entity-statistic-store";
import {InvolvementStore_API as InvolvementStore} from "../../involvement/services/involvement-store";
import {MeasurableCategoryStore_API as MeasurableCategoryStore} from "../../measurable-category/services/measurable-category-store";
import {MeasurableRatingStore_API as MeasurableRatingStore} from "../../measurable-rating/services/measurable-rating-store";
import {MeasurableRelationshipStore_API as MeasurableRelationshipStore} from "../../measurable-relationship/services/measurable-relationship-store";
import {MeasurableStore_API as MeasurableStore} from "../../measurable/services/measurable-store";
import {PersonStore_API as PersonStore} from "../../person/services/person-store";
import {PerspectiveDefinitionStore_API as PerspectiveDefinitionStore} from "../../perspective/services/perspective-definition-store";
import {PerspectiveRatingStore_API as PerspectiveRatingStore} from "../../perspective/services/perspective-rating-store";
import {PhysicalSpecDataTypeStore_API as PhysicalSpecDataTypeStore} from "../../physical-specifications/services/physical-spec-data-type-store";
import {SourceDataRatingStore_API as SourceDataRatingStore} from "../../source-data-rating/services/source-data-rating-store";
import {TechnologyStatisticsService_API as TechnologyStatisticsService} from "../../technology/services/technology-statistics-service";


export const CORE_API = {
    ApplicationStore,
    BookmarkStore,
    ChangeInitiativeStore,
    ChangeLogStore,
    DataTypeStore,
    EndUserAppStore,
    EntityNamedNoteStore,
    EntityNamedNoteTypeStore,
    EntityStatisticStore,
    InvolvementStore,
    MeasurableCategoryStore,
    MeasurableRatingStore,
    MeasurableRelationshipStore,
    MeasurableStore,
    PersonStore,
    PerspectiveDefinitionStore,
    PerspectiveRatingStore,
    PhysicalSpecDataTypeStore,
    SourceDataRatingStore,
    TechnologyStatisticsService
};


export function getApiReference(serviceName, serviceFnName) {
    return CORE_API[serviceName][serviceFnName];
}
