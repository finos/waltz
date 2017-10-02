/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import _ from 'lodash';
import {checkNotEmpty} from "../../common/checks";
import {dynamicSections} from "../dynamic-section-definitions";
import {indexSectionsByApplicableKind} from "../dynamic-section-utils";

const SOFT_SECTION_LIMIT = 3;

export function service($location, $stateParams, localStorage) {

    const sectionsByKind = indexSectionsByApplicableKind(dynamicSections);
    const sectionsById = _.keyBy(dynamicSections, 'id');

    const mkStorageKey = (kind) => `waltz-user-section-ids-${kind}`;

    const updateWindowLocation = (ids) => {
        const sectionParamValue = _
            .chain(ids)
            .take(3)
            .join(';')
            .value();
        $location.search('sections', sectionParamValue);
    };

    const getOpenSections = (kind, limit) => {
        const sectionIds = localStorage.get(mkStorageKey(kind)) || [];

        updateWindowLocation(_.take(sectionIds, SOFT_SECTION_LIMIT));

        const openSections = _
            .chain(limit ? _.take(sectionIds, SOFT_SECTION_LIMIT) : sectionIds)
            .map(sId => sectionsById[sId])
            .value();

        return openSections;
    };

    // -- API ---

    function findAvailableSectionsForKind(kind) {
        checkNotEmpty(kind);
        return sectionsByKind[kind] || [];
    }

    function findUserSectionsForKind(kind) {
        _.chain($stateParams.sections)
            .split(';')
            .map(id => Number(id))
            .reverse()
            .forEach(id => openSection(sectionsById[id], kind))
            .value();

        const openSections = getOpenSections(kind, SOFT_SECTION_LIMIT);

        // resave to localstorage to eliminate 'working' ids
        localStorage.set(
            mkStorageKey(kind),
            _.chain(openSections)
                .take(SOFT_SECTION_LIMIT)
                .map('id')
                .value());

        return openSections;
    }

    function openSection(section, kind) {
        if (section == null) {
            return getOpenSections(kind)
        }

        const userSectionIds = localStorage.get(mkStorageKey(kind)) || [];
        const workingIds =  _.reject(
            userSectionIds,
            activeSectionId => activeSectionId === section.id);
        workingIds.unshift(section.id); // push to front

        localStorage.set(mkStorageKey(kind), workingIds);

        return getOpenSections(kind);
    }

    return {
        findAvailableSectionsForKind,
        findUserSectionsForKind,
        openSection
    };
}

service.$inject = [
    '$location',
    '$stateParams',
    'localStorageService'];

export const serviceName = 'DynamicSectionManager';