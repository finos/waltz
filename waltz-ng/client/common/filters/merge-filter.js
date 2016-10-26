function filter() {
    return (value, ...extendedValues) => {
        if (! value) return null;
        return Object.assign(value, ...extendedValues);
    }
}


export default filter;