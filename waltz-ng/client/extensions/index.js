import angular from 'angular';

import {init} from "./extensions";

export default () => {

    //
    // DO NOT CHANGE THE CONTENTS OF THIS FILE
    //
    // All changes should be made in extensions.js
    //

    const module = angular.module('waltz.client.extensions', []);

    // initialise extensions
    init(module);

    return module.name;
};