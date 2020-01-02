
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
import * as staticPanelStore from './services/static-panel-store';

import {registerStore} from '../common/module-utils'
import StaticPanels from './components/static-panels';
import StaticPanel from './components/static-panel';

export default () => {
    const module = angular.module('waltz.static.panel', []);

    registerStore(module, staticPanelStore);

    module
        .component('waltzStaticPanels', StaticPanels)
        .component('waltzStaticPanel', StaticPanel);
    return module.name;
};
