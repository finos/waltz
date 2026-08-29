<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import * as d3 from "d3";
    import { onMount, onDestroy } from "svelte";

    export let startDate;
    export let endDate;
    export let period = "month"; // Optional - not used by this chart's API
    export let key = 0; // For forcing re-render

    let svgContainer;
    let containerWidth;
    let chartData = null;
    let loading = true;
    let error = null;
    let lastLoadKey = 0; // Track last loaded key

    // Load data when key changes
    $: if (key && startDate && endDate) {
        loadData();
    }

    onMount(() => {
        // Initial load will be handled by reactive statement
    });

    onDestroy(() => {
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
        console.log('EntityKindBubbleChart loading data with key:', key, 'period:', period);
        try {
            // Fetch both entity kind and child kind data
            const [entityResponse, childResponse] = await Promise.all([
                fetch(`/api/change-log-summaries/analytics/by-entity-kind?startDate=${startDate}&endDate=${endDate}&limit=15`, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    }
                }),
                fetch(`/api/change-log-summaries/analytics/by-child-kind?startDate=${startDate}&endDate=${endDate}&limit=15`, {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    }
                })
            ]);

            if (!entityResponse.ok || !childResponse.ok) {
                throw new Error(`HTTP error! entity: ${entityResponse.status}, child: ${childResponse.status}`);
            }

            const entityData = await entityResponse.json();
            const childData = await childResponse.json();

            // Combine both datasets
            const combinedData = [];

            // Add entity kind data
            if (entityData && Object.keys(entityData).length > 0) {
                Object.entries(entityData)
                    .filter(([entityKind, count]) => count > 0)
                    .forEach(([entityKind, count]) => {
                        combinedData.push({
                            name: entityKind,
                            value: count,
                            displayName: formatEntityKind(entityKind),
                            type: 'Entity',
                            category: 'parent'
                        });
                    });
            }

            // Add child kind data
            if (childData && Object.keys(childData).length > 0) {
                Object.entries(childData)
                    .filter(([childKind, count]) => childKind && childKind !== 'null' && count > 0)
                    .forEach(([childKind, count]) => {
                        combinedData.push({
                            name: childKind,
                            value: count,
                            displayName: formatEntityKind(childKind),
                            type: 'Child Entity',
                            category: 'child'
                        });
                    });
            }

            // Check if combined data is empty
            if (combinedData.length === 0) {
                chartData = { title: "Changes by Entity & Child Entity Kind", data: [] };
                loading = false;
                return;
            }

            chartData = {
                title: "Changes by Entity & Child Entity Kind",
                data: combinedData
            };
            loading = false;
        } catch (err) {
            console.error('Entity/Child Kind Bubble API failed:', err);
            error = 'Failed to load entity kind data';
            loading = false;
        }
    }

    function formatEntityKind(entityKind) {
        if (!entityKind) return 'Unknown';
        return entityKind.split('_')
                        .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
                        .join(' ');
    }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        // Clear previous chart
        d3.select(svgContainer).select("svg").remove();

        const margin = {top: 40, right: 40, bottom: 40, left: 40};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // Create a hierarchy for the pack layout
        const root = d3.hierarchy({children: chartData.data})
            .sum(d => d.value)
            .sort((a, b) => b.value - a.value);

        // Create pack layout
        const pack = d3.pack()
            .size([width, height])
            .padding(5);

        pack(root);

        // Color scale - different colors for entity vs child entity
        const colorScale = d3.scaleOrdinal()
            .domain(['parent', 'child'])
            .range([d3.schemeSet3, d3.schemeTableau10]);

        const getColor = (d) => {
            const scheme = d.data.category === 'parent' ? d3.schemeSet3 : d3.schemeTableau10;
            const index = chartData.data.filter(x => x.category === d.data.category).findIndex(x => x.name === d.data.name);
            return scheme[index % scheme.length];
        };

        // Size scale for additional visual encoding
        const sizeScale = d3.scaleSqrt()
            .domain([0, d3.max(chartData.data, d => d.value)])
            .range([10, Math.min(width, height) / 8]);

        // Create bubbles
        const node = svg.selectAll(".node")
            .data(root.leaves())
            .enter().append("g")
            .attr("class", "node")
            .attr("transform", d => `translate(${d.x},${d.y})`);

        // Add circles
        node.append("circle")
            .attr("r", d => d.r)
            .attr("fill", d => getColor(d))
            .attr("stroke", d => d.data.category === 'parent' ? "#4169E1" : "#FF8C00")
            .attr("stroke-width", 2)
            .attr("opacity", 0.8)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("opacity", 1).attr("stroke-width", 3);

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
                    .html(`<strong>${d.data.displayName}</strong><br/>Type: ${d.data.type}<br/>Kind: ${d.data.name}<br/>Changes: ${d.data.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("opacity", 0.8).attr("stroke-width", 2);
                d3.selectAll(".tooltip").remove();
            });

        // Add text labels
        node.append("text")
            .selectAll("tspan")
            .data(d => {
                const radius = d.r;
                if (radius < 25) return []; // Don't show text for very small bubbles

                const lines = [];

                // Split long names into multiple lines
                const words = d.data.displayName.split(' ');
                if (words.length > 2 && radius > 40) {
                    // Multiple lines for large bubbles
                    const mid = Math.ceil(words.length / 2);
                    lines.push(words.slice(0, mid).join(' '));
                    lines.push(words.slice(mid).join(' '));
                } else if (radius > 30) {
                    lines.push(d.data.displayName);
                } else {
                    // Abbreviated for smaller bubbles
                    lines.push(words.map(w => w.charAt(0)).join(''));
                }

                // Add count if there's space
                if (radius > 35) {
                    lines.push(d.data.value.toLocaleString());
                }

                return lines;
            })
            .enter().append("tspan")
            .attr("x", 0)
            .attr("y", function(d, i, nodes) {
                try {
                    const node = nodes[i];
                    if (!node || !node.parentNode) return 0;
                    const parent = d3.select(node.parentNode).datum();
                    if (!parent || !parent.r) return 0;
                    const lineHeight = parent.r > 40 ? 12 : 10;
                    const totalHeight = nodes.length * lineHeight;
                    return (i * lineHeight) - (totalHeight / 2) + lineHeight;
                } catch (e) {
                    return 0;
                }
            })
            .attr("text-anchor", "middle")
            .style("font-size", function(d) {
                try {
                    const node = this;
                    if (!node || !node.parentNode) return "9px";
                    const parent = d3.select(node.parentNode).datum();
                    if (!parent || !parent.r) return "9px";
                    if (parent.r > 50) return "11px";
                    if (parent.r > 30) return "9px";
                    return "8px";
                } catch (e) {
                    return "9px";
                }
            })
            .style("font-weight", (d, i) => i === 0 ? "bold" : "normal")
            .style("fill", "white")
            .style("text-shadow", "1px 1px 1px rgba(0,0,0,0.8)")
            .text(d => d);

        // Add title
        svg.append("text")
            .attr("x", width / 2)
            .attr("y", -15)
            .attr("text-anchor", "middle")
            .style("font-size", "16px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text("Changes by Entity & Child Entity Kind (Bubble Size = Change Count)");

        // Add legend with color coding explanation
        const legend = svg.append("g")
            .attr("transform", `translate(${width - 200}, 20)`);

        legend.append("text")
            .attr("x", 0)
            .attr("y", 0)
            .style("font-size", "12px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text("Legend:");

        // Parent entity legend item
        const parentLegend = legend.append("g")
            .attr("transform", `translate(0, 20)`);

        parentLegend.append("circle")
            .attr("r", 6)
            .attr("fill", d3.schemeSet3[0])
            .attr("stroke", "#4169E1")
            .attr("stroke-width", 2);

        parentLegend.append("text")
            .attr("x", 12)
            .attr("y", 4)
            .style("font-size", "10px")
            .style("fill", "#333")
            .text("Entity Kind (Blue Border)");

        // Child entity legend item
        const childLegend = legend.append("g")
            .attr("transform", `translate(0, 38)`);

        childLegend.append("circle")
            .attr("r", 6)
            .attr("fill", d3.schemeTableau10[0])
            .attr("stroke", "#FF8C00")
            .attr("stroke-width", 2);

        childLegend.append("text")
            .attr("x", 12)
            .attr("y", 4)
            .style("font-size", "10px")
            .style("fill", "#333")
            .text("Child Entity Kind (Orange Border)");

        // Add top 5 items
        const topEntities = chartData.data.slice(0, 5);

        legend.append("text")
            .attr("x", 0)
            .attr("y", 60)
            .style("font-size", "11px")
            .style("font-weight", "bold")
            .style("fill", "#333")
            .text("Top 5 by Count:");

        const legendItems = legend.selectAll(".legend-item")
            .data(topEntities)
            .enter().append("g")
            .attr("class", "legend-item")
            .attr("transform", (d, i) => `translate(0, ${78 + i * 16})`);

        legendItems.append("text")
            .attr("x", 0)
            .attr("y", 4)
            .style("font-size", "9px")
            .style("fill", "#333")
            .text(d => `${d.displayName}: ${d.value.toLocaleString()}`);
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Changes by Entity & Child Entity Kind"}
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


