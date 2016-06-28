const initialState = {
    contribution: {
        score: 0,
        directScores: [],
        leaderBoard: []
    },
    person: null,
    user: null,
    roles: [],
    changes: []
};


controller.$inject = [
    '$stateParams',
    'ChangeLogDataService',
    'PersonStore',
    'UserContributionStore',
    'UserStore'
];


function controller($stateParams,
                    changeLogStore,
                    personStore,
                    userContributionStore,
                    userStore,
) {

    const vm = Object.assign(this, initialState);
    const userId = $stateParams.userId;

    vm.userId = userId;

    personStore
        .findByUserId(userId)
        .then(p => vm.person = p);

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

