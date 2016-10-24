function filter() {
    return (value, ...extendedValues) => {
        return Object.assign(value, ...extendedValues);
    }
}


export default filter;