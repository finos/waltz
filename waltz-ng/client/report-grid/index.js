import angular from "angular";

import ReportGridStore from "./services/report-grid-store";
import ReportGridViewSection from "./components/grid-view-section/report-grid-view-section";
import ReportGridViewPanel from "./components/report-grid-view-panel/report-grid-view-panel";
import ReportGridPicker from "./components/report-grid-picker/report-grid-picker";

import {registerComponents, registerStores} from "../common/module-utils";


export default () => {
    const module = angular.module("waltz.report-grid", []);

    registerComponents(module, [
        ReportGridViewSection,
        ReportGridViewPanel,
        ReportGridPicker
    ]);

    registerStores(module, [
        ReportGridStore
    ]);

    return module.name;
};
