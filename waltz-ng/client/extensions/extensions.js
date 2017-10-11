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