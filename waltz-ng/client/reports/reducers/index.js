import { combineReducers } from 'redux';

import people from './people';
import personPortfolio from './person-portfolio';
import personPortfolioReportConfig from './person-portfolio-report-config';
import orgServerStats from './org-server-stats';


const rootReducer = combineReducers({
    personPortfolio,
    personPortfolioReportConfig,
    people,
    orgServerStats
});

export default rootReducer;
