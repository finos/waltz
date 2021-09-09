export const afcGovernance ={
    "objects" : [ {
        "stereotype" : "IntermediateEvent",
        "objectId" : 1279188,
        "topLeft" : {
            "x" : 201,
            "y" : -618
        },
        "bottomRight" : {
            "x" : 231,
            "y" : -648
        },
        "name" : "L1 Sanctions Alert Received"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225610,
        "topLeft" : {
            "x" : 177,
            "y" : -74
        },
        "bottomRight" : {
            "x" : 287,
            "y" : -134
        },
        "name" : "Document Changes or Proposals and Inform Relevant Stakeholders"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225618,
        "topLeft" : {
            "x" : 1104,
            "y" : -197
        },
        "bottomRight" : {
            "x" : 1214,
            "y" : -257
        },
        "name" : "Perform Recertification"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225614,
        "topLeft" : {
            "x" : 700,
            "y" : -75
        },
        "bottomRight" : {
            "x" : 810,
            "y" : -135
        },
        "name" : "Review Response from Forum Secretary"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225620,
        "topLeft" : {
            "x" : 1264,
            "y" : -197
        },
        "bottomRight" : {
            "x" : 1374,
            "y" : -257
        },
        "name" : "Send Recertification of AFC Goverance Framework to Forum Secretaries"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225615,
        "topLeft" : {
            "x" : 865,
            "y" : -76
        },
        "bottomRight" : {
            "x" : 975,
            "y" : -136
        },
        "name" : "Determine if Every Individual Forum Secretary Confirms the Requirements"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225621,
        "topLeft" : {
            "x" : 1105,
            "y" : -79
        },
        "bottomRight" : {
            "x" : 1215,
            "y" : -139
        },
        "name" : "Escalate to AFC ExCo for Decision"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225613,
        "topLeft" : {
            "x" : 529,
            "y" : -376
        },
        "bottomRight" : {
            "x" : 639,
            "y" : -436
        },
        "name" : "Respond to AFC COO's Validation Request"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1225612,
        "topLeft" : {
            "x" : 348,
            "y" : -77
        },
        "bottomRight" : {
            "x" : 458,
            "y" : -137
        },
        "name" : "Send Email Validation Request to Forum Secretaries"
    }, {
        "stereotype" : "Activity",
        "objectId" : 1279187,
        "topLeft" : {
            "x" : 308,
            "y" : -603
        },
        "bottomRight" : {
            "x" : 418,
            "y" : -663
        },
        "name" : "Perform Level 1 Alert Generation Sanctions Process"
    } ],
    "connections" : [ {
        "startObjectId" : 1225613,
        "endObjectId" : 1225614,
        "connectorId" : 2099094,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074157
    }, {
        "startObjectId" : 1225618,
        "endObjectId" : 1225620,
        "connectorId" : 2099099,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074162
    }, {
        "startObjectId" : 1279187,
        "endObjectId" : 1279189,
        "connectorId" : 2375128,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1098401
    }, {
        "startObjectId" : 1225617,
        "endObjectId" : 1225621,
        "connectorId" : 2099097,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074160
    }, {
        "startObjectId" : 1225621,
        "endObjectId" : 1225620,
        "connectorId" : 2099156,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074166
    }, {
        "startObjectId" : 1225610,
        "endObjectId" : 1225612,
        "connectorId" : 2099091,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074154
    }, {
        "startObjectId" : 1225615,
        "endObjectId" : 1225617,
        "connectorId" : 2099096,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074159
    }, {
        "startObjectId" : 1279188,
        "endObjectId" : 1279187,
        "connectorId" : 2375129,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1098400
    }, {
        "startObjectId" : 1225608,
        "endObjectId" : 1225610,
        "connectorId" : 2099090,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074153
    }, {
        "startObjectId" : 1225617,
        "endObjectId" : 1225618,
        "connectorId" : 2099098,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074161
    }, {
        "startObjectId" : 1225614,
        "endObjectId" : 1225615,
        "connectorId" : 2099095,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074158
    }, {
        "startObjectId" : 1225609,
        "endObjectId" : 1225612,
        "connectorId" : 2099092,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074155
    }, {
        "startObjectId" : 1225620,
        "endObjectId" : 1225622,
        "connectorId" : 2099101,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074164
    }, {
        "startObjectId" : 1225612,
        "endObjectId" : 1225613,
        "connectorId" : 2099093,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074156
    } ]
};


