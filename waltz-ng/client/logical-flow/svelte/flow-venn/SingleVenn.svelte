<script>
    export let label="??";
    export let data;
    export let sizeScale;


    function mkName(node) {
        return node;
    }

    function mkNameForFlow(flow) {
        const from = mkName(flow.source);
        const to = mkName(flow.target);

        return `${from} to ${to}`;
    }
</script>

<table class="table">
    <tr>
        <td>{label}</td>
        <td>
            <svg width="250"
                 height="100"
                 viewBox="0 0 1000 400">
                <circle class="group groupA" cx="300" cy="200" r={sizeScale(data.sizes.a)}/>
                <circle class="group groupB" cx="500" cy="200" r={sizeScale(data.sizes.b)}/>
            </svg>
        </td>
        <td>
            <table class="table table-condensed small">
                {#each data.flows as flow}
                    <tr>
                        <th>{mkNameForFlow(flow)}</th>
                        <td>{flow.count}</td>
                    </tr>
                {/each}
            </table>
        </td>
        <td></td>
    </tr>
</table>


<style>
    .group {
        stroke: #655e5e;
    }

    .groupA {
        fill: #804780;
        opacity: 0.5;
    }

    .groupB {
        fill: #7dce7d;
        opacity: 0.5;
    }
</style>