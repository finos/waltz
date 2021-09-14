export const dailyRisk = {
    "objects" : [ {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1123284,
        "externalId" : "{98B8CE71-F7EC-4317-BB5E-5216628E89BF}",
        "name" : "Daily Risk and P&L Managed"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1123582,
        "externalId" : "{9C13891A-E12B-4081-A132-622BD3DC9A42}",
        "name" : "Perform End of Day Risk Sign-off"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1123283,
        "externalId" : "{CB2BB135-184F-463f-A108-DD6DA125FE0C}",
        "name" : "Time to Perform Risk and P&L Management"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1123581,
        "externalId" : "{EB71B97D-08DD-4bfd-AF19-4EDFEFADEFF0}",
        "name" : "Perform Expected P&L Sign-off"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1123583,
        "externalId" : "{053F4909-5EB2-422d-8470-F0D95C36A40F}",
        "name" : "Perform Final P&L Sign-off"
    } ],
    "connections" : [ {
        "startObjectId" : 1123582,
        "endObjectId" : 1123583,
        "connectorId" : 1779949,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1022579,
        "externalId" : "{7B538B66-6602-4b63-9F20-E6BD39E0DA24}",
        "name" : ""
    }, {
        "startObjectId" : 1123583,
        "endObjectId" : 1123284,
        "connectorId" : 1779953,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1022580,
        "externalId" : "{28181B15-A292-4498-A6AF-B908032F294A}",
        "name" : ""
    }, {
        "startObjectId" : 1123581,
        "endObjectId" : 1123582,
        "connectorId" : 1779948,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1022578,
        "externalId" : "{4CC7BDB4-058C-45ad-9329-03DB41E907A6}",
        "name" : ""
    }, {
        "startObjectId" : 1123284,
        "endObjectId" : 1123256,
        "connectorId" : 1779954,
        "connectorType" : "NoteLink",
        "hidden" : false,
        "instanceId" : 1022586,
        "externalId" : "{7CEF52A8-7590-45cd-8DE3-48CA5C1F76B1}",
        "name" : ""
    }, {
        "startObjectId" : 1123283,
        "endObjectId" : 1123581,
        "connectorId" : 1779947,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1022577,
        "externalId" : "{AF981E1B-E164-4da7-B74C-2145B0AF07D2}",
        "name" : ""
    } ],
    "positions" : [ {
        "objectId" : 1123581,
        "topLeft" : {
            "x" : 264,
            "y" : -294
        },
        "bottomRight" : {
            "x" : 374,
            "y" : -354
        }
    }, {
        "objectId" : 1123284,
        "topLeft" : {
            "x" : 891,
            "y" : -306
        },
        "bottomRight" : {
            "x" : 921,
            "y" : -336
        }
    }, {
        "objectId" : 1123283,
        "topLeft" : {
            "x" : 163,
            "y" : -310
        },
        "bottomRight" : {
            "x" : 193,
            "y" : -340
        }
    }, {
        "objectId" : 1123583,
        "topLeft" : {
            "x" : 710,
            "y" : -294
        },
        "bottomRight" : {
            "x" : 820,
            "y" : -354
        }
    }, {
        "objectId" : 1123582,
        "topLeft" : {
            "x" : 487,
            "y" : -294
        },
        "bottomRight" : {
            "x" : 597,
            "y" : -354
        }
    } ]
};


