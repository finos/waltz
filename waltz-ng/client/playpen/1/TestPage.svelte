<script>

    import EntitySelector from "./EntitySelector.svelte";
    import _ from "lodash";
    import {mkRef, sameRef} from "../../common/entity-utils";
    import ReportGridColumnSummary from "./ReportGridColumnSummary.svelte";
    import {columnUsageKind, ratingRollupRule} from "./report-grid-utils";

    export let gridId;
    export let columnDefs = [];

    $: columnDefinitions = columnDefs;

    function onSelect(d) {
        const column = {
            columnEntityReference: mkRef(d.kind, d.id, d.name || d.questionText, d.description),
            usageKind: columnUsageKind.NONE.key,
            ratingRollupRule: ratingRollupRule.NONE.key,
            position: 0,
        }
        columnDefs = _.concat(columnDefs, column);
    }

    function removeColumn(d) {
        columnDefs = _.reject(
            columnDefs,
            r => sameRef(r.columnEntityReference, d.columnEntityReference));
    }

    $: canBeAdded = (d) => {
        console.log({columnDefs, d})
        return !_.some(
            columnDefs,
            r => sameRef(r.columnEntityReference, d));
    }

</script>

{#if gridId}
<ReportGridColumnSummary {columnDefs}
                         {gridId}
                         onRemove={removeColumn}/>
{/if}

<hr>

<EntitySelector onSelect={onSelect}
                onDeselect={removeColumn}
                selectionFilter={canBeAdded}/>