
export const nameField = {
    type: 'input',
    key: 'name',
    templateOptions: {
        label: 'Name',
        placeholder: 'Name of application or service',
        required: true
    }
};


export const assetCodeField = {
    type: 'input',
    key: 'assetCode',
    templateOptions: {
        label: 'Asset Code',
        placeholder: 'Asset code associated with application'
    }
};


export const parentAssetCodeField = {
    type: 'input',
    key: 'parentAssetCode',
    templateOptions: {
        label: 'Parent Asset Code',
        placeholder: 'Optional parent code'
    }
};


export const descriptionField = {
    type: 'textarea',
    key: 'description',
    templateOptions: {
        label: 'Description',
        rows: 9,
        placeholder: 'Name of application or service'
    }
};


export const owningAreaField = {
    type: 'ui-select',
    key: 'organisationalUnitId',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [],
        label: 'Owning Area',
        placeholder: 'Select owning Organisational Unit',
        required: true
    }
};


export const typeField = {
    type: 'select',
    key: 'kind',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [],
        label: 'Type',
        placeholder: 'Type of application',
        required: true
    }
};


export const lifecyclePhaseField = {
    type: 'select',
    key: 'lifecyclePhase',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [],
        label: 'Current Lifecycle Phase',
        placeholder: '',
        required: true
    }
};


export const aliasesField = {
    type: 'tags-input',
    key: 'aliases',
    templateOptions: {
        label: 'Aliases',
        placeholder: 'Add an alias',
        required: false
    }
};


export const tagsField = {
    type: 'tags-input',
    key: 'tags',
    templateOptions: {
        label: 'Additional Tags',
        required: false
    }
};
