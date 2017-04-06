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

import angular from "angular";

export default () => {

    const module = angular.module('waltz.flow-diagram', []);

    module
        .service('FlowDiagramStore', require('./services/flow-diagram-store'))
        .service('FlowDiagramAnnotationStore', require('./services/flow-diagram-annotation-store'))
        .service('FlowDiagramEntityStore', require('./services/flow-diagram-entity-store'))
        .service('FlowDiagramStateService', require('./services/flow-diagram-state-service'))
        ;

    module
        .component('waltzFlowDiagram', require('./components/diagram/flow-diagram'))
        .component('waltzFlowDiagramEditor', require('./components/editor/flow-diagram-editor'))
        .component('waltzDiagramsPanel', require('./components/diagrams-panel/diagrams-panel'));

    return module.name;
};
