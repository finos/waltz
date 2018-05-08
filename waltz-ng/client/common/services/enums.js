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

import _ from "lodash";

import {action} from './enums/action';
import {applicationKind} from './enums/application-kind';
import {attestationType} from './enums/attestation-type';
import {BOOLEAN} from './enums/boolean';
import {criticality} from './enums/criticality';
import {changeInitiative} from './enums/change-initiative';
import {dataFormatKind} from './enums/data-format-kind';
import {endOfLifeStatus} from './enums/end-of-life-status';
import {entity} from './enums/entity';
import {entityStatistic} from './enums/entity-statistic';
import {entityLifecycleStatus} from './enums/entity-lifecycle-status';
import {frequencyKind} from './enums/frequency-kind';
import {hierarchyQueryScope} from './enums/hierarchy-query-scope';
import {investmentRating} from './enums/investment-rating';
import {issuance} from './enums/issuance';
import {lifecyclePhase} from './enums/lifecycle-phase';
import {lifecycleStatus} from './enums/lifecycle-status';
import {usageKind} from './enums/usage-kind';
import {orgUnitKind} from './enums/org-unit-kind';
import {rag} from './enums/rag';
import {relationshipKind} from './enums/relationship-kind';
import {releaseLifecycleStatus} from './enums/release-lifecycle-status';
import {rollupKind} from './enums/rollup-kind';
import {severity} from './enums/severity';
import {surveyInstanceStatus} from './enums/survey-instance-status';
import {surveyQuestionFieldType} from './enums/survey-question-field-type';
import {surveyRunStatus} from './enums/survey-run-status';
import {physicalSpecDefinitionFieldType} from './enums/physical-spec-defn-field-type';
import {physicalSpecDefinitionType} from './enums/physical-spec-definition-type';
import {transportKind} from './enums/transport-kind';

export const capabilityRating = investmentRating;

export const applicationRating = investmentRating;

export const enums = {
    action,
    applicationKind,
    applicationRating,
    attestationType,
    BOOLEAN,
    capabilityRating,
    entityLifecycleStatus,
    investmentRating,
    lifecyclePhase,
    orgUnitKind,
    severity,
    entity,
    changeInitiative,
    entityStatistic,
    hierarchyQueryScope,
    usageKind,
    criticality,
    rollupKind,
    endOfLifeStatus,
    transportKind,
    frequencyKind,
    dataFormatKind,
    lifecycleStatus,
    physicalSpecDefinitionFieldType,
    physicalSpecDefinitionType,
    rag,
    relationshipKind,
    surveyInstanceStatus,
    releaseLifecycleStatus,
    surveyRunStatus,
    surveyQuestionFieldType,
    issuance
};


export function getEnumName(enumValues = {}, key) {
    return enumValues[key] ? enumValues[key].name : key;
}


/**
 * Used to convert a map of ( { code -> displayName }
 * @param lookups
 * @param excludeUnknown
 */
export function toOptions(lookups = {}, excludeUnknown = false) {
    return _.chain(lookups)
        .map((v, k) => ({name: v.name, code: k, position: v.position}))
        .sortBy(['position', 'name'])
        .reject(o => o.code === 'UNKNOWN' && excludeUnknown)
        .value();
}


/**
 * Used to convert a map of { code->displayName } into
 * a format suitable for use by ui-grid.
 * @param lookups
 */
export function toGridOptions(lookups = {}) {
    return _.map(
        lookups,
        (v, k) => ({label: v.name, value: k}));
}
