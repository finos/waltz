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
import * as sourceDataRatingStore from './services/source-data-rating-store';
import {registerComponents, registerStore} from '../common/module-utils';

import sourceDataOverlay from './components/overlay/source-data-overlay';
import sourceDataSectionAddon from './components/section-addon/source-data-section-addon';
import Routes from './routes';
import SourceDataInfo from './directives/source-data-info';


export default () => {
    const module = angular.module('waltz.source.data.ratings', []);

    module
        .config(Routes);

    registerStore(module, sourceDataRatingStore);

    module
        .directive('waltzSourceDataInfo', SourceDataInfo);

    registerComponents(module, [
        sourceDataOverlay,
        sourceDataSectionAddon
    ]);

    return module.name;
};
