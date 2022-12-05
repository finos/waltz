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

import {remote} from "./remote";
import {toLocalDate} from "../common/date-utils";

export function mkSurveyInstanceStore() {

    const getPermissions = (id) => remote
        .fetchViewDatum("GET", `api/survey-instance/${id}/permissions`);

    const findPossibleActions = (id, force = false) => remote
        .fetchViewList("GET", `api/survey-instance/${id}/actions`, [], {force});

    const findRecipients = (id, force = false) => remote
        .fetchViewList("GET",`api/survey-instance/${id}/recipients`, null, {force});

    const findOwners = (id, force = false) => remote
        .fetchViewList("GET",`api/survey-instance/${id}/owners`, null, {force});

    const findResponses = (id) => remote
        .fetchViewList("GET",`api/survey-instance/${id}/responses`);

    const findPreviousVersions = (originalId) => remote
        .fetchViewList("GET", `api/survey-instance/id/${originalId}/previous-versions`);


    const findVersions = (originalId, force = false) => remote
        .fetchViewList("GET", `api/survey-instance/id/${originalId}/versions`, null, {force});



    const updateStatus = (id, command) => remote
        .execute("PUT",`api/survey-instance/${id}/status`, command);

    const deleteRecipient = (id, personId) => remote
        .execute("DELETE", `api/survey-instance/${id}/recipient/${personId}`)

    const deleteOwner = (id, personId) => remote
        .execute("DELETE", `api/survey-instance/${id}/owner/${personId}`)

    const addRecipient = (surveyInstanceId, personId) => remote
        .execute("POST",`api/survey-instance/${surveyInstanceId}/recipient`, personId);

    const addOwner = (surveyInstanceId, personId) => remote
        .execute("POST", `api/survey-instance/${surveyInstanceId}/owner`, personId);

    const updateSubmissionDueDate = (id, updDate) => remote
        .execute("PUT", `api/survey-instance/${id}/submission-due-date`, toLocalDate(updDate));

    const updateApprovalDueDate = (id, updDate) => remote
        .execute("PUT", `api/survey-instance/${id}/approval-due-date`, toLocalDate(updDate));

    const copyResponses = (id, copyCmd) => remote
        .execute("POST", `api/survey-instance/${id}/copy-responses`, copyCmd);

    const reassignRecipients = () => remote
        .execute(
            "POST",
            "api/survey-instance/reassign-recipients",
            null);

    const getReassignRecipientsCounts = (force = false) => remote
        .fetchViewDatum(
            "GET",
            "api/survey-instance/reassign-recipients-counts",
            null,
            {force});

    const reassignOwners = () => remote
        .execute(
            "POST",
            "api/survey-instance/reassign-owners",
            null);

    const getReassignOwnersCounts = (force = false) => remote
        .fetchViewDatum(
            "GET",
            "api/survey-instance/reassign-owners-counts",
            null,
            {force});


    return {
        addOwner,
        addRecipient,
        deleteOwner,
        deleteRecipient,
        findOwners,
        findPossibleActions,
        findRecipients,
        findResponses,
        findPreviousVersions,
        findVersions,
        getPermissions,
        updateStatus,
        updateSubmissionDueDate,
        updateApprovalDueDate,
        copyResponses,
        reassignRecipients,
        getReassignRecipientsCounts,
        reassignOwners,
        getReassignOwnersCounts
    };
}

export const surveyInstanceStore = mkSurveyInstanceStore();