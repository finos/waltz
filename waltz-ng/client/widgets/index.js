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
import angular from "angular";
import ColumnMapper from './column-mapper/column-mapper.js';
import CurrencyAmount from './currency-amount';
import SubSection from './sub-section';
import EditableEnum from './editable-enum/editable-enum';
import SpreadsheetLoader from './spreadsheet-loader/spreadsheet-loader';
import {registerComponents} from "../common/module-utils";


export default () => {
    const module = angular.module('waltz.widgets', []);

    module.directive('waltzChangeTimeline', require('./change-timeline'));
    module.directive('waltzErrorAlert', require('./error-alert'));
    module.directive('waltzExternalLink', require('./external-link'));
    module.directive('waltzFromNow', require('./from-now'));
    module.directive('waltzJumpTo', require('./jump-to'));
    module.directive('waltzLoadingNotification', require('./loading-notification'));
    module.directive('waltzMailTo', require('./mail-to'));
    module.directive('waltzOverlayPanel', require('./overlay-panel'));
    module.directive('waltzPhoneLink', require('./phone-link'));
    module.directive('waltzRagIndicator', require('./rag-indicator'));
    module.directive('waltzSearch', require('./search'));
    module.directive('waltzSectionActions', require('./section-actions'));
    module.directive('waltzStepCount', require('./step-count'));
    module.directive('waltzYqSelect', require('./yq-select'));
    module
        .component('waltzBasicInfoTile', require('./basic-info-tile'))
        .component('waltzDataExtractLink', require('./data-extract-link'))
        .component('waltzEditableField', require('./editable-field'))
        .component('waltzIcon', require('./icon'))
        .component('waltzInlineEditArea', require('./inline-edit-area'))
        .component('waltzKeywordList', require('./keyword-list'))
        .component('waltzKeywordEdit', require('./keyword-edit'))
        .component('waltzNoData', require('./no-data'))
        .component('waltzMarkdown', require('./markdown'))
        .component('waltzPageHeader', require('./page-header/page-header'))
        .component('waltzPie', require('./pie/pie'))
        .component('waltzPieSegmentTable', require('./pie/pie-segment-table'))
        .component('waltzPieTable', require('./pie/pie-table'))
        .component('waltzSection', require('./section'))
        .component('waltzSimpleStackChart', require('./simple-stack-chart'))
        .component('waltzToggle', require('./toggle'))
        .component('waltzTwistie', require('./twistie'));

    registerComponents(module, [
        ColumnMapper,
        CurrencyAmount,
        EditableEnum,
        SpreadsheetLoader,
        SubSection
    ]);

    return module.name;
};
