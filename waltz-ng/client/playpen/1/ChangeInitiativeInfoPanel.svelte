
<script>

    import {changeInitiativeStore} from "../../svelte-stores/change-initiative-store";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import DescriptionFade from "./DescriptionFade.svelte";

    export let primaryEntityRef;

    $: changeInitiativeCall = changeInitiativeStore.getById(primaryEntityRef.id);
    $: changeInitiative = $changeInitiativeCall.data;

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

    <DescriptionFade text={changeInitiative.description}/>
    <br>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}