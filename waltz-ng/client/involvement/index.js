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

import angular from 'angular';
import { registerComponents, registerStore } from '../common/module-utils';

import involvedPeopleSection from './components/involved-people-section';
import involvedSectionService from './services/involved-section-service';
import involvementStore from './services/involvement-store';
import keyPeopleSubSection from './components/sub-section/key-people-sub-section';


export default () => {
    const module = angular.module('waltz.involvement', []);

    module
        .component('waltzInvolvedPeopleSection', involvedPeopleSection);

    registerComponents(module, [keyPeopleSubSection]);

    module
        .service('InvolvedSectionService', involvedSectionService);

    registerStore(module, involvementStore);

    return module.name;
};
