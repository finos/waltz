
<script>

    import {orgUnitStore} from "../../../svelte-stores/org-unit-store";
    import {endUserApplicationStore} from "../../../svelte-stores/end-user-application-store";
    import DescriptionFade from "../DescriptionFade.svelte";
    import {criticality} from "../../services/enums/criticality";
    import _ from "lodash";
    import {lifecyclePhase} from "../../services/enums/lifecycle-phase";

    export let primaryEntityRef;

    $: eudaCall = endUserApplicationStore.getById(primaryEntityRef.id);
    $: euda = $eudaCall.data;

    $: orgUnitCall = euda?.organisationalUnitId && orgUnitStore.getById(euda?.organisationalUnitId);
    $: orgUnit = $orgUnitCall?.data;

</script>

{#if euda}
    <h4 class="force-wrap" >{euda.name}</h4>
    <slot name="post-title"/>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">Kind</td>
                <td width="50%">
                    {euda.applicationKind}
                </td>
            </tr>
            <tr>
                <td width="50%">Organisational unit</td>
                <td width="50%">{orgUnit?.name || "Unknown"}</td>
            </tr>
            <tr>
                <td width="50%">Lifecycle Phase</td>
                <td width="50%">{_.get(lifecyclePhase[euda.lifecyclePhase], "name", euda.lifecyclePhase)}</td>
            </tr>
            <tr>
                <td width="50%">Risk Rating</td>
                <td width="50%">{_.get(criticality[euda.riskRating], "name", euda.riskRating)}</td>
            </tr>

        </tbody>
    </table>

    <DescriptionFade text={euda.description}/>

    <slot name="post-header"/>

    <slot name="footer"/>
{/if}