export const platformStrategy = {
    "objects" : [ {
        "stereotype" : "Activity",
        "objectId" : 697676,
        "topLeft" : {
            "x" : 240,
            "y" : -150
        },
        "bottomRight" : {
            "x" : 350,
            "y" : -210
        },
        "name" : "Define Product and Market Priorities"
    }, {
        "stereotype" : "Activity",
        "objectId" : 697678,
        "topLeft" : {
            "x" : 385,
            "y" : -150
        },
        "bottomRight" : {
            "x" : 495,
            "y" : -210
        },
        "name" : "Identify Structural Gaps in  Existing Platform"
    }, {
        "stereotype" : "NavigationCell",
        "objectId" : 1107421,
        "topLeft" : {
            "x" : 952,
            "y" : -150
        },
        "bottomRight" : {
            "x" : 1062,
            "y" : -210
        },
        "name" : "Create the Strategy"
    }, {
        "stereotype" : "NavigationCell",
        "objectId" : 1107420,
        "topLeft" : {
            "x" : 6,
            "y" : -150
        },
        "bottomRight" : {
            "x" : 116,
            "y" : -210
        },
        "name" : "Define the Vision"
    }, {
        "stereotype" : "Activity",
        "objectId" : 697679,
        "topLeft" : {
            "x" : 530,
            "y" : -150
        },
        "bottomRight" : {
            "x" : 640,
            "y" : -210
        },
        "name" : "Create the Target Operating Model"
    }, {
        "stereotype" : "NavigationCell",
        "objectId" : 1107422,
        "topLeft" : {
            "x" : 953,
            "y" : -241
        },
        "bottomRight" : {
            "x" : 1063,
            "y" : -301
        },
        "name" : "Assess Strategy Impact on Platform Model"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectId" : 699815,
        "topLeft" : {
            "x" : 159,
            "y" : -165
        },
        "bottomRight" : {
            "x" : 189,
            "y" : -195
        },
        "name" : "Vision for Deutsche Bank Defined"
    }, {
        "stereotype" : "Activity",
        "objectId" : 697680,
        "topLeft" : {
            "x" : 675,
            "y" : -150
        },
        "bottomRight" : {
            "x" : 785,
            "y" : -210
        },
        "name" : "Approve Strategic Investments and Divestments"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectId" : 699849,
        "topLeft" : {
            "x" : 829,
            "y" : -165
        },
        "bottomRight" : {
            "x" : 859,
            "y" : -195
        },
        "name" : "Organisational Platform Strategy Defined and Communicated"
    } ],
    "connections" : [ {
        "startObjectId" : 697680,
        "endObjectId" : 699849,
        "connectorId" : 660233,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616651
    }, {
        "startObjectId" : 1107420,
        "endObjectId" : 699815,
        "connectorId" : 1691581,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 617121
    }, {
        "startObjectId" : 699849,
        "endObjectId" : 1107422,
        "connectorId" : 1695020,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 771396
    }, {
        "startObjectId" : 699815,
        "endObjectId" : 697676,
        "connectorId" : 660232,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616644
    }, {
        "startObjectId" : 699849,
        "endObjectId" : 1107421,
        "connectorId" : 1694887,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 617120
    }, {
        "startObjectId" : 697678,
        "endObjectId" : 697679,
        "connectorId" : 660230,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616642
    }, {
        "startObjectId" : 697676,
        "endObjectId" : 697678,
        "connectorId" : 660229,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616641
    }, {
        "startObjectId" : 697679,
        "endObjectId" : 697680,
        "connectorId" : 660231,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616643
    } ]
};

export const situationalAppraisal = {
    "objects" : [ {
        "stereotype" : "IntermediateEvent",
        "objectId" : 699811,
        "topLeft" : {
            "x" : 39,
            "y" : -141
        },
        "bottomRight" : {
            "x" : 69,
            "y" : -171
        },
        "name" : "Current Vision and Strategy to be Set or Reviewed"
    }, {
        "stereotype" : "Activity",
        "objectId" : 696921,
        "topLeft" : {
            "x" : 294,
            "y" : -128
        },
        "bottomRight" : {
            "x" : 404,
            "y" : -188
        },
        "name" : "Perform Competitor Analysis"
    }, {
        "stereotype" : "Activity",
        "objectId" : 696922,
        "topLeft" : {
            "x" : 137,
            "y" : -128
        },
        "bottomRight" : {
            "x" : 247,
            "y" : -188
        },
        "name" : "Perform External Appraisal"
    }, {
        "stereotype" : "Activity",
        "objectId" : 696923,
        "topLeft" : {
            "x" : 451,
            "y" : -128
        },
        "bottomRight" : {
            "x" : 561,
            "y" : -188
        },
        "name" : "Perform Internal Appraisal"
    }, {
        "stereotype" : "NavigationCell",
        "objectId" : 1107417,
        "topLeft" : {
            "x" : 842,
            "y" : -127
        },
        "bottomRight" : {
            "x" : 952,
            "y" : -187
        },
        "name" : "Define the Vision"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectId" : 699814,
        "topLeft" : {
            "x" : 765,
            "y" : -143
        },
        "bottomRight" : {
            "x" : 795,
            "y" : -173
        },
        "name" : "Situational Appraisal Performed"
    }, {
        "stereotype" : "Activity",
        "objectId" : 696924,
        "topLeft" : {
            "x" : 608,
            "y" : -128
        },
        "bottomRight" : {
            "x" : 718,
            "y" : -188
        },
        "name" : "Perform Scenario Planning"
    } ],
    "connections" : [ {
        "startObjectId" : 699811,
        "endObjectId" : 696922,
        "connectorId" : 660189,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 741093
    }, {
        "startObjectId" : 699814,
        "endObjectId" : 1107417,
        "connectorId" : 1694909,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 616654
    }, {
        "startObjectId" : 696923,
        "endObjectId" : 696924,
        "connectorId" : 660188,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616606
    }, {
        "startObjectId" : 696922,
        "endObjectId" : 696921,
        "connectorId" : 932632,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 877828
    }, {
        "startObjectId" : 696924,
        "endObjectId" : 699814,
        "connectorId" : 660190,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616648
    }, {
        "startObjectId" : 696921,
        "endObjectId" : 696923,
        "connectorId" : 660186,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 616604
    } ]
};