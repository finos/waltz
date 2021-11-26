<script>

    import _ from "lodash";
    import {termSearch} from "../../common";
    import Icon from "../../common/svelte/Icon.svelte";

    export let columnDefs = [];
    export let rowData = [];
    export let onSelectRow = () => console.log("selecting row")

    let qry = null;

    $: filteredRows = _.isEmpty(qry)
        ? rowData
        : termSearch(rowData, qry, _.map(columnDefs, d => d.field));

    $: totalAllocatedWidth = _.sumBy(columnDefs, d => _.get(d, "width", 0));

    $: unallocatedColumns = _.filter(columnDefs, d => _.isEmpty(d.width));

    $: unallocatedWidth = _.isEmpty(unallocatedColumns)
        ? 0
        : (100 - totalAllocatedWidth) / unallocatedColumns.length;

</script>

<div class="row">
    <div class="col-sm-12"
         style="padding-bottom: 1em">
        <input class="grid-search"
               style="display: inline-block"
               type="text"
               placeholder="Search..."
               bind:value={qry}/>
        <span class={_.isEmpty(qry) ? "" : "searching"}>
            <Icon name="search"/>
        </span>
    </div>
</div>

<div class="row">
    <div class="col-sm-12">
        <div class={_.size(rowData) > 10 ? "waltz-scroll-region-300" : ""}>
            <table class="table table-condensed table-hover small fixed-table">
                <colgroup>
                    {#each columnDefs as col}
                        <col width={_.get(col, ["width"],  `${unallocatedWidth}%`)}>
                    {/each}
                </colgroup>

                <thead>
                <tr>
                    {#each columnDefs as col}
                        <th>{col.name}</th>
                    {/each}
                </tr>
                </thead>

                <tbody>
                {#each filteredRows as row}
                    <tr class="clickable"
                        on:click={() => onSelectRow(row)}>
                        {#each columnDefs as col}
                            <td>{_.get(row, col.field.split("."), "-") || "-"}</td>
                        {/each}
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    </div>
</div>

<style>
    .grid-search {
        height: 34px;
        width: 90%;
        padding: 6px 12px;
        font-size: 14px;
        color: #555555;
        background-color: #fff;
        border: 1px solid #ccc;
        border-radius: 2px;
    }

    .searching {
        color: deepskyblue;
    }
</style>