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
        .findAll()
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