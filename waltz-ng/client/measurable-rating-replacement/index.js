import angular from "angular";
import MeasurableRatingReplacementStore from "./services/measurable-rating-replacement-store";
import setup from "./routes"
import {registerStores} from "../common/module-utils";


export default () => {
    const module = angular.module("waltz.measurable-rating.replacement", []);

    module.config(setup);

    registerStores(module, [MeasurableRatingReplacementStore]);

    // registerComponents(module, [
    // ]);

    return module.name;
};