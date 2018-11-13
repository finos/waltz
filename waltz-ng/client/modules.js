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

import "babel-polyfill";
import "angular-animate";
import "angular-loading-bar";
import "angular-local-storage";
import "angular-sanitize";
import "angular-tree-control";
import "angular-ui-notification";
import "angular-ui-grid/ui-grid";
import "angular-ui-router";
import "angular-ui-bootstrap";
import "ng-tags-input";
import "ng-showdown";
import "satellizer";
import "ui-select";

import AngularFormly from "angular-formly";
import AngularFormlyTemplates from "angular-formly-templates-bootstrap";

import AccessLog from "./access-log";
import Actor from "./actor";
import Alias from "./alias";
import Applications from "./applications";
import AppGroups from "./app-groups";
import Assessments from "./assessments";
import AssetCost from "./asset-cost";
import Attestation from "./attestation";
import AuthSources from "./auth-sources";
import Bookmarks from "./bookmarks";
import ChangeInitiative from "./change-initiative";
import Complexity from "./complexity";
import Common_Module from "./common/module";
import ChangeLog from "./change-log";
import DataFlow from "./data-flow";
import DataTypeUsage from "./data-type-usage";
import DataTypes from "./data-types";
import Databases from "./databases";
import DrillGrid from "./drill-grid";
import DynamicSection from "./dynamic-section";
import EndUserApps from "./end-user-apps";
import Entity from "./entity";
import EntityDiagrams from "./entity-diagrams";
import EntityEnum from "./entity-enum";
import EntityNamedNote from "./entity-named-note";
import EntityRelationship from "./entity-relationship";
import EntityStatistics from "./entity-statistics";
import EntitySvgDiagram from "./entity-svg-diagram";
import EntityTags from "./entity-tags";
import EnumValue from "./enum-value";
import Examples from "./examples";
import Extensions from "./extensions";
import FlowDiagram from "./flow-diagram";
import Formly from "./formly";
import History from "./history";
import Involvement from "./involvement";
import InvolvementKind from "./involvement-kind";
import LogicalDataElement from "./logical-data-element";
import LogicalFlow from "./logical-flow";
import LogicalFlowDecorator from "./logical-flow-decorator";
import Measurable from "./measurable";
import MeasurableCategory from "./measurable-category";
import MeasurableRating from "./measurable-rating";
import MeasurableRelationship from "./measurable-relationship";
import Navbar from "./navbar";
import Notification from "./notification";
import OrgUnits from "./org-units";
import Orphan from "./orphan";
import Person from "./person";
import PhysicalFlows from "./physical-flows";
import PhysicalSpecifications from "./physical-specifications";
import PhysicalField from "./physical-field";
import Playpen from "./playpen";
import Playpen5 from "./playpen/5";
import Profile from "./profile";
import Ratings from "./ratings";
import Roadmap from "./roadmap";
import Scenario from "./scenario";
import SharedPreference from "./shared-preference";
import ServerInfo from "./server-info";
import SoftwareCatalog from "./software-catalog";
import SourceDataRating from "./source-data-rating";
import StaticPanel from "./static-panel";
import Survey from "./survey";
import SvgDiagram from "./svg-diagram";
import System from "./system";
import Technology from "./technology";
import TaxonomyManagement from "./taxonomy-management";
import Tour from "./tour";
import User from "./user";
import UserContribution from "./user-contribution";
import Welcome from "./welcome";
import Widgets from "./widgets";

const dependencies = [
    "ui.bootstrap",
    "ui.router",
    "ui.select",
    "ui.grid",
    "ui.grid.autoResize",
    "ui.grid.exporter",
    "ui.grid.resizeColumns",
    "ui.grid.selection",
    "ui-notification",
    "ngAnimate",
    "ngSanitize",
    "ngTagsInput",
    "ng-showdown",
    "satellizer",
    "LocalStorageModule",
    AngularFormly,
    AngularFormlyTemplates,
    "treeControl",
    "angular-loading-bar",

    // -- waltz-modules ---
    AccessLog(),
    Actor(),
    Alias(),
    Applications(),
    AppGroups(),
    Assessments(),
    AssetCost(),
    Attestation(),
    AuthSources(),
    Bookmarks(),
    ChangeInitiative(),
    Complexity(),
    Common_Module(),
    ChangeLog(),
    DataFlow(),
    DataTypeUsage(),
    DataTypes(),
    Databases(),
    DrillGrid(),
    DynamicSection(),
    EndUserApps(),
    Entity(),
    EntityDiagrams(),
    EntityEnum(),
    EntityNamedNote(),
    EntityRelationship(),
    EntityStatistics(),
    EntitySvgDiagram(),
    EntityTags(),
    EnumValue(),
    Examples(),
    Extensions(),
    FlowDiagram(),
    Formly(),
    History(),
    Involvement(),
    InvolvementKind(),
    LogicalDataElement(),
    LogicalFlow(),
    LogicalFlowDecorator(),
    Measurable(),
    MeasurableCategory(),
    MeasurableRating(),
    MeasurableRelationship(),
    Navbar(),
    Notification(),
    OrgUnits(),
    Orphan(),
    Person(),
    PhysicalFlows(),
    PhysicalSpecifications(),
    PhysicalField(),
    Playpen(),
    Playpen5(),
    Profile(),
    Ratings(),
    Roadmap(),
    Scenario(),
    SharedPreference(),
    ServerInfo(),
    SoftwareCatalog(),
    SourceDataRating(),
    StaticPanel(),
    Survey(),
    SvgDiagram(),
    System(),
    Technology(),
    Tour(),
    User(),
    UserContribution(),
    TaxonomyManagement(),
    Welcome(),
    Widgets()
];


export default dependencies;
