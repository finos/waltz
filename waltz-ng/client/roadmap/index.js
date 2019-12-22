
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

import AddRoadmap from "./components/add-roadmap/add-roadmap";
import RoadmapHeader from "./components/header/roadmap-header";
import RoadmapReferences from "./components/roadmap-references/roadmap-references";
import RoadmapScenarioConfig from "./components/roadmap-scenario-config/roadmap-scenario-config";
import RoadmapScenarioAxisConfig from "./components/roadmap-scenario-axis-config/roadmap-scenario-axis-config";
import RoadmapScenarioDiagram from "./components/roadmap-scenario-diagram/roadmap-scenario-diagram";
import RoadmapStore from "./services/roadmap-store";
import RoadmapView from "./pages/view/roadmap-view";
import RoadmapList from "./pages/list/roadmap-list";
import Routes from "./routes";

import {registerComponents, registerStores} from "../common/module-utils";


export default () => {
    const module = angular.module("waltz.roadmap", []);

    module.config(Routes);

    registerComponents(module, [
        AddRoadmap,
        RoadmapReferences,
        RoadmapScenarioAxisConfig,
        RoadmapScenarioConfig,
        RoadmapScenarioDiagram,
        RoadmapHeader,
        RoadmapView,
        RoadmapList
    ]);

    registerStores(module, [
        RoadmapStore
    ]);

    return module.name;
};
