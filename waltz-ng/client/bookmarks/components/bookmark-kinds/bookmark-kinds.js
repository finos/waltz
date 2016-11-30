import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../common";
import {bookmarkNames} from "../../../common/services/display_names";
import {bookmarkIconNames} from "../../../common/services/icon_names";


const bindings = {
    bookmarks: '<',
    onSelect: '<'
};


const initialState = {
    bookmarks: [],
    kinds: [],
    currentSelection: null,
    onSelect: (kind) => 'no onSelect handler defined for bookmark-kinds: ' + kind
};


function createKinds(bookmarks = []) {
    const bookmarksByKind = _.groupBy(bookmarks, 'kind');

    return _.chain(_.keys(bookmarkNames))
        .union(_.keys(bookmarkIconNames))
        .sortBy()
        .map(k => ({
            code: k,
            name: bookmarkNames[k] || '?',
            icon: bookmarkIconNames[k] || 'square-o',
            count: bookmarksByKind[k] ? bookmarksByKind[k].length : 0,
            selected: false
        }))
        .value();
}


const template = require('./bookmark-kinds.html');


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.kinds = createKinds(vm.bookmarks);
    };


    const resetCurrentSelection = () => {
        if(vm.currentSelection) vm.currentSelection.selected = false;
    };


    vm.select = (kind) => {
        if(!kind.count) return;

        if(vm.currentSeelction != kind) resetCurrentSelection();

        kind.selected = !kind.selected;
        invokeFunction(vm.onSelect, kind.selected ? kind.code : null);

        vm.currentSelection = kind;
    };


    vm.clearSelection = () => {
        invokeFunction(vm.onSelect, null);
        resetCurrentSelection();
        vm.currentSelection = null;
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;