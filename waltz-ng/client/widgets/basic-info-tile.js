export default  {
    template: require('./basic-info-tile.html'),
    transclude: true,
    bindings: {
        name: '@',
        subName: '@',
        icon: '@',
        description: '@'
    },
};

