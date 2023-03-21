<script>

    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import {entity} from "../../../common/services/enums/entity";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {legalEntityRelationshipKindStore} from "../../../svelte-stores/legal-entity-relationship-kind-store";

    export let primaryEntityReference;

    let legalEntityRelationshipKindCall;

    $: {
        if (primaryEntityReference) {
            legalEntityRelationshipKindCall = legalEntityRelationshipKindStore.getById(primaryEntityReference.id);
        }
    }

    $: legalEntityRelationshipKind = $legalEntityRelationshipKindCall?.data;

</script>


<PageHeader icon={entity.LEGAL_ENTITY_RELATIONSHIP_KIND.icon}
            name={legalEntityRelationshipKind?.name || "Unknown"}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.legal-entity-relationship-kind.list">Legal Entity Relationship List</ViewLink>
            </li>
            <li>
                <span>{legalEntityRelationshipKind?.name || "Unknown"}</span>
            </li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach"
     style="margin-bottom: 5em;">
    <div class="waltz-display-section">
        <div class="row">
            <div class="col-md-6">
                {#if legalEntityRelationshipKind}
                    <div class="row">
                        <div class="col-sm-2">
                            Name
                        </div>
                        <div class="col-sm-10">
                            {legalEntityRelationshipKind?.name}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            Description
                        </div>
                        <div class="col-sm-10">
                            {legalEntityRelationshipKind?.description || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            External Id
                        </div>
                        <div class="col-sm-10">
                            {legalEntityRelationshipKind?.externalId || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            Target Kind
                        </div>
                        <div class="col-sm-10">
                            {legalEntityRelationshipKind?.targetKind || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            Cardinality
                        </div>
                        <div class="col-sm-10">
                            {legalEntityRelationshipKind?.cardinality || "-"}
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-sm-2">
                            Provenance
                        </div>
                        <div class="col-sm-10">
                            {legalEntityRelationshipKind?.provenance}
                        </div>
                    </div>
                {:else}
                    <div class="row">
                        <div class="col-sm-12">
                            <NoData>No legal entity found</NoData>
                        </div>
                    </div>
                {/if}
            </div>
        </div>
    </div>
</div>