export const singlePayments = {
    "objects" : [ {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 998497,
        "externalId" : "{DDB00C3B-A776-4523-90C0-5C8FC094927D}",
        "name" : "Release Payment for Execution"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993414,
        "externalId" : "{481B9744-DD94-4650-BD8B-5C0297280C19}",
        "name" : "Authenticate Electronic Payment Instruction"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1337638,
        "externalId" : "{4B4124E1-431E-4981-8529-931A586F0F9F}",
        "name" : "Handle Exception during Payment Initiation#Financial Network"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073610,
        "externalId" : "{D786EC8C-1C59-4c28-B6AC-FC88ED3654EA}",
        "name" : "Collate Payment Activities to Complete Delivery"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995210,
        "externalId" : "{68BD56EE-EE32-4975-8BAD-EFE8AA58FE10}",
        "name" : "Billing Items Available"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995208,
        "externalId" : "{7096EFD7-853D-4dd2-AC1B-743EFDAD3E10}",
        "name" : "Single Payment Cleared and Settled"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1019291,
        "externalId" : "{306A2A72-8F0C-4c55-AC87-7537BB15EB1A}",
        "name" : "Payment Investigation Request Received"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1011821,
        "externalId" : "{ADC46453-8E50-4c3f-A8CF-C2F46D4F5EA7}",
        "name" : "Time to Initiate Standing Order"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1337636,
        "externalId" : "{393C0525-117B-462c-8A23-340986342758}",
        "name" : "Beartrap hold?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1201963,
        "externalId" : "{17658C5F-979E-4ddd-9CBA-6FE4099EC1DD}",
        "name" : "Qualify Payment#Single Payment"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073141,
        "externalId" : "{47B6741C-7893-49c9-A506-F6D7C82893B6}",
        "name" : "Payment Filter after Qualification?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995642,
        "externalId" : "{CE78C1E0-7099-4c84-8EE1-BEB47A2EB603}",
        "name" : "Obtain Authorisation for Outgoing Payment above Threshold (Beartrap)"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995647,
        "externalId" : "{9AF7D35C-70D1-4107-9E9C-AE2ADAFE3AF2}",
        "name" : "Verify Funds Availability for Payment Debtor"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1019318,
        "externalId" : "{0A1D2D06-16D4-4209-A8A0-0036B63AFBBD}",
        "name" : "Currency Conversion needed for Payment?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1261396,
        "externalId" : "{D1995AAE-2D9C-4b59-925B-0AA5F81BB75B}",
        "name" : "Handle Exception During Payment Clearing and Settlement#Single Payment"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995003,
        "externalId" : "{DFF1A178-E9BD-4a0e-94CD-7D041A7D0280}",
        "name" : "Capture Payment Instruction Manually#Single Payment"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1262943,
        "externalId" : "{74F8FDB5-8DFA-42c7-8F0A-24CB28D06F1A}",
        "name" : "Payment not Executable"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995216,
        "externalId" : "{FFC62FBB-1A0A-4c74-ADA1-69967DA4CA40}",
        "name" : "Clear and Settle Single Payment with Receiving Clearing Partner"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993452,
        "externalId" : "{40DEBDAC-CF00-4cb0-80A3-6AE43015CE2C}",
        "name" : "Authenticate Manual Payment Instruction"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993387,
        "externalId" : "{CC5FFA97-56A5-4c07-A042-7D783A9EC620}",
        "name" : "Generate Single Payment Settlement Entries for Instructing Client"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995205,
        "externalId" : "{F789E8CF-B8DA-4d32-8BB2-B3FE0A0E8614}",
        "name" : "Generate Entries for Bank Draft Issuance"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073233,
        "externalId" : "{E96BA978-6355-4664-9FE3-84F313D87AA4}",
        "name" : "Clearing, Accounting & Billing"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1000972,
        "externalId" : "{7D169926-09A2-4bed-95F0-C54309BF38D4}",
        "name" : "Payment Ready for Clearing and Settlement with Counterparty"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1011746,
        "externalId" : "{1489F677-756A-4537-9284-1807B83DCA49}",
        "name" : "Electronic or Manual Payment?"
    }, {
        "stereotype" : "NavigationCell",
        "objectType" : "Text",
        "objectId" : 1392196,
        "externalId" : "{EAD98FA4-EB6E-4690-BBE0-E68143AD2EE3}",
        "name" : "?"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 996158,
        "externalId" : "{25628DC0-6857-410e-94C9-032787872C45}",
        "name" : "Payment Instruction Available in Single Payment System"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 993392,
        "externalId" : "{8FF2BDEA-6594-48f6-AC39-C975C94AB0A3}",
        "name" : "Convert Payment Currency"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 993434,
        "externalId" : "{BF26AF03-ED15-4702-B570-14A45F0A50AF}",
        "name" : "Transaction Released for Cash Settlement"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995201,
        "externalId" : "{2AEFAD73-AF8F-4431-AFF8-10FC50BDA109}",
        "name" : "Generate Single Payment Settlement and Clearing Entries for Receiving Clearing Partner"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 994998,
        "externalId" : "{134B66C5-4272-4c39-95A0-8C50A0473575}",
        "name" : "Prepare Electronic Payment Instruction"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1202383,
        "externalId" : "{3CA1A03E-66F5-4be8-8D4A-60D7ABD98609}",
        "name" : "Handle Exception during Payment Execution#Single Payment Repair"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1203501,
        "externalId" : "{80B0243F-0057-4f4b-B2BC-7ACDCD4EF250}",
        "name" : "DB is Prepared to Deliver Payments "
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1221829,
        "externalId" : "{38048C57-5D69-4fc5-9D05-E5CA7852CE56}",
        "name" : "Handle Exception during Payment Initiation#EB Channel"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995202,
        "externalId" : "{478CCD15-74C4-4d32-A154-5B9A90CC3C62}",
        "name" : "Generate Settlement Entries and Settle Single Payment for Beneficiary"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1073232,
        "externalId" : "{9CEA8A08-5540-4f5a-AAEA-C08F5145B087}",
        "name" : "Type of Counterparty?"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 993396,
        "externalId" : "{D68065D1-BB31-4571-90DA-91A03F22ACA1}",
        "name" : "Single Payment Entries"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995098,
        "externalId" : "{28E39BFC-A181-4bb7-845C-341442AAA0F1}",
        "name" : "Perform Internal and Business Intervention Filtering of Payment"
    }, {
        "stereotype" : "NavigationCell",
        "objectType" : "Text",
        "objectId" : 1201309,
        "externalId" : "{6C7E4634-A0C3-4f37-A9A4-7810F5A714DC}",
        "name" : "Perform Client Billing"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1113416,
        "externalId" : "{54C07BEB-C5C3-4db7-96B2-1B9673414499}",
        "name" : "Perform Payment Fraud Checks"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1213847,
        "externalId" : "{4608C5B0-4FA6-48a4-8899-A1A05C04CC3B}",
        "name" : "Cash Accounting and Billing Entries"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 994843,
        "externalId" : "{342D29E1-EE06-4042-A685-D6E9174C56AA}",
        "name" : "Payment Instruction (File) Received from Client"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 995648,
        "externalId" : "{3298CA12-ED8D-4a7c-AF75-EB6479598564}",
        "name" : "Verify Compliance with Embargo & Sanction Requirements"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995447,
        "externalId" : "{F05A3BEB-CF5C-4812-B001-39F1ECC6E98D}",
        "name" : "Payment Delivery Completed"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1011822,
        "externalId" : "{41C0162B-3286-491c-9908-42E8EC411844}",
        "name" : "FX Pre-Deal Instruction Received from Client"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 995209,
        "externalId" : "{DB96E4A2-4E7F-4361-B431-EBC2509920DB}",
        "name" : "Cash Accounting Entries Available"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1211670,
        "externalId" : "{AFBB6459-58AA-48ca-B7E9-317708241FBC}",
        "name" : "Payment Submitted to Cheque Issuance and Printing"
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
        "startObjectId" : 995210,
        "endObjectId" : 1201309,
        "connectorId" : 1992092,
        "connectorType" : "NoteLink",
        "hidden" : false,
        "instanceId" : 1053506,
        "externalId" : "{5EE3AB23-E09B-4f00-9BF7-B02144150278}",
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
        "startObjectId" : 1019291,
        "endObjectId" : 1392198,
        "connectorId" : 2559553,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131247,
        "externalId" : "{2CA794F3-FA23-42e1-850E-0AD00D025146}",
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
        "startObjectId" : 1262943,
        "endObjectId" : 1392198,
        "connectorId" : 2559554,
        "connectorType" : "Dependency",
        "hidden" : false,
        "instanceId" : 1131248,
        "externalId" : "{B63032C7-65D0-40d0-833D-BF76140ED23E}",
        "name" : ""
    }, {
        "startObjectId" : 1011822,
        "endObjectId" : 993414,
        "connectorId" : 1959785,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1047088,
        "externalId" : "{C960DC99-31B3-4cb3-902A-CA03D3C6E3FE}",
        "name" : ""
    } ],
    "positions" : [ {
        "objectId" : 1113416,
        "topLeft" : {
            "x" : 1118,
            "y" : -460
        },
        "bottomRight" : {
            "x" : 1236,
            "y" : -533
        }
    }, {
        "objectId" : 995098,
        "topLeft" : {
            "x" : 1119,
            "y" : -155
        },
        "bottomRight" : {
            "x" : 1237,
            "y" : -228
        }
    }, {
        "objectId" : 1261396,
        "topLeft" : {
            "x" : 2591,
            "y" : -569
        },
        "bottomRight" : {
            "x" : 2709,
            "y" : -644
        }
    }, {
        "objectId" : 1011746,
        "topLeft" : {
            "x" : 374,
            "y" : -170
        },
        "bottomRight" : {
            "x" : 416,
            "y" : -212
        }
    }, {
        "objectId" : 1011822,
        "topLeft" : {
            "x" : 292,
            "y" : -437
        },
        "bottomRight" : {
            "x" : 322,
            "y" : -469
        }
    }, {
        "objectId" : 995447,
        "topLeft" : {
            "x" : 2877,
            "y" : -288
        },
        "bottomRight" : {
            "x" : 2907,
            "y" : -318
        }
    }, {
        "objectId" : 993434,
        "topLeft" : {
            "x" : 292,
            "y" : -280
        },
        "bottomRight" : {
            "x" : 322,
            "y" : -310
        }
    }, {
        "objectId" : 1213847,
        "topLeft" : {
            "x" : 2570,
            "y" : -273
        },
        "bottomRight" : {
            "x" : 2612,
            "y" : -315
        }
    }, {
        "objectId" : 1337638,
        "topLeft" : {
            "x" : 665,
            "y" : -568
        },
        "bottomRight" : {
            "x" : 783,
            "y" : -643
        }
    }, {
        "objectId" : 995202,
        "topLeft" : {
            "x" : 2295,
            "y" : -258
        },
        "bottomRight" : {
            "x" : 2430,
            "y" : -331
        }
    }, {
        "objectId" : 995209,
        "topLeft" : {
            "x" : 2720,
            "y" : -402
        },
        "bottomRight" : {
            "x" : 2750,
            "y" : -432
        }
    }, {
        "objectId" : 1019291,
        "topLeft" : {
            "x" : 2853,
            "y" : -558
        },
        "bottomRight" : {
            "x" : 2883,
            "y" : -588
        }
    }, {
        "objectId" : 1011821,
        "topLeft" : {
            "x" : 292,
            "y" : -358
        },
        "bottomRight" : {
            "x" : 322,
            "y" : -389
        }
    }, {
        "objectId" : 1392196,
        "topLeft" : {
            "x" : 2936,
            "y" : -146
        },
        "bottomRight" : {
            "x" : 3054,
            "y" : -219
        }
    }, {
        "objectId" : 1201309,
        "topLeft" : {
            "x" : 2936,
            "y" : -462
        },
        "bottomRight" : {
            "x" : 3054,
            "y" : -535
        }
    }, {
        "objectId" : 1221829,
        "topLeft" : {
            "x" : 518,
            "y" : -568
        },
        "bottomRight" : {
            "x" : 636,
            "y" : -643
        }
    }, {
        "objectId" : 1202383,
        "topLeft" : {
            "x" : 1810,
            "y" : -569
        },
        "bottomRight" : {
            "x" : 1928,
            "y" : -644
        }
    }, {
        "objectId" : 1203501,
        "topLeft" : {
            "x" : 292,
            "y" : -124
        },
        "bottomRight" : {
            "x" : 322,
            "y" : -154
        }
    }, {
        "objectId" : 995210,
        "topLeft" : {
            "x" : 2720,
            "y" : -485
        },
        "bottomRight" : {
            "x" : 2750,
            "y" : -515
        }
    }, {
        "objectId" : 1201963,
        "topLeft" : {
            "x" : 881,
            "y" : -155
        },
        "bottomRight" : {
            "x" : 999,
            "y" : -228
        }
    }, {
        "objectId" : 1019318,
        "topLeft" : {
            "x" : 1668,
            "y" : -166
        },
        "bottomRight" : {
            "x" : 1710,
            "y" : -216
        }
    }, {
        "objectId" : 1000972,
        "topLeft" : {
            "x" : 2100,
            "y" : -176
        },
        "bottomRight" : {
            "x" : 2130,
            "y" : -206
        }
    }, {
        "objectId" : 993387,
        "topLeft" : {
            "x" : 1876,
            "y" : -155
        },
        "bottomRight" : {
            "x" : 1994,
            "y" : -228
        }
    }, {
        "objectId" : 993452,
        "topLeft" : {
            "x" : 465,
            "y" : -155
        },
        "bottomRight" : {
            "x" : 583,
            "y" : -228
        }
    }, {
        "objectId" : 996158,
        "topLeft" : {
            "x" : 805,
            "y" : -176
        },
        "bottomRight" : {
            "x" : 835,
            "y" : -206
        }
    }, {
        "objectId" : 995208,
        "topLeft" : {
            "x" : 2720,
            "y" : -177
        },
        "bottomRight" : {
            "x" : 2750,
            "y" : -207
        }
    }, {
        "objectId" : 994843,
        "topLeft" : {
            "x" : 292,
            "y" : -202
        },
        "bottomRight" : {
            "x" : 322,
            "y" : -232
        }
    }, {
        "objectId" : 998497,
        "topLeft" : {
            "x" : 1507,
            "y" : -155
        },
        "bottomRight" : {
            "x" : 1625,
            "y" : -228
        }
    }, {
        "objectId" : 995642,
        "topLeft" : {
            "x" : 1318,
            "y" : -241
        },
        "bottomRight" : {
            "x" : 1436,
            "y" : -317
        }
    }, {
        "objectId" : 995003,
        "topLeft" : {
            "x" : 633,
            "y" : -155
        },
        "bottomRight" : {
            "x" : 751,
            "y" : -228
        }
    }, {
        "objectId" : 1073141,
        "topLeft" : {
            "x" : 1032,
            "y" : -170
        },
        "bottomRight" : {
            "x" : 1074,
            "y" : -212
        }
    }, {
        "objectId" : 993392,
        "topLeft" : {
            "x" : 1718,
            "y" : -258
        },
        "bottomRight" : {
            "x" : 1836,
            "y" : -331
        }
    }, {
        "objectId" : 1073232,
        "topLeft" : {
            "x" : 2175,
            "y" : -170
        },
        "bottomRight" : {
            "x" : 2217,
            "y" : -212
        }
    }, {
        "objectId" : 995647,
        "topLeft" : {
            "x" : 1119,
            "y" : -380
        },
        "bottomRight" : {
            "x" : 1237,
            "y" : -453
        }
    }, {
        "objectId" : 993396,
        "topLeft" : {
            "x" : 2020,
            "y" : -170
        },
        "bottomRight" : {
            "x" : 2062,
            "y" : -212
        }
    }, {
        "objectId" : 1337636,
        "topLeft" : {
            "x" : 1276,
            "y" : -170
        },
        "bottomRight" : {
            "x" : 1318,
            "y" : -212
        }
    }, {
        "objectId" : 995201,
        "topLeft" : {
            "x" : 2295,
            "y" : -155
        },
        "bottomRight" : {
            "x" : 2430,
            "y" : -228
        }
    }, {
        "objectId" : 995648,
        "topLeft" : {
            "x" : 1118,
            "y" : -299
        },
        "bottomRight" : {
            "x" : 1236,
            "y" : -372
        }
    }, {
        "objectId" : 995216,
        "topLeft" : {
            "x" : 2569,
            "y" : -156
        },
        "bottomRight" : {
            "x" : 2687,
            "y" : -229
        }
    }, {
        "objectId" : 1073610,
        "topLeft" : {
            "x" : 2802,
            "y" : -283
        },
        "bottomRight" : {
            "x" : 2844,
            "y" : -325
        }
    }, {
        "objectId" : 1073233,
        "topLeft" : {
            "x" : 2496,
            "y" : -170
        },
        "bottomRight" : {
            "x" : 2538,
            "y" : -212
        }
    }, {
        "objectId" : 995205,
        "topLeft" : {
            "x" : 2295,
            "y" : -48
        },
        "bottomRight" : {
            "x" : 2430,
            "y" : -121
        }
    }, {
        "objectId" : 1211670,
        "topLeft" : {
            "x" : 2720,
            "y" : -68
        },
        "bottomRight" : {
            "x" : 2750,
            "y" : -98
        }
    }, {
        "objectId" : 994998,
        "topLeft" : {
            "x" : 633,
            "y" : -255
        },
        "bottomRight" : {
            "x" : 751,
            "y" : -328
        }
    }, {
        "objectId" : 1262943,
        "topLeft" : {
            "x" : 2853,
            "y" : -637
        },
        "bottomRight" : {
            "x" : 2883,
            "y" : -667
        }
    }, {
        "objectId" : 993414,
        "topLeft" : {
            "x" : 469,
            "y" : -255
        },
        "bottomRight" : {
            "x" : 587,
            "y" : -328
        }
    } ]
};


