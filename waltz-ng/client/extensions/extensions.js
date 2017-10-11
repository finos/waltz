// extensions initialisation

import {registerComponents} from '../common/module-utils';
import {dynamicSections, dynamicSectionsByKind} from "../dynamic-section/dynamic-section-definitions";

import dbChangeInitiativeBrowser from './components/change-initiative/change-initiative-browser/db-change-initiative-browser';
import dbChangeInitiativeSection from './components/change-initiative/change-initiative-section/db-change-initiative-section';

export const init = (module) => {

    registerComponents(module, [
        dbChangeInitiativeBrowser,
        dbChangeInitiativeSection
    ]);

    _overrideChangeInitiativeSection();
};


function _overrideChangeInitiativeSection() {
    dynamicSections.dbChangeInitiativesSection = {
        componentId: 'db-change-initiative-section',
        name: 'Change Initiatives',
        icon: 'paper-plane-o',
        id: 10000
    };

    dynamicSectionsByKind["APPLICATION"] = [
        dynamicSections.measurableRatingAppSection,
        dynamicSections.entityNamedNotesSection,
        dynamicSections.bookmarksSection,
        dynamicSections.involvedPeopleSection,
        dynamicSections.dbChangeInitiativesSection, //overridden
        dynamicSections.flowDiagramsSection,
        dynamicSections.dataFlowSection,
        dynamicSections.technologySection,
        dynamicSections.appCostsSection,
        dynamicSections.entityStatisticSection,
        dynamicSections.surveySection,
        dynamicSections.changeLogSection
    ];
}