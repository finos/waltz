function filter(iconNameService) {
    return (value, type) => iconNameService.lookup(type, value);
}


filter.$inject = [
    'IconNameService'
];


export default filter;


