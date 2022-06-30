<script>
    import {numberFormatter} from "../../../../../common/string-utils";
    import {scaleSqrt} from "d3-scale";
    import {RenderModes} from "../../aggregate-overlay-diagram-utils";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";

    export let cellData = {};
    export let maxCost;
    export let width;
    export let height;
    export let renderMode;
    export let applicationsById = {};
    export let measurablesById = {};
    export let costKindsById = {};

    $:r = scaleSqrt()
        .domain([0, maxCost])
        .range([0, height / 2]);

    $: tr = r(cellData?.totalCost) || 0;

    $: tableRows = _
        .chain(cellData.measurableCosts)
        .map(d => Object.assign(
            {},
            d,
            {
                application: applicationsById[d.appId],
                measurable: measurablesById[d.measurableId],
                costKind: costKindsById[d.costKindId],
            }))
        .orderBy(d => d.application.name)
        .value();

</script>


<div>
    <svg class="content"
         viewBox="0 0 {width} {height}">
        <circle r={tr}
                fill="#c6eeff"
                stroke="#25b0ff"
                stroke-width="0.5"
                cx={width * 0.9}
                cy={height / 2}/>
        <text x={width * 0.5}
              text-anchor="middle"
              font-size={height * 0.8}
              y={height / 2 + height * 0.4}>
            {#if cellData?.totalCost}
                {numberFormatter(cellData?.totalCost, 2)}
            {:else}
                -
            {/if}
        </text>

    </svg>

    {#if renderMode === RenderModes.FOCUSED}
        <table class="summary-table table table-condensed table-hover small">
            <thead>
            <tr>
                <th>Application</th>
                <th>Asset Code</th>
                <th>Measurable</th>
                <th>Cost Kind</th>
                <th class="number-cell">Overall Cost</th>
                <th class="number-cell">Allocation Percentage</th>
                <th class="number-cell">Allocated Cost</th>
            </tr>
            </thead>
            <tbody>
                {#each tableRows as row}
                <tr>
                    <td>
                        <EntityLink showIcon={false}
                                    ref={row.application}/>
                    </td>
                    <td>
                        {_.get(row, ["application", 'assetCode'], "?")}
                    </td>
                    <td>
                        <EntityLink showIcon={false}
                                    ref={row.measurable}/>
                    </td>
                    <td>
                        {_.get(row, ["costKind", 'name'], "?")}
                    </td>
                    <td class="number-cell text-muted">
                        {numberFormatter(row.overallCost, 2)}
                    </td>
                    <td class="number-cell"
                        class:derived={row.allocationDerivation === 'DERIVED'}>
                        {row.allocationPercentage}%
                    </td>
                    <td class="number-cell"
                        class:derived={row.allocationDerivation === 'DERIVED'}>
                        {numberFormatter(row.allocatedCost, 2)}
                    </td>
                </tr>
                {/each}
            </tbody>
        </table>
    {/if}
</div>


<style>
    .summary-table .number-cell {
        text-align: right
    }

    .summary-table .derived {
        font-style: italic;
    }
</style>