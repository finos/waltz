<script>

    import NoData from "../../../../common/svelte/NoData.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash"
    import {entity} from "../../../../common/services/enums/entity";
    import {sameColumnRef} from "../report-grid-utils";
    import {gridService} from "../report-grid-service";
    import {flip} from "svelte/animate";
    import {quintOut} from "svelte/easing";


    export let onRemove = () => console.log("Removing entity");
    export let onEdit = () => console.log("Editing entity");
    export let selectedColumn;

    const {columnDefs} = gridService;

    const animationParams = {duration: 200, easing: quintOut};

    function getIcon(entityKind) {
        return _.get(entity[entityKind], 'icon', "fw");
    }

    function moveColumn(positionCount, column) {
        gridService.moveColumn(column, positionCount);
    }

    function moveToTop(column) {
        gridService.moveColumnToStart(column);
    }

    function moveToBottom(column) {
        gridService.moveColumnToEnd(column);
    }

    function moveUp(column) {
        moveColumn(-1, column);
    }

    function moveDown(column) {
        moveColumn(1, column);
    }

    function mkColumnName(column) {
        return _.join(_.compact([column.entityFieldReference?.displayName, column.columnName]), ' / ');
    }

    function determineColumnName(column) {
        const name = column.displayName
            ? column.displayName
            : mkColumnName(column);

        return column.externalId
            ? `${name} (${column.externalId})`
            : name;
    }

    $: console.log({cols: $columnDefs});


    $: cols = _.map($columnDefs, d => ({colDef: d, trackingId: JSON.stringify(_.omit(d, ["position"]))}));

    // function mkColTrackingId(column) {
    //     return JSON.stringify(_.omit(column, ["position"]));
    // }

</script>

<div class="col-sm-12">
    <div>
        <table class="table table-condensed small">
            <colgroup>
                <col width="60%">
                <col width="5%">
                <col width="5%">
                <col width="5%">
                <col width="5%">
                <col width="10%">
                <col width="10%">
            </colgroup>
            <thead>
            <tr>
                <th>Column (external id)</th>
                <th colspan="4">Position</th>
                <th colspan="2">Actions</th>
            </tr>
            </thead>
            <tbody>
            {#each _.orderBy(cols, d => d.colDef.position) as columnInfo (columnInfo.trackingId)}
                {@const column = columnInfo.colDef}
                <tr class:selected={selectedColumn && sameColumnRef(column, selectedColumn)}
                    class="waltz-visibility-parent"
                    animate:flip={animationParams}>
                    <td>
                        <Icon name={getIcon(column?.columnEntityKind)}/>
                        <span>
                            {determineColumnName(column)}
                            <span style={`color: ${column.externalId ? "green" : "orange"}`}
                                  title={column.externalId
                                            ? "This column can be used in derived columns, filter notes and exports"
                                            : "It is highly recommended to set an external id for use in derived columns, filter notes and exports"}>
                                <Icon name={column.externalId ? "check" : "exclamation-triangle"}/>
                            </span>
                        </span>
                        {#if column?.displayName}
                            <div title="This is the original name which has been overridden"
                                 class="help-block small">{mkColumnName(column)}</div>
                        {/if}
                    </td>
                    <td>
                            <span style="text-align: center">
                                <button class="btn btn-skinny waltz-visibility-child-50"
                                        title="move to top"
                                        disabled={column.position === 0}
                                        on:click={() => moveToTop(column)}>
                                    <Icon name="step-forward" rotate="270"/>
                                </button>
                            </span>
                    </td>
                    <td>
                            <span style="text-align: center">
                                <button class="btn btn-skinny waltz-visibility-child-50"
                                        title="move up"
                                        disabled={column.position === 0}
                                        on:click={() => moveUp(column)}>
                                    <Icon name="arrow-up"/>
                                </button>
                            </span>
                    </td>
                    <td>
                        <span style="text-align: center">
                            <button class="btn btn-skinny waltz-visibility-child-50"
                                    title="move down"
                                    disabled={column.position === _.maxBy($columnDefs, d => d.position)?.position}
                                    on:click={() => moveDown(column)}>
                                <Icon name="arrow-down"/>
                            </button>
                        </span>
                    </td>
                    <td>
                        <span style="text-align: center">
                            <button class="btn btn-skinny waltz-visibility-child-50"
                                    title="move to bottom"
                                    disabled={column.position === _.maxBy($columnDefs, d => d.position)?.position}
                                    on:click={() => moveToBottom(column)}>
                                <Icon name="step-forward" rotate="90"/>
                            </button>
                        </span>
                    </td>
                    <td>
                        <button class="btn btn-skinny waltz-visibility-child-50"
                                on:click={() => onEdit(column)}>
                            <Icon name="pencil"/>
                        </button>
                    </td>
                    <td>
                        <button class="btn btn-skinny waltz-visibility-child-50"
                                on:click={() => onRemove(column)}>
                            <Icon name="trash"/>
                        </button>
                    </td>
                </tr>
            {:else}
                <tr>
                    <td colspan="5">
                        <NoData>This grid has no columns.</NoData>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
</div>

<style>
    .selected{
        font-weight: bold;
        background: #f3f9ff;
    }

    thead tr {
        background-color: #fafafa;
        background: linear-gradient(90deg, #fafafa 0%, rgba(255,255,255,1) 100%);
    }
</style>