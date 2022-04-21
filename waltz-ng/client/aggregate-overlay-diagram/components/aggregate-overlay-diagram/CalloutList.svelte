<script>

    import {getContext} from "svelte";
    import _ from "lodash";

    let hoveredCallout = getContext("hoveredCallout");
    let callouts = getContext("callouts");

    function hover(callout) {
        $hoveredCallout = callout;
    }

    function leave(callout) {
        $hoveredCallout = null;
    }

</script>

{#if !_.isEmpty($callouts)}
    <h4>Callout annotations:</h4>
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
{/if}

<style>

    .hovered {
        background-color: #fffbdc;
    }
</style>