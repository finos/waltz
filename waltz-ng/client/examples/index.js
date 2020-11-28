

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

import angular from "angular";
import Index from "./index.html";
import Toc from "./toc.html";
import DemoDirectiveAppSelector from "./demo-directive-app-selector.html";
import DemoDirectiveBookmarkKindSelect from "./demo-directive-bookmark-kind-select.html";
import DemoDirectiveAppOverview from "./demo-directive-app-overview.html";
import DemoDirectiveKeywordList from "./demo-directive-keyword-list.html";
import DemoEndpointApplication from "./demo-endpoint-application.html";
import DemoEndpointDataFlows from "./demo-endpoint-data-flows.html";
import DemoEndpointDataTypes from "./demo-endpoint-data-types.html";
import DemoEndpointPerson from "./demo-endpoint-person.html";
import DemoEndpointServerInformation from "./demo-endpoint-server-information.html";
import DemoEndpointCapability from "./demo-endpoint-capability.html";
import DemoEndpointAppCapability from "./demo-endpoint-app-capability.html";
import DemoEndpointOrgUnit from "./demo-endpoint-organisational-unit.html";
import DemoEndpointInvolvement from "./demo-endpoint-involvement.html";
import DemoEndpointRoadmap from "./waltz-roadmap.html";
import DemoEndpointEngagement from "./waltz-engagement.html";
import DemoEndpointPrerequisites from "./waltz-prerequisites.html";


export default () => {

    const module = angular.module("waltz.examples", []);

    module.config([
        "$stateProvider",
        ($stateProvider) => {
            $stateProvider
                .state("main.examples", {
                    url: "examples",
                    views: {
                        "docs-content@": { template: Index },
                        "docs-sidebar@": { template: Toc }
                    }
                })
                .state("main.examples.directive-app-selector", {
                    url: "/directive-app-selector",
                    views: { "content@": {template: DemoDirectiveAppSelector } }
                })
                .state("main.examples.directive-bookmark-kind-select", {
                    url: "/directive-bookmark-kind-select",
                    views: { "content@": {
                        template: DemoDirectiveBookmarkKindSelect,
                        controller: function() {
                            this.onSelect = (d) => {
                                this.value = d;
                            };
                        },
                        bindToController: true,
                        controllerAs: "ctrl"
                    } }
                })
                .state("main.examples.directive-app-overview", {
                    url: "/directive-app-overview",
                    views: { "content@": {
                        template: DemoDirectiveAppOverview,
                        controller: ["$scope", function($scope) {
                            $scope.app = {
                                name: "example app",
                                description: "blah",
                                aliases: ["aka"],
                                kind: "IN_HOUSE",
                                lifecyclePhase: "PRODUCTION"
                            };
                        }]
                    }}
                })
                .state("main.examples.directive-keyword-list", {
                    url: "/directive-keyword-list",
                    views: { "content@": {
                        template: DemoDirectiveKeywordList,
                        controller: ["$scope", function($scope) {
                            $scope.clicked = function(keyword) {
                                $scope.selected = keyword;
                            };
                        }]
                    }}
                })
                .state("main.examples.endpoint-application", {
                    url: "/endpoint-application",
                    views: { "content@": {template: DemoEndpointApplication } }
                })
                .state("main.examples.endpoint-data-flows", {
                    url: "/data-flows",
                    views: { "content@": {template: DemoEndpointDataFlows } }
                })
                .state("main.examples.endpoint-data-types", {
                    url: "/data-types",
                    views: { "content@": {template: DemoEndpointDataTypes } }
                })
                .state("main.examples.endpoint-person", {
                    url: "/person",
                    views: { "content@": {template: DemoEndpointPerson } }
                })
                .state("main.examples.endpoint-server-information", {
                    url: "/server-information",
                    views: { "content@": {template: DemoEndpointServerInformation } }
                })
                .state("main.examples.endpoint-capability", {
                    url: "/capability",
                    views: { "content@": {template: DemoEndpointCapability } }
                })
                .state("main.examples.endpoint-app-capability", {
                    url: "/app-capability",
                    views: { "content@": {template: DemoEndpointAppCapability } }
                })
                .state("main.examples.endpoint-organisational-unit", {
                    url: "/organisational-unit",
                    views: { "content@": {template: DemoEndpointOrgUnit } }
                })
                .state("main.examples.endpoint-involvement", {
                    url: "/involvement",
                    views: { "content@": {template: DemoEndpointInvolvement } }
                })
                .state("main.examples.waltz-roadmap", {
                    url: "/waltz-roadmap",
                    views: { "content@": {template: DemoEndpointRoadmap } }
                })
                .state("main.examples.waltz-engagement", {
                    url: "/waltz-engagement",
                    views: { "content@": {template: DemoEndpointEngagement } }
                })
                .state("main.examples.waltz-prerequisites", {
                    url: "/waltz-prerequisites",
                    views: { "content@": {template: DemoEndpointPrerequisites } }
                });
        }
    ]);

    return module.name;

};
