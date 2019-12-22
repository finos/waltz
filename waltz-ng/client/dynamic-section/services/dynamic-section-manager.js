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
import { dynamicSections, dynamicSectionsByKind } from "../dynamic-section-definitions";

const sectionManagerSvc = {
    serviceName: "DynamicSectionManager",
    service: ($location, $state, $stateParams, accessLogStore, localStorageService) => {
        let available = [];
        let active = [];
        let kind = null;

        function mkStorageKey() {
            return `waltz-user-section-ids-${kind}`;
        }


        /* Sort list of sections, ensuring 'change log' is the last one */
        function sortList(list) {
            return _.orderBy(list,
                    s => s === dynamicSections.changeLogSection
                        ? "zzz"
                        : _.toLower(s.name));
        }


        /* modifies a list (rather than replacing it) so watchers see changes */
        function blat(list, replacement) {
            list.splice(0, list.length, ...replacement);
        }


        function persistState() {
            const top3SectionIds = _
                .chain(active)
                .compact()
                .take(3)
                .map(s => s.id)
                .value();

            localStorageService.set(
                mkStorageKey(),
                top3SectionIds);

            $location.search("sections", _.join(top3SectionIds, ";"));
        }


        function initialise(newKind) {
            kind = newKind;
            blat(available, sortList(dynamicSectionsByKind[kind]));

            const previousViaParam = _.chain($location.search())
                .get("sections", "")
                .split(";")
                .map(s => Number(s))
                .value();

            const previousViaLocalHistory = localStorageService.get(mkStorageKey());

            const toActivate = _
                .chain(previousViaParam)
                .concat(previousViaLocalHistory)
                .map(sId => _.find(available, { id: sId }))
                .uniq()
                .compact()
                .take(3)
                .value();

            localStorageService.set(
                mkStorageKey(),
                _.map(toActivate, s => s.id));

            // log component activations to access log
            _.chain(toActivate)
                .map(s => `${$state.current.name}|${s.componentId}`)
                .forEach(state => accessLogStore.write(state, $stateParams))
                .value();

            blat(active, toActivate);
        }


        function getAvailable() {
            return available;
        }


        function getActive() {
            return active;
        }


        function activate(d) {
            accessLogStore.write(`${$state.current.name}|${d.componentId}`, $stateParams);

            blat(
                active,
                _.concat([d], _.without(active, d)));

            persistState();
        }


        function close(d) {
            blat(
                active,
                _.without(active, d));

            persistState();
        }


        function clear() {
            blat(active, []);
        }

        return {
            activate,
            close,
            getAvailable,
            getActive,
            initialise,
            clear
        };
    }
};

sectionManagerSvc.service.$inject = [
    "$location",
    "$state",
    "$stateParams",
    "AccessLogStore",
    "localStorageService"];


export default sectionManagerSvc;