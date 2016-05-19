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
    module.directive('waltzBasicInfoTile', require('./basic-info-tile'));
    module.directive('waltzChangeTimeline', require('./change-timeline'));
    module.directive('waltzDataExtractLink', require('./data-extract-link'));
    module.directive('waltzInlineEditArea', require('./inline-edit-area'));
    module.directive('waltzErrorAlert', require('./error-alert'));
    module.directive('waltzExternalLink', require('./external-link'));
    module.directive('waltzFromNow', require('./from-now'));
    module.directive('waltzIcon', require('./icon'));
    module.directive('waltzJumpTo', require('./jump-to'));
    module.directive('waltzKeywordList', require('./keyword-list'));
    module.directive('waltzLoadingNotification', require('./loading-notification'));
    module.directive('waltzMailTo', require('./mail-to'));
    module.directive('waltzNavSearchResults', require('./nav-search-results'));
    module.directive('waltzOverlayPanel', require('./overlay-panel'));
    module.directive('waltzPageHeader', require('./page-header'));
    module.directive('waltzPhoneLink', require('./phone-link'));
    module.directive('waltzPie', require('./pie'));
    module.directive('waltzPieTable', require('./pie-table'));
    module.directive('waltzRagIndicator', require('./rag-indicator'));
    module.directive('waltzSearch', require('./search'));
    module.directive('waltzSection', require('./section'));
    module.directive('waltzSectionActions', require('./section-actions'));
    module.directive('waltzStepCount', require('./step-count'));
    module.directive('waltzToggle', require('./toggle'));
    module.directive('waltzTwistie', require('./twistie'));
    module.directive('waltzYqSelect', require('./yq-select'));
};
