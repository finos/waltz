<script>

    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import _ from "lodash";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";
    import {containsAny} from "../../../common/list-utils";

    export let primaryEntityReference;

    let viewCall;
    let tableData = [];
    let filteredData = [];
    let qry;
    let flowClassificationsCall = flowClassificationStore.findAll();
    let dataTypesCall = dataTypeStore.findAll();
    let selectedDecorator;
    let definitionsById;
    let assessmentFilters = [];
    let filteredAssessments = [];

    function selectDecorator(decorator) {
        selectedDecorator = decorator
    }

    function goToRule(rule) {
        $pageInfo = {
            state: "main.flow-classification-rule.view",
            params: {
                id: rule.id
            }
        };
    }

    function clearFiltersForDefinition() {
        filteredAssessments = [];
        refreshDecorators();
    }

    function refreshDecorators() {
        const noRatingFilters = _.isEmpty(filteredAssessments);
        if (noRatingFilters) {
            filteredData = tableData;
        } else {
            filteredData = _.filter(tableData, d => containsAny(filteredAssessments, d.assessmentRatings));
        }
    }

    function selectRating(definitionId, ratingId) {

        const ratingInfo = {
            definitionId,
            ratingId
        }

        if (_.some(filteredAssessments, r => _.isEqual(r, ratingInfo))) {
            filteredAssessments = _.filter(filteredAssessments, d => !_.isEqual(d, ratingInfo));
            refreshDecorators();
        } else {
            filteredAssessments = _.concat(filteredAssessments, ratingInfo);
            refreshDecorators();
        }
    }

    $: classifications = $flowClassificationsCall?.data || [];
    $: classificationsById = _.keyBy(classifications, d => d.id);

    $: dataTypes = $dataTypesCall?.data || [];
    $: dataTypesById = _.keyBy(dataTypes, d => d.id);

    $: {
        if (primaryEntityReference) {
            viewCall = dataTypeDecoratorStore.getViewForParentRef(primaryEntityReference);
        }
    }

    $: viewData = $viewCall?.data;

    $:{
        if (viewData) {

            const ratingsById = _.keyBy(viewData.ratingSchemeItems, d => d.id);
            const assessmentRatingsByDecoratorId = _.groupBy(viewData.decoratorRatings, d => d.entityReference.id);
            const flowClassificationRulesById = _.keyBy(viewData.flowClassificationRules, d => d.id);
            definitionsById = _.keyBy(viewData.primaryAssessmentDefinitions, d => d.id);

            const assessmentRatingsByDefinitionId = _
                .chain(viewData.decoratorRatings)
                .groupBy(r => r.assessmentDefinitionId)
                .mapValues(v => _
                    .chain(v)
                    .map(r => ratingsById[r.ratingId])
                    .filter(d => d != null)
                    .uniq()
                    .sortBy(r => r.position, r => r.name)
                    .value())
                .value();

            assessmentFilters = _.map(
                viewData.primaryAssessmentDefinitions,
                d => Object.assign({}, { definition: d, ratings: _.get(assessmentRatingsByDefinitionId, d.id, [])}));

            tableData = _
                .chain(viewData.dataTypeDecorators)
                .map(d => {

                    const ratingsForDecorator = _.get(assessmentRatingsByDecoratorId, d.id, []);
                    const assessmentRatings = _.map(ratingsForDecorator, d => ({ definitionId : d.assessmentDefinitionId, ratingId: d.ratingId}))
                    const enrichedRatings = _.chain(ratingsForDecorator)
                        .map(d => Object.assign({}, d, {rating: ratingsById[d.ratingId]}))
                        .orderBy(d => d.rating.name)
                        .value();
                    const ratingsByDefnId = _.groupBy(enrichedRatings, d => d.assessmentDefinitionId);
                    const flowClassificationRule = _.get(flowClassificationRulesById, d.flowClassificationRuleId);

                    return  Object.assign({}, d, {ratingsByDefnId, flowClassificationRule, assessmentRatings});
                })
                .value();

            filteredData = tableData;
        }
    }

    $: visibleRows = _.isEmpty(qry)
        ? filteredData
        : termSearch(filteredData, qry, ["decoratorEntity.name", "rating"]);

    $: classificationForSelected = _.get(classificationsById, selectedDecorator?.flowClassificationRule?.classificationId);

</script>

