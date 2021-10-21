
<script>

    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import _ from "lodash";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import DescriptionFade from "./DescriptionFade.svelte";
    import {measurableStore} from "../../svelte-stores/measurables";
    import {orgUnitStore} from "../../svelte-stores/org-unit-store";

    export let primaryEntityRef;

    let orgUnit = null;

    $: measurableCall = measurableStore.getById(primaryEntityRef.id);
    $: measurable = $measurableCall.data;

    $: orgUnitCall = measurable.organisationalUnitId && orgUnitStore.getById(measurable.organisationalUnitId);
    $: orgUnit = orgUnitCall && $orgUnitCall.data;

</script>

{#if measurable}
    <h4><EntityLink ref={measurable}/></h4>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">External Id</td>
                <td width="50%">{measurable.externalId}</td>
            </tr>
            <tr>
                <td width="50%">Concrete</td>
                <td width="50%">{measurable.concrete ? 'Yes' : 'No'}</td>
            </tr>
            {#if !_.isNull(measurable.organisationalUnitId)}
            <tr>
                <td width="50%">Owning Org Unit</td>
                <td width="50%">
                    <EntityLink ref={orgUnit}/>
                </td>
            </tr>
            {/if}
        </tbody>
    </table>

    <DescriptionFade text={measurable.description}/>
    <br>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}