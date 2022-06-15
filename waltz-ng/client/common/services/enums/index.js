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

import _ from "lodash";

import { action } from "./action";
import { applicationKind } from "./application-kind";
import { appGroupKind } from "./app-group-kind";
import { attestationType } from "./attestation-type";
import { attestationStatus } from "./attestation-status";
import { BOOLEAN } from "./boolean";
import { criticality } from "./criticality";
import { changeInitiative } from "./change-initiative";
import { changeAction } from "./change-action";
import { dataFormatKind } from "./data-format-kind";
import { endOfLifeStatus } from "./end-of-life-status";
import { entity } from "./entity";
import { entityStatistic } from "./entity-statistic";
import { entityLifecycleStatus } from "./entity-lifecycle-status";
import { executionStatus } from "./execution-status";
import { frequencyKind } from "./frequency-kind";
import { hierarchyQueryScope } from "./hierarchy-query-scope";
import { investmentRating } from "./investment-rating";
import { issuance } from "./issuance";
import { lifecyclePhase } from "./lifecycle-phase";
import { lifecycleStatus } from "./lifecycle-status";
import { usageKind } from "./usage-kind";
import { orgUnitKind } from "./org-unit-kind";
import { participantKind } from "./participation-kind";
import { rag } from "./rag";
import { relationshipKind } from "./relationship-kind";
import { releaseLifecycleStatus } from "./release-lifecycle-status";
import { rollupKind } from "./rollup-kind";
import { severity } from "./severity";
import { surveyInstanceStatus } from "./survey-instance-status";
import { surveyQuestionFieldType } from "./survey-question-field-type";
import { surveyRunStatus } from "./survey-run-status";
import { fieldDataType } from "./field-data-type";
import { physicalSpecDefinitionType } from "./physical-spec-definition-type";

export const capabilityRating = investmentRating;

export const applicationRating = investmentRating;

export const enums = {
    action,
    applicationKind,
    applicationRating,
    appGroupKind,
    attestationType,
    attestationStatus,
    BOOLEAN,
    capabilityRating,
    changeAction,
    entityLifecycleStatus,
    executionStatus,
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
    frequencyKind,
    dataFormatKind,
    lifecycleStatus,
    fieldDataType,
    participantKind,
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
 * Used to convert a map of ( { code -> displayName } or a list of items
 * taken directly from the `enum-store`.
 * @param items
 * @param excludeUnknown
 */
export function toOptions(items = {}, excludeUnknown = false) {
    const converter = _.isArray(items)
        ? x => Object.assign({}, { code: x.key }, x)
        : (v, k) => ({name: v.name, code: k, position: v.position});

    return _.chain(items)
        .map(converter)
        .sortBy(["position", "name"])
        .reject(o => o.code === "UNKNOWN" && excludeUnknown)
        .value();
}
