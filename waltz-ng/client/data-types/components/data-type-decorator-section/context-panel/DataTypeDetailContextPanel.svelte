<script>

    import {selectedDataType, selectedDecorator, viewData} from "../data-type-decorator-section-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import _ from "lodash";
    import {mkDefinitionKey} from "../../../../common/view-grid-utils";
    import FlowClassificationRuleViewTable from "./FlowClassificationRuleViewTable.svelte";
    import DataTypeDecoratorViewTable from "./DataTypeDecoratorViewTable.svelte";
    import DataTypeViewTable from "./DataTypeViewTable.svelte";


    let dataTypesById = {};
    let definitionsById = {};

    function clearSelected() {
        $selectedDecorator = null;
        $selectedDataType = null;
    }

</script>

<div>
    <span class="pull-right">
        <button class="btn btn-skinny"
                on:click={clearSelected}>
            <Icon name="times"/> Close
        </button>
    </span>
</div>
<br>
{#if $selectedDataType}
    <div>
        <strong>Data Type</strong>
        <DataTypeViewTable dataType={$selectedDataType}/>
    </div>
{/if}

{#if $selectedDecorator}
    <br>
    <div>

        <div>
            <strong>Decorator</strong>
            <DataTypeDecoratorViewTable decorator={$selectedDecorator}/>
        </div>

        <div>
            {#if $selectedDecorator.flowClassificationRule}
                <strong>Flow Classification Rule</strong>
                <FlowClassificationRuleViewTable {...$selectedDecorator.flowClassificationRule}/>
                <div class="help-block small"><Icon name="info-circle"/>This is the flow classification rule driving the classification of the flow</div>
            {/if}
        </div>

        <div>
            {#if !_.isEmpty($selectedDecorator.assessmentRatings)}
                <strong>Assessments</strong>
                <table class="table table-condensed small">
                    <thead>
                    <tr>
                        <td>Definition</td>
                        <td>Ratings</td>
                    </tr>
                    </thead>
                    <tbody>
                    {#each $viewData.primaryAssessments.assessmentDefinitions as definition}
                        {@const ratings = _.get($selectedDecorator, mkDefinitionKey(definition), [])}
                        <tr>
                            <td>
                                {definition.name}
                            </td>
                            <td>
                                <ul class="list-inline">
                                    {#each ratings as assessmentRating}
                                        <li>
                                            <RatingIndicatorCell {...assessmentRating.ratingSchemeItem}/>
                                        </li>
                                    {/each}
                                </ul>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            {/if}
        </div>
    </div>
{/if}

