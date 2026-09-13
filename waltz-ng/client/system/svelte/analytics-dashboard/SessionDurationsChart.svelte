<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ChartInfo from "./ChartInfo.svelte";
    import * as d3 from "d3";
    import {analyticsDashboardStore} from "../../../svelte-stores/analytics-dashboard-store";
    import {REMOTE_API_STATUS} from "../../../common/constants";

    export let startDate;
    export let endDate;

    let svgContainer;
    let containerWidth;

    // Duration buckets (minutes). Session length is approximate: the span from a user's first to
    // last access within a day (see AccessLogDao.findSessionDurations).
    const BUCKETS = [
        {label: "< 15m", min: 0, max: 15},
        {label: "15-30m", min: 15, max: 30},
        {label: "30-60m", min: 30, max: 60},
        {label: "1-2h", min: 60, max: 120},
        {label: "2-4h", min: 120, max: 240},
        {label: "4h+", min: 240, max: Infinity}
    ];

    $: call = (startDate && endDate) ? analyticsDashboardStore.findSessionDurations(startDate, endDate) : null;
    $: loading = call ? $call.status === REMOTE_API_STATUS.LOADING : true;
    $: error = call && $call.error ? "Failed to load session duration data" : null;
    $: chartData = toChartData(call ? $call.data : null);

    function toChartData(data) {
        if (!data || !Array.isArray(data) || data.length === 0) {
            return { title: "Session Duration Distribution", data: [] };
        }

        // Bin each session by its duration in minutes.
        const counts = BUCKETS.map(b => ({ name: b.label, value: 0 }));
        data.forEach(item => {
            const mins = item.sessionDuration || 0;
            const idx = BUCKETS.findIndex(b => mins >= b.min && mins < b.max);
            if (idx >= 0) {
                counts[idx].value += 1;
            }
        });

        return {
            title: "Session Duration Distribution",
            data: counts
        };
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 20, right: 30, bottom: 50, left: 60};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        const x = d3.scaleBand()
            .domain(chartData.data.map(d => d.name))
            .range([0, width])
            .padding(0.2);

        const y = d3.scaleLinear()
            .domain([0, d3.max(chartData.data, d => d.value) || 1])
            .nice()
            .range([height, 0]);

        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(x))
            .selectAll("text")
            .style("font-size", "11px");

        svg.append("g")
            .call(d3.axisLeft(y));

        // Axis titles
        svg.append("text")
            .attr("transform", `translate(${width / 2}, ${height + 40})`)
            .attr("text-anchor", "middle")
            .style("font-size", "11px")
            .style("fill", "#666")
            .text("Session length");

        svg.selectAll(".bar")
            .data(chartData.data)
            .enter().append("rect")
            .attr("class", "bar")
            .attr("x", d => x(d.name))
            .attr("width", x.bandwidth())
            .attr("y", d => y(d.value))
            .attr("height", d => height - y(d.value))
            .attr("fill", "#26A69A")
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
                    .html(`<strong>${d.name}</strong><br/>Sessions: ${d.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function() {
                d3.select(this).attr("opacity", 1);
                d3.selectAll(".tooltip").remove();
            });

        svg.selectAll(".label")
            .data(chartData.data)
            .enter().append("text")
            .attr("class", "label")
            .attr("x", d => x(d.name) + x.bandwidth() / 2)
            .attr("y", d => y(d.value) - 5)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("fill", "#333")
            .text(d => d.value > 0 ? d.value.toLocaleString() : "");
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Session Duration Distribution"}
        <span class="pull-right"><ChartInfo help="How user session lengths are spread across duration bands in the selected range."/></span>
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
