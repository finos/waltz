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

import _ from 'lodash';
import {checkNotEmpty} from "../../common/checks";
import {dynamicSections, dynamicSectionsByKind} from "../dynamic-section-definitions";

const SOFT_SECTION_LIMIT = 3;

export function service($location, $stateParams, localStorage) {

    const sectionsById = _.keyBy(_.values(dynamicSections), 'id');

    const mkStorageKey = (kind) => `waltz-user-section-ids-${kind}`;

    const updateWindowLocation = (ids) => {
        const sectionParamValue = _
            .chain(ids)
            .compact()
            .take(3)
            .join(';')
            .value();
        $location.search('sections', sectionParamValue);
    };

    const readFromLocalStorage = (kind) => {
        return _.compact(localStorage.get(mkStorageKey(kind)) || []);
    };

    const writeToLocalStorage = (kind, sectionIds = []) => {
        localStorage.set(mkStorageKey(kind), _.compact(sectionIds));
    };

    const getOpenSections = (kind, limit) => {
        const sectionIds = readFromLocalStorage(kind);

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
        const sections = dynamicSectionsByKind[kind] || [];
        const changeSection = _.filter(sections, { id: dynamicSections.changeLogSection.id });
        return _.chain(sections)
            .reject({ id: dynamicSections.changeLogSection.id })
            .sortBy('name')
            .union(changeSection)
            .value();
    }

    function findUserSectionsForKind(kind) {
        _.chain($stateParams.sections)
            .split(';')
            .map(id => Number(id))
            .reverse()
            .forEach(id => openSection(sectionsById[id], kind))
            .value();

        const openSections = getOpenSections(kind, SOFT_SECTION_LIMIT);

        // re-save to localStorage to eliminate 'working' ids
        writeToLocalStorage(
            kind,
            _.chain(openSections)
                .compact()
                .take(SOFT_SECTION_LIMIT)
                .map('id')
                .value());

        return openSections;
    }

    function openSection(section, kind) {
        if (section == null) {
            return getOpenSections(kind)
        }

        const userSectionIds = readFromLocalStorage(kind);

        // we toggle the section if it's the current top one...
        const discardSection = userSectionIds.length > 0 && userSectionIds[0] === section.id;

        const workingIds =  _.reject(
            userSectionIds,
            activeSectionId => activeSectionId === section.id);

        if (!discardSection) {
            // push to front
            workingIds.unshift(section.id);
        }

        writeToLocalStorage(kind, workingIds);

        return getOpenSections(kind);
    }

    function removeSection(section, kind) {
        if (section == null) {
            return getOpenSections(kind);
        }

        const sectionIds = readFromLocalStorage(kind);
        const updatedSectionIds = _.without(sectionIds, section.id);
        writeToLocalStorage(kind, updatedSectionIds);


        return getOpenSections(kind);
    }

    return {
        findAvailableSectionsForKind,
        findUserSectionsForKind,
        openSection,
        removeSection
    };
}

service.$inject = [
    '$location',
    '$stateParams',
    'localStorageService'];

const serviceName = 'DynamicSectionManager';

export default {
    service,
    serviceName
};