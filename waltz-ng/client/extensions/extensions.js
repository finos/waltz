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

// extensions initialisation

import _ from "lodash";
import {registerComponents} from '../common/module-utils';
import {dynamicSections, dynamicSectionsByKind} from "../dynamic-section/dynamic-section-definitions";

import dbChangeInitiativeBrowser from './components/change-initiative/change-initiative-browser/db-change-initiative-browser';
import dbChangeInitiativeSection from './components/change-initiative/change-initiative-section/db-change-initiative-section';

export const init = (module) => {

    registerComponents(module, [
        dbChangeInitiativeBrowser,
        dbChangeInitiativeSection
    ]);

    // overrideChangeInitiativeSection();
    addAttestationSection();
    addEntitySvgDiagramsSection();
};


function overrideChangeInitiativeSection() {
    dynamicSections.dbChangeInitiativesSection = {
        componentId: 'db-change-initiative-section',
        name: 'Change Initiatives',
        icon: 'paper-plane-o',
        id: 10000
    };

    _.forIn(dynamicSectionsByKind, (v, k) => dynamicSectionsByKind[k] = _.map(
            v,
            ds => ds.id === dynamicSections.changeInitiativeSection.id
                ? dynamicSections.dbChangeInitiativesSection
                : ds
        ));
}


function addAttestationSection() {
    dynamicSections.dbAttestationSection = {
        componentId: 'attestation-section',
        name: 'Attestations',
        icon: 'check-square-o',
        id: 10001
    };

    dynamicSectionsByKind['APPLICATION'].push(dynamicSections.dbAttestationSection);
}


function addEntitySvgDiagramsSection() {
    dynamicSections.dbEntitySvgDiagramsSection = {
        componentId: 'entity-svg-diagrams-section',
        name: 'Diagrams',
        icon: 'sticky-note-o',
        id: 10002
    };

    dynamicSectionsByKind['MEASURABLE'].splice(1, 0, dynamicSections.dbEntitySvgDiagramsSection);
}