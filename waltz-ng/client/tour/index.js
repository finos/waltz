
export default (module) => {
    module
        .service('TourService', require('./services/tour-service'))
        .service('TourStore', require('./services/tour-store'));
}