<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount } from "svelte";

    export let startDate;
    export let endDate;
    export let key = 0; // For forcing re-render

    let svgContainer;
    let containerWidth;
    let chartData = null;
    let loading = true;
    let error = null;

    $: if (key) {
        loadData();
    }

    onMount(() => {
        loadData();
    });

    async function loadData() {
        loading = true;
        error = null;
        try {
            const url = `/api/access-log/analytics/top-pages?startDate=${startDate}&endDate=${endDate}&limit=15`;
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();

            console.log('TopPagesChart received data:', data);
            console.log('TopPagesChart data type:', typeof data, 'isArray:', Array.isArray(data), 'length:', data?.length);

            // Check if data is empty or invalid
            if (!data || !Array.isArray(data) || data.length === 0) {
                console.warn('TopPagesChart: No valid data received');
                chartData = { title: "Top Pages by Access Count", data: [] };
                loading = false;
                return;
            }

            // Transform the data for the chart, filtering out zero values
            const transformedData = data
                .filter(item => item.counts > 0)
                .map(item => ({
                    name: formatPageName(item.state),
                    value: item.counts || 0,
                    fullName: item.state
                }));

            chartData = {
                title: "Top Pages by HITS Count",
                data: transformedData
            };
            loading = false;
        } catch (err) {
            console.error('Top Pages API failed:', err);
            error = 'Failed to load top pages data';
            loading = false;
        }
    }

    function formatPageName(state) {
        if (!state) return 'Unknown';
        // Extract the main page name from state (e.g., "main.application.view" -> "Application View")
        const parts = state.split('.');
        if (parts.length > 1) {
            return parts.slice(1).map(part =>
                part.split('-').map(word =>
                    word.charAt(0).toUpperCase() + word.slice(1)
                ).join(' ')
            ).join(' - ');
        }
        return state;
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        // Clear previous chart
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

        // X scale (ordinal)
        const x = d3.scaleBand()
            .domain(chartData.data.map(d => d.name))
            .range([0, width])
            .padding(0.2);

        // Y scale (linear)
        const y = d3.scaleLinear()
            .domain([0, d3.max(chartData.data, d => d.value)])
            .nice()
            .range([height, 0]);

        // Add X axis
        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(x))
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", "rotate(-45)")
            .style("font-size", "10px");

        // Add Y axis
        svg.append("g")
            .call(d3.axisLeft(y));

        // Color scale
        const colorScale = d3.scaleOrdinal()
            .domain(chartData.data.map(d => d.name))
            .range(d3.schemeCategory10);

        // Create bars
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
                // Tooltip and highlight
                d3.select(this).attr("opacity", 0.8);

                const tooltip = d3.select("body").append("div")
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
                    .html(`<strong>${d.name}</strong><br/>Full Path: ${d.fullName}<br/>Hits Count: ${d.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("opacity", 1);
                d3.selectAll(".tooltip").remove();
            });

        // Add value labels on top of bars
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
        {chartData?.title || "Top Pages by Access Count"}
    </div>
    <div slot="content">
        <div bind:clientWidth={containerWidth} style="position: relative; min-height: 400px;">
            {#if loading}
                <LoadingPlaceholder/>
            {:else if error}
                <div class="alert alert-danger">
                    <i class="fa fa-warning"></i>
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


