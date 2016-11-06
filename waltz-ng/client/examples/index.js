

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

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.examples', {
                    url: 'examples',
                    views: {
                        'docs-content@': { template: require('./index.html') },
                        'docs-sidebar@': { template: require('./toc.html') }
                    }
                })
                .state('main.examples.directive-app-selector', {
                    url: '/directive-app-selector',
                    views: { 'content@': {template: require('./demo-directive-app-selector.html') } }
                })
                .state('main.examples.directive-person-selector', {
                    url: '/directive-person-selector',
                    views: { 'content@': {template: require('./demo-directive-person-selector.html') } }
                })
                .state('main.examples.directive-rating-indicator', {
                    url: '/directive-rating-indicator',
                    views: { 'content@': {
                        template: require('./demo-directive-rating-indicator.html')
                    } }
                })
                .state('main.examples.directive-yq-selector', {
                    url: '/directive-yq-selector',
                    views: { 'content@': {
                        template: require('./demo-directive-yq-selector.html'),
                        controller: function() {
                            this.onSelect = (d) => {
                                this.selected = d;
                            };
                        },
                        bindToController: true,
                        controllerAs: 'ctrl'
                    } }
                })
                .state('main.examples.directive-rating-brush-select', {
                    url: '/directive-rating-brush-select',
                    views: { 'content@': {
                        template: require('./demo-directive-rating-brush-select.html'),
                        controller: function() {
                            this.onSelect = (d) => {
                                this.selected = d;
                            };
                        },
                        bindToController: true,
                        controllerAs: 'ctrl'
                    } }
                })
                .state('main.examples.directive-bookmark-kind-select', {
                    url: '/directive-bookmark-kind-select',
                    views: { 'content@': {
                        template: require('./demo-directive-bookmark-kind-select.html'),
                        controller: function() {
                            this.onSelect = (d) => {
                                this.value = d;
                            };
                        },
                        bindToController: true,
                        controllerAs: 'ctrl'
                    } }
                })
                .state('main.examples.directive-change-timeline', {
                    url: '/directive-change-timeline',
                    views: { 'content@': {
                        template: require('./demo-directive-change-timeline.html'),
                        controller: function() {
                            this.changes = [
                                { year: 2016, quarter: 3, size: 1 },
                                { year: 2016, quarter: 4, size: 2 },
                                { year: 2018, quarter: 1, size: 4 },
                                { year: 2018, quarter: 2, size: 3 }
                            ];
                            this.selected = { year: 2016, quarter: 3 };

                            this.onSelect = (d) => {
                                this.selected = d;
                            };
                        },
                        controllerAs: 'ctrl',
                        bindToController: true
                    } }
                })
                .state('main.examples.directive-rating-group', {
                    url: '/directive-rating-group',
                    views: { 'content@': {
                        template: require('./demo-directive-rating-group.html'),
                        controller: function() {
                            const m1 = { code: 'm1', name: 'M1', description: 'emm1' };
                            const m2 = { code: 'm2', name: 'M2', description: 'emm2' };
                            const c1 = { id: 100, name: 'Cap1' };
                            const c2 = { id: 200, name: 'Cap2' };

                            this.group = {
                                groupRef: { id: 1, kind: 'APPLICATION', name: 'group'},
                                measurables: [ m1, m2 ],
                                capabilities: [ c1, c2 ],
                                raw: [
                                    {
                                        ratings: [
                                            { current: 'R', measurable: 'm1' },
                                            { current: 'A', measurable: 'm2' }
                                        ],
                                        subject: c1
                                    },
                                    {
                                        ratings: [
                                            { current: 'R', measurable: 'm1' },
                                            { current: 'Z', measurable: 'm2' }
                                        ],
                                        subject: c2
                                    }
                                ],
                                summaries: [
                                    { measurable: 'm1', R: 2 },
                                    { measurable: 'm2', A: 1, Z: 1 }
                                ],
                                collapsed: false
                            };
                        },
                        controllerAs: 'ctrl',
                        bindToController: true
                    } }
                })
                .state('main.examples.directive-app-overview', {
                    url: '/directive-app-overview',
                    views: { 'content@': {
                        template: require('./demo-directive-app-overview.html'),
                        controller: ['$scope', function($scope) {
                            $scope.app = {
                                name: 'example app',
                                description: 'blah',
                                aliases: ['aka'],
                                kind: 'IN_HOUSE',
                                lifecyclePhase: 'PRODUCTION'
                            };
                        }]
                    }}
                })
                .state('main.examples.directive-keyword-list', {
                    url: '/directive-keyword-list',
                    views: { 'content@': {
                        template: require('./demo-directive-keyword-list.html'),
                        controller: ['$scope', function($scope) {
                            $scope.clicked = function(keyword) {
                                $scope.selected = keyword;
                            };
                        }]
                    }}
                })
                .state('main.examples.endpoint-application', {
                    url: '/endpoint-application',
                    views: { 'content@': {template: require('./demo-endpoint-application.html') } }
                })
                .state('main.examples.endpoint-data-flows', {
                    url: '/data-flows',
                    views: { 'content@': {template: require('./demo-endpoint-data-flows.html') } }
                })
                .state('main.examples.endpoint-data-types', {
                    url: '/data-types',
                    views: { 'content@': {template: require('./demo-endpoint-data-types.html') } }
                })
                .state('main.examples.endpoint-person', {
                    url: '/person',
                    views: { 'content@': {template: require('./demo-endpoint-person.html') } }
                })
                .state('main.examples.endpoint-server-information', {
                    url: '/server-information',
                    views: { 'content@': {template: require('./demo-endpoint-server-information.html') } }
                })
                .state('main.examples.endpoint-capability', {
                    url: '/capability',
                    views: { 'content@': {template: require('./demo-endpoint-capability.html') } }
                })
                .state('main.examples.endpoint-app-capability', {
                    url: '/app-capability',
                    views: { 'content@': {template: require('./demo-endpoint-app-capability.html') } }
                })
                .state('main.examples.endpoint-organisational-unit', {
                    url: '/organisational-unit',
                    views: { 'content@': {template: require('./demo-endpoint-organisational-unit.html') } }
                })
                .state('main.examples.endpoint-perspective', {
                    url: '/perspective',
                    views: { 'content@': {template: require('./demo-endpoint-perspective.html') } }
                })
                .state('main.examples.endpoint-perspective-measurable', {
                    url: '/perspective-measurable',
                    views: { 'content@': {template: require('./demo-endpoint-perspective-measurable.html') } }
                })
                .state('main.examples.endpoint-involvement', {
                    url: '/involvement',
                    views: { 'content@': {template: require('./demo-endpoint-involvement.html') } }
                })
                .state('main.examples.waltz-roadmap', {
                    url: '/waltz-roadmap',
                    views: { 'content@': {template: require('./waltz-roadmap.html') } }
                })
                .state('main.examples.waltz-engagement', {
                    url: '/waltz-engagement',
                    views: { 'content@': {template: require('./waltz-engagement.html') } }
                })
                .state('main.examples.waltz-prerequisites', {
                    url: '/waltz-prerequisites',
                    views: { 'content@': {template: require('./waltz-prerequisites.html') } }
                });

        }
    ]);

};
