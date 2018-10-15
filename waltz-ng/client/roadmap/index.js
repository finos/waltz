
/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import angular from "angular";

import AddRoadmap from "./components/add-roadmap/add-roadmap";
import RoadmapHeader from "./components/header/roadmap-header";
import RoadmapReferences from "./components/roadmap-references/roadmap-references";
import RoadmapScenarioConfig from "./components/roadmap-scenario-config/roadmap-scenario-config";
import RoadmapScenarioAxisConfig from "./components/roadmap-scenario-axis-config/roadmap-scenario-axis-config";
import RoadmapScenarioDiagram from "./components/roadmap-scenario-diagram/roadmap-scenario-diagram";
import RoadmapStore from "./services/roadmap-store";
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
        RoadmapHeader
    ]);

    registerStores(module, [
        RoadmapStore
    ]);

    return module.name;
};
