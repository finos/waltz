import _ from "lodash";


const initialState = {

};



function controller() {
    const vm = _.defaultsDeep(this, initialState);
}


controller.$inject = [];


export default () => {
    return {
        restrict: 'E',
        template: require("./navbar.html"),
        controller,
        scope: {},
        controllerAs: 'ctrl'
    };
};
