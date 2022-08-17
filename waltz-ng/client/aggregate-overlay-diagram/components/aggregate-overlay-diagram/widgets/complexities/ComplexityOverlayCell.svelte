<script>
    import {numberFormatter} from "../../../../../common/string-utils";
    import {scaleSqrt} from "d3-scale";
    import {RenderModes} from "../../aggregate-overlay-diagram-utils";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";

    export let cellData = {};
    export let maxComplexity;
    export let width;
    export let height;
    export let renderMode;
    export let applicationsById = {};
    export let complexityKindsById = {};

    $:r = scaleSqrt()
        .domain([0, maxComplexity])
        .range([0, height / 2]);

    $: tr = r(cellData?.totalComplexity) || 0;

    $: tableRows = _
        .chain(cellData.complexities)
        .map(d => Object.assign(
            {},
            d,
            {
                application: applicationsById[d.appId],
            }))
        .orderBy(d => d.application?.name)
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
            {#if cellData?.totalComplexity}
                {numberFormatter(cellData?.totalComplexity, 2)}
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
                <th class="number-cell">Complexity</th>
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
                    <td class="number-cell text-muted">
                        {row.complexityScore}
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