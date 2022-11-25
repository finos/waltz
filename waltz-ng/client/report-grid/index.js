import angular from "angular";

import ReportGridStore from "./services/report-grid-store";
import ReportGridViewSection from "./components/grid-view-section/report-grid-view-section";
import ReportGridView from "./pages/view/report-grid-view";

import {registerComponents, registerStores} from "../common/module-utils";
import Routes from "./routes";


export default () => {
    const module = angular.module("waltz.report-grid", []);

    module
        .config(Routes);

    registerComponents(module, [
        ReportGridViewSection,
        ReportGridView
    ]);

    registerStores(module, [
        ReportGridStore
    ]);

    return module.name;
};
