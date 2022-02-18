
<script>

    import EntityLink from "../EntityLink.svelte";
    import _ from "lodash";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import DescriptionFade from "../DescriptionFade.svelte";
    import {measurableStore} from "../../../svelte-stores/measurables";
    import {orgUnitStore} from "../../../svelte-stores/org-unit-store";
    import {measurableCategoryStore} from "../../../svelte-stores/measurable-category-store";

    export let primaryEntityRef;

    let orgUnit = null;
    let orgUnitCall;
    let measurableCall;

    $: {
        if (primaryEntityRef) {
            measurableCall = measurableStore.getById(primaryEntityRef.id);
        }
        if (measurable?.organisationalUnitId) {
            orgUnitCall = orgUnitStore.getById(measurable?.organisationalUnitId);
        }
    }

    $: measurable = $measurableCall?.data;
    $: orgUnit = $orgUnitCall?.data;

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

    <div class="help-block small">
        <DescriptionFade text={measurable.description}/>
    </div>

    <br>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}