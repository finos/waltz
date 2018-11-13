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

import {ActorStore_API as ActorStore} from "../../actor/services/actor-store";
import {AliasStore_API as AliasStore} from "../../alias/services/alias-store";
import {AppGroupStore_API as AppGroupStore} from "../../app-groups/services/app-group-store";
import {ApplicationStore_API as ApplicationStore} from "../../applications/services/application-store";
import {AssessmentDefinitionStore_API as AssessmentDefinitionStore} from "../../assessments/services/assessment-definition-store";
import {AssessmentRatingStore_API as AssessmentRatingStore} from "../../assessments/services/assessment-rating-store";
import {AttestationInstanceStore_API as AttestationInstanceStore} from "../../attestation/services/attestation-instance-store";
import {AttestationRunStore_API as AttestationRunStore} from "../../attestation/services/attestation-run-store";
import {AssetCostStore_API as AssetCostStore} from "../../asset-cost/services/asset-cost-store";
import {AuthSourcesStore_API as AuthSourcesStore} from "../../auth-sources/services/auth-sources-store";
import {BookmarkStore_API as BookmarkStore} from "../../bookmarks/services/bookmark-store";
import {ChangeInitiativeStore_API as ChangeInitiativeStore} from "../../change-initiative/services/change-initiative-store";
import {ChangeLogStore_API as ChangeLogStore} from "../../change-log/services/change-log-store";
import {ComplexityStore_API as ComplexityStore} from "../../complexity/services/complexity-store";
import {DatabaseStore_API as DatabaseStore} from "../../databases/services/database-store";
import {DataTypeStore_API as DataTypeStore} from "../../data-types/services/data-type-store";
import {DataTypeUsageStore_API as DataTypeUsageStore} from "../../data-type-usage/services/data-type-usage-store";
import {DrillGridDefinitionStore_API as DrillGridDefinitionStore} from "../../drill-grid/services/drill-grid-definition-store";
import {EndUserAppStore_API as EndUserAppStore} from "../../end-user-apps/services/end-user-app-store";
import {EntityEnumStore_API as EntityEnumStore} from "../../entity-enum/services/entity-enum-store";
import {EntityNamedNoteStore_API as EntityNamedNoteStore} from "../../entity-named-note/services/entity-named-note-store";
import {EntityNamedNoteTypeStore_API as EntityNamedNoteTypeStore} from "../../entity-named-note/services/entity-named-note-type-store";
import {EntityRelationshipStore_API as EntityRelationshipStore} from "../../entity-relationship/services/entity-relationship-store";
import {EntitySearchStore_API as EntitySearchStore} from "../../entity/services/entity-search-store";
import {EntityStatisticStore_API as EntityStatisticStore} from "../../entity-statistics/services/entity-statistic-store";
import {EntityTagStore_API as EntityTagStore} from "../../entity-tags/services/entity-tag-store";
import {EnumValueStore_API as EnumValueStore} from "../../enum-value/services/enum-value-store";
import {EntitySvgDiagramStore_API as EntitySvgDiagramStore} from "../../entity-svg-diagram/services/entity-svg-diagram-store";
import {FlowDiagramStore_API as FlowDiagramStore} from "../../flow-diagram/services/flow-diagram-store";
import {FlowDiagramAnnotationStore_API as FlowDiagramAnnotationStore} from "../../flow-diagram/services/flow-diagram-annotation-store";
import {FlowDiagramEntityStore_API as FlowDiagramEntityStore} from "../../flow-diagram/services/flow-diagram-entity-store";
import {InvolvementStore_API as InvolvementStore} from "../../involvement/services/involvement-store";
import {InvolvementKindStore_API as InvolvementKindStore} from "../../involvement-kind/services/involvement-kind-store";
import {LogicalDataElementStore_API as LogicalDataElementStore} from "../../logical-data-element/services/logical-data-element-store";
import {LogicalFlowStore_API as LogicalFlowStore} from "../../logical-flow/services/logical-flow-store";
import {LogicalFlowDecoratorStore_API as LogicalFlowDecoratorStore} from "../../logical-flow-decorator/services/logical-flow-decorator-store";
import {MeasurableCategoryStore_API as MeasurableCategoryStore} from "../../measurable-category/services/measurable-category-store";
import {MeasurableRatingStore_API as MeasurableRatingStore} from "../../measurable-rating/services/measurable-rating-store";
import {MeasurableRelationshipStore_API as MeasurableRelationshipStore} from "../../measurable-relationship/services/measurable-relationship-store";
import {MeasurableStore_API as MeasurableStore} from "../../measurable/services/measurable-store";
import {NotificationStore_API as NotificationStore} from "../../notification/services/notification-store";
import {OrgUnitStore_API as OrgUnitStore} from "../../org-units/services/org-unit-store";
import {PersonStore_API as PersonStore} from "../../person/services/person-store";
import {PhysicalFlowStore_API as PhysicalFlowStore} from "../../physical-flows/service/physical-flow-store";
import {PhysicalSpecDataTypeStore_API as PhysicalSpecDataTypeStore} from "../../physical-specifications/services/physical-spec-data-type-store";
import {PhysicalSpecDefinitionFieldStore_API as PhysicalSpecDefinitionFieldStore} from "../../physical-specifications/services/physical-spec-definition-field-store";
import {PhysicalSpecDefinitionStore_API as PhysicalSpecDefinitionStore} from "../../physical-specifications/services/physical-spec-definition-store";
import {PhysicalSpecDefinitionSampleFileStore_API as PhysicalSpecDefinitionSampleFileStore} from "../../physical-specifications/services/physical-spec-definition-sample-file-store";
import {PhysicalSpecificationStore_API as PhysicalSpecificationStore} from "../../physical-specifications/services/physical-specification-store";
import {RatingSchemeStore_API as RatingSchemeStore} from "../../ratings/services/rating-scheme-store";
import {RoadmapStore_API as RoadmapStore} from "../../roadmap/services/roadmap-store";
import {ScenarioStore_API as ScenarioStore} from "../../scenario/services/scenario-store";
import {ServerInfoStore_API as ServerInfoStore} from "../../server-info/services/server-info-store";
import {SettingsStore_API as SettingsStore} from "../../system/services/settings-store";
import {SharedPreferenceStore_API as SharedPreferenceStore} from "../../shared-preference/services/shared-preference-store";
import {SourceDataRatingStore_API as SourceDataRatingStore} from "../../source-data-rating/services/source-data-rating-store";
import {StaticPanelStore_API as StaticPanelStore} from "../../static-panel/services/static-panel-store";
import {SurveyInstanceStore_API as SurveyInstanceStore} from "../../survey/services/survey-instance-store";
import {SurveyRunStore_API as SurveyRunStore} from "../../survey/services/survey-run-store";
import {SurveyTemplateStore_API as SurveyTemplateStore} from "../../survey/services/survey-template-store";
import {SvgDiagramStore_API as SvgDiagramStore} from "../../svg-diagram/services/svg-diagram-store";
import {TaxonomyManagementStore_API as TaxonomyManagementStore} from "../../taxonomy-management/services/taxonomy-management-store";
import {TechnologyStatisticsService_API as TechnologyStatisticsService} from "../../technology/services/technology-statistics-service";
import {UserStore_API as UserStore} from "../../user/services/user-store";


