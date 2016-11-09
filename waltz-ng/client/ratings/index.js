
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */



export default (module) => {
    module.directive('waltzRatingGroup', require('./directives/rating-group/directive'));
    module.directive('waltzRatingGroups', require('./directives/rating-groups/directive'));
    module.directive('waltzRatingGroupHeader', require('./directives/rating-group-header/directive'));
    module.directive('waltzRatingBrushSelect', require('./directives/rating-brush-select/directive'));


    module.directive('waltzAppRatingTable', require('./directives/viewer/app-rating-table'));
    module.directive('waltzCapabilityMultiSelector', require('./directives/viewer/capability-multi-selector'));
    module.directive('waltzMultiAppRatingViewer', require('./directives/viewer/multi-app-rating-viewer'));
    module.directive('waltzRatingColorStrategyOptions', require('./directives/viewer/rating-color-strategy-options'));

    module.directive('waltzRagLine', require('./directives/rating-explorer/rag-line'));
    module.directive('waltzRatingExplorerSection', require('./directives/rating-explorer/rating-explorer-section'));


    module.service('RatingStore', require('./services/ratings-store'));

};
