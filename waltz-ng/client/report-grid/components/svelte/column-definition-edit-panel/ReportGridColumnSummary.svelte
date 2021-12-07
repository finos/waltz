<script>

    import NoData from "../../../../common/svelte/NoData.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash"
    import {entity} from "../../../../common/services/enums/entity";
    import {columnDefs, selectedColumn, selectedGrid, lastMovedColumn} from "../report-grid-store";
    import {move} from "../../../../common/list-utils";
    import {sameRef} from "../../../../common/entity-utils";

    export let onRemove = () => console.log("Removing entity")
    export let onEdit = () => console.log("Editing entity")

    let lastMoved = null;

    function getIcon(entityKind) {
        return _.get(entity[entityKind], 'icon', "fw");
    }

    function moveColumn(positionCount, column) {
        const reorderedList = move($columnDefs, _.indexOf($columnDefs, column), positionCount);
        $lastMovedColumn = column;
        $columnDefs = _.map(reorderedList,
            d => Object.assign(
                {},
                d,
                {
                    position:  _.indexOf(reorderedList, d),
                    originalPosition: _.findIndex($selectedGrid.definition.columnDefinitions, r => sameRef(d.columnEntityReference, r.columnEntityReference))
                }));
    }

    $: maxPos = _.maxBy($columnDefs, d => d.position);

</script>

<div class="row">
    <div class="col-sm-12">
        <div>
            <table class="table table-condensed small">
                <colgroup>
                    <col width="60%">
                    <col width="10%">
                    <col width="10%">
                    <col width="10%">
                    <col width="10%">
                </colgroup>
                <thead>
                    <tr>
                        <th>Entity</th>
                        <th colspan="2">Position</th>
                        <th colspan="2">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {#each _.orderBy($columnDefs, d => d.position) as column}
                        <tr class:selected={$selectedColumn && sameRef(column.columnEntityReference, $selectedColumn?.columnEntityReference)}
                            class:last-moved={$lastMovedColumn && sameRef(column.columnEntityReference, $lastMovedColumn?.columnEntityReference)}>
                            <td>
                                <Icon name={getIcon(column?.columnEntityReference?.kind)}/>{column?.columnEntityReference?.name || column?.columnEntityReference?.questionText}
                            </td>
                            <td>
                                <span style="text-align: center">
                                    <button class="btn btn-skinny"
                                            disabled={column.position === 0}
                                            on:click={() => moveColumn(-1, column)}>
                                        <Icon name="arrow-up"/>
                                    </button>
                                </span>
                            </td>
                            <td>
                                <span style="text-align: center">
                                    <button class="btn btn-skinny"
                                            disabled={column.position === _.maxBy($columnDefs, d => d.position)?.position}
                                            on:click={() => moveColumn(1, column)}>
                                        <Icon name="arrow-down"/>
                                    </button>
                                </span>
                            </td>
                            <td>
                                <button class="btn btn-skinny"
                                        on:click={() => onEdit(column)}>
                                    <Icon name="pencil"/>
                                </button>
                            </td>
                            <td>
                                <button class="btn btn-skinny"
                                        on:click={() => onRemove(column)}>
                                    <Icon name="trash"/>
                                </button>
                            </td>
                        </tr>
                    {:else}
                        <tr>
                            <td colspan="5">
                                <NoData>You have no entities selected</NoData>
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
</style>