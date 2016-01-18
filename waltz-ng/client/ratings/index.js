
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

import ratingsEditor from './ratings-editor';


export default (module) => {
    module.directive('waltzRatingGroup', require('./directives/rating-group/directive'));
    module.directive('waltzRatingGroups', require('./directives/rating-groups/directive'));
    module.directive('waltzRatingGroupHeader', require('./directives/rating-group-header/directive'));
    module.directive('waltzRatingBrushSelect', require('./directives/rating-brush-select/directive'));
    module.service('RatingStore', require('./services/ratings-store'));

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.ratings', {
                })
                .state('main.ratings.editor', {
                    url: 'ratings/{kind}/{entityId:int}/{perspectiveCode}/edit',
                    views: {'content@': ratingsEditor }
                });
        }
    ]);
};
