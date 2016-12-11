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
import angular from 'angular';

import playpenView1 from "./1/playpen1";
import playpenView2 from "./2/playpen2";
import playpenView3 from "./3/playpen3";
import playpenView4 from "./4/playpen4";


export default () => {

    const module = angular.module('waltz.playpen', []);
    module
        .component('waltzAssetCostGraph', require('./3/asset-cost-graph.js'))
        .component('waltzSvgManipulator', require('./4/svg-manipulator'));

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.playpen', {
                    url: 'playpen',
                    views: {
                        'content@': { template: require('./list.html') }
                    }
                })
                .state('main.playpen.1', {
                    url: '/1',
                    views: { 'content@': playpenView1 }
                })
                .state('main.playpen.2', {
                    url: '/2',
                    views: { 'content@': playpenView2 }
                })
                .state('main.playpen.3', {
                    url: '/3',
                    views: { 'content@': playpenView3 }
                })
                .state('main.playpen.4', {
                    url: '/4',
                    views: { 'content@': playpenView4 }
                })
        }
    ]);

    return module.name;

};
