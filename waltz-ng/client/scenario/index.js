
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
import {registerComponents, registerStores} from "../common/module-utils";
import ScenarioStore from "./services/scenario-store";
import ScenarioView from "./pages/view/scenario-view";
import ScenarioHeader from "./components/header/scenario-header";
import ScenarioDiagram from "./components/scenario-diagram/scenario-diagram";
import ScenarioList from "./components/list/scenario-list";
import ScenarioListSection from "./components/list-section/scenario-list-section";
import Routes from "./routes";


export default () => {
    const module = angular.module("waltz.scenario", []);

    module
        .config(Routes);

    registerComponents(module, [
        ScenarioDiagram,
        ScenarioHeader,
        ScenarioList,
        ScenarioListSection,
        ScenarioView ]);

    registerStores(module, [ ScenarioStore ]);

    return module.name;
};
