<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount, onDestroy, tick } from "svelte";

    export let startDate;
    export let endDate;
    export let period = "month"; // Optional - not used by this chart's API
    export let key = 0; // For forcing re-render

    let svgContainer;
    let containerWidth;
    let chartData = null;
    let loading = true;
    let error = null;
    let lastLoadKey = 0;

    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    // Load data when key changes
    $: if (key && startDate && endDate) {
        loadData();
    }

    onMount(() => {
        if (startDate && endDate) {
            loadData();
        }
    });

    onDestroy(() => {
        // Clean up any pending timeouts or DOM references
        if (svgContainer) {
            try {
                d3.select(svgContainer).selectAll("*").remove();
            } catch (e) {
                // Ignore cleanup errors
            }
        }
    });

    async function loadData() {
        if (!startDate || !endDate) {
            loading = false;
            return;
        }

        // Simple duplicate prevention
        const currentKey = key;
        if (currentKey === lastLoadKey) {
            return;
        }
        lastLoadKey = currentKey;

        loading = true;
        error = null;
        chartData = null;
        console.log('DayOfWeekRadarChart loading data with key:', key, 'period:', period);
        try {
            // Fetch both access log and change log data for comparison
            const [accessResponse, changeResponse] = await Promise.all([
                fetch(`/api/access-log/analytics/activity-by-day?startDate=${startDate}&endDate=${endDate}`),
                fetch(`/api/change-log-summaries/analytics/by-day?startDate=${startDate}&endDate=${endDate}`)
            ]);

            if (!accessResponse.ok || !changeResponse.ok) {
                throw new Error(`HTTP error! Access: ${accessResponse.status}, Change: ${changeResponse.status}`);
            }

            const accessData = await accessResponse.json();
            const changeData = await changeResponse.json();

            console.log('DayOfWeekRadarChart access data:', accessData);
            console.log('DayOfWeekRadarChart change data:', changeData);

            // Create data for all 7 days
            const accessByDay = [];
            const changesByDay = [];

            for (let day = 0; day < 7; day++) {
                // Map JavaScript day (0=Sunday) to database day (1=Monday...7=Sunday)
                const dbDay = day === 0 ? 7 : day; // Convert Sunday from 0 to 7

                // Access log data
                const accessFound = accessData.find(item => item.dayOfWeek === dbDay);
                accessByDay.push({
                    day: day,
                    dayName: dayNames[day],
                    value: accessFound ? accessFound.counts : 0
                });

                // Change log data (note: API returns Map<Integer, Long>)
                const changeFound = changeData[dbDay];
                changesByDay.push({
                    day: day,
                    dayName: dayNames[day],
                    value: changeFound || 0
                });
            }

            // Check if all values are zero
            const hasAccessData = accessByDay.some(item => item.value > 0);
            const hasChangeData = changesByDay.some(item => item.value > 0);

            if (!hasAccessData && !hasChangeData) {
                chartData = { title: "Activity by Day of Week", datasets: [] };
                loading = false;
                return;
            }

            chartData = {
                title: "Activity by Day of Week",
                datasets: [
                    {
                        name: "Hits",
                        data: accessByDay,
                        color: "#2196F3"
                    },
                    {
                        name: "Changes",
                        data: changesByDay,
                        color: "#FF9800"
                    }
                ]
            };
            loading = false;
        } catch (err) {
            console.error('Day of Week Radar API failed:', err);
            console.error('Access response status:', accessResponse?.status);
            console.error('Change response status:', changeResponse?.status);
            error = `Failed to load day of week data: ${err.message}`;
            loading = false;
        }
    }

    $: if (svgContainer && chartData && chartData.datasets && chartData.datasets.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        if (!svgContainer || !chartData || !chartData.datasets) {
            return;
        }

        // Clear previous chart safely
        try {
            const container = d3.select(svgContainer);
            if (!container.empty()) {
                container.selectAll("svg").remove();
            }
        } catch (e) {
            console.warn('Error clearing chart:', e);
        }

        const margin = {top: 40, right: 120, bottom: 40, left: 120};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;
        const radius = Math.min(width, height) / 2;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left + width/2},${margin.top + height/2})`);

        // Calculate max value for scaling
        const maxValue = d3.max(chartData.datasets, dataset =>
            d3.max(dataset.data, d => d.value)
        );

        // Angle scale
        const angleScale = d3.scaleLinear()
            .domain([0, 7])
            .range([0, 2 * Math.PI]);

        // Radius scale
        const radiusScale = d3.scaleLinear()
            .domain([0, maxValue])
            .range([0, radius]);

        // Draw concentric circles (grid)
        const gridLevels = 5;
        for (let level = 1; level <= gridLevels; level++) {
            svg.append("circle")
                .attr("r", (radius / gridLevels) * level)
                .attr("fill", "none")
                .attr("stroke", "#e0e0e0")
                .attr("stroke-width", 1);
        }

        // Draw axis lines
        dayNames.forEach((day, i) => {
            const angle = angleScale(i) - Math.PI / 2; // Start from top
            const x = Math.cos(angle) * radius;
            const y = Math.sin(angle) * radius;

            svg.append("line")
                .attr("x1", 0)
                .attr("y1", 0)
                .attr("x2", x)
                .attr("y2", y)
                .attr("stroke", "#e0e0e0")
                .attr("stroke-width", 1);
        });

        // Add day labels
        dayNames.forEach((day, i) => {
            const angle = angleScale(i) - Math.PI / 2;
            const labelRadius = radius + 20;
            const x = Math.cos(angle) * labelRadius;
            const y = Math.sin(angle) * labelRadius;

            svg.append("text")
                .attr("x", x)
                .attr("y", y)
                .attr("text-anchor", "middle")
                .attr("dominant-baseline", "middle")
                .style("font-size", "12px")
                .style("font-weight", "bold")
                .style("fill", "#333")
                .text(day);
        });

        // Draw datasets
        chartData.datasets.forEach((dataset, datasetIndex) => {
            // Create line generator
            const line = d3.lineRadial()
                .angle((d, i) => angleScale(i) - Math.PI / 2)
                .radius(d => radiusScale(d.value))
                .curve(d3.curveLinearClosed);

            // Draw area
            const area = d3.areaRadial()
                .angle((d, i) => angleScale(i) - Math.PI / 2)
                .innerRadius(0)
                .outerRadius(d => radiusScale(d.value))
                .curve(d3.curveLinearClosed);

            svg.append("path")
                .datum(dataset.data)
                .attr("d", area)
                .attr("fill", dataset.color)
                .attr("fill-opacity", 0.2)
                .attr("stroke", dataset.color)
                .attr("stroke-width", 2);

            // Draw line
            svg.append("path")
                .datum(dataset.data)
                .attr("d", line)
                .attr("fill", "none")
                .attr("stroke", dataset.color)
                .attr("stroke-width", 3);

            // Draw points
            dataset.data.forEach((d, i) => {
                const angle = angleScale(i) - Math.PI / 2;
                const r = radiusScale(d.value);
                const x = Math.cos(angle) * r;
                const y = Math.sin(angle) * r;

                svg.append("circle")
                    .attr("cx", x)
                    .attr("cy", y)
                    .attr("r", 4)
                    .attr("fill", dataset.color)
                    .attr("stroke", "#fff")
                    .attr("stroke-width", 2)
                    .style("cursor", "pointer")
                    .on("mouseover", function(event) {
                        d3.select(this).attr("r", 6);

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
                            .html(`<strong>${d.dayName}</strong><br/>${dataset.name}: ${d.value.toLocaleString()}`)
                            .style("left", (event.pageX + 10) + "px")
                            .style("top", (event.pageY - 10) + "px");
                    })
                    .on("mouseout", function() {
                        d3.select(this).attr("r", 4);
                        d3.selectAll(".tooltip").remove();
                    });
            });
        });

        // Add legend
        const legend = svg.append("g")
            .attr("transform", `translate(${radius + 40}, ${-chartData.datasets.length * 15 / 2})`);

        const legendItems = legend.selectAll(".legend-item")
            .data(chartData.datasets)
            .enter().append("g")
            .attr("class", "legend-item")
            .attr("transform", (d, i) => `translate(0, ${i * 25})`);

        legendItems.append("circle")
            .attr("r", 6)
            .attr("fill", d => d.color);

        legendItems.append("text")
            .attr("x", 15)
            .attr("y", 5)
            .style("font-size", "12px")
            .style("fill", "#333")
            .text(d => d.name);

        // Add scale labels
        for (let level = 1; level <= gridLevels; level++) {
            const value = (maxValue / gridLevels) * level;
            svg.append("text")
                .attr("x", 5)
                .attr("y", -(radius / gridLevels) * level)
                .style("font-size", "10px")
                .style("fill", "#666")
                .text(value.toLocaleString());
        }
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Activity by Day of Week"}
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
            {:else if !chartData || !chartData.datasets || chartData.datasets.length === 0}
                <div class="alert alert-info text-center">
                    <i class="fa fa-info-circle"></i>
                    No data found
                </div>
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


