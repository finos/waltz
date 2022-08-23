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
import {nest} from "d3-collection";
import {ascending} from "d3-array";
import template from './survey-template-list.html';


function processTemplates(templates = []) {
    return nest()
        .key(t => t.targetEntityKind)
        .sortKeys(ascending)
        .sortValues((t1, t2) => t1.name.localeCompare(t2.name))
        .entries(templates);
}


function loadOwners(holder, personStore, templates = []) {

    holder.owners = {};

    const personIds = _
        .chain(templates)
        .map('ownerId')
        .uniq()
        .value();

    _.each(
        personIds,
        id => personStore.getById(id).then(p => {
            if (p) {
                holder.owners[id] = p;
            }
        }));
}

function controller(personStore, surveyTemplateStore) {

    const vm = this;

    surveyTemplateStore
        .findByOwner()
        .then(ts => {
            vm.groupedTemplates = processTemplates(ts);
            loadOwners(vm, personStore, ts)
        });

}


controller.$inject = [
    'PersonStore',
    'SurveyTemplateStore'
];


const page = {
    controller,
    controllerAs: 'ctrl',
    template
};


export default page;