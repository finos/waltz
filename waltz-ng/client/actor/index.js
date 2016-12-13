/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

export default () => {

    const module = angular.module('waltz.actor', []);

    module
        .config(require('./routes'))
        .service('ActorStore', require('./services/actor-store'))
        .service('ActorService', require('./services/actor-service'));

    module
        .component('waltzActorOverview', require('./components/actor-overview'))
        .component('waltzActorSelector', require('./components/actor-selector'))
        .component('waltzBasicActorSelector', require('./components/basic-actor-selector'));

    return module.name;
};
