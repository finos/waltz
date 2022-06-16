const cells = {
    "CORE_PROCESSING": {
        "name": "CORE PROCESSING",
        "id": "CORE_PROCESSING"
    },
    "TRANSACTION_INITIATION": {
        "name": "TRANSACTION INITIATION",
        "id": "TRANSACTION_INITIATION"
    },
    "TRADING": {
        "name": "Trading",
        "id": "TRADING"
    },
    "EVENT_TOPICS": {
        "name": "Event Topics",
        "id": "EVENT_TOPICS"
    },
    "ANALYTICS_AND_REPORTING": {
        "name": "ANALYTICS & REPORTING",
        "id": "ANALYTICS_AND_REPORTING"
    },
    "BUSINESS_INTELLIGENCE_AND_ANALYTICS": {
        "name": "Business Intelligence & Analytics",
        "id": "BUSINESS_INTELLIGENCE_AND_ANALYTICS"
    },
    "BUSINESS_PERFORMANCE_AND_MANAGEMENT": {
        "name": "Business Performance & Management",
        "id": "BUSINESS_PERFORMANCE_AND_MANAGEMENT"
    },
    "CLIENT_MANAGEMENT": {
        "name": "Client Management",
        "id": "CLIENT_MANAGEMENT"
    },
    "CLIENT_SERVICING": {
        "name": "Client Servicing",
        "id": "CLIENT_SERVICING"
    },
    "COMPLIANCE": {
        "name": "Compliance",
        "id": "COMPLIANCE"
    },
    "BANKING_MANAGEMENT": {
        "name": "Banking Management",
        "id": "BANKING_MANAGEMENT"
    },
    "PAYMENTS": {
        "name": "Payments",
        "id": "PAYMENTS"
    },
    "MARKET_REFERENCE_DATA_MANAGEMENT": {
        "name": "Market Reference Data Management",
        "id": "MARKET_REFERENCE_DATA_MANAGEMENT"
    },
    "SALES_AND_ADVISORY": {
        "name": "Sales & Advisory",
        "id": "SALES_AND_ADVISORY"
    },
    "LENDING": {
        "name": "Lending",
        "id": "LENDING"
    },
    "LENDING_H": {
        "name": "Lending",
        "id": "LENDING-H"
    },
    "TRADE_BANKING": {
        "name": "Trade Banking",
        "id": "TRADE_BANKING"
    },
    "FINANCE_CONTROL": {
        "name": "Finance Control",
        "id": "FINANCE_CONTROL"
    },
    "CLIENT_REFERENCE_DATA_MANAGEMENT": {
        "name": "Client Reference Data Management",
        "id": "CLIENT_MANAGEMENT"
    },
    "REFERENCE_DATA_MANAGEMENT": {
        "name": "Reference Data Management",
        "id": "REFERENCE_DATA_MANAGEMENT"
    },
    "TAX_MANAGEMENT": {
        "name": "Tax Management",
        "id": "TAX_MANAGEMENT"
    },
    "TREASURY": {
        "name": "Treasury",
        "id": "TREASURY"
    },
    "FINANCE_PERFORMANCE": {
        "name": "Finance Performance",
        "id": "FINANCE_PERFORMANCE"
    },
    "FINANCIAL_RISK_CONTROL": {
        "name": "Financial Risk Control",
        "id": "FINANCIAL_RISK_CONTROL"
    },
    "NON_FINANCIAL_RISK_CONTROL": {
        "name": "Non-Financial Risk Control",
        "id": "NON_FINANCIAL_RISK_CONTROL"
    },
    "FINANCIAL_MANAGEMENT": {
        "name": "Financial Management",
        "id": "FINANCIAL_MANAGEMENT"
    },
    "CLEARING_AND_SETTLEMENT": {
        "name": "Clearing & Settlement",
        "id": "CLEARING_AND_SETTLEMENT"
    },
    "CLIENT_AND_FUNDS_PORTFOLIO_MANAGEMENT": {
        "name": "Client & Funds Portfolio Management",
        "id": "CLIENT_AND_FUNDS_PORTFOLIO_MANAGEMENT"
    },
    "ANTI_FINANCIAL_CRIME": {
        "name": "Anti-Financial Crime",
        "id": "ANTI_FINANCIAL_CRIME"
    },
    "COLLATERAL_AND_MARGIN": {
        "name": "Collateral & Margin",
        "id": "COLLATERAL_AND_MARGIN"
    },
    "LEGAL": {
        "name": "Legal",
        "id": "LEGAL"
    },
    "HUMAN_RESOURCES": {
        "name": "Human Resources",
        "id": "HUMAN_RESOURCES"
    },
    "MARKET_OPERATIONS": {
        "name": "Market Operations",
        "id": "MARKET_OPERATIONS"
    },
    "RISK_AND_VALUATION": {
        "name": "Risk & Valuation",
        "id": "RISK_AND_VALUATION"
    },
    "RESEARCH": {
        "name": "Research",
        "id": "RESEARCH"
    },
    "TRANSACTION_ACCOUNTING": {
        "name": "Transaction Accounting",
        "id": "TRANSACTION_ACCOUNTING"
    },
    "POSITION_MANAGEMENT": {
        "name": "Position Management",
        "id": "POSITION_MANAGEMENT"
    }
};



