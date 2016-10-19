
const template = require('./lineage-panel.html');


const bindings = {
    lineage: '<'
};




function controller() {
    const vm = this;

    vm.$onChanges = () => {

    }
}



const component = {
    controller,
    bindings,
    template
};


export default component;