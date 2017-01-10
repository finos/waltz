
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
    const module = angular.module('waltz.ratings', []);

    module
        .directive('waltzRatingGroup', require('./directives/rating-group/directive'))
        .directive('waltzRatingGroups', require('./directives/rating-groups/directive'))
        .directive('waltzRatingGroupHeader', require('./directives/rating-group-header/directive'))
        .directive('waltzAppRatingTable', require('./directives/viewer/app-rating-table'))
        .directive('waltzRatingBrushSelect', require('./directives/rating-brush-select/directive'))
        .directive('waltzCapabilityMultiSelector', require('./directives/viewer/capability-multi-selector'))
        .directive('waltzMultiAppRatingViewer', require('./directives/viewer/multi-app-rating-viewer'))
        .directive('waltzRatingColorStrategyOptions', require('./directives/viewer/rating-color-strategy-options'))
        .directive('waltzRagLine', require('./directives/rating-explorer/rag-line'))
        .directive('waltzRatingExplorerSection', require('./directives/rating-explorer/rating-explorer-section'));

    module
        .component('waltzRatingPicker', require('./components/rating-picker/rating-picker'))
        .component('waltzRatingIndicatorCell', require('./components/rating-indicator-cell/rating-indicator-cell'));

    module
        .service('RatingStore', require('./services/ratings-store'));

    return module.name;
};