const referenceData = {
    id: "REF_DATA",
    name: "Reference Data",
    cellColor: "#a3b2c9",
    headerColor: "#6a7485",
    rows: [
        [
            ns1("REFERENCE_DATA_MANAGEMENT", cells.REFERENCE_DATA_MANAGEMENT),
            ns1("CLIENT_REFERENCE_DATA_MANAGEMENT", cells.CLIENT_REFERENCE_DATA_MANAGEMENT),
            ns1("MARKET_REFERENCE_DATA_MANAGEMENT", cells.MARKET_REFERENCE_DATA_MANAGEMENT)
        ]
    ]
};

const transactionInitiation = {
    id: "TRANSACTION_INITIATION",
    name: "Transaction Initiation",
    cellColor: "#abd5d5",
    headerColor: "#6e8d8d",
    rows: [
        ns("TRANSACTION_INITIATION", [cells.TRADING, cells.LENDING, cells.BANKING_MANAGEMENT, cells.TRADE_BANKING]),
        ns("TRANSACTION_INITIATION", [cells.SALES_AND_ADVISORY, cells.CLIENT_MANAGEMENT, cells.CLIENT_SERVICING, cells.PAYMENTS])
    ]
};

const coreProcessing = {
    id: "CORE_PROCESSING",
    name: "Core Processing",
    cellColor: "#efdfc4",
    headerColor: "#9d9179",
    rows: [
        ns("CORE_PROCESSING", [cells.CLIENT_SERVICING, cells.CLIENT_MANAGEMENT, cells.CLIENT_AND_FUNDS_PORTFOLIO_MANAGEMENT, cells.SALES_AND_ADVISORY]),
        ns("CORE_PROCESSING", [cells.TRADING, cells.RISK_AND_VALUATION]),
        ns("CORE_PROCESSING", [cells.HUMAN_RESOURCES, cells.LEGAL]),
        ns("CORE_PROCESSING", [cells.ANTI_FINANCIAL_CRIME, cells.COMPLIANCE])
    ]
};

const coreProcessingOperations = {
    id: "CORE_PROCESSING_OPERATIONS",
    name: "Core Processing (Operations)",
    cellColor: "#efdfc4",
    headerColor: "#9d9179",
    rows: [
        ns("CORE_PROCESSING", [cells.MARKET_OPERATIONS, cells.CLEARING_AND_SETTLEMENT, cells.PAYMENTS]),
        ns("CORE_PROCESSING", [cells.COLLATERAL_AND_MARGIN, cells.POSITION_MANAGEMENT, cells.LENDING_H]),
        ns("CORE_PROCESSING", [cells.BANKING_MANAGEMENT])
    ]
};

const coreProcessingFinance = {
    id: "CORE_PROCESSING_FINANCE",
    name: "Core Processing (Finance)",
    cellColor: "#efdfc4",
    headerColor: "#9d9179",
    rows: [
        ns("CORE_PROCESSING", [cells.FINANCE_CONTROL, cells.FINANCIAL_RISK_CONTROL, cells.FINANCIAL_MANAGEMENT, cells.TRANSACTION_ACCOUNTING]),
        ns("CORE_PROCESSING", [cells.FINANCE_PERFORMANCE, cells.NON_FINANCIAL_RISK_CONTROL, cells.TREASURY, cells.TAX_MANAGEMENT])
    ]
};

function ns(namespace, cells) {
    return _.map(cells, c => ns1(namespace, c));
}


function ns1(namespace, cell) {
    return Object.assign({}, cell, {id: namespace + "_" + cell.id});
}

const analyticsAndReporting = {
    id: "ANALYTICS_REPORTING",
    name: "Analytics and Reporting",
    cellColor: "#FECBC6",
    headerColor: "#a87d7a",
    rows: [
        ns("ANALYTICS_AND_REPORTING", [cells.RESEARCH, cells.COMPLIANCE, cells.FINANCE_CONTROL]),
        ns("ANALYTICS_AND_REPORTING", [cells.TREASURY, cells.TAX_MANAGEMENT]),
        ns("ANALYTICS_AND_REPORTING", [cells.BUSINESS_INTELLIGENCE_AND_ANALYTICS, cells.BUSINESS_PERFORMANCE_AND_MANAGEMENT, cells.CLIENT_SERVICING, cells.CLIENT_MANAGEMENT])
    ]
};


export default [referenceData,
    transactionInitiation,
    coreProcessing,
    coreProcessingOperations, coreProcessingFinance, analyticsAndReporting];
