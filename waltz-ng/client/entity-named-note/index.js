/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import angular from 'angular';

import * as entityNamedNoteStore from './services/entity-named-note-store';
import * as entityNamedNoteTypeStore from './services/entity-named-note-type-store';


export default () => {

    const module = angular.module('waltz.entity.named-note', []);

    module
        .service(entityNamedNoteStore.serviceName, entityNamedNoteStore.store)
        .service(entityNamedNoteTypeStore.serviceName, entityNamedNoteTypeStore.store)
        .service('EntityNamedNoteTypeService', require('./services/entity-named-note-type-service'));

    module
        .component('waltzEntityNamedNotesPanel', require('./components/entity-named-notes-panel'));

    return module.name;
};
