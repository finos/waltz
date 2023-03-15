<script>

    import {resolvedRows, resolveResponse, sortedHeaders} from "./bulk-upload-relationships-store";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";


    let erroredHeaders;
    let erroredRelationships;

    $: {
        if ($resolveResponse) {
            erroredHeaders = _.filter($resolveResponse.assessmentHeaders, d => d.status !== "HEADER_FOUND");
            erroredRelationships = _.filter($resolveResponse.rows, d => d.legalEntityRelationship.operation === "ERROR");
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
            <col width="10%"/>
            <col width="15%"/>
            <col width="15%"/>
            <col width="20%"/>
        </colgroup>
        <thead>
        <tr>
            <th>Operation</th>
            <th>Target Entity</th>
            <th>Legal Entity</th>
            <th>Comment</th>
            {#each $sortedHeaders as header}
                <th>
                    {header.inputString}
                    {#if header.status !== "HEADER_FOUND"}
                        <span style="color: red"><Icon name="exclamation-triangle"/></span>
                    {/if}
                </th>
            {/each}
        </tr>
        </thead>
        <tbody>
        {#each $resolvedRows as row}
            {@const assessmentByColumnId = _.keyBy(row.assessmentRatings, d => d.columnId)}
            <tr class:relationship-error={row.legalEntityRelationship.operation === "ERROR"}>
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
                                    <span style="color: red"><Icon name="exclamation-triangle"/></span>
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
