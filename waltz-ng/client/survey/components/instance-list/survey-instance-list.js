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
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import moment from "moment";
import {isSurveyTargetKind} from "../../survey-utils";


import template from "./survey-instance-list.html";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    surveys: [],
    visibility: {
        dataTab: 0,
        showSurveySubject: false
    }
};


function mkTableData(surveyRuns = [], surveyInstances = []) {
    const runsById = _.keyBy(surveyRuns, "id");

    const surveys = _.map(surveyInstances, instance => {
        return {
            "surveyInstance": instance,
            "surveyRun": runsById[instance.surveyRunId],
            "surveyEntity": instance.surveyEntity
        }
    });

    const now = moment();
    const grouped = _.groupBy(surveys, s => {
        const subMoment = moment(s.surveyInstance.submittedAt);
        return s.surveyInstance.status == "WITHDRAWN" || now.diff(subMoment, "months") >= 12 ? "ARCHIVE" : "CURRENT"
    });
    return grouped;
}


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.showTab = (idx) => {
        vm.visibility.dataTab = idx;
    };


    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            let runsPromise;
            let instancesPromise;

            if (vm.parentEntityRef.kind === 'PERSON') {
                runsPromise = serviceBroker.loadViewData(
                    CORE_API.SurveyRunStore.findForRecipientId,
                    [vm.parentEntityRef.id],
                    { force: true });

                instancesPromise = serviceBroker.loadViewData(
                    CORE_API.SurveyInstanceStore.findForRecipientId,
                    [vm.parentEntityRef.id],
                    { force: true });
            } else {
                runsPromise = serviceBroker.loadViewData(
                        CORE_API.SurveyRunStore.findByEntityReference,
                        [vm.parentEntityRef],
                        { force: true });

                instancesPromise = serviceBroker.loadViewData(
                        CORE_API.SurveyInstanceStore.findByEntityReference,
                        [vm.parentEntityRef],
                        { force: true });
            }

            vm.visibility.showSurveySubject = ! isSurveyTargetKind(vm.parentEntityRef.kind);

            $q.all([runsPromise, instancesPromise])
                .then(([runsResult, instancesResult]) =>
                    vm.surveys = mkTableData(runsResult.data, instancesResult.data));
        }
    };

    vm.getSurveyTabMessage = (type) => {

        const surveyType = (type === "CURRENT") ? " current " : " archived ";

        if (_.isEmpty(vm.surveys[type])){
            return "No" + surveyType + "surveys";
        } else if (vm.surveys[type].length === 1) {
            return "1" + surveyType + "survey"
        } else {
            return vm.surveys[type].length + surveyType + "surveys";
        }
    }

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};

export default {
    component,
    id: "waltzSurveyInstanceList"
};



