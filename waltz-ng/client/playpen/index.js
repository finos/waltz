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

import playpenView1 from "./1/playpen1";
import playpenView2 from "./2/playpen2";
import playpenView3 from "./3/playpen3";
import playpenView4 from "./4/playpen4";

import {registerComponents} from "../common/module-utils";
import list from "./list.html";

export default () => {

    const module = angular.module("waltz.playpen", []);


    module.config([
        "$stateProvider",
        ($stateProvider) => {
            $stateProvider
                .state("main.playpen", {
                    url: "playpen",
                    views: {
                        "content@": { template: list }
                    }
                })
                .state("main.playpen.1", {
                    url: "/1",
                    views: { "content@": playpenView1 }
                })
                .state("main.playpen.2", {
                    url: "/2",
                    views: { "content@": playpenView2 }
                })
                .state("main.playpen.3", {
                    url: "/3?{id:int}",
                    views: { "content@": playpenView3 }
                })
                .state("main.playpen.4", {
                    url: "/4?kind&{id:int}",
                    views: { "content@": playpenView4 }
                });

        }
    ]);

    registerComponents(module, []);
    return module.name;

};
