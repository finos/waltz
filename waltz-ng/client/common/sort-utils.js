
export function cmp(a, b) {
    return a > b
        ? 1
        : a < b
            ? -1
            : 0;
}

export function refCmp(a, b) {
    return cmp(a.name, b.name);
}