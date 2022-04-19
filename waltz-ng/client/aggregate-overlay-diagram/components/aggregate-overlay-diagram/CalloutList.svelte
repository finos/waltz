<script>

    import {callouts, hoveredCallout} from "../../aggregate-overlay-diagram-store";


    function hover(callout) {
        $hoveredCallout = callout;
    }

    function leave(callout) {
        $hoveredCallout = null;
    }

    $: console.log({hcall: $hoveredCallout});
</script>


<table class="table table-condensed">
    <tbody>
    {#each $callouts as callout, idx}
        <tr class:hovered={$hoveredCallout?.id === callout?.id}
            on:mouseenter={() => hover(callout)}
            on:mouseleave={() => leave(callout)}>
            <td>{idx + 1}</td>
            <td>{callout.title}</td>
            <td>{callout.content}</td>
        </tr>
    {/each}
    </tbody>
</table>


<style>

    .hovered {
        background-color: #fffbdc;
    }
</style>