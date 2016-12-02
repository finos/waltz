/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

export default (module) => {

    module.directive('waltzChangeTimeline', require('./change-timeline'));
    module.directive('waltzInlineEditArea', require('./inline-edit-area'));
    module.directive('waltzErrorAlert', require('./error-alert'));
    module.directive('waltzExternalLink', require('./external-link'));
    module.directive('waltzFromNow', require('./from-now'));
    module.directive('waltzJumpTo', require('./jump-to'));
    module.directive('waltzKeywordList', require('./keyword-list'));
    module.directive('waltzLoadingNotification', require('./loading-notification'));
    module.directive('waltzMailTo', require('./mail-to'));

    module.directive('waltzPhoneLink', require('./phone-link'));
    module.directive('waltzRagIndicator', require('./rag-indicator'));
    module.directive('waltzSearch', require('./search'));
    module.directive('waltzSectionActions', require('./section-actions'));
    module.directive('waltzStepCount', require('./step-count'));
    module.directive('waltzToggle', require('./toggle'));
    module.directive('waltzYqSelect', require('./yq-select'));
    module
        .component('waltzBasicInfoTile', require('./basic-info-tile'))
        .component('waltzDataExtractLink', require('./data-extract-link'))
        .component('waltzEditableField', require('./editable-field'))
        .component('waltzIcon', require('./icon'))
        .component('waltzNoData', require('./no-data'))
        .component('waltzOverlayPanel', require('./overlay-panel'))
        .component('waltzPageHeader', require('./page-header'))
        .component('waltzPie', require('./pie'))
        .component('waltzPieSegmentTable', require('./pie-segment-table'))
        .component('waltzPieTable', require('./pie-table'))
        .component('waltzSection', require('./section'))
        .component('waltzSimpleStackChart', require('./simple-stack-chart'))
        .component('waltzTwistie', require('./twistie'));



};
