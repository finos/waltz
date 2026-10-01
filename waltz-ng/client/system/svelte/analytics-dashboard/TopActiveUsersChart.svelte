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

    $: call = (startDate && endDate) ? analyticsDashboardStore.findTopUsers(startDate, endDate, 15) : null;
    $: loading = call ? $call.status === REMOTE_API_STATUS.LOADING : true;
    $: error = call && $call.error ? "Failed to load top users data" : null;
    $: chartData = toChartData(call ? $call.data : null);

    function toChartData(data) {
        if (!data || !Array.isArray(data) || data.length === 0) {
            return { title: "Top Active Users by Hits Count", data: [] };
        }

        const transformedData = data
            .filter(item => item.counts > 0)
            .map(item => ({
                name: item.userId || 'Unknown',
                value: item.counts || 0
            }));

        return {
            title: "Top Active Users by Hits Count",
            data: transformedData
        };
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 20, right: 30, bottom: 120, left: 60};
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
            .domain([0, d3.max(chartData.data, d => d.value)])
            .nice()
            .range([height, 0]);

        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(x))
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", "rotate(-45)")
            .style("font-size", "10px");

        svg.append("g")
            .call(d3.axisLeft(y));

        const colorScale = d3.scaleOrdinal()
            .domain(chartData.data.map(d => d.name))
            .range(d3.schemeCategory10);

        svg.selectAll(".bar")
            .data(chartData.data)
            .enter().append("rect")
            .attr("class", "bar")
            .attr("x", d => x(d.name))
            .attr("width", x.bandwidth())
            .attr("y", d => y(d.value))
            .attr("height", d => height - y(d.value))
            .attr("fill", d => colorScale(d.name))
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
                    .html(`<strong>${d.name}</strong><br/>Hits Count: ${d.value.toLocaleString()}`)
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
            .text(d => d.value.toLocaleString());
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Top Active Users by Hits Count"}
        <span class="pull-right"><ChartInfo help="The users with the most page hits in the selected range."/></span>
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
