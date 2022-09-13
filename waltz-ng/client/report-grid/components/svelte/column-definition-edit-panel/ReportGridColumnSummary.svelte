<script>

    import NoData from "../../../../common/svelte/NoData.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash"
    import {entity} from "../../../../common/services/enums/entity";
    import {columnDefs, lastMovedColumn, selectedColumn, selectedGrid} from "../report-grid-store";
    import {move} from "../../../../common/list-utils";
    import {sameColumnRef} from "../report-grid-utils";

    export let onRemove = () => console.log("Removing entity")
    export let onEdit = () => console.log("Editing entity")

    let lastMoved = null;

    function getIcon(entityKind) {
        return _.get(entity[entityKind], 'icon', "fw");
    }

    function moveColumn(positionCount, column) {
        const reorderedList = move($columnDefs, _.indexOf($columnDefs, column), positionCount);
        $lastMovedColumn = column;

        $columnDefs = recalcPositions(reorderedList, $selectedGrid.definition.fixedColumnDefinitions);

    }

    function recalcPositions(reorderedList, originalList) {
        return _.map(reorderedList,
            d => Object.assign(
                {},
                d,
                {
                    position: _.indexOf(reorderedList, d),
                    originalPosition: _.findIndex(originalList, r => sameColumnRef(d, r))
                }));
    }

    $: maxPos = _.maxBy($columnDefs, d => d.position);

    function moveToTop(column) {
        const columnTail = _.reject($columnDefs, column);
        const reorderedColumns = _.concat([column], columnTail);
        $columnDefs = recalcPositions(reorderedColumns, $selectedGrid.definition.fixedColumnDefinitions)
    }

    function moveToBottom(column) {
        const columnHead = _.reject($columnDefs, column);
        const reorderedColumns = _.concat(columnHead, [column]);
        $columnDefs = recalcPositions(reorderedColumns, $selectedGrid.definition.fixedColumnDefinitions)
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
        if (column.displayName) {
            return column.displayName;
        } else {
            return mkColumnName(column);
        }
    }

</script>

<div class="row">
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
                        <th>Entity</th>
                        <th colspan="4">Position</th>
                        <th colspan="2">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {#each _.orderBy($columnDefs, d => d.position) as column}
                        <tr class:selected={$selectedColumn && sameColumnRef(column, $selectedColumn)}
                            class:last-moved={$lastMovedColumn && sameColumnRef(column, $lastMovedColumn)}
                            class="waltz-visibility-parent">
                            <td>
                                <Icon name={getIcon(column?.columnEntityKind)}/>
                                <span>{determineColumnName(column)}</span>
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
</div>

<style>
    .selected{
        font-weight: bold;
        background: #f3f9ff;
    }

    .last-moved{
        background: #f3f9ff;
    }

    thead tr {
        background-color: #fafafa;
        background: linear-gradient(90deg, #fafafa 0%, rgba(255,255,255,1) 100%);
    }
</style>