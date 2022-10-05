<script>
    import NoData from "../../../common/svelte/NoData.svelte";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {selectedGrid} from "./report-grid-store";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import {truncateMiddle} from "../../../common/string-utils";
    import {entity} from "../../../common/services/enums/entity";


    export let onGridSelect = () => "selecting grid";
    export let onCreate = () => "creating grid";
    export let grids =[];

    let qry = "";

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    let activeMode = Modes.VIEW

    function onSelect(grid) {
        onGridSelect(grid);
    }

    $: orderedGrids = _.sortBy(grids, 'name');

    $: gridList = _.isEmpty(qry)
        ? orderedGrids
        : termSearch(orderedGrids, qry, ['name', 'description']);

    function determineSubjectIcon(gridSubjectKind) {
        return _.get(entity[gridSubjectKind], "icon", "fw");
    }

</script>

<div class="row">
    <div class="col-sm-12" >
        {#if _.size(grids) > 10}
            <SearchInput bind:value={qry}
                         placeholder="Search available grids"/>
            <br>
        {/if}
        <div class:waltz-scroll-region-350={_.size(grids) > 10}>
            {#if _.isEmpty(grids)}
                <NoData>
                    There are no report grids, would you like to
                    <button class="btn btn-skinny"
                            on:click={() => onCreate()}>
                        create one?
                    </button>
                </NoData>
            {:else }
                <table class="table table-condensed table-hover small">
                    <colgroup>
                        <col width="40%">
                        <col width="40%">
                    </colgroup>
                    <thead>
                    <tr>
                        <th>Grid Name</th>
                        <th>Description</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each gridList as grid}
                        <tr class:selected={$selectedGrid?.definition.id === grid?.id}
                            class="clickable"
                            on:click={() => onSelect(grid)}>
                            <td>
                                <a class="force-wrap">
                                    <Icon name={determineSubjectIcon(grid?.subjectKind)}/> {grid?.name}
                                </a>
                            </td>
                            <td class="force-wrap"
                                title={grid?.description}>
                                <span class:text-muted={!grid?.description}>
                                    {truncateMiddle(grid?.description, 100) || "No description"}
                                </span>
                            </td>
                        </tr>
                    {/each}
                    </tbody>
                </table>
            {/if}
        </div>
        <div style="padding-top: 1em">
            <button class="btn btn-skinny"
                    on:click={onCreate}>
                <Icon name="plus"/>
                Create a new report grid
            </button>
        </div>
    </div>
</div>


<style type="text/scss">
    .selected{
        background: #f3f9ff;
    }
</style>