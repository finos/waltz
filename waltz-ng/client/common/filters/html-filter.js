function filter($sce) {
    return (value) => $sce.trustAsHtml(value).valueOf();
};

filter.$inject = ['$sce'];


export default filter;