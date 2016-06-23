const directive = {
    restrict: 'E',
    replace: false,
    scope: {
        keywords: '=',
        onSelect: '&?'
    },
    transclude: {
        empty: '?empty',
        last: '?last'
    },
    template: require('./keyword-list.html')
};


export default () => directive;
