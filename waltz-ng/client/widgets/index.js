/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import angular from "angular";
import ColumnMapper from "./column-mapper/column-mapper.js";
import CurrencyAmount from "./currency-amount";
import SubSection from "./sub-section";
import EditableEnum from "./editable-enum/editable-enum";
import EditableDropdown from "./editable-dropdown/editable-dropdown";
import SpreadsheetLoader from "./spreadsheet-loader/spreadsheet-loader";
import {registerComponents} from "../common/module-utils";
import ChangeTimeline from './change-timeline';
import ErrorAlert from './error-alert';
import ExternalLink from './external-link';
import FromNow from './from-now';
import JumpTo from './jump-to';
import LoadingNotification from './loading-notification';
import MailTo from './mail-to';
import OverlayPanel from './overlay-panel';
import PhoneLink from './phone-link';
import RagIndicator from './rag-indicator';
import Search from './search';
import SectionActions from './section-actions';
import StepCount from './step-count';
import YqSelect from './yq-select';
import BasicInfoTile from './basic-info-tile';
import DataExtractLink from './data-extract-link';
import EditableField from './editable-field';
import Icon from './icon';
import InlineEditArea from './inline-edit-area';
import KeywordList from './keyword-list';
import KeywordEdit from './keyword-edit';
import NoData from './no-data';
import Markdown from './markdown';
import PageHeader from './page-header/page-header';
import Pie from './pie/pie';
import PieSegmentTable from './pie/pie-segment-table';
import PieTable from './pie/pie-table';
import Section from './section';
import SimpleStackChart from './simple-stack-chart';
import Toggle from './toggle';
import Twistie from './twistie';


export default () => {
    const module = angular.module('waltz.widgets', []);

    module.directive('waltzChangeTimeline', ChangeTimeline);
    module.directive('waltzErrorAlert', ErrorAlert);
    module.directive('waltzExternalLink', ExternalLink);
    module.directive('waltzFromNow', FromNow);
    module.directive('waltzJumpTo', JumpTo);

    module.directive('waltzMailTo', MailTo);
    module.directive('waltzOverlayPanel', OverlayPanel);
    module.directive('waltzPhoneLink', PhoneLink);
    module.directive('waltzRagIndicator', RagIndicator);
    module.directive('waltzSearch', Search);
    module.directive('waltzSectionActions', SectionActions);
    module.directive('waltzStepCount', StepCount);
    module.directive('waltzYqSelect', YqSelect);
    module
        .component('waltzBasicInfoTile', BasicInfoTile)
        .component('waltzDataExtractLink', DataExtractLink)
        .component('waltzEditableField', EditableField)
        .component('waltzIcon', Icon)
        .component('waltzInlineEditArea', InlineEditArea)
        .component('waltzKeywordList', KeywordList)
        .component('waltzKeywordEdit', KeywordEdit)
        .component('waltzNoData', NoData)
        .component('waltzMarkdown', Markdown)
        .component('waltzPageHeader', PageHeader)
        .component('waltzPie', Pie)
        .component('waltzPieSegmentTable', PieSegmentTable)
        .component('waltzPieTable', PieTable)
        .component('waltzSection', Section)
        .component('waltzSimpleStackChart', SimpleStackChart)
        .component('waltzToggle', Toggle)
        .component('waltzTwistie', Twistie);

    registerComponents(module, [
        ColumnMapper,
        CurrencyAmount,
        EditableEnum,
        EditableDropdown,
        LoadingNotification,
        SpreadsheetLoader,
        SubSection
    ]);

    return module.name;
};
