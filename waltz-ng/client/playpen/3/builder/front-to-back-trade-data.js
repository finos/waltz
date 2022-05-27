const channels = {id: "CHANNELS", name: "Channels / Client Care"};
const cashManagement = {id: "CASH_MGMT", name: "Cash Management"};
const tradeFinance = {id: "TRADE_FINANCE", name: "Trade Finance"};
const securitiesServices = {id: "SECURITIES", name: "Securities Services"};

const surveillance = {id: "SURV", name: "Surveillance", associatedEntities: [{id: 19010, kind: "MEASURABLE"}]};
const analytics = {id: "REPORTING", name: "Analytics & Reporting"};
const controls = {id: "CONTROLS", name: "Controls"};
const billing = {id: "BILLING", name: "Billing"};
const informationManagement = {id: "INFO_MGMT", name: "Information Management"};

const afc = {id: "AFC", name: "Anti Financial Crime"};
const compliance = {id: "COMPLIANCE", name: "Compliance"};
const finance = {id: "FINANCE", name: "Finance"};
const risk = {id: "RISK", name: "Risk"};
const treasury = {id: "TREASURE", name: "Treasury"};

const accessPoints = {
    id: "ACCESS_POINTS",
    name: "Access Points & Business Lines",
    rows: [
        [channels, risk, treasury],
        [cashManagement, tradeFinance, securitiesServices]
    ]
};

const tradeManagement = {
    id: "TRADE_MGMT",
    name: "Trade, Position & Deal Management",
    rows: [
        [surveillance, analytics],
        [billing, informationManagement, controls, risk]
    ]
};

const controlFunctions = {
    id: "CONTROL_FNS",
    name: "Control Functions / Service Integration",
    statsBoxHeight: 60,
    rows: [
        [afc, compliance, finance, risk],
        [treasury, compliance, finance, risk, cashManagement]
    ]
}

export default [accessPoints, tradeManagement, controlFunctions];
