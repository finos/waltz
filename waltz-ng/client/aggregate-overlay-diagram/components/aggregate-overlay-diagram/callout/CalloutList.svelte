<script>
    import {getContext} from "svelte";
    import _ from "lodash";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import Markdown from "../../../../common/svelte/Markdown.svelte";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import CalloutCreatePanel from "./CalloutCreatePanel.svelte";
    import CalloutDeletePanel from "./CalloutDeletePanel.svelte";
    import {userStore} from "../../../../svelte-stores/user-store";
    import systemRoles from "../../../../user/system-roles";
    import {determineCell} from "../aggregate-overlay-diagram-utils";


    const Modes = {
        VIEW: "VIEW",
        ADD: "ADD",
        DELETE: "DELETE"
    }

    let activeMode = Modes.VIEW;

    let hoveredCallout = getContext("hoveredCallout");
    let callouts = getContext("callouts");
    let selectedCallout = getContext("selectedCallout");
    let selectedInstance = getContext("selectedInstance");
    let svgDetail = getContext("svgDetail");
    let selectedCellId = getContext("selectedCellId");
    let selectedCellCallout = getContext("selectedCellCallout");
    let selectedOverlay = getContext("selectedOverlay");


    let permissionsCall = userStore.load();
    $: permissions = $permissionsCall?.data;

    $: hasEditPermissions = _.includes(permissions?.roles, systemRoles.AGGREGATE_OVERLAY_DIAGRAM_EDITOR.key) || false;

    function hover(callout) {
        $hoveredCallout = callout;
    }

    function leave() {
        $hoveredCallout = null;
    }

    function selectCallout(callout) {
        if ($selectedCallout?.id === callout.id) {
            $selectedCallout = null;
            $selectedOverlay = null;
        } else {
            $selectedCallout = callout;
            $selectedOverlay = Object.assign({}, {cellId: callout.cellExternalId});
        }
    }


    function setSelectedCell() {
        return (e) => {

            const clickedElem = e.target;
            const dataCell = determineCell(clickedElem);

            $selectedCellId = dataCell !== null
                ? dataCell.getAttribute("data-cell-id")
                : null;

            if ($selectedCellId == null) {
                return;
            }

            const existingCallout = _.find($callouts, c => c.cellExternalId === $selectedCellId);

            if (activeMode === Modes.ADD) {
                if (!_.isNil(existingCallout)) {
                    editCallout(existingCallout?.cellExternalId);
                } else {
                    addCallout();
                }
            } else {
                if (!_.isNil(existingCallout)) {
                    selectCallout(existingCallout);
                } else {
                    $selectedCallout = null;
                }
            }
        };
    }


    $: {
        if ($svgDetail) {
            $svgDetail.addEventListener("click", setSelectedCell())
        }
    }


    function cancel() {
        $selectedCellId = null;
        activeMode = Modes.VIEW
    }

    function addCallout() {
        $selectedCellCallout = emptyCallout;
        activeMode = Modes.ADD;
    }


    function editMode() {
        $selectedOverlay = null;
        addCallout();
    }

    function editCallout(cellExtId) {
        $selectedCellId = cellExtId;
        $selectedCellCallout = _.find($callouts, c => c.cellExternalId === cellExtId);
        activeMode = Modes.ADD;
    }

    function deleteCallout(callout) {
        $selectedCellId = callout.cellExternalId;
        $selectedCellCallout = _.find($callouts, c => c.cellExternalId === callout.cellExternalId);
        activeMode = Modes.DELETE;
    }

    const emptyCallout = {
        title: null,
        content: null,
        startColor: null,
        endColor: null,
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
            <colgroup>
                <col width="10%">
                <col width="90%">
            </colgroup>
            <tbody>
            {#each $callouts as callout, idx}
                <tr class:hovered={$hoveredCallout?.id === callout?.id || $selectedCallout?.id === callout?.id}
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
                            {#if hasEditPermissions}
                                <br>
                                <div style="border-top: 1px solid #eee;padding-top: 0.5em">
                                    <button class="btn btn-skinny"
                                            on:click={() => editCallout(callout.cellExternalId)}>
                                        <Icon name="pencil"/>
                                        Edit
                                    </button>
                                    |
                                    <button class="btn btn-skinny"
                                            on:click={() => deleteCallout(callout)}>
                                        <Icon name="trash"/>
                                        Delete
                                    </button>
                                </div>
                            {/if}
                        </td>
                    </tr>
                {/if}
            {/each}
            </tbody>
        </table>
        {#if hasEditPermissions}
            <button class="btn btn-skinny"
                    on:click={() => editMode()}>
                <Icon name="plus"/>
                Add a callout
            </button>
        {/if}
    {:else}
        <NoData type="info">
            There are no callouts for this instance
            {#if hasEditPermissions}
                , would you like
                <button class="btn btn-skinny"
                        on:click={() => editMode()}>
                    to add one?
                </button>
            {/if}
        </NoData>
    {/if}
{:else if activeMode === Modes.ADD}
    {#key $selectedCellCallout}
        <CalloutCreatePanel on:cancel={cancel}/>
    {/key}
{:else if activeMode === Modes.DELETE}
    <CalloutDeletePanel on:cancel={cancel}/>
{/if}

<style>

    .hovered {
        background-color: #fffbdc;
        transition: background-color 0.5s;
    }

</style>