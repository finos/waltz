
export default (module) => {
    module
        .component(
            'waltzEntityHierarchyNavigator',
            require('./components/entity-hierarchy-navigator/entity-hierarchy-navigator'))
        .component(
            'waltzImmediateHierarchyNavigator',
            require('./components/immediate-hierarchy-navigator/immediate-hierarchy-navigator'));
};
