/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import {dynamicSectionsByKind, dynamicSections} from "../dynamic-section-definitions";

const sectionManagerSvc = {
    serviceName: "DynamicSectionManager",
    service: ($location, $stateParams, localStorageService) => {
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

            blat(active, toActivate);
        }


        function getAvailable() {
            return available;
        }


        function getActive() {
            return active;
        }


        function activate(d) {
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
    "$stateParams",
    "localStorageService"];


export default sectionManagerSvc;