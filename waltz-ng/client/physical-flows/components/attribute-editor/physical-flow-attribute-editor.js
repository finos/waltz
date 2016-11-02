import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../common";
import {
    transportField,
    frequencyField,
    basisOffsetSelectField,
    basisOffsetInputField
} from "../../formly/physical-flow-fields";


const bindings = {
    current: '<',
    onDismiss: '<',
    onChange: '<'
};


const template = require('./physical-flow-attribute-editor.html');


const initialState = {
    onDismiss: () => console.log("No dismiss handler for physical flow attribute editor"),
    onChange: (atts) => console.log("No dismiss handler for physical flow attribute editor: ", atts)
};


function getBasisOffset(basisOffsetSelect, basisOffsetInput) {
    if(basisOffsetSelect && basisOffsetSelect !== 'OTHER') return basisOffsetSelect;
    else if(basisOffsetInput) {
        const basisOffsetRegex = /^(?:T|t)?((?:\+?|-)\d+)$/g;
        const match = basisOffsetRegex.exec(basisOffsetInput);
        if(match !== null && match[1]) {
            return match[1];
        } else throw "Could not parse basis offset: " + basisOffsetInput;
    }
    else throw "No valid Basis Offset value supplied"
}


function controller(waltzDisplayNameService) {
    const vm = initialiseData(this, initialState);

    transportField.templateOptions.options = waltzDisplayNameService.toOptions('transportKind');
    frequencyField.templateOptions.options = waltzDisplayNameService.toOptions('frequencyKind');

    const fields = [
        {
            className: 'row',
            fieldGroup: [
                { className: 'col-sm-6', fieldGroup: [transportField] },
                { className: 'col-sm-6', fieldGroup: [] }
            ]
        }, {
            className: 'row',
            fieldGroup: [
                { className: 'col-sm-6', fieldGroup: [frequencyField] },
                { className: 'col-sm-6', fieldGroup: [] }
            ]
        }, {
            className: 'row',
            fieldGroup: [
                { className: 'col-sm-6', fieldGroup: [basisOffsetSelectField] },
                { className: 'col-sm-6', fieldGroup: [basisOffsetInputField] }
            ]
        }
    ];

    vm.fields = fields;

    const basisOffsetOptions = _.map(basisOffsetSelectField.templateOptions.options, 'code');

    vm.$onChanges = (changes) => {
        if(vm.current) {
            const isOtherBasisOffset = !_.includes(basisOffsetOptions, vm.current.basisOffset);

            vm.model = {
                transport: vm.current.transport,
                frequency: vm.current.frequency,
                basisOffsetSelect: isOtherBasisOffset ? 'OTHER' : vm.current.basisOffset,
                basisOffsetInput: isOtherBasisOffset ? vm.current.basisOffset : undefined
            };

        }
    };

    vm.onSubmit = () => {
        // get the submitted fields
        const { frequency, transport } = vm.model;
        const basisOffset = getBasisOffset(vm.model.basisOffsetSelect, vm.model.basisOffsetInput);
        invokeFunction(vm.onChange, { frequency, transport, basisOffset });
    };


    vm.onCancel = () => {
        invokeFunction(vm.onDismiss);
    };

}


controller.$inject = [
    'WaltzDisplayNameService'
];


const component = {
    bindings,
    template,
    controller
};


export default component;