export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        keywords: '=',
        onSelect: '&?'
    },
    template: require('./keyword-list.html')
});
