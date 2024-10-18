<script>
    import {numberFormatter} from "../../../../../common/string-utils";
    import {scaleLinear} from "d3-scale";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";
    import {
        RenderModes
    } from "../../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";

    export let cellData = {};
    export let maxCost;
    export let width;
    export let height;
    export let renderMode;
    export let applicationsById = {};
    export let measurablesById = {};
    export let costKindsById = {};


    const layout = {
        costHeightProportion: 0.6,
        appCountHeightProportion: 0.4,
        padding: 0.05
    }

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


    const colorScale = scaleLinear()
        .domain([0, maxCost])
        .range(["#ffffff", "#76fd58"])
        .clamp(true);

</script>


<div>
    <svg class="content"
         viewBox="0 0 {width} {height}"
         width="100%"
         style={`background: ${cellData?.totalCost ? colorScale(cellData?.totalCost) : "#fff"}`}>
        <g>
            <text x={width / 2}
                  text-anchor="middle"
                  dominant-baseline="middle"
                  font-size={height * layout.costHeightProportion - height * layout.padding}
                  y={height * layout.costHeightProportion / 2 + height * layout.padding}>
                {#if cellData?.totalCost}
                    {numberFormatter(cellData?.totalCost, 2)}
                {:else}
                    -
                {/if}
            </text>
            <text x={width / 2}
                  text-anchor="middle"
                  dominant-baseline="middle"
                  font-size={height * layout.appCountHeightProportion - height * layout.padding}
                  y={height * layout.costHeightProportion + height * layout.appCountHeightProportion / 2}>
                # Apps: {cellData?.appCount || 0}
            </text>
        </g>
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

    svg {
        display: block;
    }
</style>