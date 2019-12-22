
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
import * as authSourcesStore from './services/auth-sources-store';
import * as AuthSourceEditorPanel from './components/editor/auth-source-editor-panel';
import * as AuthSourcesTable from './components/table/auth-sources-table';
import * as NonAuthSourcesPanel from './components/non-auth-sources-panel/non-auth-sources-panel';
import * as AuthSourcesSection from './components/section/auth-sources-section';
import * as AuthSourcesSummaryPanel from './components/summary-panel/auth-sources-summary-panel';
import * as TreePicker from './components/tree-picker/tree-picker';
import * as TreeFilter from './components/tree-filter/tree-filter';
import {registerComponents, registerStore} from '../common/module-utils';
import RatingIndicator from './directives/rating-indicator';


export default () => {

    const module = angular.module('waltz.auth.sources', []);

    module
        .directive('waltzRatingIndicator', RatingIndicator);

    registerStore(
        module,
        authSourcesStore);
    registerComponents(
        module,
        [
            AuthSourceEditorPanel,
            AuthSourcesTable,
            AuthSourcesSection,
            AuthSourcesSummaryPanel,
            TreePicker,
            TreeFilter,
            NonAuthSourcesPanel
        ]);

    return module.name;
};
