/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import angular from "angular";
import ColumnMapper from "./column-mapper/column-mapper.js";
import CurrencyAmount from "./currency-amount";
import SubSection from "./sub-section";
import EditableEnum from "./editable-enum/editable-enum";
import EditableDropdown from "./editable-dropdown/editable-dropdown";
import SpreadsheetLoader from "./spreadsheet-loader/spreadsheet-loader";
import {registerComponents} from "../common/module-utils";
import ExternalLink from "./external-link";
import FromNow from "./from-now";
import JumpTo from "./jump-to";
import LoadingNotification from "./loading-notification";
import MailTo from "./mail-to";
import OverlayPanel from "./overlay-panel";
import PhoneLink from "./phone-link";
import RagIndicator from "./rag-indicator";
import Search from "./search";
import SectionActions from "./section-actions";
import StepCount from "./step-count";
import BasicInfoTile from "./basic-info-tile";
import DataExtractLink from "./data-extract-link";
import EditableField from "./editable-field";
import Icon from "./icon";
import InlineEditArea from "./inline-edit-area";
import KeywordList from "./keyword-list";
import KeywordEdit from "./keyword-edit";
import NoData from "./no-data/no-data";
import Markdown from "./markdown";
import PageHeader from "./page-header/page-header";
import Pie from "./pie/pie";
import PieSegmentTable from "./pie/pie-segment-table";
import PieTable from "./pie/pie-table";
import Section from "./section";
import SimpleStackChart from "./simple-stack-chart";
import Toggle from "./toggle";
import Twistie from "./twistie";
import Warning from "./warning/warning"
import SvelteComponent from "./svelte-component";


export default () => {
    const module = angular.module("waltz.widgets", []);

    module.directive("waltzExternalLink", ExternalLink);
    module.directive("waltzFromNow", FromNow);
    module.directive("waltzJumpTo", JumpTo);

    module.directive("waltzMailTo", MailTo);
    module.directive("waltzOverlayPanel", OverlayPanel);
    module.directive("waltzPhoneLink", PhoneLink);
    module.directive("waltzRagIndicator", RagIndicator);
    module.directive("waltzSearch", Search);
    module.directive("waltzSectionActions", SectionActions);
    module.directive("waltzStepCount", StepCount);
    module.directive("waltzSvelteComponent", SvelteComponent);


    module
        .component("waltzBasicInfoTile", BasicInfoTile)
        .component("waltzDataExtractLink", DataExtractLink)
        .component("waltzEditableField", EditableField)
        .component("waltzIcon", Icon)
        .component("waltzInlineEditArea", InlineEditArea)
        .component("waltzKeywordList", KeywordList)
        .component("waltzKeywordEdit", KeywordEdit)
        .component("waltzNoData", NoData)
        .component("waltzMarkdown", Markdown)
        .component("waltzPageHeader", PageHeader)
        .component("waltzPie", Pie)
        .component("waltzPieSegmentTable", PieSegmentTable)
        .component("waltzPieTable", PieTable)
        .component("waltzSection", Section)
        .component("waltzSimpleStackChart", SimpleStackChart)
        .component("waltzToggle", Toggle)
        .component("waltzTwistie", Twistie)
        .component("waltzWarning", Warning);

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
