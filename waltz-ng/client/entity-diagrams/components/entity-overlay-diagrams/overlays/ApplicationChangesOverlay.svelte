<script>

    import {scaleBand, scaleOrdinal, scaleSqrt} from "d3-scale";
    import {axisLeft} from "d3-axis";
    import _ from "lodash";
    import {appChangeAdditionalYears} from "./overlay-store";
    import {
        RenderModes
    } from "../../../../aggregate-overlay-diagram/components/aggregate-overlay-diagram/aggregate-overlay-diagram-utils";
    import {schemeBuGn, schemeOrRd} from "d3";
    import {reverse} from "../../../../common/list-utils";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import {waltzFontColor, waltzLinkColor} from "../../../../common/colors";


    export let cellData = [];
    export let maxOutboundCount = 0;
    export let maxInboundCount = 0;

    export let renderMode;
    export let height;
    export let width;

    const DetailModes = {
        APP_LIST: "APP_LIST",
        CHANGE_LIST: "CHANGE_LIST",
        NONE: "NONE"
    }

    let svgElem;
    let selectedChanges = null;
    let selectedDirection = null;
    let activeMode = DetailModes.NONE;

    const appCountProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0.25;
    const axisProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0;
    const paddingProportion = renderMode === RenderModes.FOCUSED ? 0.1 : 0.05;
    const graphHeightProportion = 1 - (axisProportion + appCountProportion + paddingProportion * 2);
    const graphWidthProportion = 1 - axisProportion ;

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
            activeMode = DetailModes.NONE;
        } else {
            selectedChanges = changeInfo;
            selectedDirection = direction;
            activeMode = DetailModes.CHANGE_LIST;
        }
    }


    function clickAppCount() {
        if(activeMode === DetailModes.APP_LIST) {
            activeMode = DetailModes.NONE;
        } else {
            activeMode = DetailModes.APP_LIST;
        }
    }

</script>

<div class="content">

    <div>
        <svg viewBox={`0 0 ${width} ${height}`}
             bind:this={svgElem}
             class:focused={renderMode === RenderModes.FOCUSED}
             style="background: white">
            <g class="app-summary">
                <rect x="0"
                      y={height - (height * appCountProportion + height * paddingProportion)}
                      on:click={() => clickAppCount()}
                      on:keydown={() => clickAppCount()}
                      width={width * graphWidthProportion}
                      height={height * appCountProportion + height * paddingProportion}
                      style="pointer-events: all; opacity: 0.3"
                      fill="none"
                      stroke-width="0">
                </rect>
                <text transform={`translate(${width * graphWidthProportion / 2} ${height - (height * appCountProportion / 2)})`}
                      text-anchor="middle"
                      font-size={height * appCountProportion}
                      fill={renderMode === RenderModes.FOCUSED ? waltzLinkColor : waltzFontColor}
                      dominant-baseline="middle"
                      style="pointer-events: none">
                    # Apps: {cellData?.currentAppCount || 0} (+{cellData?.totalInboundCount || 0} / -{cellData?.totalOutboundCount || 0})
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
                                  style="pointer-events: all; opacity: 0.3"
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
                        {#if !_.isEmpty(cellData.inboundCounts)}
                            <text transform={`translate(${width}, ${countScale(countScaleMax)})`}
                                  text-anchor="end"
                                  font-size={height * axisProportion}
                                  dominant-baseline="hanging">
                                {countScaleMax}
                            </text>
                        {/if}
                        {#if !_.isEmpty(cellData.outboundCounts)}
                            <text transform={`translate(${width}, ${countScale(countScaleMin)})`}
                                  text-anchor="end"
                                  font-size={height * axisProportion}
                                  dominant-baseline="auto">
                                {countScaleMin}
                            </text>
                        {/if}
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
        {#if activeMode === DetailModes.CHANGE_LIST}
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
        {:else if activeMode === DetailModes.APP_LIST}
            <div style="padding-top: 1em">
                <p>Showing all applications related to this group cell</p>
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
                        {#each _.orderBy(cellData?.currentApplications, d => d.name) as app}
                            <tr>
                                <td>{app.externalId}</td>
                                <td><EntityLink ref={app}/></td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
        {:else if activeMode === DetailModes.NONE}
            <div class="help-block">Select a bar on the diagram above to see the breakdown of changes, or select the app count to see the full list of apps currently associated</div>
        {/if}
    {/if}
</div>


<style type="text/css">

    .focused .app-summary rect:hover {
        fill: #ddd;
    }

    .focused .outbound rect:hover {
        fill: #ddd;
    }

    .focused .inbound rect:hover {
        fill: #ddd;
    }

</style>