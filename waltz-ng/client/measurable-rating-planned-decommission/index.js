import angular from "angular";
import MeasurableRatingPlannedDecommissionStore from "./services/measurable-rating-planned-decommission-store";
import setup from "./routes"
import {registerStores} from "../common/module-utils";


export default () => {
    const module = angular.module("waltz.measurable-rating.planned-decommission", []);

    module.config(setup);

    registerStores(module, [MeasurableRatingPlannedDecommissionStore]);

    return module.name;
};