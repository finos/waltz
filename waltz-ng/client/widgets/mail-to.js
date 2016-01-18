
export default () => ({
    restrict: 'E',
    replace: true,
    template: '<a href="mailto:{{person.email}}">{{person.email}} <waltz-icon name="envelope-o"></waltz-icon></a>',
    scope: {
        person: '='
    }
});

