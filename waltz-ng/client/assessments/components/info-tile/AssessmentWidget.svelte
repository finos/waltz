<script>
    import _ from "lodash";
    import {scaleLinear} from "d3-scale";
    import {max} from "d3-array";

    import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import {userPreferenceStore} from "../../../svelte-stores/user-preference-store";
    import {assessmentStores, createStores,} from "../list/assessment-rating-store";
    import {favouriteAssessmentDefinitionStore} from "../../../svelte-stores/favourite-assessment-definition-store";
    import {determineDownwardsScopeForKind, mkSelectionOptions} from "../../../common/selector-utils";
    import {lastViewedAssessmentInfoTileKey} from "../../../user";
    import DropdownPicker
        from "../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {primaryEntityReference} from "../rating-editor/rating-store";

    export let primaryEntityRef;
    export let filters;

    let stores = null;
    let favouriteIds;
    let graphData = [];
    let selectedDefinition = null;
    let assessmentSummaryCall;
    let initialPageLoad = true;
    let userPreferenceCall = userPreferenceStore.findAllForUser();
    let ratingsCall = ratingSchemeStore.loadAll();


    function saveLastViewedPreference(defnId) {
        initialPageLoad = false;
        const userPreference = {key: lastViewedAssessmentInfoTileKey, value: _.toString(defnId)};
        userPreferenceStore.saveForUser(userPreference);
    }

    function selectDefinition(defn) {
        selectedDefinition = defn;
        saveLastViewedPreference(defn.id);
    }

    $: userPreferences = $userPreferenceCall?.data;

    $: lastViewedDefinitionString = _.find(userPreferences, d => d.key === lastViewedAssessmentInfoTileKey);
    $: lastViewedDefinition = !_.isNil(lastViewedDefinitionString)
        ? _.find(sortedDefinitions, d => d.id === _.toNumber(lastViewedDefinitionString.value))
        : _.first(sortedDefinitions);

    $: selectedDefinition = !_.isNil(lastViewedDefinition)
        ? lastViewedDefinition
        : _.first(sortedDefinitions);

    $: sortedDefinitions = _
        .chain(favourites)
        .filter(d => _.includes(availableDefinitions, d.id))
        .orderBy(d => d.name)
        .value();

    $: definitionsById = _.keyBy(sortedDefinitions, d => d.id);

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
    $: displayedDefinition = _.find(assessmentCounts, d => d.definitionId === selectedDefinition.id);
    $: availableDefinitions = _.map(assessmentCounts, d => d.definitionId);

    $: graphData = _
        .chain(displayedDefinition?.ratingCounts || [])
        .map(d => Object.assign({}, d, {rating: _.get(ratingsById, [d.id])}))
        .orderBy(['count', 'name'], ['desc', 'asc'])
        .value();

    $: xScale = scaleLinear().domain([0, max(graphData, d => d.count)]).range([0, 100]);

    $: favourites = $favouriteAssessmentDefinitionStore['APPLICATION'] || [];

    $: {
        if ($primaryEntityReference && _.isNil($assessmentStores)) {
            $assessmentStores = createStores($primaryEntityReference.kind);
        }
    }

</script>

{#if _.size(graphData) === 0}
    <div>
        <NoData>There are no assessments to display</NoData>
    </div>
{:else}
    <div>
        <DropdownPicker style="display: inline-block"
                        selectedItem={selectedDefinition}
                        items={sortedDefinitions}
                        onSelect={selectDefinition}/>
        <table class="table table-condensed table-hover small">
            <colgroup>
                <col width="40%">
                <col width="50%">
                <col width="10%">
            </colgroup>
            <tbody>
            {#each graphData as ratingCount, i}
                <tr>
                    <td class="bar-header-cell">
                        {ratingCount.rating?.name}
                    </td>
                    <td class="bar-cell">
                        <div class="bar"
                             style={`width: ${xScale(ratingCount.count)}%; background-color: ${ratingCount.rating?.color};`}/></td>
                    <td class="bar-cell">
                        {ratingCount.count}
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
{/if}


<style>
    .bar-header-cell {
        text-align: right;
    }

    .bar-cell {
        vertical-align: middle;
    }

    .bar {
        width: 20%;
        height: 1em;

        border: 1px solid #aaa;
    }


</style>
