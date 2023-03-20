<script>

    import {resolvedRows, resolveResponse, sortedHeaders} from "./bulk-upload-relationships-store";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import Tooltip from "../../../common/svelte/Tooltip.svelte";
    import RowErrorTooltip from "./RowErrorTooltip.svelte";
    import AssessmentErrorTooltip from "./AssessmentErrorTooltip.svelte";


    let erroredHeaders;
    let erroredRelationships;

    $: {
        if ($resolveResponse) {
            erroredHeaders = _.filter($resolveResponse.assessmentHeaders, d => d.status !== "HEADER_FOUND");
            erroredRelationships = _.filter($resolveResponse.rows, d => !_.isEmpty(d.legalEntityRelationship.errors));
        }
    }

    function mkErrorProps(row) {
        return {
            relationshipErrors: row.legalEntityRelationship.errors,
        }
    }

    function mkAssessmentErrorProps(assessmentRating) {
        return {
            assessmentRating
        }
    }

    function determineAssessmentHeaderTooltip(status) {
        if (status === "HEADER_DEFINITION_NOT_FOUND") {
            return "Invalid column header, definition not found ";
        } else if (status === "HEADER_RATING_NOT_FOUND") {
            return "Invalid column header, rating not found";
        } else {
            return "";
        }
    }

</script>

<div class="help-block">

    <div>Resolved {$resolvedRows.length} rows of data.</div>

    {#if !_.isEmpty(erroredHeaders)}
        <div>There are {erroredHeaders.length} assessment header/s which could not be resolved.</div>
    {/if}

    {#if !_.isEmpty(erroredRelationships)}
        <div>There are {erroredRelationships.length} relationship/s which could not be resolved.</div>
    {/if}

</div>

<div class="waltz-scroll-region-300" style="margin-bottom: 2em">
    <table class="table table-condensed small">
        <colgroup>
            <col width="5%"/>
            <col width="10%"/>
            <col width="15%"/>
            <col width="15%"/>
            <col width="20%"/>
        </colgroup>
        <thead>
        <tr>
            <th></th>
            <th>Operation</th>
            <th>Target Entity</th>
            <th>Legal Entity</th>
            <th>Comment</th>
            {#each $sortedHeaders as header}
                <th title={determineAssessmentHeaderTooltip(header.status)}>
                    {header.inputString}
                    {#if header.status !== "HEADER_FOUND"}
                        <span style="color: red">
                            <Icon name="exclamation-triangle"/>
                        </span>
                    {/if}
                </th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each $resolvedRows as row}
            {@const assessmentByColumnId = _.keyBy(row.assessmentRatings, d => d.columnId)}
            <tr class:relationship-error={!_.isEmpty(row.legalEntityRelationship.errors)}>
                <td class="relationship-cell">
                    <Tooltip content={RowErrorTooltip}
                             placement="left-start"
                             props={mkErrorProps(row)}>
                        <svelte:fragment slot="target">
                                <span style="color: red">
                                    {#if !_.isEmpty(row.legalEntityRelationship.errors)}
                                    <Icon name="exclamation-triangle" size="lg"/>
                                    {/if}
                                </span>
                        </svelte:fragment>
                    </Tooltip>
                </td>
                <td class="relationship-cell">
                                <span>
                                    {row.legalEntityRelationship.operation}
                                </span>
                </td>
                <td class="relationship-cell">
                        <span>
                            {row.legalEntityRelationship.targetEntityReference.inputString}
                            {#if _.isNil(row.legalEntityRelationship.targetEntityReference.resolvedEntityReference)}
                                <span style="color: red"><Icon name="exclamation-triangle"/></span>
                            {/if}
                        </span>
                </td>
                <td class="relationship-cell">
                    <span>{row.legalEntityRelationship.legalEntityReference.inputString}</span>
                    {#if _.isNil(row.legalEntityRelationship.legalEntityReference.resolvedEntityReference)}
                        <span style="color: red"><Icon name="exclamation-triangle"/></span>
                    {/if}
                </td>
                <td class="relationship-cell"
                    style="border-right: 1px dashed #ccc">
                    {row.legalEntityRelationship.comment || ""}
                </td>
                {#each $sortedHeaders as header}
                    {@const assessmentRating = _.get(assessmentByColumnId, header.columnId)}
                    <td class:assessment-error={_.includes(assessmentRating?.statuses, "ERROR") || header.status !== "HEADER_FOUND"}>
                        <span>
                            {#if assessmentRating}
                                {assessmentRating?.inputString}
                                {#if _.includes(assessmentRating?.statuses, "ERROR")}
                                    <Tooltip content={AssessmentErrorTooltip}
                                             placement="left-start"
                                             props={mkAssessmentErrorProps(assessmentRating)}>
                                        <svelte:fragment slot="target">
                                            <span style="color: red">
                                                <Icon name="exclamation-triangle"/>
                                            </span>
                                        </svelte:fragment>
                                    </Tooltip>
                                {/if}
                            {/if}
                        </span>
                    </td>
                {/each}
            </tr>
        {/each}
        </tbody>
    </table>
</div>


<style>


    .relationship-error .relationship-cell {
        background-color: #ffcdcd;
    }

    .assessment-error {
        background-color: #ffcdcd;
    }


</style>
