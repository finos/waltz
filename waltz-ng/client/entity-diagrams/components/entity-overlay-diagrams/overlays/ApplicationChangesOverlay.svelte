<script>

    import {scaleBand, scaleOrdinal, scaleSqrt} from "d3-scale";
    import {axisLeft} from "d3-axis";
    import _ from "lodash";
    import {appChangeAdditionalYears} from "./overlay-store";
    import {select} from "d3-selection";
    import {
        RenderModes
    } from "../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import {schemeBuGn, schemeOrRd} from "d3";
    import {reverse} from "../../../../common/list-utils";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";


    export let cellData = [];
    export let maxOutboundCount = 0;
    export let maxInboundCount = 0;

    export let renderMode;
    export let height;
    export let width;

    let svgElem;
    let selectedChanges = null;
    let selectedDirection = null;

    const appCountProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0.25;
    const axisProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0;
    const paddingProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0;
    const graphHeightProportion = 1 - (axisProportion + appCountProportion + paddingProportion * 2);
    const graphWidthProportion = 1 - axisProportion;

    const minYear = new Date().getFullYear();
    $: futureYear = getFutureYear($appChangeAdditionalYears)

    // Date picker selects up to the 1st of the next year, only need up to the end of the previous year included in graph
    $: maxYear = futureYear - 1;
    $: years = getIncrements(minYear, maxYear) || [];

    function getFutureYear(additionalYears) {
        const currentYear = new Date().getFullYear();
        return currentYear + additionalYears;
    }

    function getIncrements(current, end) {
        let incrementArray = [];
        let currentIncrement = current;
        while (currentIncrement <= end) {
            incrementArray.push(Number(currentIncrement));
            currentIncrement = currentIncrement + 1;
        }
        return incrementArray;
    }

    $: quarters = _.size(years) * 4;

    // Fix so that at short range there is still gradual change, ensure that if more years than colors the last value is taken
    $: requiredColoursCount = _.min([9, _.max([5, _.size(years)])]);

    $: outboundColors = reverse(schemeOrRd[requiredColoursCount]);
    $: inboundColors = reverse(schemeBuGn[requiredColoursCount]);

    $: inboundColorScale = scaleOrdinal()
        .domain(_.map(years, d => d.toString())) //years)
        .range(inboundColors);

    $: outboundColorScale = scaleOrdinal()
        .domain(_.map(years, d => d.toString())) //years)
        .range(outboundColors);

    $: inboundColorScale.unknown(_.last(inboundColors));
    $: outboundColorScale.unknown(_.last(outboundColors));

    $: countScale = scaleSqrt()
        .domain([countScaleMin, countScaleMax])
        .range([height * graphHeightProportion, 0])

    $: yearScale = scaleBand()
        .domain(years)
        .range([0, width * graphWidthProportion]);

    $: yAxis = axisLeft(countScale)
        .tickSizeInner(0)
        .ticks([countScaleMax, countScaleMin, 0])
        .tickSizeOuter(height * 0.01)
        .tickPadding(height * 0.01);

    $: columnScale = scaleBand()
        .domain(getIncrements(1, quarters))
        .range([0, width * graphWidthProportion]);

    $: cellMaxOutbound = !_.isEmpty(cellData) ? _.maxBy(cellData.outboundCounts, d => d.count) : 0;
    $: cellMaxInbound = !_.isEmpty(cellData) ? _.maxBy(cellData.inboundCounts, d => d.count) : 0;

    $: countScaleMin = _.max([maxOutboundCount, _.get(cellMaxOutbound, "count", 0)]) * -1;
    $: countScaleMax = _.max([maxInboundCount,  _.get(cellMaxInbound, "count", 0)]);

    function mkTitle(quarter, count) {
        return `${quarter.quarterName} ${quarter.year}: ${count} applications`
    }

    function clickChart(changeInfo, direction) {
        if(_.isEqual(selectedChanges, changeInfo)) {
            selectedChanges = null;
            selectedDirection = null
        } else {
            selectedChanges = changeInfo;
            selectedDirection = direction;
        }
    }

</script>

