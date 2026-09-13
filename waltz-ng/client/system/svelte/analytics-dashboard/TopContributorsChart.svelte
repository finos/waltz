<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ChartInfo from "./ChartInfo.svelte";
    import * as d3 from "d3";
    import {onDestroy} from "svelte";
    import {analyticsDashboardStore} from "../../../svelte-stores/analytics-dashboard-store";
    import {REMOTE_API_STATUS} from "../../../common/constants";

    export let startDate;
    export let endDate;
    export let period = "MONTH";

    let svgContainer;
    let containerWidth;

    const MAX_CONTRIBUTORS = 15;

    // Period-aware trends endpoint when a non-default frequency is chosen, otherwise the
    // simpler totals endpoint. Either way we chart the per-contributor total here.
    $: call = (startDate && endDate && period)
        ? (period.toUpperCase() !== 'MONTH'
            ? analyticsDashboardStore.findTopContributorsTrends(period, startDate, endDate, MAX_CONTRIBUTORS)
            : analyticsDashboardStore.findTopContributors(startDate, endDate, MAX_CONTRIBUTORS))
        : null;
    $: loading = call ? $call.status === REMOTE_API_STATUS.LOADING : true;
    $: error = call && $call.error ? "Failed to load top contributors data" : null;
    $: chartData = toChartData(call ? $call.data : null);

    onDestroy(() => {
        if (svgContainer) {
            try {
                d3.select(svgContainer).selectAll("*").remove();
            } catch (e) {
                // Ignore cleanup errors
            }
        }
    });

    // Sum each contributor's counts to a single total and keep the top N. The trends
    // endpoint returns { user: { period: count } }; the totals endpoint returns { user: count }.
    function toChartData(data) {
        if (!data || Object.keys(data).length === 0) {
            return { title: "Top Contributors by Change Count", data: [] };
        }

        const transformedData = Object.entries(data)
            .map(([userId, value]) => ({
                name: userId,
                value: typeof value === 'object'
                    ? Object.values(value).reduce((sum, c) => sum + c, 0)
                    : value
            }))
            .filter(item => item.value > 0)
            .sort((a, b) => b.value - a.value)
            .slice(0, MAX_CONTRIBUTORS);

        return {
            title: "Top Contributors by Change Count",
            data: transformedData
        };
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        d3.select(svgContainer).select("svg").remove();

        const data = chartData.data;
        const margin = {top: 10, right: 60, bottom: 30, left: 140};
        const width = containerWidth - margin.left - margin.right;
        const barHeight = 22;
        const height = Math.max(data.length * barHeight, 80);

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        const y = d3.scaleBand()
            .domain(data.map(d => d.name))
            .range([0, height])
            .padding(0.2);

        const x = d3.scaleLinear()
            .domain([0, d3.max(data, d => d.value)])
            .nice()
            .range([0, width]);

        // Y axis (contributor names)
        svg.append("g")
            .call(d3.axisLeft(y))
            .selectAll("text")
            .style("font-size", "11px");

        // X axis (change counts)
        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(x).ticks(5))
            .selectAll("text")
            .style("font-size", "10px");

        const color = d3.scaleOrdinal()
            .domain(data.map(d => d.name))
            .range(d3.schemeCategory10);

        svg.selectAll(".bar")
            .data(data)
            .enter().append("rect")
            .attr("class", "bar")
            .attr("y", d => y(d.name))
            .attr("height", y.bandwidth())
            .attr("x", 0)
            .attr("width", d => x(d.value))
            .attr("fill", d => color(d.name))
            .attr("stroke", "#fff")
            .attr("stroke-width", 1)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("opacity", 0.8);

                d3.select("body").append("div")
                    .attr("class", "tooltip")
                    .style("position", "absolute")
                    .style("background", "rgba(0,0,0,0.9)")
                    .style("color", "white")
                    .style("padding", "10px")
                    .style("border-radius", "6px")
                    .style("font-size", "13px")
                    .style("pointer-events", "none")
                    .style("z-index", "1000")
                    .style("box-shadow", "0 4px 8px rgba(0,0,0,0.3)")
                    .html(`<strong>${d.name}</strong><br/>Changes: ${d.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function() {
                d3.select(this).attr("opacity", 1);
                d3.selectAll(".tooltip").remove();
            });

        // Value labels at the end of each bar
        svg.selectAll(".label")
            .data(data)
            .enter().append("text")
            .attr("class", "label")
            .attr("x", d => x(d.value) + 4)
            .attr("y", d => y(d.name) + y.bandwidth() / 2)
            .attr("dominant-baseline", "middle")
            .style("font-size", "10px")
            .style("fill", "#333")
            .text(d => d.value.toLocaleString());
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Top Contributors by Change Count"}
        <span class="pull-right"><ChartInfo help="The users who recorded the most change-log entries in the selected range."/></span>
    </div>
    <div slot="content">
        <div bind:clientWidth={containerWidth} style="position: relative; min-height: 400px;">
            {#if loading}
                <LoadingPlaceholder/>
            {:else if error}
                <div class="alert alert-danger">
                    <Icon name="warning"/>
                    {error}
                </div>
            {:else if !chartData || !chartData.data || chartData.data.length === 0}
                <NoData/>
            {:else}
                <div bind:this={svgContainer}></div>
            {/if}
        </div>
    </div>
</SubSection>

<style>
    :global(.tooltip) {
        opacity: 0.9;
    }
</style>
