
<script>

    import EntityLink from "../EntityLink.svelte";
    import KeyInvolvementInfoPanel from "./KeyInvolvementInfoPanel.svelte";
    import KeyAssessmentInfoPanel from "./KeyAssessmentInfoPanel.svelte";
    import DescriptionFade from "../DescriptionFade.svelte";
    import {actorStore} from "../../../svelte-stores/actor-store";

    export let primaryEntityRef;

    $: actorCall = actorStore.getById(primaryEntityRef.id);
    $: actor = $actorCall.data;

</script>

{#if actor}
    <h4><EntityLink ref={actor}/></h4>
    <table class="table table-condensed small">
        <tbody>
            <tr>
                <td width="50%">External</td>
                <td width="50%">{actor.isExternal ? "Yes" : "No"}</td>
            </tr>
        </tbody>
    </table>

    <DescriptionFade text={actor.description}/>
    <br>

    <slot name="post-header"/>

    <KeyInvolvementInfoPanel {primaryEntityRef}/>

    <KeyAssessmentInfoPanel {primaryEntityRef}/>

    <slot name="footer"/>
{/if}