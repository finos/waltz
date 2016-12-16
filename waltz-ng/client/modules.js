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
import "ng-redux";
import "ng-tags-input";
import "satellizer";
import "ui-select";

const dependencies = [
    'ui.bootstrap',
    'ui.router',
    'ui.select',
    'ui.grid',
    'ui.grid.exporter',
    'ui.grid.resizeColumns',
    'ui.grid.selection',
    'ui-notification',
    'ngAnimate',
    'ngSanitize',
    'ngTagsInput',
    'satellizer',
    'LocalStorageModule',
    'ngRedux',
    require('angular-formly'),
    require('angular-formly-templates-bootstrap'),
    'treeControl',
    'angular-loading-bar',

    // -- waltz-modules ---
    require('./access-log')(),
    require('./actor')(),
    require('./alias')(),
    require('./applications')(),
    require('./app-capabilities')(),
    require('./app-groups')(),
    require('./asset-cost')(),
    require('./auth-sources')(),
    require('./bookmarks')(),
    require('./capabilities')(),
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
    require('./examples')(),
    require('./formly')(),
    require('./history')(),
    require('./involvement')(),
    require('./involvement-kind')(),
    require('./lineage')(),
    require('./logical-flow')(),
    require('./logical-flow-decorator')(),
    require('./navbar')(),
    require('./org-units')(),
    require('./orphan')(),
    require('./perspectives')(),
    require('./person')(),
    require('./physical-flow-lineage')(),
    require('./physical-flows')(),
    require('./physical-specifications')(),
    require('./playpen')(),
    require('./process')(),
    require('./profile')(),
    require('./ratings')(),
    require('./server-info')(),
    require('./software-catalog')(),
    require('./source-data-rating')(),
    require('./static-panel')(),
    require('./svg-diagram')(),
    require('./system')(),
    require('./technology')(),
    require('./tour')(),
    require('./traits')(),
    require('./user')(),
    require('./user-contribution')(),
    require('./welcome')(),
    require('./widgets')(),
];


export default dependencies;
