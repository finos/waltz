const template = require('./person-entity-statistic-view.html');


function controller() {
    console.log('hello');
}


const page = {
    controller,
    template,
    controllerAs: 'ctrl'
};


export default page;


// http://localhost:8000/#/entity-statistic/PERSON/mwpHhjMzq/34