/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import playpenView1 from "./1/playpen1";
import playpenView2 from "./2/playpen2";
import playpenView3 from "./3/playpen3";
import playpenView4 from "./4/playpen4";

import {registerComponents} from "../common/module-utils";
import list from "./list.html";
import SvelteComponent from "./svelte-component";

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

    registerComponents(module, [SvelteComponent]);
    return module.name;

};
