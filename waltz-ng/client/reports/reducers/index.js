import { combineReducers } from 'redux';
import people from './people';
import personPortfolio from './person-portfolio';
import personPortfolioReportConfig from './person-portfolio-report-config';

const rootReducer = combineReducers({
    personPortfolio,
    personPortfolioReportConfig,
    people
});

export default rootReducer;
