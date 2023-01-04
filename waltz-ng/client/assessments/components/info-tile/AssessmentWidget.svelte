<script>

    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import {determineDownwardsScopeForKind, mkSelectionOptions} from "../../../common/selector-utils";
    import _ from "lodash";
    import AssessmentRatingGraph from "./AssessmentRatingGraph.svelte";
    import {assessmentDefinitionStore} from "../../../svelte-stores/assessment-definition";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {userPreferenceStore} from "../../../svelte-stores/user-preference-store";
    import {getIdsFromString} from "../../assessment-utils";
    import {
        favouriteAssessmentDefinitionIdsKey,
        lastViewedAssessmentInfoTileKey,
        mkAssessmentDefinitionsIdsBaseKey
    } from "../../../user";
    import {
        defaultPrimaryList,
        favouriteExcludedIds,
        favouriteIds,
        favouriteIncludedIds
    } from "./assessment-widget-store";
    import DropdownPicker
        from "../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import {determineIndexOfNextItemInList, determineIndexOfPreviousItemInList} from "../../../common/list-utils";
    import {getSymbol} from "../../../common/svg-icon";

    export let primaryEntityRef;
    export let filters;

    let dimensions = {
        svg: {
            height: 200,
            width: 300
        },
        padding: {
            left: 40,
            right: 40,
            bar: 5,
            header: 10
        },
        bar: {
            height: 15,
        },
        label: {
            width: 80,
            height: 10
        },
        header: {
            height: 12,
            button: 8
        }
    };

    const favouriteIncludedKey = `${favouriteAssessmentDefinitionIdsKey}.application.included`;
    const favouriteExcludedKey = `${favouriteAssessmentDefinitionIdsKey}.application.excluded`;

    let assessmentSummaryCall;
    let selectedDefnId;
    let showAssessmentPicker = false;
    let initialPageLoad = true;

    function moveRight() {
        const currentIndex = _.findIndex(sortedDefinitions, d => d.id === selectedDefnId);
        const nextIndex = determineIndexOfNextItemInList(sortedDefinitions, currentIndex);
        selectedDefnId = _.get(sortedDefinitions, [nextIndex, "id"], selectedDefnId);
        saveLastViewedPreference(selectedDefnId);
    }

    function moveLeft() {
        const currentIndex = _.findIndex(sortedDefinitions, d => d.id === selectedDefnId);
        const nextIndex = determineIndexOfPreviousItemInList(sortedDefinitions, currentIndex);
        selectedDefnId = _.get(sortedDefinitions, [nextIndex, "id"], selectedDefnId);
        saveLastViewedPreference(selectedDefnId);
    }

    function changeAssessment() {
        showAssessmentPicker = true;
    }

    function saveLastViewedPreference(defnId) {
        initialPageLoad = false;
        const userPreference = {key: lastViewedAssessmentInfoTileKey, value: _.toString(defnId)};
        userPreferenceStore.saveForUser(userPreference);
        userPreferenceCall = userPreferenceStore.findAllForUser(true);
    }

    function selectDefinition(defn) {
        selectedDefnId = defn.id
        showAssessmentPicker = false;
        saveLastViewedPreference(defn.id);
    }

    let userPreferenceCall = userPreferenceStore.findAllForUser();
    $: userPreferences = $userPreferenceCall?.data;

    $: includedFavouritesString = _.find(userPreferences, d => d.key === favouriteIncludedKey);
    $: excludedFavouritesString = _.find(userPreferences, d => d.key === favouriteExcludedKey);
    $: lastViewedDefinitionString = _.find(userPreferences, d => d.key === lastViewedAssessmentInfoTileKey);

    $: $favouriteIncludedIds = getIdsFromString(includedFavouritesString);
    $: $favouriteExcludedIds = getIdsFromString(excludedFavouritesString);

    let assessmentDefinitionCall = assessmentDefinitionStore.loadAll();
    $: assessmentDefinitions = $assessmentDefinitionCall?.data;

    $: lastViewedDefinition = !_.isNil(lastViewedDefinitionString)
        ? _.find(sortedDefinitions, d => d.id === _.toNumber(lastViewedDefinitionString.value))
        : _.first(sortedDefinitions);

    $: selectedDefinition = !_.isNil(lastViewedDefinition)
        ? lastViewedDefinition
        : _.first(sortedDefinitions);

    $: {
        if (initialPageLoad && selectedDefinition) {
            selectedDefnId = selectedDefinition.id;
        }
    }

    $: $defaultPrimaryList = _
        .chain(assessmentDefinitions)
        .filter(d => d.visibility === "PRIMARY")
        .map(d => d.id)
        .value();

    $: sortedDefinitions = _
        .chain($assessmentDefinitionCall?.data)
        .filter(d => _.includes(availableDefinitions, d.id))
        .orderBy(d => d.name)
        .value();

    $: definitionsById = _.keyBy(sortedDefinitions, d => d.id);

    let ratingsCall = ratingSchemeStore.loadAll();
    $: ratingsById = _
        .chain($ratingsCall.data)
        .flatMap(d => d.ratings)
        .keyBy(d => d.id)
        .value();

    $: summaryRequest = {
        idSelectionOptions: mkSelectionOptions(primaryEntityRef, determineDownwardsScopeForKind(primaryEntityRef?.kind), ["ACTIVE"], filters),
        definitionIds: $favouriteIds
    }

    $: {
        if (primaryEntityRef) {
            assessmentSummaryCall = assessmentRatingStore.findSummaryCounts(summaryRequest, "APPLICATION", true);
        }
    }

    $: assessmentCounts = $assessmentSummaryCall?.data;
    $: displayedDefinition = _.find(assessmentCounts, d => d.definitionId === selectedDefnId);
    $: availableDefinitions = _.map(assessmentCounts, d => d.definitionId);

    $: graphData = _
        .chain(displayedDefinition?.ratingCounts || [])
        .map(d => Object.assign({}, d, {rating: _.get(ratingsById, [d.id])}))
        .orderBy(['count', 'name'], ['desc', 'asc'])
        .value();

    $: height = Math.max(graphData.length * (dimensions.bar.height + dimensions.padding.bar), dimensions.bar.height);

