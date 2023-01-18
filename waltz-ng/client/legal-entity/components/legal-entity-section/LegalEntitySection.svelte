<script>

    import NoData from "../../../common/svelte/NoData.svelte";
    import entity from "../../../entity";
    import {legalEntityRelationshipStore} from "../../../svelte-stores/legal-entity-relationship-store";
    import _ from "lodash";
    import {legalEntityRelationshipKindStore} from "../../../svelte-stores/legal-entity-relationship-kind-store";

    export let primaryEntityRef = null;

    let legalEntityRelationshipsCall;
    let legalEntityRelationshipKindsCall = legalEntityRelationshipKindStore.findAll();

    $: {
        if (primaryEntityRef) {
            legalEntityRelationshipsCall = legalEntityRelationshipStore.findByEntityReference(primaryEntityRef);
        }
    }

    $: legalEntityRels = $legalEntityRelationshipsCall?.data || [];
    $: relKinds = $legalEntityRelationshipKindsCall?.data || [];

</script>


<h4>This entity is related to the following legal entities</h4>

{#if _.isEmpty(legalEntityRels)}
    <h4>legal entities</h4>
{:else}
    <NoData>There are no legal entites related to
        this {_.get(entity, [primaryEntityRef.kind, "name"], "entity")}</NoData>
{/if}