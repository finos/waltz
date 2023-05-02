<script>

    import _ from "lodash";
    import {reportGridMember} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import PersonInfoPanel from "../../../../common/svelte/info-panels/PersonInfoPanel.svelte";
    import {personStore} from "../../../../svelte-stores/person-store";

    export let members = [];
    export let selectedMember = null;
    export let onDelete = () => console.log("deleting");
    export let onEdit = () => console.log("editing");

    $: lastOwner = _
        .chain(members)
        .filter(d => d.role === reportGridMember.OWNER.key)
        .size()
        .value() === 1;

    $: person = selectedMember?.user

</script>


{#if selectedMember && person}
    <PersonInfoPanel primaryEntityRef={person}>
        <div slot="post-title">
            {#if selectedMember?.role === reportGridMember.OWNER.key}
                            <span>
                                <button class="btn btn-xs btn-warning"
                                        disabled={lastOwner}
                                        title={lastOwner ? "You cannot remove the last owner from a report grid" : ""}
                                        on:click={() => onEdit(selectedMember.user, reportGridMember.VIEWER.key)}>
                                    Make Viewer Only
                                </button>
                                <button class="btn btn-xs btn-danger"
                                        title={lastOwner ? "You cannot remove the last owner from a report grid" : ""}
                                        disabled={lastOwner}
                                        on:click={() => onDelete(selectedMember)}>
                                    Delete
                                </button>
                            </span>
                <div class="help-block small">
                    <Icon name="info-circle"/>By making this user a viewer only they will no longer have the ability
                    to modify this report grid or maintain the member roles, however, they will still be able to see it in the report picker.
                </div>
            {:else if selectedMember?.role === reportGridMember.VIEWER.key}
                <button class="btn btn-xs btn-warning"
                        on:click={() => onEdit(person, reportGridMember.OWNER.key)}>
                    Promote to Owner
                </button>
                <button class="btn btn-xs btn-danger"
                        on:click={() => onDelete(selectedMember)}>
                    Delete
                </button>
                <div class="help-block small">
                    <Icon name="info-circle"/>By promoting this user to a report owner they will have the ability
                    to modify this report grid or maintain the member roles.
                </div>
            {/if}
        </div>
    </PersonInfoPanel>
{:else}
    <div class="help-block small">
        <Icon name="info-circle"/>Select a user from the list to see more info.
    </div>
{/if}