<div class="content">

    <div>
        <svg viewBox={`0 0 ${width} ${height}`}
             bind:this={svgElem}
             style="background: white">
            <g>
                <text transform={`translate(${width * graphWidthProportion / 2} ${height - (height * appCountProportion / 2)})`}
                      text-anchor="middle"
                      font-size={height * appCountProportion}
                      dominant-baseline="middle">
                    # Apps: {cellData?.currentAppCount || 0} (+{cellData?.totalInboundCount} / -{cellData?.totalOutboundCount})
                </text>
            </g>
            <g>
                {#if cellData.outboundCounts}
                    <g class="outbound">
                        {#each cellData.outboundCounts as outboundCount}
                            <rect x={columnScale(outboundCount.quarter.additionalQuarters)}
                                  y={countScale(0)}
                                  width={yearScale.bandwidth() / 4}
                                  height={countScale(outboundCount.count * -1) - countScale(0)}
                                  fill={outboundColorScale(outboundCount.quarter.year)}
                                  stroke-width="0">
                            </rect>
                            <rect x={columnScale(outboundCount.quarter.additionalQuarters)}
                                  y={countScale(0)}
                                  on:click={() => clickChart(outboundCount, "Outbound")}
                                  on:keydown={d => clickChart(outboundCount, "Outbound")}
                                  width={yearScale.bandwidth() / 4}
                                  height={height * graphHeightProportion - countScale(0)}
                                  style="pointer-events: all"
                                  fill="none"
                                  stroke-width="0">
                                <title>{mkTitle(outboundCount.quarter, outboundCount.count)}</title>
                            </rect>
                        {/each}
                    </g>
                {/if}
                {#if cellData.inboundCounts}
                    <g class="inbound">
                        {#each cellData.inboundCounts as inboundCount}
                            <rect x={columnScale(inboundCount.quarter.additionalQuarters)}
                                  y={countScale(inboundCount.count)}
                                  width={yearScale.bandwidth() / 4}
                                  height={countScale(0) - countScale(inboundCount.count)}
                                  fill={inboundColorScale(inboundCount.quarter.year)}
                                  pointer-events="none"
                                  stroke-width="0">
                            </rect>
                            <rect x={columnScale(inboundCount.quarter.additionalQuarters)}
                                  y="0"
                                  on:click={() => clickChart(inboundCount, "Inbound")}
                                  on:keydown={d => clickChart(inboundCount, "Inbound")}
                                  width={yearScale.bandwidth() / 4}
                                  height={countScale(0)}
                                  style="pointer-events: all; opacity: 0.3"
                                  fill="none"
                                  stroke-width="0">
                                <title>{mkTitle(inboundCount.quarter, inboundCount.count)}</title>
                            </rect>
                        {/each}
                    </g>
                {/if}
                <g class="midline">
                    <line x1={0}
                          y1={countScale(0)}
                          x2={width * graphWidthProportion}
                          y2={countScale(0)}
                          stroke="#ccc"
                          stroke-width={height * 0.01}
                          opacity="0.8"/>
                </g>
            </g>
            <g>
                {#if renderMode === RenderModes.FOCUSED}
                    <g>
                        {#each years as year}
                            <text transform={`translate(${yearScale(year) + yearScale.bandwidth() / 2} ${height * graphHeightProportion + height * paddingProportion + (height * axisProportion / 2)})`}
                                  text-anchor="middle"
                                  font-size={height * axisProportion}
                                  dominant-baseline="middle">
                                {year}
                            </text>
                        {/each}
                    </g>
                    <g>
                        <text transform={`translate(${width}, ${countScale(countScaleMax)})`}
                              text-anchor="end"
                              font-size={height * axisProportion}
                              dominant-baseline="hanging">
                            {countScaleMax}
                        </text>
                        <text transform={`translate(${width}, ${countScale(countScaleMin)})`}
                              text-anchor="end"
                              font-size={height * axisProportion}
                              dominant-baseline="auto">
                            {countScaleMin}
                        </text>
                        <text transform={`translate(${width}, ${countScale(0)})`}
                              text-anchor="end"
                              font-size={height * axisProportion}
                              dominant-baseline="middle">
                            {0}
                        </text>
                    </g>
                {/if}
            </g>
        </svg>
    </div>

    {#if renderMode === RenderModes.FOCUSED}
        {#if selectedChanges}
            <div style="padding-top: 1em">
                <p>Showing {selectedChanges.count} {selectedDirection} changes for {selectedChanges.quarter.quarterName} {selectedChanges.quarter.year}</p>
                <table class="table table-condensed small">
                    <colgroup>
                        <col width="30%"/>
                        <col width="70%"/>
                    </colgroup>
                    <thead>
                    <tr>
                        <th>Date</th>
                        <th>App</th>
                    </tr>
                    </thead>
                    <tbody>
                        {#each _.orderBy(selectedChanges?.changes, d => d.date) as change}
                            <tr>
                                <td>{change.date}</td>
                                <td><EntityLink ref={change.appRef}/></td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
        {:else }
            <div class="help-block">Select a bar on the diagram above to see the breakdown of changes</div>
        {/if}
    {/if}
</div>