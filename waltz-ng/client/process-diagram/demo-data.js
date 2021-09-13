export const singlePayments = {
    "objects" : [ {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073232,
        "externalId" : "{9CEA8A08-5540-4f5a-AAEA-C08F5145B087}",
        "topLeft" : {
            "x" : 2216,
            "y" : -202
        },
        "bottomRight" : {
            "x" : 2258,
            "y" : -244
        },
        "name" : "Type of Counterparty?"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1011746,
        "externalId" : "{1489F677-756A-4537-9284-1807B83DCA49}",
        "topLeft" : {
            "x" : 415,
            "y" : -202
        },
        "bottomRight" : {
            "x" : 457,
            "y" : -244
        },
        "name" : "Electronic or Manual Payment?"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 993396,
        "externalId" : "{D68065D1-BB31-4571-90DA-91A03F22ACA1}",
        "topLeft" : {
            "x" : 2061,
            "y" : -202
        },
        "bottomRight" : {
            "x" : 2103,
            "y" : -244
        },
        "name" : "Single Payment Entries"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1213847,
        "externalId" : "{4608C5B0-4FA6-48a4-8899-A1A05C04CC3B}",
        "topLeft" : {
            "x" : 2611,
            "y" : -305
        },
        "bottomRight" : {
            "x" : 2653,
            "y" : -347
        },
        "name" : "Cash Accounting and Billing Entries"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 996158,
        "externalId" : "{25628DC0-6857-410e-94C9-032787872C45}",
        "topLeft" : {
            "x" : 846,
            "y" : -208
        },
        "bottomRight" : {
            "x" : 876,
            "y" : -238
        },
        "name" : "Payment Instruction Available in Single Payment System"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073610,
        "externalId" : "{D786EC8C-1C59-4c28-B6AC-FC88ED3654EA}",
        "topLeft" : {
            "x" : 2863,
            "y" : -315
        },
        "bottomRight" : {
            "x" : 2905,
            "y" : -357
        },
        "name" : "Collate Payment Activities to Complete Delivery"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995647,
        "externalId" : "{9AF7D35C-70D1-4107-9E9C-AE2ADAFE3AF2}",
        "topLeft" : {
            "x" : 1160,
            "y" : -412
        },
        "bottomRight" : {
            "x" : 1278,
            "y" : -485
        },
        "name" : "Verify Funds Availability for Payment Debtor"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995648,
        "externalId" : "{3298CA12-ED8D-4a7c-AF75-EB6479598564}",
        "topLeft" : {
            "x" : 1160,
            "y" : -331
        },
        "bottomRight" : {
            "x" : 1278,
            "y" : -404
        },
        "name" : "Verify Compliance with Embargo & Sanction Requirements"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073233,
        "externalId" : "{E96BA978-6355-4664-9FE3-84F313D87AA4}",
        "topLeft" : {
            "x" : 2537,
            "y" : -202
        },
        "bottomRight" : {
            "x" : 2579,
            "y" : -244
        },
        "name" : "Clearing, Accounting & Billing"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993387,
        "externalId" : "{CC5FFA97-56A5-4c07-A042-7D783A9EC620}",
        "topLeft" : {
            "x" : 1917,
            "y" : -187
        },
        "bottomRight" : {
            "x" : 2035,
            "y" : -260
        },
        "name" : "Generate Single Payment Settlement Entries for Instructing Client"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1203501,
        "externalId" : "{80B0243F-0057-4f4b-B2BC-7ACDCD4EF250}",
        "topLeft" : {
            "x" : 303,
            "y" : -163
        },
        "bottomRight" : {
            "x" : 333,
            "y" : -193
        },
        "name" : "DB is Prepared to Deliver Payments "
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 994998,
        "externalId" : "{134B66C5-4272-4c39-95A0-8C50A0473575}",
        "topLeft" : {
            "x" : 674,
            "y" : -287
        },
        "bottomRight" : {
            "x" : 792,
            "y" : -360
        },
        "name" : "Prepare Electronic Payment Instruction"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995210,
        "externalId" : "{68BD56EE-EE32-4975-8BAD-EFE8AA58FE10}",
        "topLeft" : {
            "x" : 2761,
            "y" : -517
        },
        "bottomRight" : {
            "x" : 2791,
            "y" : -547
        },
        "name" : "Billing Items Available"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1000972,
        "externalId" : "{7D169926-09A2-4bed-95F0-C54309BF38D4}",
        "topLeft" : {
            "x" : 2141,
            "y" : -208
        },
        "bottomRight" : {
            "x" : 2171,
            "y" : -238
        },
        "name" : "Payment Ready for Clearing and Settlement with Counterparty"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1201963,
        "externalId" : "{17658C5F-979E-4ddd-9CBA-6FE4099EC1DD}",
        "topLeft" : {
            "x" : 922,
            "y" : -187
        },
        "bottomRight" : {
            "x" : 1040,
            "y" : -260
        },
        "name" : "Qualify Payment#Single Payment"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1113416,
        "externalId" : "{54C07BEB-C5C3-4db7-96B2-1B9673414499}",
        "topLeft" : {
            "x" : 1160,
            "y" : -492
        },
        "bottomRight" : {
            "x" : 1278,
            "y" : -565
        },
        "name" : "Perform Payment Fraud Checks"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995205,
        "externalId" : "{F789E8CF-B8DA-4d32-8BB2-B3FE0A0E8614}",
        "topLeft" : {
            "x" : 2336,
            "y" : -80
        },
        "bottomRight" : {
            "x" : 2471,
            "y" : -153
        },
        "name" : "Generate Entries for Bank Draft Issuance"
    }, {
        "stereotype" : "NavigationCell",
        "objectType" : "Text",
        "objectId" : 1392196,
        "externalId" : "{EAD98FA4-EB6E-4690-BBE0-E68143AD2EE3}",
        "topLeft" : {
            "x" : 3047,
            "y" : -178
        },
        "bottomRight" : {
            "x" : 3165,
            "y" : -251
        },
        "name" : "?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1202383,
        "externalId" : "{3CA1A03E-66F5-4be8-8D4A-60D7ABD98609}",
        "topLeft" : {
            "x" : 1738,
            "y" : -652
        },
        "bottomRight" : {
            "x" : 1856,
            "y" : -727
        },
        "name" : "Handle Exception during Payment Execution#Single Payment Repair"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995003,
        "externalId" : "{DFF1A178-E9BD-4a0e-94CD-7D041A7D0280}",
        "topLeft" : {
            "x" : 674,
            "y" : -187
        },
        "bottomRight" : {
            "x" : 792,
            "y" : -260
        },
        "name" : "Capture Payment Instruction Manually#Single Payment"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995201,
        "externalId" : "{2AEFAD73-AF8F-4431-AFF8-10FC50BDA109}",
        "topLeft" : {
            "x" : 2336,
            "y" : -187
        },
        "bottomRight" : {
            "x" : 2471,
            "y" : -260
        },
        "name" : "Generate Single Payment Settlement and Clearing Entries for Receiving Clearing Partner"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 993434,
        "externalId" : "{BF26AF03-ED15-4702-B570-14A45F0A50AF}",
        "topLeft" : {
            "x" : 303,
            "y" : -312
        },
        "bottomRight" : {
            "x" : 333,
            "y" : -342
        },
        "name" : "Transaction Released for Cash Settlement"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993452,
        "externalId" : "{40DEBDAC-CF00-4cb0-80A3-6AE43015CE2C}",
        "topLeft" : {
            "x" : 506,
            "y" : -187
        },
        "bottomRight" : {
            "x" : 624,
            "y" : -260
        },
        "name" : "Authenticate Manual Payment Instruction"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 994843,
        "externalId" : "{342D29E1-EE06-4042-A685-D6E9174C56AA}",
        "topLeft" : {
            "x" : 303,
            "y" : -244
        },
        "bottomRight" : {
            "x" : 333,
            "y" : -274
        },
        "name" : "Payment Instruction (File) Received from Client"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995202,
        "externalId" : "{478CCD15-74C4-4d32-A154-5B9A90CC3C62}",
        "topLeft" : {
            "x" : 2336,
            "y" : -290
        },
        "bottomRight" : {
            "x" : 2471,
            "y" : -363
        },
        "name" : "Generate Settlement Entries and Settle Single Payment for Beneficiary"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995447,
        "externalId" : "{F05A3BEB-CF5C-4812-B001-39F1ECC6E98D}",
        "topLeft" : {
            "x" : 2938,
            "y" : -320
        },
        "bottomRight" : {
            "x" : 2968,
            "y" : -350
        },
        "name" : "Payment Delivery Completed"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1261396,
        "externalId" : "{D1995AAE-2D9C-4b59-925B-0AA5F81BB75B}",
        "topLeft" : {
            "x" : 1871,
            "y" : -652
        },
        "bottomRight" : {
            "x" : 1989,
            "y" : -727
        },
        "name" : "Handle Exception During Payment Clearing and Settlement#Single Payment"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1221829,
        "externalId" : "{38048C57-5D69-4fc5-9D05-E5CA7852CE56}",
        "topLeft" : {
            "x" : 1472,
            "y" : -652
        },
        "bottomRight" : {
            "x" : 1590,
            "y" : -727
        },
        "name" : "Handle Exception during Payment Initiation#EB Channel"
    }, {
        "stereotype" : "NavigationCell",
        "objectType" : "Text",
        "objectId" : 1201309,
        "externalId" : "{6C7E4634-A0C3-4f37-A9A4-7810F5A714DC}",
        "topLeft" : {
            "x" : 3047,
            "y" : -494
        },
        "bottomRight" : {
            "x" : 3165,
            "y" : -567
        },
        "name" : "Perform Client Billing"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995208,
        "externalId" : "{7096EFD7-853D-4dd2-AC1B-743EFDAD3E10}",
        "topLeft" : {
            "x" : 2761,
            "y" : -209
        },
        "bottomRight" : {
            "x" : 2791,
            "y" : -239
        },
        "name" : "Single Payment Cleared and Settled"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993414,
        "externalId" : "{481B9744-DD94-4650-BD8B-5C0297280C19}",
        "topLeft" : {
            "x" : 510,
            "y" : -287
        },
        "bottomRight" : {
            "x" : 628,
            "y" : -360
        },
        "name" : "Authenticate Electronic Payment Instruction"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1011822,
        "externalId" : "{41C0162B-3286-491c-9908-42E8EC411844}",
        "topLeft" : {
            "x" : 303,
            "y" : -469
        },
        "bottomRight" : {
            "x" : 333,
            "y" : -501
        },
        "name" : "FX Pre-Deal Instruction Received from Client"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1011821,
        "externalId" : "{ADC46453-8E50-4c3f-A8CF-C2F46D4F5EA7}",
        "topLeft" : {
            "x" : 303,
            "y" : -390
        },
        "bottomRight" : {
            "x" : 333,
            "y" : -421
        },
        "name" : "Time to Initiate Standing Order"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1019318,
        "externalId" : "{0A1D2D06-16D4-4209-A8A0-0036B63AFBBD}",
        "topLeft" : {
            "x" : 1709,
            "y" : -198
        },
        "bottomRight" : {
            "x" : 1751,
            "y" : -248
        },
        "name" : "Currency Conversion needed for Payment?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1337638,
        "externalId" : "{4B4124E1-431E-4981-8529-931A586F0F9F}",
        "topLeft" : {
            "x" : 1605,
            "y" : -652
        },
        "bottomRight" : {
            "x" : 1723,
            "y" : -727
        },
        "name" : "Handle Exception during Payment Initiation#Financial Network"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1211670,
        "externalId" : "{AFBB6459-58AA-48ca-B7E9-317708241FBC}",
        "topLeft" : {
            "x" : 2761,
            "y" : -100
        },
        "bottomRight" : {
            "x" : 2791,
            "y" : -130
        },
        "name" : "Payment Submitted to Cheque Issuance and Printing"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993392,
        "externalId" : "{8FF2BDEA-6594-48f6-AC39-C975C94AB0A3}",
        "topLeft" : {
            "x" : 1759,
            "y" : -290
        },
        "bottomRight" : {
            "x" : 1877,
            "y" : -363
        },
        "name" : "Convert Payment Currency"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995216,
        "externalId" : "{FFC62FBB-1A0A-4c74-ADA1-69967DA4CA40}",
        "topLeft" : {
            "x" : 2610,
            "y" : -188
        },
        "bottomRight" : {
            "x" : 2728,
            "y" : -261
        },
        "name" : "Clear and Settle Single Payment with Receiving Clearing Partner"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 998497,
        "externalId" : "{DDB00C3B-A776-4523-90C0-5C8FC094927D}",
        "topLeft" : {
            "x" : 1548,
            "y" : -187
        },
        "bottomRight" : {
            "x" : 1666,
            "y" : -260
        },
        "name" : "Release Payment for Execution"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1337636,
        "externalId" : "{393C0525-117B-462c-8A23-340986342758}",
        "topLeft" : {
            "x" : 1317,
            "y" : -202
        },
        "bottomRight" : {
            "x" : 1359,
            "y" : -244
        },
        "name" : "Beartrap hold?"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995209,
        "externalId" : "{DB96E4A2-4E7F-4361-B431-EBC2509920DB}",
        "topLeft" : {
            "x" : 2761,
            "y" : -434
        },
        "bottomRight" : {
            "x" : 2791,
            "y" : -464
        },
        "name" : "Cash Accounting Entries Available"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995642,
        "externalId" : "{CE78C1E0-7099-4c84-8EE1-BEB47A2EB603}",
        "topLeft" : {
            "x" : 1359,
            "y" : -273
        },
        "bottomRight" : {
            "x" : 1477,
            "y" : -349
        },
        "name" : "Obtain Authorisation for Outgoing Payment above Threshold (Beartrap)"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073141,
        "externalId" : "{47B6741C-7893-49c9-A506-F6D7C82893B6}",
        "topLeft" : {
            "x" : 1073,
            "y" : -202
        },
        "bottomRight" : {
            "x" : 1115,
            "y" : -244
        },
        "name" : "Payment Filter after Qualification?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995098,
        "externalId" : "{28E39BFC-A181-4bb7-845C-341442AAA0F1}",
        "topLeft" : {
            "x" : 1160,
            "y" : -187
        },
        "bottomRight" : {
            "x" : 1278,
            "y" : -260
        },
        "name" : "Perform Internal and Business Intervention Filtering of Payment"
    } ],
    "connections" : [ {
        "startObjectId" : 1019318,
        "endObjectId" : 1000972,
        "connectorId" : 1536976,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 991295,
        "externalId" : "{0192098C-CBE8-4426-9916-683388E0E51C}",
        "name" : "No Currency Conversion"
    }, {
        "startObjectId" : 998497,
        "endObjectId" : 1019318,
        "connectorId" : 2126194,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1082246,
        "externalId" : "{0022B162-2875-4792-8C6B-F3A596E46355}",
        "name" : ""
    }, {
        "startObjectId" : 995098,
        "endObjectId" : 998497,
        "connectorId" : 2726192,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1138653,
        "externalId" : "{98475F65-0FDB-43f7-BB01-5666855A110F}",
        "name" : ""
    }, {
        "startObjectId" : 994843,
        "endObjectId" : 1372869,
        "connectorId" : 1300682,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062293,
        "externalId" : "{C2C91C2E-449F-4712-9298-AB76240316AD}",
        "name" : ""
    }, {
        "startObjectId" : 995648,
        "endObjectId" : 998497,
        "connectorId" : 2169252,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1088782,
        "externalId" : "{26F5842B-368E-4151-A219-13F73917BE86}",
        "name" : ""
    }, {
        "startObjectId" : 993387,
        "endObjectId" : 993396,
        "connectorId" : 1255523,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062296,
        "externalId" : "{CB612BD3-4363-4e5e-9A1D-5EC80F2E1376}",
        "name" : ""
    }, {
        "startObjectId" : 993392,
        "endObjectId" : 993387,
        "connectorId" : 2169269,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1088779,
        "externalId" : "{E6DD8B85-D7AE-4c9a-947C-FA3F3900A405}",
        "name" : ""
    }, {
        "startObjectId" : 995447,
        "endObjectId" : 1392195,
        "connectorId" : 2559561,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131251,
        "externalId" : "{333CE77B-634C-430d-8896-A729BCBC787C}",
        "name" : ""
    }, {
        "startObjectId" : 1073232,
        "endObjectId" : 995201,
        "connectorId" : 1258289,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 991306,
        "externalId" : "{4234AFC5-C75D-4907-8F41-35391603DDC5}",
        "name" : "Outgoing or Intermediate Payment"
    }, {
        "startObjectId" : 994843,
        "endObjectId" : 1011746,
        "connectorId" : 2540116,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131087,
        "externalId" : "{550FEF69-2219-47a7-9D39-79D81723C046}",
        "name" : ""
    }, {
        "startObjectId" : 995208,
        "endObjectId" : 1073610,
        "connectorId" : 2193065,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131257,
        "externalId" : "{EC3E9505-4597-4e08-B17A-E505C469C2C8}",
        "name" : ""
    }, {
        "startObjectId" : 993392,
        "endObjectId" : 1000972,
        "connectorId" : 2098805,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1074058,
        "externalId" : "{33C2D834-4DF0-45b3-84A7-65DA4CEEB75E}",
        "name" : ""
    }, {
        "startObjectId" : 1011747,
        "endObjectId" : 1118158,
        "connectorId" : 1300686,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 991301,
        "externalId" : "{F0B8C57D-F4DB-4f4e-95B5-1D30A4D0CD01}",
        "name" : "No Beartrap required"
    }, {
        "startObjectId" : 995003,
        "endObjectId" : 996158,
        "connectorId" : 1340244,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 984634,
        "externalId" : "{239F4ED9-B8EA-4e3f-9A6C-AE45C9F87360}",
        "name" : ""
    }, {
        "startObjectId" : 1073141,
        "endObjectId" : 995647,
        "connectorId" : 1259013,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062265,
        "externalId" : "{8A60FFD2-0862-4d9f-8397-B56E2F4B5C20}",
        "name" : "Outgoing Payment only"
    }, {
        "startObjectId" : 1073233,
        "endObjectId" : 995216,
        "connectorId" : 2041500,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062321,
        "externalId" : "{45CFA1A8-DA18-4af2-815C-6FFD80AFEE48}",
        "name" : ""
    }, {
        "startObjectId" : 1019318,
        "endObjectId" : 993387,
        "connectorId" : 2126202,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1082248,
        "externalId" : "{DD66FE83-C9DF-4b73-B3D3-A8BDD63BAD16}",
        "name" : "No Currency Conversion"
    }, {
        "startObjectId" : 1073232,
        "endObjectId" : 995202,
        "connectorId" : 1258290,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 991308,
        "externalId" : "{CEF276BF-933A-4e25-9567-248153020B4C}",
        "name" : "Incoming or Book-to-Book"
    }, {
        "startObjectId" : 1073141,
        "endObjectId" : 995098,
        "connectorId" : 1258106,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062266,
        "externalId" : "{252F3CF2-A6F3-42f5-B4E3-C9D5BEC84E6C}",
        "name" : "always"
    }, {
        "startObjectId" : 1392190,
        "endObjectId" : 1203501,
        "connectorId" : 2559551,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131241,
        "externalId" : "{AF91AEA6-A8C1-4a07-8E7F-0220BF0D88B8}",
        "name" : ""
    }, {
        "startObjectId" : 1073232,
        "endObjectId" : 995205,
        "connectorId" : 1258292,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 991307,
        "externalId" : "{0C025640-86DE-414a-B900-1F9D76C25BA1}",
        "name" : "Outgoing Payment settled as Bank Draft"
    }, {
        "startObjectId" : 995216,
        "endObjectId" : 995208,
        "connectorId" : 1258672,
        "connectorType" : "Dependency",
        "hidden" : true,
        "instanceId" : 984644,
        "externalId" : "{DCA62B1C-A61E-4eb3-A695-F8F6FE85DB2A}",
        "name" : ""
    }, {
        "startObjectId" : 993396,
        "endObjectId" : 1213847,
        "connectorId" : 1300723,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1062325,
        "externalId" : "{2A5D0516-8C18-4285-AE82-AC6A5B4F58C7}",
        "name" : "Cash Accounting Entries and Billing Entries"
    }, {
        "startObjectId" : 995205,
        "endObjectId" : 1211670,
        "connectorId" : 1264617,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 984648,
        "externalId" : "{7458A321-AF43-41f2-B462-30FFF78E4C65}",
        "name" : ""
    }, {
        "startObjectId" : 995447,
        "endObjectId" : 1392196,
        "connectorId" : 2559559,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131250,
        "externalId" : "{973C0387-427D-451f-B4C6-77236AA7FB9D}",
        "name" : ""
    }, {
        "startObjectId" : 995210,
        "endObjectId" : 1201309,
        "connectorId" : 2769707,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1142012,
        "externalId" : "{18AFB53D-85A5-4315-B8FD-73C7AD034019}",
        "name" : ""
    }, {
        "startObjectId" : 993396,
        "endObjectId" : 995209,
        "connectorId" : 2041526,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062327,
        "externalId" : "{5812323F-C395-4b8b-840E-F8AE77297200}",
        "name" : "Cash Accounting Entries"
    }, {
        "startObjectId" : 995216,
        "endObjectId" : 995209,
        "connectorId" : 1258616,
        "connectorType" : "Dependency",
        "hidden" : true,
        "instanceId" : 984616,
        "externalId" : "{A2C13291-E381-4796-B99C-FEE86B1E31D6}",
        "name" : ""
    }, {
        "startObjectId" : 1392193,
        "endObjectId" : 1011821,
        "connectorId" : 2559550,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131240,
        "externalId" : "{BED97414-BBD8-4554-BF08-7211080ACE7D}",
        "name" : ""
    }, {
        "startObjectId" : 993392,
        "endObjectId" : 1000972,
        "connectorId" : 2041530,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1066187,
        "externalId" : "{E3626898-94E6-48dd-8825-FF1239636CA5}",
        "name" : ""
    }, {
        "startObjectId" : 995202,
        "endObjectId" : 1213847,
        "connectorId" : 1536978,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062326,
        "externalId" : "{C114F519-5F22-456e-8BF9-660D74144C34}",
        "name" : ""
    }, {
        "startObjectId" : 1019318,
        "endObjectId" : 1000972,
        "connectorId" : 2126215,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1082274,
        "externalId" : "{E9C23ED7-99A1-4634-B6A0-AACF69C065FC}",
        "name" : "No Currency Conversion"
    }, {
        "startObjectId" : 995209,
        "endObjectId" : 1392197,
        "connectorId" : 2559557,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131249,
        "externalId" : "{907B8D4D-03BB-4aa5-8981-0DC4874A9652}",
        "name" : ""
    }, {
        "startObjectId" : 1019318,
        "endObjectId" : 993392,
        "connectorId" : 2126195,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1082247,
        "externalId" : "{290A5DCD-6ECB-4ae5-A3D3-923239DEB741}",
        "name" : "Currency Conversion Required"
    }, {
        "startObjectId" : 1073233,
        "endObjectId" : 1213847,
        "connectorId" : 2041524,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062322,
        "externalId" : "{0295E028-BA7C-45e3-8956-DD5719FFBE78}",
        "name" : ""
    }, {
        "startObjectId" : 995447,
        "endObjectId" : 1392199,
        "connectorId" : 2559552,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131246,
        "externalId" : "{8ADB11DA-FCBE-40d6-960A-DD704D127551}",
        "name" : ""
    }, {
        "startObjectId" : 993434,
        "endObjectId" : 993452,
        "connectorId" : 1622089,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1000049,
        "externalId" : "{0A1DB6B8-31C1-4318-9B74-4130B4BB9475}",
        "name" : ""
    }, {
        "startObjectId" : 995201,
        "endObjectId" : 995209,
        "connectorId" : 1264203,
        "connectorType" : "Dependency",
        "hidden" : true,
        "instanceId" : 984610,
        "externalId" : "{03023EEA-69C2-419d-B6B1-BC3353EBB203}",
        "name" : ""
    }, {
        "startObjectId" : 1073141,
        "endObjectId" : 1113416,
        "connectorId" : 1705622,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1010986,
        "externalId" : "{9EFC419C-735F-4d39-9A2E-36F03AA1014D}",
        "name" : "always"
    }, {
        "startObjectId" : 763201,
        "endObjectId" : 975161,
        "connectorId" : 904265,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 850620,
        "externalId" : "{93879B45-C663-499f-9E9A-A6F9E1891479}",
        "name" : ""
    }, {
        "startObjectId" : 773603,
        "endObjectId" : 918330,
        "connectorId" : 904268,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 850623,
        "externalId" : "{F210AFDF-CE3A-473f-AFD5-5AE17AD72A34}",
        "name" : ""
    }, {
        "startObjectId" : 993392,
        "endObjectId" : 1000972,
        "connectorId" : 1536977,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 984650,
        "externalId" : "{3D336CA8-C776-475a-9A39-30C62954E769}",
        "name" : ""
    }, {
        "startObjectId" : 1337636,
        "endObjectId" : 995642,
        "connectorId" : 2485635,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131245,
        "externalId" : "{D348D69C-8550-4133-80D0-F398DE5A67CA}",
        "name" : "Beartrap Hold"
    }, {
        "startObjectId" : 993387,
        "endObjectId" : 995209,
        "connectorId" : 1258670,
        "connectorType" : "Dependency",
        "hidden" : true,
        "instanceId" : 984609,
        "externalId" : "{F589A167-C6C4-4ff2-BA51-63358517408F}",
        "name" : ""
    }, {
        "startObjectId" : 1000972,
        "endObjectId" : 995202,
        "connectorId" : 2041529,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1066188,
        "externalId" : "{973B2FCF-E789-422a-B571-02D258E05FDB}",
        "name" : ""
    }, {
        "startObjectId" : 1113416,
        "endObjectId" : 998497,
        "connectorId" : 2169254,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1088785,
        "externalId" : "{3A4B06C7-3151-4f76-A905-66D9D7DCE9BE}",
        "name" : ""
    }, {
        "startObjectId" : 993452,
        "endObjectId" : 995003,
        "connectorId" : 1300698,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 984633,
        "externalId" : "{C103D421-D731-40c8-A1B4-E98CD952DCD2}",
        "name" : ""
    }, {
        "startObjectId" : 995642,
        "endObjectId" : 998497,
        "connectorId" : 2485636,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1108122,
        "externalId" : "{5CCF8D53-68B5-408e-9985-5F2C99B4C216}",
        "name" : ""
    }, {
        "startObjectId" : 993434,
        "endObjectId" : 993414,
        "connectorId" : 1959783,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1047086,
        "externalId" : "{1F5BD173-1E0F-4564-A5BB-E7818A8DD5D1}",
        "name" : ""
    }, {
        "startObjectId" : 1203501,
        "endObjectId" : 1011746,
        "connectorId" : 2540117,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131242,
        "externalId" : "{04FDF87F-7868-4f26-8B12-5CBD34CB1167}",
        "name" : ""
    }, {
        "startObjectId" : 1011746,
        "endObjectId" : 993414,
        "connectorId" : 1991743,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062295,
        "externalId" : "{65B32464-3670-4c35-B56B-0304D85CA9F0}",
        "name" : "Electronic Receipt"
    }, {
        "startObjectId" : 1213847,
        "endObjectId" : 995209,
        "connectorId" : 1390765,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062323,
        "externalId" : "{FB079238-8272-420a-A8BD-8A3E80FC883E}",
        "name" : ""
    }, {
        "startObjectId" : 763201,
        "endObjectId" : 991989,
        "connectorId" : 1453539,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 973869,
        "externalId" : "{2524FABC-FDD5-4830-9E39-87230695F8CB}",
        "name" : ""
    }, {
        "startObjectId" : 995202,
        "endObjectId" : 995209,
        "connectorId" : 1264199,
        "connectorType" : "Dependency",
        "hidden" : true,
        "instanceId" : 984618,
        "externalId" : "{FF642185-738B-4b77-B587-16A02CDC1C73}",
        "name" : ""
    }, {
        "startObjectId" : 1201963,
        "endObjectId" : 1073141,
        "connectorId" : 2013049,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131237,
        "externalId" : "{33FE2C1C-7B2C-410c-844A-9901325D777E}",
        "name" : ""
    }, {
        "startObjectId" : 1337636,
        "endObjectId" : 998497,
        "connectorId" : 2485634,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131244,
        "externalId" : "{3C6C270F-D1EC-470f-8AF7-FDD4ABB3D8BA}",
        "name" : "No Beartrap Rrquired or Beartrap Passed"
    }, {
        "startObjectId" : 994998,
        "endObjectId" : 996158,
        "connectorId" : 2023358,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1056074,
        "externalId" : "{0B40EE9B-C9B1-4952-A0A8-B0C019E329CA}",
        "name" : ""
    }, {
        "startObjectId" : 993396,
        "endObjectId" : 1000972,
        "connectorId" : 1351069,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062297,
        "externalId" : "{A52B2A7F-D425-43a4-953A-FCDB87A95F85}",
        "name" : "Second Party Activities "
    }, {
        "startObjectId" : 995647,
        "endObjectId" : 998497,
        "connectorId" : 2169253,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1088781,
        "externalId" : "{2F958EB9-7D3E-492e-A06D-F41C58F0704F}",
        "name" : ""
    }, {
        "startObjectId" : 993414,
        "endObjectId" : 994998,
        "connectorId" : 2169268,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1088783,
        "externalId" : "{64DD2A93-A51E-4447-946E-DDC913FC7277}",
        "name" : ""
    }, {
        "startObjectId" : 995209,
        "endObjectId" : 1073610,
        "connectorId" : 1772249,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131255,
        "externalId" : "{63803D84-15EC-4f38-8B35-0FC5DF359A50}",
        "name" : ""
    }, {
        "startObjectId" : 1011821,
        "endObjectId" : 993414,
        "connectorId" : 1959784,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1047087,
        "externalId" : "{7554BD82-5213-4454-99C7-47BBB6450AE5}",
        "name" : ""
    }, {
        "startObjectId" : 1211670,
        "endObjectId" : 1073610,
        "connectorId" : 2039900,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131256,
        "externalId" : "{D55BC8E4-A58F-4ac7-9744-A0FA149F5399}",
        "name" : ""
    }, {
        "startObjectId" : 995201,
        "endObjectId" : 1073233,
        "connectorId" : 2041499,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062320,
        "externalId" : "{834277EC-2B6C-499f-A897-AEF1842C9AC4}",
        "name" : ""
    }, {
        "startObjectId" : 995647,
        "endObjectId" : 1203612,
        "connectorId" : 2023373,
        "connectorType" : "ControlFlow",
        "hidden" : true,
        "instanceId" : 1056107,
        "externalId" : "{18BA4D22-5794-4c08-BE94-E4280A1C2CDD}",
        "name" : ""
    }, {
        "startObjectId" : 995216,
        "endObjectId" : 995208,
        "connectorId" : 1537079,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 984645,
        "externalId" : "{5AAFCFA6-F23B-44ce-A2EE-BE8E8DE8E548}",
        "name" : ""
    }, {
        "startObjectId" : 995210,
        "endObjectId" : 1073610,
        "connectorId" : 1541439,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131254,
        "externalId" : "{1E7A1983-C405-492e-A3B9-A23EEE6556F3}",
        "name" : ""
    }, {
        "startObjectId" : 1011746,
        "endObjectId" : 993452,
        "connectorId" : 1535836,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062294,
        "externalId" : "{ADD2AA70-2E1C-42a6-992E-20FD50FEE5CD}",
        "name" : "Manual Receipt"
    }, {
        "startObjectId" : 1213847,
        "endObjectId" : 995210,
        "connectorId" : 1258303,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062324,
        "externalId" : "{2CAEF208-F9F3-4c36-9207-1791ACD5F0D5}",
        "name" : ""
    }, {
        "startObjectId" : 921651,
        "endObjectId" : 946749,
        "connectorId" : 935255,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 886540,
        "externalId" : "{ACA36A15-458C-4fa8-B125-0332AC583015}",
        "name" : ""
    }, {
        "startObjectId" : 996158,
        "endObjectId" : 1201963,
        "connectorId" : 2013046,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131236,
        "externalId" : "{70EF9EBB-935E-4393-89AC-84E4B1B2DDF1}",
        "name" : ""
    }, {
        "startObjectId" : 1073141,
        "endObjectId" : 995648,
        "connectorId" : 1259014,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062267,
        "externalId" : "{730A4035-E452-4d0f-930F-FE23D60DC707}",
        "name" : "always"
    }, {
        "startObjectId" : 1211670,
        "endObjectId" : 1392194,
        "connectorId" : 2559562,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131252,
        "externalId" : "{1F856D08-3865-4129-ACAF-B696C777B8F7}",
        "name" : ""
    }, {
        "startObjectId" : 995098,
        "endObjectId" : 1337636,
        "connectorId" : 2485633,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131243,
        "externalId" : "{7B2EB54E-1521-492c-9959-5D5724AE5E01}",
        "name" : ""
    }, {
        "startObjectId" : 1073610,
        "endObjectId" : 995447,
        "connectorId" : 1541336,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1131253,
        "externalId" : "{95D4D716-8AB2-4e76-A92D-B702DB3B4E0F}",
        "name" : ""
    }, {
        "startObjectId" : 1000972,
        "endObjectId" : 1073232,
        "connectorId" : 2039465,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062269,
        "externalId" : "{01996AEA-6EDC-48ef-8916-5D702D5F7398}",
        "name" : ""
    }, {
        "startObjectId" : 993396,
        "endObjectId" : 995210,
        "connectorId" : 2041527,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1062328,
        "externalId" : "{F0215ED1-3044-4015-930F-CE2D8E23CB13}",
        "name" : "Billing"
    }, {
        "startObjectId" : 1011822,
        "endObjectId" : 993414,
        "connectorId" : 1959785,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1047088,
        "externalId" : "{C960DC99-31B3-4cb3-902A-CA03D3C6E3FE}",
        "name" : ""
    } ]
};


export const afcGovernance ={
    "objects" : [ {
        "stereotype" : "Activity",
        "objectType" : "Activity",
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
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1225617,
        "topLeft" : {
            "x" : 1011,
            "y" : -86
        },
        "bottomRight" : {
            "x" : 1053,
            "y" : -128
        },
        "name" : "?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
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
        "objectType" : "Activity",
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
        "objectType" : "Activity",
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
        "objectType" : "Activity",
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
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1225609,
        "topLeft" : {
            "x" : 92,
            "y" : -205
        },
        "bottomRight" : {
            "x" : 122,
            "y" : -235
        },
        "name" : "Received Notification of Changes by DB House Governance; Received Proposal of Material Changes from Forum Secretary"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
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
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1225608,
        "topLeft" : {
            "x" : 94,
            "y" : -88
        },
        "bottomRight" : {
            "x" : 124,
            "y" : -118
        },
        "name" : "Time for Annual Review / Event Driven Change"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
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
        "objectType" : "Activity",
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
        "objectType" : "Activity",
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
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
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