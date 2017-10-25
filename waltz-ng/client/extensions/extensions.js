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

    overrideChangeInitiativeSection();
    addAttestationSection();
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
        id: 10100
    };

    dynamicSectionsByKind['APPLICATION'].push(dynamicSections.dbAttestationSection);
}