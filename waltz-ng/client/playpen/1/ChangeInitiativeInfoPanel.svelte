
<script>

    import {changeInitiativeStore} from "../../svelte-stores/change-initiative-store";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import _ from "lodash";
    import Markdown from "../../common/svelte/Markdown.svelte";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";

    export let primaryEntityRef;

    $: changeInitiativeCall = changeInitiativeStore.getById(primaryEntityRef.id);
    $: changeInitiative = $changeInitiativeCall.data;


    $: console.log({primaryEntityRef, changeInitiative});

</script>

{#if changeInitiative}
    <h4><EntityLink ref={changeInitiative}/></h4>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">External Id</td>
                <td width="50%">{changeInitiative.externalId}</td>
            </tr>
            <tr>
                <td width="50%">Kind</td>
                <td width="50%">{changeInitiative.changeInitiativeKind}</td>
            </tr>
            <tr>
                <td width="50%">Lifecycle</td>
                <td width="50%">{changeInitiative.lifecyclePhase}</td>
            </tr>
            <tr>
                <td width="50%">Start Date</td>
                <td width="50%">{changeInitiative.startDate}</td>
            </tr>
            <tr>
                <td width="50%">End Date</td>
                <td width="50%">{changeInitiative.endDate}</td>
            </tr>
        </tbody>
    </table>

    <div class="waip-description">
        <div class={_.size(changeInitiative.description) > 50 ? "waip-description-fade" : ""}></div>
        <Markdown text={changeInitiative.description}/>
    </div>

    <br>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>
{/if}


<style>

    .waip-description {
        position:relative;
        max-height:6em;
        overflow:hidden;
    }

    .waip-description-fade {
        position:absolute;
        top:2em;
        width:100%;
        height:4em;
        background: -webkit-linear-gradient(transparent, white);
        background: -o-linear-gradient(transparent, white);
        background: -moz-linear-gradient(transparent, white);
        background: linear-gradient(transparent, white);
    }


</style>