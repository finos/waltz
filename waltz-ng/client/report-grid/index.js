

import angular from "angular";

import ReportGridStore from "./services/report-grid-store";
import ReportGridViewSection from "./components/grid-view-section/report-grid-view-section";

import {registerComponents, registerStores} from "../common/module-utils";


export default () => {
    const module = angular.module("waltz.report-grid", []);

    registerComponents(module, [
        ReportGridViewSection
    ]);

    registerStores(module, [
        ReportGridStore
    ]);

    return module.name;
};
