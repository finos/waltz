/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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
import {registerComponents, registerStores} from "../common/module-utils";
import ActorOverview from "./components/actor-overview";
import ActorStore from './services/actor-store';

import Routes from './routes';
import ActorService from './services/actor-service';
import ActorSelector from './components/actor-selector';
import BasicActorSelector from './components/basic-actor-selector';

export default () => {

    const module = angular.module('waltz.actor', []);

    module
        .config(Routes)
        .service('ActorService', ActorService);

    registerComponents(
        module,
        [ ActorOverview ]);

    registerStores(
        module,
        [ ActorStore ]);

    module
        .component('waltzActorSelector', ActorSelector)
        .component('waltzBasicActorSelector', BasicActorSelector);

    return module.name;
};
