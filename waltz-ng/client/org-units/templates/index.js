export default module => {
    module.run([
        '$templateCache',
        ($templateCache) => $templateCache.put('popup-org-unit-name.html', require('./popup-org-unit-name.html'))
    ]);
};
