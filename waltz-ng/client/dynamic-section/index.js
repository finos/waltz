
import angular from 'angular';

import {registerComponents, registerServices} from "../common/module-utils";
import * as DynamicSection from "./components/dynamic-section/dynamic-section";
import * as DynamicSectionNavigation from "./components/dynamic-section-navigation/dynamic-section-navigation";
import * as DynamicSectionWrapper from "./components/dynamic-section-wrapper/dynamic-section-wrapper";
import * as DynamicSectionsView from "./components/dynamic-sections-view/dynamic-sections-view";
import * as DynamicSectionManager from './services/dynamic-section-manager';


export default () => {

    const module = angular.module('waltz.dynamic-section', []);

    registerComponents(module, [
        DynamicSection,
        DynamicSectionNavigation,
        DynamicSectionWrapper,
        DynamicSectionsView
    ]);

    registerServices(module, [
        DynamicSectionManager
    ]);

    return module.name;
};