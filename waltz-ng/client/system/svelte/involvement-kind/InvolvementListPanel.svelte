<script>

    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";
    import InvolvementBreakdown from "./InvolvementBreakdown.svelte";
    import _ from "lodash";
    import {involvementViewStore} from "../../../svelte-stores/involvement-view-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    export let involvementKind;

    let usageStatsCall;
    let involvementsCall;

    let selectedEntityKind;

    function selectStat(usageStat) {
        selectedEntityKind = usageStat.entityInfo
        console.log({usageStat})
    }

    $: {
        if (involvementKind.id) {
            usageStatsCall = involvementKindStore.findUsageStatsForKind(involvementKind.id)
        }
    }


    $: {
        if (involvementKind.id && selectedEntityKind) {
            involvementsCall = involvementViewStore
                .findInvolvementsByKindAndEntityKind(involvementKind.id, selectedEntityKind.key);
        }
    }


    $: involvementUsageStats = $usageStatsCall?.data || [];
    $: involvements = _.orderBy($involvementsCall?.data || [], d => _.toLower(d.involvement.entityReference.name));

</script>


{#if !_.isEmpty(involvementUsageStats.breakdown)}
    <InvolvementBreakdown breakdownStats={involvementUsageStats.breakdown}
                          onClick={selectStat}
                          displayMode="TABLE"/>

    {#if selectedEntityKind}
        <hr>
        <h4>Involvements to {selectedEntityKind.name}s <span class="text-muted">({_.size(involvements)})</span>:</h4>
        <div class:waltz-scroll-region-350={_.size(involvements) > 10}>
            <table class="table table-condensed">
                <thead>
                <tr>
                    <th>Entity</th>
                    <th>Person</th>
                </tr>
                </thead>
                <tbody>
                {#each involvements as involvement}
                    <tr>
                        <td>
                            <EntityLink ref={involvement.involvement.entityReference}/>
                        </td>
                        <td>
                            <EntityLink ref={involvement.person}/>
                        </td>
                    </tr>
                {:else}
                    <tr>
                        <td>
                            There are no active people with the involvement {involvementKind.name}
                            for {selectedEntityKind.name}s
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {:else}
    {/if}
    {#if !_.isEmpty(involvements)}
    {/if}
{:else}
    <span>There are no usages of this involvement kind</span>
{/if}