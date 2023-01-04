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

    export let primaryEntityRef;
    export let filters;

    let dimensions = {
        svg: {
            height: 200,
            width: 200
        },
        padding: {
            left: 40,
            right: 40,
            bar: 5,
            header: 10
        },
        bar: {
            height: 10,
        },
        label: {
            width: 80,
            height: 8
        },
        header: {
            height: 12
        }
    };

    const favouriteIncludedKey = `${favouriteAssessmentDefinitionIdsKey}.application.included`;
    const favouriteExcludedKey = `${favouriteAssessmentDefinitionIdsKey}.application.excluded`;

    let assessmentSummaryCall;
    let selectedDefnId;
    let showAssessmentPicker = false;

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
        const userPreference = {key: lastViewedAssessmentInfoTileKey, value: _.toString(defnId)}
        userPreferenceStore.saveForUser(userPreference)
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

    $: lastViewedDefinition = _.isNil(lastViewedDefinitionString)
        ? _.first(sortedDefinitions)
        : _.find(sortedDefinitions, d => d.id === _.toNumber(lastViewedDefinitionString.value));

    $: {
        if (lastViewedDefinition && userPreferences && sortedDefinitions) {
            selectedDefnId = lastViewedDefinition.id;
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
    <div class="col-sm-2" style="padding: 0">
        <p>
            <button class="btn btn-plain"
                    on:click={moveLeft}>
                <Icon name="arrow-circle-left"/>
            </button>
        </p>
    </div>
    <div class="col-sm-8" style="padding: 0">
        <p style="text-align: center">
            {#if !showAssessmentPicker}
                {_.get(definitionsById, [selectedDefnId, "name"], `Unknown definition [${selectedDefnId}]`)}
                <button class="btn btn-plain btn-xs"
                        on:click={changeAssessment}>
                    <Icon name="pencil"/>
                </button>
            {:else}
                <DropdownPicker items={sortedDefinitions}
                                onSelect={selectDefinition}/>
            {/if}
        </p>
    </div>
    <div class="col-sm-2 pull-right" style="padding: 0">
        <p>
            <button class="btn btn-plain"
                    on:click={moveRight}>
                <Icon name="arrow-circle-right"/>
            </button>
        </p>
    </div>
</div>
<div>
    <div class="col-sm-12"
         class:waltz-scroll-region-200={_.size(graphData) > 12}>
        <svg width="100%"
             height={height + dimensions.header.height + dimensions.padding.header}>
            <g class="header">
                <text text-anchor="middle"
                      font-size={dimensions.header.height}
                      transform={`translate(${dimensions.svg.width - dimensions.label.width} ${(dimensions.header.height + dimensions.padding.header) / 2})`}>
                    {_.get(definitionsById, [selectedDefnId, "name"], `Unknown definition [${selectedDefnId}]`)}
                </text>
            </g>
            <g class="assessment-bars"
               transform={`translate(0, ${dimensions.header.height + dimensions.padding.header})`}>
                <AssessmentRatingGraph {dimensions}
                                       {graphData}/>
            </g>
        </svg>
    </div>
</div>


<style>

    .assessment-bars {
        overflow-y: auto;
        height: 100%;
    }

</style>
