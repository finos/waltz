<script>
    import {getContext} from "svelte";
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";

    let hoveredCallout = getContext("hoveredCallout");
    let callouts = getContext("callouts");
    let selectedCallout = getContext("selectedCallout");

    function hover(callout) {
        $hoveredCallout = callout;
    }

    function leave(callout) {
        $hoveredCallout = null;
    }

    function selectCallout(callout) {
        console.log({callout});
        if ($selectedCallout?.id === callout.id) {
            $selectedCallout = null;
        } else {
            $selectedCallout = callout;
        }
    }

</script>

{#if !_.isEmpty($callouts)}
    <h4>Callout annotations:</h4>
    <div class="small help-block">
        <Icon name="info-circle"/>
        Click to view callout detail
    </div>
    <table class="table table-condensed">
        <tbody>
        {#each $callouts as callout, idx}
            <tr class:hovered={$hoveredCallout?.id === callout?.id}
                class="clickable"
                on:click={() => selectCallout(callout)}
                on:mouseenter={() => hover(callout)}
                on:mouseleave={() => leave(callout)}>
                <td>{idx + 1}</td>
                <td>{callout.title}</td>
            </tr>
            {#if $selectedCallout?.id === callout?.id}
                <tr>
                    <td></td>
                    <td>
                        <Markdown text={callout.content}/>
                    </td>
                </tr>
            {/if}
        {/each}
        </tbody>
    </table>
{/if}

<style>

    .hovered {
        background-color: #fffbdc;
        transition: background-color 0.5s;
    }

</style>