</script>


<div>
    {#if showAssessmentPicker}
        <DropdownPicker style="display: inline-block"
                        items={sortedDefinitions}
                        onSelect={selectDefinition}
                        defaultMessage="Select assessment"/>
        <div style="padding-top: 1em; display:block">
            <button class="btn btn-default"
                    style="align-items: start"
                    on:click={() => showAssessmentPicker = false}>
                Cancel
            </button>
        </div>
    {:else}
        <div class="col-sm-12"
             class:waltz-scroll-region-200={_.size(graphData) > 10}>
            <svg width="100%"
                 height={height + dimensions.header.height + dimensions.padding.header}>
                <g class="header clickable"
                   transform={`translate(0 ${(dimensions.header.height + dimensions.padding.header) / 2})`}>
                    <g on:click|stopPropagation={changeAssessment}
                       on:keydown|stopPropagation={changeAssessment}>
                        <rect fill="#fff"
                              transform={`translate(${dimensions.header.button} ${- dimensions.header.button})`}
                              stroke="none"
                              width={dimensions.svg.width - dimensions.header.button * 2}
                              height={dimensions.header.button * 2}>
                        </rect>
                        <text text-anchor="middle"
                              font-size={dimensions.header.height}
                              transform={`translate(${dimensions.svg.width / 2} 0)`}>
                            {_.get(definitionsById, [selectedDefnId, "name"], `Unknown definition [${selectedDefnId}]`)}
                        </text>
                        <path d={getSymbol("pencil", dimensions.header.button)}
                              transform={`translate(${dimensions.svg.width - dimensions.header.button * 4} ${- dimensions.header.button / 2})`}
                              fill="none"
                              stroke="#000"/>
                    </g>
                    <g on:click|stopPropagation={moveLeft}
                       on:keydown|stopPropagation={moveLeft}
                       transform={`translate(${dimensions.header.button} ${- dimensions.header.button / 2})`}
                       class="left-toggle clickable">
                        <rect fill="#fff"
                              transform={`translate(${- dimensions.header.button} ${- dimensions.header.button})`}
                              stroke="none"
                              width={dimensions.header.button * 2}
                              height={dimensions.header.button * 2}>
                        </rect>
                        <path d={getSymbol("leftArrow", dimensions.header.button)}
                              fill="none"
                              stroke="#000"/>
                    </g>
                    <g on:click|stopPropagation={moveRight}
                       on:keydown|stopPropagation={moveRight}
                       transform={`translate(${dimensions.svg.width - dimensions.header.button} ${- dimensions.header.button / 2})`}
                       class="right-toggle clickable">
                        <rect fill="#fff"
                              transform={`translate(${- dimensions.header.button} ${- dimensions.header.button})`}
                              stroke="none"
                              width={dimensions.header.button * 2}
                              height={dimensions.header.button * 2}>
                        </rect>
                        <path d={getSymbol("rightArrow", dimensions.header.button)}
                              fill="none"
                              stroke="#000"
                              class="right-toggle"/>
                    </g>
                </g>
                <g class="assessment-bars"
                   transform={`translate(0, ${dimensions.header.height + dimensions.padding.header})`}>
                    <AssessmentRatingGraph {dimensions}
                                           {graphData}/>
                </g>
            </svg>
        </div>
    {/if}
</div>


<style>

    .assessment-bars {
        overflow-y: auto;
        height: 100%;
    }

</style>
