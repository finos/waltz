<script>

    import _ from "lodash";
    import {termSearch} from "../index";
    import Icon from "./Icon.svelte";
    import {truncateMiddle} from "../string-utils";

    export let columnDefs = [];
    export let rowData = [];
    export let onSelectRow = () => console.log("selecting row");
    export let clickable = true;

    let qry = null;
    let sortColumn = null;
    let sortDirection = null;


    $: filteredRows = _.isEmpty(qry)
        ? rowData
        : termSearch(rowData, qry, _.map(columnDefs, d => d.field));

    $: {
        if (sortColumn && sortDirection) {
            const fieldParts = sortColumn.split(".");
            filteredRows = _.orderBy(filteredRows, [r => {
                const v = _.get(r, fieldParts, null);
                if (_.isString(v)) return v.toLowerCase();
                if (_.isFinite(v)) return v;
                if (_.isDate(v)) return new Date(v);
                return "";
            }], [sortDirection === 'asc' ? 'asc' : 'desc']);
        }
    }

    $: totalAllocatedWidth = _.sumBy(columnDefs, d => _.get(d, "width", 0));

    $: unallocatedColumns = _.filter(columnDefs, d => _.isEmpty(d.width));

    $: unallocatedWidth = _.isEmpty(unallocatedColumns)
        ? 0
        : (100 - totalAllocatedWidth) / unallocatedColumns.length;

    function toggleSort(col) {
        if (col.sortable === false) return;

        const field = col.field;

        if (sortColumn !== field) {
            sortColumn = field;
            sortDirection = 'asc';
            return;
        }

        if (sortDirection === 'asc') {
            sortDirection = 'desc';
        } else if (sortDirection === 'desc') {
            sortDirection = null;
            sortColumn = null;
        } else {
            sortDirection = 'asc';
        }
    }

    function isSortable(col) {
        return col?.sortable ?? false;
    }

    function getColVal(col, row) {
        const rawVal = _.get(
            row,
            col.field.split("."),
            "-") || "-";

        return col.maxLength
            ? truncateMiddle(rawVal, col.maxLength)
            : rawVal;
    }


    function getColTitle(col, row) {
        return col.maxLength
            ? _.get(
                row,
                col.field.split("."),
                "-")
            : null; // not truncated, therefore no title needed
    }

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
        <div class={_.size(rowData) > 6 ? "waltz-scroll-region-300" : ""} style="padding: 0">
            <table class="table table-condensed table-hover small fixed-table">
                <colgroup>
                    {#each columnDefs as col}
                        <col width={_.get(col, ["width"],  `${unallocatedWidth}%`)}>
                    {/each}
                </colgroup>

                <thead>
                <tr>
                    {#each columnDefs as col}
                        <th class:sortable={isSortable(col)}
                            on:click={() => isSortable(col) ? toggleSort(col) : null}
                            title={isSortable(col) ? "Click to sort" : null}>
                            <span>{col.name}</span>
                            <div class="sort-icon">
                                {#if isSortable(col)}
                                    {#if sortColumn === col.field}
                                        {#if sortDirection === 'asc'}
                                            <Icon name="arrow-down-short-wide"/>
                                        {:else if sortDirection === 'desc'}
                                            <Icon name="arrow-down-wide-short"/>
                                        {/if}
                                    {/if}
                                    {#if sortColumn !== col.field}
                                        <Icon name="sort"/>
                                    {/if}
                                {/if}
                            </div>
                        </th>
                    {/each}
                </tr>
                </thead>

                <tbody>
                {#each filteredRows as row}
                    <tr class:clickable={clickable}
                        on:click={() => onSelectRow(row)}>
                        {#each columnDefs as col}
                            <td title={getColTitle(col, row)}>
                                {#if col.cellRendererComponent}
                                    <svelte:component this={col.cellRendererComponent} {...col.cellRendererProps(row)} />
                                {:else if col.cellRenderer}
                                    {@html col.cellRenderer(row)}
                                {:else}
                                    {getColVal(col, row)}
                                {/if}
                            </td>
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

    thead th {
        position: sticky;
        top: 0;
        background-color: white;
        z-index: 1;
    }

    th.sortable {
        cursor: pointer;
        user-select: none;
    }

    .sort-icon {
        display: inline-block;
        margin-left: 6px;
        vertical-align: middle;
    }
</style>