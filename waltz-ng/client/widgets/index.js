/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

export default () => {
    const module = angular.module('waltz.widgets', []);

    module.directive('waltzChangeTimeline', require('./change-timeline'));
    module.directive('waltzInlineEditArea', require('./inline-edit-area'));
    module.directive('waltzErrorAlert', require('./error-alert'));
    module.directive('waltzExternalLink', require('./external-link'));
    module.directive('waltzFromNow', require('./from-now'));
    module.directive('waltzJumpTo', require('./jump-to'));
    module.directive('waltzKeywordList', require('./keyword-list'));
    module.directive('waltzLoadingNotification', require('./loading-notification'));
    module.directive('waltzMailTo', require('./mail-to'));
    module.directive('waltzOverlayPanel', require('./overlay-panel'));
    module.directive('waltzPhoneLink', require('./phone-link'));
    module.directive('waltzRagIndicator', require('./rag-indicator'));
    module.directive('waltzSearch', require('./search'));
    module.directive('waltzSectionActions', require('./section-actions'));
    module.directive('waltzStepCount', require('./step-count'));
    module.directive('waltzToggle', require('./toggle'));
    module.directive('waltzYqSelect', require('./yq-select'));
    module
        .component('waltzBasicInfoTile', require('./basic-info-tile'))
        .component('waltzDataExtractLink', require('./data-extract-link'))
        .component('waltzEditableField', require('./editable-field'))
        .component('waltzIcon', require('./icon'))
        .component('waltzNoData', require('./no-data'))
        .component('waltzPageHeader', require('./page-header'))
        .component('waltzPie', require('./pie'))
        .component('waltzPieSegmentTable', require('./pie-segment-table'))
        .component('waltzPieTable', require('./pie-table'))
        .component('waltzSection', require('./section'))
        .component('waltzSimpleStackChart', require('./simple-stack-chart'))
        .component('waltzTwistie', require('./twistie'));

    return module.name;
};
