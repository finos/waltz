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

import {action} from './action';
import {applicationKind} from './application-kind';
import {appGroupKind} from './app-group-kind';
import {attestationType} from './attestation-type';
import {BOOLEAN} from './boolean';
import {criticality} from './criticality';
import {changeInitiative} from './change-initiative';
import {dataFormatKind} from './data-format-kind';
import {endOfLifeStatus} from './end-of-life-status';
import {entity} from './entity';
import {entityStatistic} from './entity-statistic';
import {entityLifecycleStatus} from './entity-lifecycle-status';
import {frequencyKind} from './frequency-kind';
import {hierarchyQueryScope} from './hierarchy-query-scope';
import {investmentRating} from './investment-rating';
import {issuance} from './issuance';
import {lifecyclePhase} from './lifecycle-phase';
import {lifecycleStatus} from './lifecycle-status';
import {usageKind} from './usage-kind';
import {orgUnitKind} from './org-unit-kind';
import {rag} from './rag';
import {relationshipKind} from './relationship-kind';
import {releaseLifecycleStatus} from './release-lifecycle-status';
import {rollupKind} from './rollup-kind';
import {severity} from './severity';
import {surveyInstanceStatus} from './survey-instance-status';
import {surveyQuestionFieldType} from './survey-question-field-type';
import {surveyRunStatus} from './survey-run-status';
import {fieldDataType} from './field-data-type';
import {physicalSpecDefinitionType} from './physical-spec-definition-type';
import {transportKind} from './transport-kind';

export const capabilityRating = investmentRating;

export const applicationRating = investmentRating;

export const enums = {
    action,
    applicationKind,
    applicationRating,
    appGroupKind,
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
    fieldDataType,
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
