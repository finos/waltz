<script>
    import NoData from "../../../common/svelte/NoData.svelte";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {selectedGrid} from "./report-grid-store";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import {truncateMiddle} from "../../../common/string-utils";
    import {gridService} from "./report-grid-service";


    import {entity} from "../../../common/services/enums/entity";

    export let onGridSelect = () => "selecting grid";
    export let onCreate = () => "creating grid";

    export let grids = [];

    const {gridDefinition} = gridService;

    const filters = {
        ALL: d => d,
        PRIVATE: d => d.kind === 'PRIVATE',
        PUBLIC: d => d.kind === 'PUBLIC'
    }

    let qry = "";

    let filterCondition = filters.ALL;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    }

    let activeMode = Modes.VIEW

    function onSelect(grid) {
        onGridSelect(grid);
    }

    $: orderedGrids = _
        .chain(grids)
        .filter(filterCondition)
        .sortBy('name')
        .value();

    $: gridList = _.isEmpty(qry)
        ? orderedGrids
        : termSearch(orderedGrids, qry, ['name', 'description']);

    function determineSubjectIcon(gridSubjectKind) {
        return _.get(entity[gridSubjectKind], "icon", "fw");
    }

    $: console.log({grids});

</script>

<div class="row">
    <div class="col-sm-12">
        {#if _.size(grids) > 10}
            <SearchInput bind:value={qry}
                         placeholder="Search available grids"/>
            <br>
        {/if}
        <div class="pull-right" style="padding-bottom: 1em">
            <span class="small">
                <button class="btn btn-default btn-xs"
                        title="Filter grids that are private but visible to you"
                        on:click={() => filterCondition = filters.PRIVATE}>
                    <Icon name="user-secret"/>
                </button>
                <button class="btn btn-default btn-xs"
                        title="Filter grids that are publicly visible"
                        on:click={() => filterCondition = filters.PUBLIC}>
                    <Icon name="users"/>
                </button>
                {#if filterCondition !== filters.ALL}
                    <button class="btn btn-default btn-xs"
                            title="Clear filters and show all grids"
                            on:click={() => filterCondition = filters.ALL}>
                        <Icon name="ban"/>
                    </button>
                {/if}
            </span>
        </div>
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
                        <col width="10%">
                        <col width="45%">
                        <col width="45%">
                    </colgroup>
                    <thead>
                    <tr>
                        <th></th>
                        <th>Grid Name</th>
                        <th>Description</th>
                    </tr>
                    </thead>
                    <tbody>
                    {#each gridList as grid}
                        <tr class:selected={$gridDefinition?.id === grid?.gridId}
                            class="clickable"
                            on:click={() => onSelect(grid)}>
                            <td title={grid.kind === "PUBLIC" ? "Public" : "Private"}>
                                <Icon name={grid.kind === "PUBLIC" ? "users" : "user-secret"}/>
                            </td>
                            <td>
                                <button class="btn-skinny force-wrap text-left">
                                    <Icon name={determineSubjectIcon(grid?.subjectKind)}/> {grid?.name}
                                </button>
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