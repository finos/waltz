<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount } from "svelte";

    export let startDate;
    export let endDate;
    export let period;
    export let key = 0; // For forcing re-render

    let svgContainer;
    let containerWidth;
    let chartData = null;
    let loading = true;
    let error = null;

    $: if (key && startDate && endDate) {
        loadData();
    }

    onMount(() => {
        // Initial load will be handled by reactive statement
    });

        // Helper function to format period labels
        function formatPeriodLabel(key, period) {
            if (period === 'week') {
                return `Week ${key}`;
            } else if (period === 'month') {
                const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                                  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
                if (key.includes('-')) {
                    const [year, month] = key.split('-');
                    return `${monthNames[parseInt(month) - 1]} ${year}`;
                }
                return key;
            } else if (period === 'day') {
                if (key.includes('-')) {
                    return new Date(key).toLocaleDateString();
                }
                return key;
            } else if (period === 'year') {
                return key;
            }
            return key;
        }
        async function fetchAccessLogData(startDate, endDate, period) {
            try {
                const url = `/api/access-log/summary/period/${period}?startDate=${startDate}&endDate=${endDate}`;
                console.log('AccessLogTrendsChart fetching:', url);
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
                console.log('AccessLogTrendsChart received data:', data);

                // Transform the data to match our chart format
                let chartData = [];

                if (Array.isArray(data)) {
                    console.log('Data is array with length:', data.length);
                    chartData = data.map(item => ({
                        name: formatPeriodLabel(item.period || item.date || item.key, period),
                        value: item.counts || item.count || item.value || item.total || 0,
                        distinctUserCount: item.distinctUserCount || 0
                    }));
                } else if (typeof data === 'object') {
                    console.log('Data is object with keys:', Object.keys(data));
                    chartData = Object.entries(data).map(([key, value]) => ({
                        name: formatPeriodLabel(key, period),
                        value: value || 0,
                        distinctUserCount: 0
                    }));
                }

                console.log('Transformed chartData:', chartData);
                return {
                    title: "Access Log Analytics",
                    data: chartData
                };
            } catch (error) {
                console.error('Access Log API failed:', error);
                throw error;
            }
        }

    async function loadData() {
        loading = true;
        error = null;
        console.log('AccessLogTrendsChart loading data with key:', key);
        try {
            const response = await fetchAccessLogData(startDate, endDate, period);

            // Check if response has valid data
            if (!response || !response.data || response.data.length === 0) {
                chartData = { title: response?.title || "Access Log Trends", data: [] };
            } else {
                // Filter out zero values if all values are zero
                const hasNonZeroData = response.data.some(item => item.value > 0);
                chartData = hasNonZeroData ? response : { title: response.title, data: [] };
            }
            loading = false;
        } catch (err) {
            console.error('Error loading chart data:', err);
            error = 'Failed to load chart data';
            loading = false;
        }
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        if (!svgContainer || !chartData || !chartData.data) {
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

        const margin = {top: 40, right: 30, bottom: 40, left: 60};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // X scale (band for grouped bars)
        const x0 = d3.scaleBand()
            .domain(chartData.data.map(d => d.name))
            .range([0, width])
            .padding(0.2);

        const x1 = d3.scaleBand()
            .domain(['accesses', 'users'])
            .range([0, x0.bandwidth()])
            .padding(0.05);

        // Y scale (linear) - use max of both values
        const y = d3.scaleLinear()
            .domain([0, d3.max(chartData.data, d => Math.max(d.value, d.distinctUserCount))])
            .nice()
            .range([height, 0]);

        // Color scale
        const color = d3.scaleOrdinal()
            .domain(['accesses', 'users'])
            .range(['#2196F3', '#FF9800']);

        // Add X axis
        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(x0))
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", "rotate(-45)");

        // Add Y axis
        svg.append("g")
            .call(d3.axisLeft(y));

        // Add legend
        const legend = svg.append("g")
            .attr("transform", `translate(${width - 150}, -25)`);

        legend.append("rect")
            .attr("x", 0)
            .attr("width", 15)
            .attr("height", 15)
            .attr("fill", "#2196F3");

        legend.append("text")
            .attr("x", 20)
            .attr("y", 12)
            .style("font-size", "12px")
            .text("Hits Count");

        legend.append("rect")
            .attr("x", 92)
            .attr("width", 15)
            .attr("height", 15)
            .attr("fill", "#FF9800");

        legend.append("text")
            .attr("x", 114)
            .attr("y", 12)
            .style("font-size", "12px")
            .text("Users Count");

        // Create groups for each period
        const groups = svg.selectAll(".group")
            .data(chartData.data)
            .enter().append("g")
            .attr("class", "group")
            .attr("transform", d => `translate(${x0(d.name)}, 0)`);

        // Add access count bars
        groups.append("rect")
            .attr("class", "bar-accesses")
            .attr("x", d => x1('accesses'))
            .attr("width", x1.bandwidth())
            .attr("y", d => y(d.value))
            .attr("height", d => height - y(d.value))
            .attr("fill", "#2196F3")
            .attr("stroke", "#1565C0")
            .attr("stroke-width", 1)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("fill", "#42A5F5");
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
                    .html(`<strong>${d.name}</strong><br/>Hits Count: ${d.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("fill", "#2196F3");
                d3.selectAll(".tooltip").remove();
            });

        // Add distinct user bars
        groups.append("rect")
            .attr("class", "bar-users")
            .attr("x", d => x1('users'))
            .attr("width", x1.bandwidth())
            .attr("y", d => y(d.distinctUserCount))
            .attr("height", d => height - y(d.distinctUserCount))
            .attr("fill", "#FF9800")
            .attr("stroke", "#F57C00")
            .attr("stroke-width", 1)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("fill", "#FFB74D");
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
                    .html(`<strong>${d.name}</strong><br/>Users Count: ${d.distinctUserCount.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("fill", "#FF9800");
                d3.selectAll(".tooltip").remove();
            });

        // Add value labels on top of access count bars
        groups.append("text")
            .attr("class", "label")
            .attr("x", d => x1('accesses') + x1.bandwidth() / 2)
            .attr("y", d => y(d.value) - 5)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("fill", "#333")
            .text(d => d.value.toLocaleString());

        // Add value labels on top of distinct user bars
        groups.append("text")
            .attr("class", "label")
            .attr("x", d => x1('users') + x1.bandwidth() / 2)
            .attr("y", d => y(d.distinctUserCount) - 5)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("fill", "#333")
            .text(d => d.distinctUserCount.toLocaleString());
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Access Log Trends"}
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


