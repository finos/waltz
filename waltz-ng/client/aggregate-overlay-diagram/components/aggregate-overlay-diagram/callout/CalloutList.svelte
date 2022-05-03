<script>
    import {getContext} from "svelte";
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import CalloutCreatePanel from "./CalloutCreatePanel.svelte";


    const Modes = {
        VIEW: "VIEW",
        ADD: "ADD"
    }

    let activeMode = Modes.VIEW;

    let hoveredCallout = getContext("hoveredCallout");
    let callouts = getContext("callouts");
    let selectedCallout = getContext("selectedCallout");
    let selectedInstance = getContext("selectedInstance");
    let svgDetail = getContext("svgDetail");
    let selectedCellId = getContext("selectedCellId");
    let selectedCellCallout = getContext("selectedCellCallout");


    function hover(callout) {
        $hoveredCallout = callout;
    }

    function leave() {
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

    function determineCell(elem) {
        if (elem == null) {
            return null;
        } else {
            const cellId = elem.getAttribute("data-cell-id");
            if (!_.isNil(cellId)) {
                return elem;
            } else {
                return determineCell(elem.parentElement)
            }

        }
    }


    $: {
        if ($svgDetail && activeMode === Modes.ADD) {

            $svgDetail.addEventListener(
                "click",
                (e) => {
                    const clickedElem = e.target;
                    const dataCell = determineCell(clickedElem);
                    $selectedCellId = dataCell == null
                        ? null
                        : dataCell.getAttribute("data-cell-id");

                    console.log({cs: $callouts});
                })
        }
    }


    $: {
        if ($svgDetail) {
            const outers = $svgDetail.querySelectorAll(".outer");
            _.forEach(
                outers,
                cell => {
                    const parent = cell.parentElement;
                    const cellId = parent.getAttribute("data-cell-id");
                    cell.setAttribute("style", `opacity: ${$selectedCellId === cellId ? "0.7" : "1"}`)
                });
        }
    }

    function cancel() {
        $selectedCellId = null;
        activeMode = Modes.VIEW
    }


    const emptyCallout = {
        title: null,
        content: null,
        startColor: null,
        endColor: null,
    }

    $: {
        if (selectedCellId) {
            $selectedCellCallout = _.find($callouts, c => c.cellExternalId === $selectedCellId) || emptyCallout;
        }
    }

</script>

{#if activeMode === Modes.VIEW}
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
                    on:mouseleave={() => leave()}>
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
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD}>
            <Icon name="plus"/>
            Add a callout
        </button>
    {:else}
        <NoData type="info">
            There are no callouts for this instance, would you like
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.ADD}>
                to add one?
            </button>
        </NoData>
    {/if}
{:else if activeMode === Modes.ADD}
    {#if $selectedCellCallout}
        <CalloutCreatePanel on:cancel={cancel}/>
    {/if}
{/if}

<style>

    .hovered {
        background-color: #fffbdc;
        transition: background-color 0.5s;
    }

</style>