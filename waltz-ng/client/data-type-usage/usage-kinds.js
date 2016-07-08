const usageKinds = [
    {
        displayName: 'Consumer',
        kind: "CONSUMER",
        nameDescription: "A consumer is an application that acquires data from an upstream source",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>incoming</strong> flows'
    }, {
        displayName: 'Distributor',
        kind: "DISTRIBUTOR",
        nameDescription: "A distributor is an application that provides data to one or more downstream applications",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>outgoing</strong> flows'
    }, {
        displayName: 'Modifier',
        kind: "MODIFIER",
        nameDescription: "A modifier is an application that changes the data acquired from an upstream source. Changes may be as a result of an application function or direct manipulation in a GUI",
        readOnly: false,
        readOnlyDescription: ''
    }, {
        displayName: 'Originator / Manual Entry',
        kind: "ORIGINATOR",
        nameDescription: "An originator is an application that captures data via a GUI or other input type (e.g. scanner, smartphone, card reader)",
        readOnly: false,
        readOnlyDescription: ''
    }
];


export default usageKinds;