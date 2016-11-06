const initialState = {
    changes: [],
    contribution: {
        score: 0,
        directScores: [],
        leaderBoard: []
    },
    directs: [],
    managers: [],
    person: null,
    roles: [],
    user: null
};


controller.$inject = [
    '$stateParams',
    'ChangeLogStore',
    'PersonStore',
    'UserContributionStore',
    'UserStore'
];


function controller($stateParams,
                    changeLogStore,
                    personStore,
                    userContributionStore,
                    userStore) {

    const vm = Object.assign(this, initialState);
    const userId = $stateParams.userId;

    const loadManagerAndDirects = (p) => {
        if (p) {
            const empId = p.employeeId;
            personStore
                .findManagers(empId)
                .then(managers => vm.managers = managers);

            personStore
                .findDirects(empId)
                .then(directs => vm.directs = directs);
        }
    };

    vm.userId = userId;

    const personPromise = personStore
        .findByUserId(userId)
        .then(p => vm.person = p);

    personPromise
        .then(loadManagerAndDirects);


    changeLogStore
        .findForUserName(userId)
        .then(cs => vm.changes = cs);

    userStore
        .findForUserId(userId)
        .then(u => vm.user = u);

    userContributionStore
        .findForUserId(userId)
        .then(score => vm.contribution.score = score);

    userContributionStore
        .findForDirects(userId)
        .then(scores => vm.contribution.directScores = scores);

    userContributionStore
        .getLeaderBoard()
        .then(leaderBoard => vm.contribution.leaderBoard = leaderBoard);

}


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;

