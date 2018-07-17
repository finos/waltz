
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

import angular from 'angular';
import * as authSourcesStore from './services/auth-sources-store';
import * as AuthSourceEditorPanel from './components/editor/auth-source-editor-panel';
import * as AuthSourcesTable from './components/table/auth-sources-table';
import * as NonAuthSourcesPanel from './components/non-auth-sources-panel/non-auth-sources-panel';
import * as AuthSourcesSection from './components/section/auth-sources-section';
import * as AuthSourcesSummaryPanel from './components/summary-panel/auth-sources-summary-panel';
import * as TreePicker from './components/tree-picker/tree-picker';
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
        [   AuthSourceEditorPanel,
            AuthSourcesTable,
            AuthSourcesSection,
            AuthSourcesSummaryPanel,
            TreePicker,
            NonAuthSourcesPanel ]);

    return module.name;
};
