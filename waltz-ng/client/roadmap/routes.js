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

import RoadmapView from "./pages/view/roadmap-view";
import RoadmapList from "./pages/list/roadmap-list";


const baseState = {
};


const viewState = {
    url: "roadmap/{id:int}",
    views: {
        "content@": RoadmapView.id
    }
};


const listState = {
    url: "roadmap",
    views: {
        "content@": RoadmapList.id
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.roadmap", baseState)
        .state("main.roadmap.list", listState)
        .state("main.roadmap.view", viewState);
}

setup.$inject = [
    "$stateProvider"
];


export default setup;