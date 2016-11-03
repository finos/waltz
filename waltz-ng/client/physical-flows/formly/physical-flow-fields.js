export const transportField = {
    type: 'select',
    key: 'transport',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [],
        label: 'Transport',
        placeholder: '',
        required: true
    }
};


export const frequencyField = {
    type: 'select',
    key: 'frequency',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [],
        label: 'Frequency',
        placeholder: '',
        required: true
    }
};


export const basisOffsetSelectField = {
    type: 'select',
    key: 'basisOffsetSelect',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [
            { code: '-30',  name: 'T-30'},
            { code: '-10',  name: 'T-10'},
            { code: '-7',   name: 'T-7'},
            { code: '-5',   name: 'T-5'},
            { code: '-3',   name: 'T-3'},
            { code: '-1',   name: 'T-1'},
            { code: '0',    name: 'T' },
            { code: '1',    name: 'T+1' },
            { code: '3',    name: 'T+3' },
            { code: '5',    name: 'T+5' },
            { code: '7',    name: 'T+7' },
            { code: '10',   name: 'T+10' },
            { code: '30',   name: 'T+30' },
            { code: 'OTHER', name: 'Other (Please specify)' },
        ],
        label: 'Basis Offset',
        placeholder: '',
        required: true
    }
};


export const basisOffsetInputField = {
    type: 'input',
    key: 'basisOffsetInput',
    templateOptions: {
        label: 'Custom Offset',
        placeholder: 'please specify basis offset in (+/-)n format',
        required: true
    },
    validators: {
        basisOffset: {
            expression: function(viewValue, modelValue) {
                var value = modelValue || viewValue;
                return /^(?:T|t)?((?:\+?|-)\d+)$/.test(value);
            },
            message: '$viewValue + " is not a valid basis offset"'
        }
    },
    hideExpression: 'model.basisOffsetSelect !== "OTHER"'
};