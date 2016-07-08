const usageKinds = [
    {
        displayName: 'Consumer',
        kind: "CONSUMER",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>incoming</strong> flows'
    }, {
        displayName: 'Distributor',
        kind: "DISTRIBUTOR",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>outgoing</strong> flows'
    }, {
        displayName: 'Modifier',
        kind: "MODIFIER",
        readOnly: false,
        readOnlyDescription: ''
    }, {
        displayName: 'Originator / Manual Entry',
        kind: "ORIGINATOR",
        readOnly: false,
        readOnlyDescription: ''
    }
];


export default usageKinds;