export const afcGovernance = {
    "objects" : [ {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225620,
        "externalId" : "{B4834D68-0F89-41f8-80CB-463AFF8B381D}",
        "name" : "Send Recertification of AFC Goverance Framework to Forum Secretaries"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225613,
        "externalId" : "{4E377E02-02AA-4d86-AB5F-F5EB6E70A42D}",
        "name" : "Respond to AFC COO's Validation Request"
    }, {
        "stereotype" : "IntermediateEvent",
        "objectType" : "Event",
        "objectId" : 1279188,
        "externalId" : "{5F42FA93-CAF0-4296-BC52-29147E7F9AFB}",
        "name" : "L1 Sanctions Alert Received"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1225608,
        "externalId" : "{BC202984-9166-4920-9D45-85E39D3AFF40}",
        "name" : "Time for Annual Review / Event Driven Change"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225612,
        "externalId" : "{E888127E-72A1-49d8-9721-73BB287ACEF0}",
        "name" : "Send Email Validation Request to Forum Secretaries"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225618,
        "externalId" : "{9E275F43-9982-4696-86C7-844B742269FD}",
        "name" : "Perform Recertification"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225615,
        "externalId" : "{8F0C9A4D-94C8-41f8-B993-DA25ADB12E2D}",
        "name" : "Determine if Every Individual Forum Secretary Confirms the Requirements"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225621,
        "externalId" : "{765D3756-E6B2-41d7-8DB9-DA5D50FEC443}",
        "name" : "Escalate to AFC ExCo for Decision"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225610,
        "externalId" : "{80C33B8E-A665-499a-8BC5-C8644836CCD0}",
        "name" : "Document Changes or Proposals and Inform Relevant Stakeholders"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1279187,
        "externalId" : "{2D413FF5-CA57-4d7f-8194-A16B70822F9E}",
        "name" : "Perform Level 1 Alert Generation Sanctions Process"
    }, {
        "stereotype" : "StartEvent",
        "objectType" : "Event",
        "objectId" : 1225609,
        "externalId" : "{F09D3753-A15D-40e0-8944-77F4A4B7755E}",
        "name" : "Received Notification of Changes by DB House Governance; Received Proposal of Material Changes from Forum Secretary"
    }, {
        "stereotype" : "Gateway",
        "objectType" : "Decision",
        "objectId" : 1225617,
        "externalId" : "{1E10530C-B5F2-44f1-858C-695588FF6CF6}",
        "name" : "?"
    }, {
        "stereotype" : "Activity",
        "objectType" : "Activity",
        "objectId" : 1225614,
        "externalId" : "{355949D1-66DF-4971-AA37-0983010984B2}",
        "name" : "Review Response from Forum Secretary"
    } ],
    "connections" : [ {
        "startObjectId" : 1225620,
        "endObjectId" : 1225622,
        "connectorId" : 2099101,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074164,
        "externalId" : "{4754419C-C0BE-4a9e-8ABB-68E175CD2D88}",
        "name" : ""
    }, {
        "startObjectId" : 1225609,
        "endObjectId" : 1225612,
        "connectorId" : 2099092,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074155,
        "externalId" : "{95AF1F59-B84A-44d2-9F3E-98800F65EE6F}",
        "name" : ""
    }, {
        "startObjectId" : 1225617,
        "endObjectId" : 1225618,
        "connectorId" : 2099098,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074161,
        "externalId" : "{E47E9898-0B7B-495f-B08F-551AA73E5119}",
        "name" : "Yes"
    }, {
        "startObjectId" : 1225608,
        "endObjectId" : 1225610,
        "connectorId" : 2099090,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074153,
        "externalId" : "{A85CC865-8FB7-4866-8409-D0BC260E5035}",
        "name" : ""
    }, {
        "startObjectId" : 1279187,
        "endObjectId" : 1279189,
        "connectorId" : 2375128,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1098401,
        "externalId" : "{8FCAAD14-9E66-4269-9EA3-F6237947F8A6}",
        "name" : ""
    }, {
        "startObjectId" : 1225615,
        "endObjectId" : 1225617,
        "connectorId" : 2099096,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074159,
        "externalId" : "{5B51E970-5B39-4291-BB4E-00F224DAAF39}",
        "name" : ""
    }, {
        "startObjectId" : 1225618,
        "endObjectId" : 1225620,
        "connectorId" : 2099099,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074162,
        "externalId" : "{5F32DAFE-AA39-41c9-8DBF-9AEE6926DC64}",
        "name" : ""
    }, {
        "startObjectId" : 1225621,
        "endObjectId" : 1225620,
        "connectorId" : 2099156,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074166,
        "externalId" : "{E6F45EB8-75CD-476f-BAFC-1268333ACC08}",
        "name" : ""
    }, {
        "startObjectId" : 1225612,
        "endObjectId" : 1225613,
        "connectorId" : 2099093,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074156,
        "externalId" : "{E77EFE5E-0536-410d-9029-AB649E78D141}",
        "name" : ""
    }, {
        "startObjectId" : 1225610,
        "endObjectId" : 1225612,
        "connectorId" : 2099091,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074154,
        "externalId" : "{D5946D9B-890D-4b5e-A358-154DA343A580}",
        "name" : ""
    }, {
        "startObjectId" : 1279188,
        "endObjectId" : 1279187,
        "connectorId" : 2375129,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1098400,
        "externalId" : "{21789C77-BB0C-4ebd-9118-DE1767CF0ADA}",
        "name" : ""
    }, {
        "startObjectId" : 1225617,
        "endObjectId" : 1225621,
        "connectorId" : 2099097,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074160,
        "externalId" : "{C8BD6C58-F811-4637-85ED-17BF20BD387B}",
        "name" : "No"
    }, {
        "startObjectId" : 1225614,
        "endObjectId" : 1225615,
        "connectorId" : 2099095,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074158,
        "externalId" : "{BC6B5B4E-42FE-4bb9-87C6-42FE14FADF0E}",
        "name" : ""
    }, {
        "startObjectId" : 1225613,
        "endObjectId" : 1225614,
        "connectorId" : 2099094,
        "connectorType" : "ControlFlow",
        "hidden" : false,
        "instanceId" : 1074157,
        "externalId" : "{F6CEBAF4-C65F-4363-833B-4F6B1EEF5435}",
        "name" : ""
    } ],
    "positions" : [ {
        "objectId" : 1225618,
        "topLeft" : {
            "x" : 1104,
            "y" : -197
        },
        "bottomRight" : {
            "x" : 1214,
            "y" : -257
        }
    }, {
        "objectId" : 1225612,
        "topLeft" : {
            "x" : 348,
            "y" : -77
        },
        "bottomRight" : {
            "x" : 458,
            "y" : -137
        }
    }, {
        "objectId" : 1225614,
        "topLeft" : {
            "x" : 700,
            "y" : -75
        },
        "bottomRight" : {
            "x" : 810,
            "y" : -135
        }
    }, {
        "objectId" : 1279187,
        "topLeft" : {
            "x" : 308,
            "y" : -603
        },
        "bottomRight" : {
            "x" : 418,
            "y" : -663
        }
    }, {
        "objectId" : 1225620,
        "topLeft" : {
            "x" : 1264,
            "y" : -197
        },
        "bottomRight" : {
            "x" : 1374,
            "y" : -257
        }
    }, {
        "objectId" : 1225621,
        "topLeft" : {
            "x" : 1105,
            "y" : -79
        },
        "bottomRight" : {
            "x" : 1215,
            "y" : -139
        }
    }, {
        "objectId" : 1225608,
        "topLeft" : {
            "x" : 94,
            "y" : -88
        },
        "bottomRight" : {
            "x" : 124,
            "y" : -118
        }
    }, {
        "objectId" : 1279188,
        "topLeft" : {
            "x" : 201,
            "y" : -618
        },
        "bottomRight" : {
            "x" : 231,
            "y" : -648
        }
    }, {
        "objectId" : 1225609,
        "topLeft" : {
            "x" : 92,
            "y" : -205
        },
        "bottomRight" : {
            "x" : 122,
            "y" : -235
        }
    }, {
        "objectId" : 1225610,
        "topLeft" : {
            "x" : 177,
            "y" : -74
        },
        "bottomRight" : {
            "x" : 287,
            "y" : -134
        }
    }, {
        "objectId" : 1225613,
        "topLeft" : {
            "x" : 529,
            "y" : -376
        },
        "bottomRight" : {
            "x" : 639,
            "y" : -436
        }
    }, {
        "objectId" : 1225615,
        "topLeft" : {
            "x" : 865,
            "y" : -76
        },
        "bottomRight" : {
            "x" : 975,
            "y" : -136
        }
    }, {
        "objectId" : 1225617,
        "topLeft" : {
            "x" : 1011,
            "y" : -86
        },
        "bottomRight" : {
            "x" : 1053,
            "y" : -128
        }
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