<div class="decorator-detail">
    <div class="decorator-detail-table">
        <h4>Data Type Decorators</h4>

        <details>
            <summary>
                Filters
            </summary>

            <div class="help-block"
                 style="padding-top: 1em">Use the assessment ratings to filter the logical flow decorators. Only ratings aligned to a data type decorator can be filtered upon</div>
            <div style="display: flex; gap: 1em">
                <div style="flex: 1 1 30%">
                    {#each assessmentFilters as assessment}
                        <table class="table table-condensed">
                            <thead>
                            <tr>
                                <th>{assessment?.definition?.name}
                                    <span>
                                            <button class="btn btn-skinny"
                                                    on:click={() => clearFiltersForDefinition(assessment.definition.id)}>
                                                Clear
                                            </button>
                                        </span>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            {#each assessment?.ratings as rating}
                                <tr class="clickable"
                                    class:selected={_.some(filteredAssessments, r => _.isEqual(r, { definitionId: assessment.definition.id, ratingId: rating.id}))}
                                    on:click={() => selectRating(assessment.definition.id, rating.id)}>
                                    <td>
                                        <RatingIndicatorCell {...rating}/>
                                    </td>
                                </tr>
                            {/each}
                            </tbody>
                        </table>
                    {/each}
                </div>
            </div>
        </details>

        <div>
            <SearchInput bind:value={qry}/>
        </div>
        <table class="table table-condensed"
               style="margin-top: 1em">
            <thead>
            <tr>
                <th>Data Type</th>
                <th>Flow Classification Rule</th>
                {#each viewData?.primaryAssessmentDefinitions  || [] as defn}
                    <th>{defn.name}</th>
                {/each}
            </tr>
            </thead>
            <tbody>
            {#each visibleRows as decorator}
                {@const classification = _.get(classificationsById, decorator.flowClassificationRule?.classificationId)}
                <tr on:click={() => selectDecorator(decorator)}
                    class="clickable">
                    <td>{decorator.decoratorEntity.name}</td>
                    <td>
                        <RatingIndicatorCell {...classification}/>
                    </td>
                    {#each _.get(viewData, "primaryAssessmentDefinitions", []) as defn}
                        {@const assessmentRatings = _.get(decorator.ratingsByDefnId, [defn.id], [])}
                        <td>
                            <div class="rating-col">
                                {#each assessmentRatings as rating}
                                    <RatingIndicatorCell {...rating.rating}/>
                                {/each}
                            </div>
                        </td>
                    {/each}
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
    {#if selectedDecorator}
        <div class="decorator-detail-panel">
            <h4>{selectedDecorator.decoratorEntity.name}</h4>

            <LastEdited showLabel={true}
                        entity={selectedDecorator}/>

            <div style="padding-top: 2em">
                <strong>Flow Classification Rule</strong>
                <table class="table table-condensed small">
                    <tbody>
                        <tr>
                            <td>Source</td>
                            <td>
                                <EntityLink ref={selectedDecorator.flowClassificationRule.subjectReference}/>
                            </td>
                        </tr>
                        <tr>
                            <td>Scope</td>
                            <td>
                                <EntityLink ref={selectedDecorator.flowClassificationRule.vantagePointReference}/>
                            </td>
                        </tr>
                        <tr>
                            <td>Data Type</td>
                            <td><EntityLink ref={_.get(dataTypesById, selectedDecorator.flowClassificationRule.dataTypeId)}/></td>
                        </tr>
                        <tr>
                            <td>Classification</td>
                            <td>
                                <RatingIndicatorCell {...classificationForSelected}/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <button class="btn btn-skinny" on:click={() => goToRule(selectedDecorator.flowClassificationRule)}>
                                    Visit page
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div>
                <strong>Assessments</strong>
                <table class="table table-condensed small">
                    <thead>
                    <tr>
                        <td>Definition</td>
                        <td>Ratings</td>
                    </tr>
                    </thead>
                    <tbody>
                    {#each _.keys(selectedDecorator.ratingsByDefnId) as defnId}
                        {@const ratings = _.get(selectedDecorator.ratingsByDefnId, defnId, [])}
                        <tr>
                            <td>
                                {_.get(definitionsById, [defnId, "name"], "Unknown")}
                            </td>
                            <td>
                                <ul class="list-inline">
                                    {#each ratings as assessmentRating}
                                        <li>
                                            <RatingIndicatorCell {...assessmentRating.rating}/>
                                        </li>
                                    {/each}
                                </ul>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            </div>
        </div>
    {/if}
</div>


<style>

    .rating-col {
        display: flex;
        gap: 1em;
    }

    .decorator-detail {
        display: flex;
        gap: 10px;
    }

    .decorator-detail-table {
        width: 70%;
        flex: 1 1 50%
    }

    .decorator-detail-panel {
        width: 30%;
        padding-left: 1em;
    }

    .selected {
        background-color: white;
    }

</style>