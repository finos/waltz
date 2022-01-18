<script>
    import Icon from "../../../../common/svelte/Icon.svelte";
    import _ from "lodash";
    import {surveyInstanceStore} from "../../../../svelte-stores/survey-instance-store";
    import {createEventDispatcher, writable} from "svelte";
    import {toLocalDate} from "../../../../common/date-utils";

    const dispatch = createEventDispatcher();


    function toDesc(v) {
        switch (v.status) {
            case "COMPLETED":
                return `Submitted by ${v.submittedBy} on ${toLocalDate(v.submittedAt)}`
            case "APPROVED":
                return `Approved by ${v.approvedBy} on ${toLocalDate(v.approvedAt)}`
            default:
                return v.status
        }
    }

    export let instance = null;

    let versions = [];
    let showAll = false;

    $: versionsCall = instance && surveyInstanceStore
        .findVersions(instance.id);

    $: versions = _
        .chain($versionsCall?.data)
        .compact()
        .uniqBy(v => v.id)
        .orderBy(v => v.id)
        .map((v, idx) => {
            const verStr = idx === 0 ? "Current" : "v" + idx;
            return {
                label: verStr,
                isActive: v.id === instance.id,
                id: v.id,
                instance: v,
                description: toDesc(v)
            }
        })
        .value();

    function onSelect(v) {
        if (v.isActive) {
            // nothing to do
            return;
        }
        dispatch("select", v.id);
    }


</script>


{#if _.size(versions) > 1}
    <h5 style="padding-top: 1em;">
        <Icon name="history"/>
        Versions
    </h5>

    {#if instance.originalInstanceId || showAll}
    <table class="table table-condensed table-hover small">
        <tbody>
        {#each versions as version}
        <tr class:selected={version.isActive}
            class:clickable={!version.isActive}
            on:click={() => onSelect(version)}>
            <td>
                {version.label}
            </td>
            <td>
                {version.description}
            </td>
        </tr>
        {/each}
        </tbody>
    </table>
    {/if}

    {#if !( instance.originalInstanceId || showAll)}
        This survey has {versions.length} earlier versions.
        <button class="btn-link" on:click={() => showAll = true}>
            Show historic versions.
        </button>
    {/if}
{/if}

<style>
    .selected {
        background-color: #e2ffd9;
    }
</style>