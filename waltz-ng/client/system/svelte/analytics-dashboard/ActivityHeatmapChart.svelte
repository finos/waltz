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
            const url = `/api/access-log/analytics/activity-by-hour?startDate=${startDate}&endDate=${endDate}`;
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

            console.log('ActivityHeatmapChart received data:', data);
            console.log('ActivityHeatmapChart data type:', typeof data, 'isArray:', Array.isArray(data));

            // Check if data is empty
            if (!data || !Array.isArray(data)) {
                console.warn('ActivityHeatmapChart: Data is not an array or is empty');
                chartData = { title: "User Activity Heatmap by Hour", data: [] };
                loading = false;
                return;
            }

            // Create data for all 24 hours, filling in zeros for missing hours
            const hourlyData = [];
            for (let hour = 0; hour < 24; hour++) {
                const found = data.find(item => item.hour === hour);
                hourlyData.push({
                    hour: hour,
                    value: found ? found.counts : 0,
                    label: formatHour(hour)
                });
            }

            // Check if all values are zero
            const hasNonZeroData = hourlyData.some(item => item.value > 0);

            chartData = {
                title: "User Activity Heatmap by Hour",
                data: hasNonZeroData ? hourlyData : []
            };
            loading = false;
        } catch (err) {
            console.error('Activity Heatmap API failed:', err);
            error = 'Failed to load activity heatmap data';
            loading = false;
        }
    }

    function formatHour(hour) {
        if (hour === 0) return '12 AM';
        if (hour === 12) return '12 PM';
        if (hour < 12) return `${hour} AM`;
        return `${hour - 12} PM`;
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        // Clear previous chart
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 40, right: 30, bottom: 60, left: 60};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // Calculate cell dimensions
        const cellWidth = width / 24;
        const cellHeight = height;

        // Color scale
        const maxValue = d3.max(chartData.data, d => d.value);
        const colorScale = d3.scaleSequential()
            .domain([0, maxValue])
            .interpolator(d3.interpolateBlues);

        // Create heatmap cells
        svg.selectAll(".heatmap-cell")
            .data(chartData.data)
            .enter().append("rect")
            .attr("class", "heatmap-cell")
            .attr("x", d => d.hour * cellWidth)
            .attr("y", 0)
            .attr("width", cellWidth - 1)
            .attr("height", cellHeight - 1)
            .attr("fill", d => d.value === 0 ? "#f0f0f0" : colorScale(d.value))
            .attr("stroke", "#fff")
            .attr("stroke-width", 1)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                // Tooltip and highlight
                d3.select(this).attr("stroke", "#333").attr("stroke-width", 2);

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
                    .html(`<strong>Hour: ${d.label}</strong><br/>Activity Count: ${d.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("stroke", "#fff").attr("stroke-width", 1);
                d3.selectAll(".tooltip").remove();
            });

        // Add hour labels
        svg.selectAll(".hour-label")
            .data(chartData.data.filter((d, i) => i % 2 === 0)) // Show every other hour to avoid crowding
            .enter().append("text")
            .attr("class", "hour-label")
            .attr("x", d => d.hour * cellWidth + cellWidth / 2)
            .attr("y", height + 15)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("fill", "#666")
            .text(d => d.hour);

        // Add title
        svg.append("text")
            .attr("x", width / 2)
            .attr("y", -15)
            .attr("text-anchor", "middle")
            .style("font-size", "14px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text("Activity by Hour of Day");

        // Add legend
        const legendWidth = 200;
        const legendHeight = 10;
        const legend = svg.append("g")
            .attr("transform", `translate(${width - legendWidth}, ${height + 35})`);

        // Create gradient for legend
        const defs = svg.append("defs");
        const gradient = defs.append("linearGradient")
            .attr("id", "legend-gradient");

        gradient.selectAll("stop")
            .data(d3.range(0, 1.1, 0.1))
            .enter().append("stop")
            .attr("offset", d => `${d * 100}%`)
            .attr("stop-color", d => colorScale(d * maxValue));

        // Legend rectangle
        legend.append("rect")
            .attr("width", legendWidth)
            .attr("height", legendHeight)
            .style("fill", "url(#legend-gradient)")
            .attr("stroke", "#ccc");

        // Legend labels
        legend.append("text")
            .attr("x", 0)
            .attr("y", legendHeight + 12)
            .style("font-size", "10px")
            .style("fill", "#666")
            .text("0");

        legend.append("text")
            .attr("x", legendWidth)
            .attr("y", legendHeight + 12)
            .attr("text-anchor", "end")
            .style("font-size", "10px")
            .style("fill", "#666")
            .text(maxValue.toLocaleString());
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Activity Heatmap by Hour"}
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


