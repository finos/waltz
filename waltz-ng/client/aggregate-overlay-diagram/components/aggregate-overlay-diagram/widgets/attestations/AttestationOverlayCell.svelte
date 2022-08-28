<script>
    import {scaleLinear} from "d3-scale";
    import {RenderModes} from "../../aggregate-overlay-diagram-utils";
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import _ from "lodash";
    import {threshold, cutoff} from "./store"

    export let cellData = {}; // {attestations: [{ appId: 123, attestedBy: "bob", attestedAt: "2022-12-12" }, ...], cellExternalId: "ABC"}
    export let width;
    export let height;
    export let renderMode;
    export let maxApps = 0;
    export let applicationsById = {};

    function mkBaseStats(total = 0) {
        return {never: 0, passed: 0, failed: 0, total};
    }

    $:r = scaleLinear()
        .domain([0, maxApps])
        .range([0, height / 2 - 5]);

    $: rows = _
        .chain(cellData.attestations)
        .map(d => {
            const attestedAt = d.attestedAt
                ? new Date(d.attestedAt)
                : null;

            return {
                attestedBy: d.attestedBy,
                attestedAt,
                application: applicationsById[d.appId],
                state: attestedAt === null
                    ? "NEVER"
                    : attestedAt < $cutoff
                        ? "FAILED"
                        : "PASSED"
            }
        })
        .orderBy(d => d.application.name)
        .value();

    $: stats = _
        .reduce(
            rows,
            (acc, r) => {
                if (r.state === "NEVER") {
                    acc.never = acc.never + 1;
                } else if (r.state === "FAILED") {
                    acc.failed = acc.failed + 1;
                } else {
                    acc.passed = acc.passed + 1;
                }
                return acc;
            },
            mkBaseStats(_.size(rows)));

    $: enrichedStats = Object.assign(
        {},
        stats,
        {
            percentagePassed: Math.round((stats.passed / stats.total) * 100),
            overallState: stats.total === 0
                ? "NA"
                : ((stats.passed / stats.total) >= ($threshold / 100))
                    ? "PASSED"
                    : "FAILED"
        });

</script>


<div>
    <svg class="content"
         viewBox="0 0 {width} {height}">
        {#if enrichedStats.total > 0}
            <circle stroke="#79CDF6"
                    fill="#E0F6F6"
                    cx={height / 2}
                    cy={height / 2}
                    r={r(stats.total)}/>
            <text x={height + 4}
                  fill={enrichedStats.overallState === 'PASSED'
                            ? "green"
                            : "red"}
                  font-size="16"
                  dy={height / 2 + 6}>
                {enrichedStats.percentagePassed}%
            </text>

            <text y={height / 2 - 3}
                  x={width / 1.9}
                  font-size="8">
                Attested / Total
            </text>
            <text y={height / 2 + 7}
                  x={width / 1.9}
                  font-size="8">
                <tspan>{enrichedStats.passed}</tspan> / <tspan>{enrichedStats.total}</tspan>
            </text>
        {/if}
    </svg>

    {#if renderMode === RenderModes.FOCUSED}
        <table class="summary-table table table-condensed table-hover small">
            <thead>
            <tr>
                <th>Application</th>
                <th>Asset Code</th>
                <th>Attested By</th>
                <th>Attested At</th>
            </tr>
            </thead>
            <tbody>
                {#each rows as row}
                <tr class:danger={row.state === "FAILED" || row.state === "NEVER"}
                    class:success={row.state === "PASSED"}>
                    <td>
                        <EntityLink showIcon={false}
                                    ref={row.application}/>
                    </td>
                    <td>
                        {_.get(row, ["application", 'assetCode'], "?")}
                    </td>
                    <td>
                        {row.attestedAt?.toDateString() || ""}
                    </td>
                    <td>
                        {row.attestedBy || ""}
                    </td>
                </tr>
                {/each}
            </tbody>
        </table>
    {/if}
</div>


<style>
    svg {
        display: block;
    }
</style>