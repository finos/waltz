<script>

    import NoData from "../../../../common/svelte/NoData.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash"
    import {entity} from "../../../../common/services/enums/entity";
    import {columnDefs, selectedGrid} from "../report-grid-store";
    import {move} from "../../../../common/list-utils";
    import {sameRef} from "../../../../common/entity-utils";

    export let onRemove = () => console.log("Removing entity")
    export let onEdit = () => console.log("Editing entity")

    function getIcon(entityKind) {
        return _.get(entity[entityKind], 'icon', "fw");
    }

    function moveColumn(positionCount, column) {
        const reorderedList = move($columnDefs, _.indexOf($columnDefs, column), positionCount);
        $columnDefs = _.map(reorderedList,
            d => Object.assign(
                {},
                d,
                {
                    position:  _.indexOf(reorderedList, d),
                    originalPosition: _.findIndex($selectedGrid.definition.columnDefinitions, r => sameRef(d.columnEntityReference, r.columnEntityReference))
                }));
    }

</script>

<div class="row">
    <div class="col-sm-12">
        <div>
            <table class="table table-condensed table-striped small">
                <colgroup>
                    <col width="60%">
                    <col width="20%">
                    <col width="20%">
                </colgroup>
                <thead>
                    <tr>
                        <th>Entity</th>
                        <th>Position</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {#each $columnDefs as column}
                        <tr>
                            <td>
                                <Icon name={getIcon(column?.columnEntityReference?.kind)}/>{column?.columnEntityReference?.name || column?.columnEntityReference?.questionText}
                            </td>
                            <td>
                                <span style="text-align: center">
                                    <button class="btn btn-skinny small"
                                            on:click={() => moveColumn(-1, column)}>
                                        <Icon name="arrow-up"/>
                                    </button>
                                    <button class="btn btn-skinny small"
                                            on:click={() => moveColumn(1, column)}>
                                        <Icon name="arrow-down"/>
                                    </button>
                                </span>
                            </td>
                            <td>
                                <button class="btn-skinny"
                                        on:click={() => onRemove(column)}>
                                    <Icon name="trash"/>
                                </button>
                                <button class="btn-skinny"
                                        on:click={() => onEdit(column)}>
                                    <Icon name="pencil"/>
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
