export const STATES = {
    PENDING_APPROVALS: "PENDING_APPROVALS",
    SUBMITTED: "SUBMITTED",
    SOURCE_APPROVED: "SOURCE_APPROVED",
    SOURCE_REJECTED: "SOURCE_REJECTED",
    TARGET_APPROVED: "TARGET_APPROVED",
    TARGET_REJECTED: "TARGET_REJECTED",
    FULLY_APPROVED: "FULLY_APPROVED"
};

export const stateMeta = {
    PENDING_APPROVALS: { label: 'Pending Approvals', color: '#ffc107', icon: 'clock' },
    SOURCE_APPROVED: { label: 'Source Approved', color: '#1976d2', icon: 'check' },
    SOURCE_REJECTED: { label: 'Source Rejected', color: '#d9534f', icon: 'xmark' },
    TARGET_APPROVED: { label: 'Target Approved', color: '#1976d2', icon: 'check' },
    TARGET_REJECTED: { label: 'Target Rejected', color: '#d9534f', icon: 'xmark' },
    FULLY_APPROVED: { label: 'Fully Approved', color: '#25b740', icon: 'check' }
};

export const defaultPermissions = {
    sourceApprover: [],
    targetApprover: []
}

export function safe(val, def = "") {
    return val === null || val === undefined ? def : val;
}

export function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleString();
}

/**
 * Sorts an array of objects by a given field.
 * @param {Array} arr - The array to sort.
 * @param {string|Array} field - The field or path to sort by.
 * @param {'asc'|'desc'} - Sort order.
 * @returns {Array} Sorted array.
 */
export function sortByField(arr, field, order = 'asc') {
    return _.orderBy(arr, [field], [order]);
}