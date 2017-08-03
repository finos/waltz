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
import {AuthSourcesStore_API as AuthSourcesStore} from "../../auth-sources/services/auth-sources-store";
import {BookmarkStore_API as BookmarkStore} from "../../bookmarks/services/bookmark-store";
import {ChangeInitiativeStore_API as ChangeInitiativeStore} from "../../change-initiative/services/change-initiative-store";
import {ChangeLogStore_API as ChangeLogStore} from "../../change-log/services/change-log-store";
import {DataTypeStore_API as DataTypeStore} from "../../data-types/services/data-type-store";
import {EndUserAppStore_API as EndUserAppStore} from "../../end-user-apps/services/end-user-app-store";
import {EntityNamedNoteStore_API as EntityNamedNoteStore} from "../../entity-named-note/services/entity-named-note-store";
import {EntityNamedNoteTypeStore_API as EntityNamedNoteTypeStore} from "../../entity-named-note/services/entity-named-note-type-store";
import {EntitySearchStore_API as EntitySearchStore} from "../../entity/services/entity-search-store";
import {EntityStatisticStore_API as EntityStatisticStore} from "../../entity-statistics/services/entity-statistic-store";
import {EntitySvgDiagramStore_API as EntitySvgDiagramStore} from "../../entity-svg-diagram/services/entity-svg-diagram-store";
import {FlowDiagramStore_API as FlowDiagramStore} from "../../flow-diagram/services/flow-diagram-store";
import {FlowDiagramAnnotationStore_API as FlowDiagramAnnotationStore} from "../../flow-diagram/services/flow-diagram-annotation-store";
import {FlowDiagramEntityStore_API as FlowDiagramEntityStore} from "../../flow-diagram/services/flow-diagram-entity-store";
import {InvolvementStore_API as InvolvementStore} from "../../involvement/services/involvement-store";
import {LogicalFlowStore_API as LogicalFlowStore} from "../../logical-flow/services/logical-flow-store";
import {MeasurableCategoryStore_API as MeasurableCategoryStore} from "../../measurable-category/services/measurable-category-store";
import {MeasurableRatingStore_API as MeasurableRatingStore} from "../../measurable-rating/services/measurable-rating-store";
import {MeasurableRelationshipStore_API as MeasurableRelationshipStore} from "../../measurable-relationship/services/measurable-relationship-store";
import {MeasurableStore_API as MeasurableStore} from "../../measurable/services/measurable-store";
import {PersonStore_API as PersonStore} from "../../person/services/person-store";
import {PerspectiveDefinitionStore_API as PerspectiveDefinitionStore} from "../../perspective/services/perspective-definition-store";
import {PerspectiveRatingStore_API as PerspectiveRatingStore} from "../../perspective/services/perspective-rating-store";
import {PhysicalSpecDataTypeStore_API as PhysicalSpecDataTypeStore} from "../../physical-specifications/services/physical-spec-data-type-store";
import {SourceDataRatingStore_API as SourceDataRatingStore} from "../../source-data-rating/services/source-data-rating-store";
import {StaticPanelStore_API as StaticPanelStore} from "../../static-panel/services/static-panel-store";
import {TechnologyStatisticsService_API as TechnologyStatisticsService} from "../../technology/services/technology-statistics-service";
import {UserStore_API as UserStore} from "../../user/services/user-store";


export const CORE_API = {
    ApplicationStore,
    AuthSourcesStore,
    BookmarkStore,
    ChangeInitiativeStore,
    ChangeLogStore,
    DataTypeStore,
    EndUserAppStore,
    EntityNamedNoteStore,
    EntityNamedNoteTypeStore,
    EntitySearchStore,
    EntityStatisticStore,
    FlowDiagramStore,
    FlowDiagramAnnotationStore,
    FlowDiagramEntityStore,
    EntitySvgDiagramStore,
    InvolvementStore,
    LogicalFlowStore,
    MeasurableCategoryStore,
    MeasurableRatingStore,
    MeasurableRelationshipStore,
    MeasurableStore,
    PersonStore,
    PerspectiveDefinitionStore,
    PerspectiveRatingStore,
    PhysicalSpecDataTypeStore,
    SourceDataRatingStore,
    StaticPanelStore,
    TechnologyStatisticsService,
    UserStore
};


export function getApiReference(serviceName, serviceFnName) {
    return CORE_API[serviceName][serviceFnName];
}
