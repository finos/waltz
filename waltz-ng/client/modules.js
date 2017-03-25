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

import "angular-animate";
import "angular-loading-bar";
import "angular-local-storage";
import "angular-sanitize";
import "angular-tree-control";
import "angular-ui-notification";
import "angular-ui-grid/ui-grid";
import "angular-ui-router";
import "angular-ui-bootstrap";
import "babel-core/polyfill";
import "ng-tags-input";
import "ng-showdown";
import "satellizer";
import "ui-select";

const dependencies = [
    'ui.bootstrap',
    'ui.router',
    'ui.select',
    'ui.grid',
    'ui.grid.autoResize',
    'ui.grid.exporter',
    'ui.grid.resizeColumns',
    'ui.grid.selection',
    'ui-notification',
    'ngAnimate',
    'ngSanitize',
    'ngTagsInput',
    'ng-showdown',
    'satellizer',
    'LocalStorageModule',
    require('angular-formly'),
    require('angular-formly-templates-bootstrap'),
    'treeControl',
    'angular-loading-bar',

    // -- waltz-modules ---
    require('./access-log')(),
    require('./actor')(),
    require('./alias')(),
    require('./applications')(),
    require('./app-groups')(),
    require('./asset-cost')(),
    require('./attestation')(),
    require('./auth-sources')(),
    require('./bookmarks')(),
    require('./change-initiative')(),
    require('./complexity')(),
    require('./common/module')(),
    require('./change-log')(),
    require('./data-type-usage')(),
    require('./data-types')(),
    require('./databases')(),
    require('./end-user-apps')(),
    require('./entity')(),
    require('./entity-statistics')(),
    require('./entity-tags')(),
    require('./examples')(),
    require('./flow-diagram')(),
    require('./formly')(),
    require('./history')(),
    require('./involvement')(),
    require('./involvement-kind')(),
    require('./lineage')(),
    require('./logical-flow')(),
    require('./logical-flow-decorator')(),
    require('./measurable')(),
    require('./measurable-category')(),
    require('./measurable-rating')(),
    require('./navbar')(),
    require('./org-units')(),
    require('./orphan')(),
    require('./person')(),
    require('./perspective')(),
    require('./physical-flow-lineage')(),
    require('./physical-flows')(),
    require('./physical-specifications')(),
    require('./playpen')(),
    require('./profile')(),
    require('./ratings')(),
    require('./server-info')(),
    require('./software-catalog')(),
    require('./source-data-rating')(),
    require('./static-panel')(),
    require('./survey')(),
    require('./svg-diagram')(),
    require('./system')(),
    require('./technology')(),
    require('./tour')(),
    require('./user')(),
    require('./user-contribution')(),
    require('./welcome')(),
    require('./widgets')(),
];


export default dependencies;
