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
import moment from "moment";
import {formats} from "../common";
import {CORE_API} from "../common/services/core-api-utils";
import {loadEntity} from "../common/entity-utils";
import {mkSiphon} from "../common/siphon-utils";


export function groupQuestions(questions = []) {
    const byExtId = _.keyBy(questions, d => d.externalId);

    const [subQs, topQs] = _.partition(
        questions,
        q => q.parentExternalId && byExtId[q.parentExternalId]);

    _.forEach(
        subQs,
        sq => {
            const parent = byExtId[sq.parentExternalId];
            parent.subQuestions = _.union(parent.subQuestions, [sq]);
        });


    const sections = _
        .chain(topQs)
        .map(q => q.sectionName || "Other")
        .uniq()
        .value();

    const groupedQuestions = _.groupBy(topQs, q => q.sectionName || "Other");

    const result = _.map(sections, s => {
        return {
            "sectionName": s,
            "questions": groupedQuestions[s]
        };
    });

    return result;
}



export function isSurveyTargetKind(entityKind = "") {
    return entityKind === "APPLICATION"
            || entityKind === "CHANGE_INITIATIVE";
}



export function mkDescription(descriptions = []) {
    return _
        .chain(descriptions)
        .filter(d => !_.isEmpty(d))
        .uniq()
        .join("\n\n --- \n\n")
        .value();
}


/**
 * Indexes the given survey responses by question id.
 * Also ensures that booleans and dates are properly
 * represented as strings.
 *
 * @param responses
 * @returns {qid -> surveyResponse}
 */
export function indexResponses(responses = []) {
    return _
        .chain(responses)
        .map(d => d.questionResponse)
        .map(qr => {
            if (!_.isNil(qr.booleanResponse) && !_.isString(qr.booleanResponse)) {
                qr.booleanResponse = qr.booleanResponse
                    ? "true"
                    : "false";
            }
            if (_.isNil(qr.booleanResponse) && !_.isString(qr.booleanResponse)){
                qr.booleanResponse = "null"
            }
            if (!_.isNil(qr.dateResponse)) {
                qr.dateResponse = moment(qr.dateResponse, formats.parseDateOnly).toDate()
            }
            return qr;
        })
        .keyBy("questionId")
        .value();
}


export function loadSurveyInfo($q,
                               serviceBroker,
                               userService,
                               surveyInstanceId,
                               force = false) {


    const dropdownEntriesPromise = serviceBroker
        .loadViewData(CORE_API.SurveyQuestionStore.findDropdownEntriesForInstance, [surveyInstanceId], {force})
        .then(r => r.data);

    const recipientsPromise = serviceBroker
        .loadViewData(CORE_API.SurveyInstanceStore.findRecipients, [surveyInstanceId], {force})
        .then(r => r.data);

    const instanceOwnersPromise = serviceBroker
        .loadViewData(CORE_API.SurveyInstanceStore.findOwners, [surveyInstanceId], {force})
        .then(r => r.data);

    const instancePromise = serviceBroker
        .loadViewData(CORE_API.SurveyInstanceStore.getById, [surveyInstanceId], {force})
        .then(r => r.data);

    const possibleActionsPromise = serviceBroker
        .loadViewData(CORE_API.SurveyInstanceStore.findPossibleActions, [surveyInstanceId], {force})
        .then(r => r.data);

    const permissionsPromise = serviceBroker
        .loadViewData(CORE_API.SurveyInstanceStore.getPermissions, [surveyInstanceId], {force})
        .then(r => r.data);

    const versionsPromise = instancePromise
        .then(instance => serviceBroker
            .loadViewData(
                CORE_API.SurveyInstanceStore.findPreviousVersions,
                [instance.originalInstanceId || instance.id]))
        .then(r => r.data);

    const runPromise = instancePromise
        .then(instance => serviceBroker
            .loadViewData(CORE_API.SurveyRunStore.getById, [instance.surveyRunId]))
        .then(r => r.data);

    const ownerPromise = runPromise
        .then(run => serviceBroker
            .loadViewData(CORE_API.PersonStore.getById, [run.ownerId]))
        .then(r => r.data);

    const owningRolePromise = instancePromise
        .then(instance => serviceBroker
            .loadAppData(CORE_API.RoleStore.findAllRoles)
            .then(r => _.find(r.data, d => d.key === instance.owningRole)));

    const templatePromise = runPromise
        .then(run => serviceBroker
            .loadViewData(CORE_API.SurveyTemplateStore.getById, [run.surveyTemplateId]))
        .then(r => r.data);

    const userPromise = userService.whoami();

    const  subjectPromise = instancePromise
        .then(instance => loadEntity(serviceBroker, instance.surveyEntity));


    const promises = [
        userPromise,
        instancePromise,
        dropdownEntriesPromise,
        runPromise,
        templatePromise,
        recipientsPromise,
        versionsPromise,
        subjectPromise,
        ownerPromise,
        owningRolePromise,
        instanceOwnersPromise,
        possibleActionsPromise,
        permissionsPromise
    ];

    return $q
        .all(promises)
        .then(([u, instance, dropdownEntries, run, template, recipients,  versions, subject, owner, ownerRole, instanceOwners, possibleActions, permissions]) => {

            const latestInstanceId = instance.originalInstanceId || instance.id;
            const isLatest = latestInstanceId === instance.id;

            const result = {
                instance,
                recipients,
                dropdownEntries,
                run,
                template,
                isLatest,
                latestInstanceId,
                versions,
                subject,
                owner,
                ownerRole,
                instanceOwners,
                possibleActions,
                permissions
            };

            return result;
        });
}


/**
 * Takes a listResponse from a survey instance and returns the list of measurable ids,
 * and theNotFoundSiphon
 *
 * @param responses
 * @param measurablesById
 * @returns {measurableIds, notFoundSiphon: (function(*=): boolean)}}
 */
export function parseMeasurableListResponse(responses, measurablesById){
    const measurableIds = _.map(_.keys(measurablesById), d => Number(d));
    const notFoundSiphon = mkSiphon(d => !_.includes(measurableIds, d.id));

    const checkedItemIds = _
        .chain(responses)
        .reject(notFoundSiphon)
        .map(d => d.id)
        .value();

    return {
        measurableIds: checkedItemIds,
        notFoundSiphon
    }
}