export const CORE_API = {
    ActorStore,
    AliasStore,
    AppGroupStore,
    ApplicationStore,
    AttestationInstanceStore,
    AttestationRunStore,
    AssetCostStore,
    AssessmentDefinitionStore,
    AssessmentRatingStore,
    AuthSourcesStore,
    BookmarkStore,
    ChangeInitiativeStore,
    ChangeLogStore,
    ComplexityStore,
    DatabaseStore,
    DataTypeStore,
    DataTypeUsageStore,
    DrillGridDefinitionStore,
    EndUserAppStore,
    EntityEnumStore,
    EntityNamedNoteStore,
    EntityNamedNoteTypeStore,
    EntityRelationshipStore,
    EntitySearchStore,
    EntityStatisticStore,
    EntityTagStore,
    EnumValueStore,
    FlowDiagramStore,
    FlowDiagramAnnotationStore,
    FlowDiagramEntityStore,
    EntitySvgDiagramStore,
    InvolvementStore,
    InvolvementKindStore,
    LogicalDataElementStore,
    LogicalFlowStore,
    LogicalFlowDecoratorStore,
    MeasurableCategoryStore,
    MeasurableRatingStore,
    MeasurableRelationshipStore,
    MeasurableStore,
    NotificationStore,
    OrgUnitStore,
    PersonStore,
    PhysicalFlowStore,
    PhysicalSpecDataTypeStore,
    PhysicalSpecDefinitionFieldStore,
    PhysicalSpecDefinitionStore,
    PhysicalSpecDefinitionSampleFileStore,
    PhysicalSpecificationStore,
    RatingSchemeStore,
    RoadmapStore,
    ScenarioStore,
    ServerInfoStore,
    SettingsStore,
    SharedPreferenceStore,
    SourceDataRatingStore,
    StaticPanelStore,
    SurveyInstanceStore,
    SurveyRunStore,
    SurveyTemplateStore,
    SvgDiagramStore,
    TaxonomyManagementStore,
    TechnologyStatisticsService,
    UserStore
};


export function getApiReference(serviceName, serviceFnName) {
    return CORE_API[serviceName][serviceFnName];
}
