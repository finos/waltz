import moment from 'moment';


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        timestamp: '='
    },
    template: '<span title={{::full}}>{{::fromNow}}</span>',
    link: (scope) => {
        scope.$watch('timestamp', (nv) => {

            const m = moment.utc(nv, 'YYYY-MM-DDThh:mm:ss.SSS');

            scope.fromNow = m.fromNow();
            scope.full = m.local().format('ddd Do MMM YYYY - HH:mm:ss');
        });
    }
});
