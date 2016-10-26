function filter() {
    return (value) => {
        if (! value) return "T";
        if (value > 0) { return `T+${value}`; }
        if (value < 0) { return `T${value}`; }
        return value;
    }
}

export default filter;