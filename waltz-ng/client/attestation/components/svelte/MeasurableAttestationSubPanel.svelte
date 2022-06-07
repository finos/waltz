<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import {createEventDispatcher} from "svelte";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";
    import DateTime from "../../../common/svelte/DateTime.svelte";
    import _ from "lodash";

    export let measurableCategory;
    export let isAttestable = false;
    export let latestAttestation = null;

    const dispatcher = createEventDispatcher();

    const attestMiniAction = {
        name: "Attest Now",
        icon: "check",
        description: "Initiate Attestation",
        handleAction: ctx => dispatcher("attestationInitiated", ctx)
    };

    $: actions = _.compact([
        isAttestable ? attestMiniAction : null
    ]);

    $: hasEverBeenAttested = ! _.isNil(_.get(latestAttestation, "attestedBy"))
</script>

{#if measurableCategory}
    <SubSection>
        <div slot="header">
            {measurableCategory.name}
        </div>
        <div slot="content">
            {#if hasEverBeenAttested}

                <table class="table waltz-field-table waltz-field-table-border">
                    <tr>
                        <td class="wft-label">Attested By:</td>
                        <td>{latestAttestation.attestedBy}</td>
                    </tr>
                    <tr>
                        <td class="wft-label">Attested At:</td>
                        <td>
                            <DateTime dateTime={latestAttestation.attestedAt}/>
                        </td>
                    </tr>
                </table>

            {:else}

                <NoData type="warning">
                    Never attested.
                    {#if isAttestable}
                        <button class="btn-link"
                                on:click={() => attestMiniAction.handleAction(measurableCategory)}>
                            Attest now
                        </button>
                    {/if}
                </NoData>

            {/if}
        </div>

        <div slot="controls">
            <div style="float:right" class="small">
                {#if isAttestable}
                    <MiniActions actions={actions} ctx={measurableCategory}/>
                {/if}
            </div>
        </div>

    </SubSection>
{/if}