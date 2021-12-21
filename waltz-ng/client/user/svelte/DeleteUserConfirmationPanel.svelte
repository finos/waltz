<script>
    import {userStore} from "../../svelte-stores/user-store";
    import {activeMode, Modes, selectedUser, userRoles} from "./user-management-store";
    import toasts from "../../svelte-stores/toast-store";
    import NoData from "../../common/svelte/NoData.svelte";
    import _ from "lodash";

    function deleteUser() {
        const deletePromise = userStore.deleteUser($selectedUser.userName);
        Promise.resolve(deletePromise)
            .then(r => {
                toasts.success("Successfully deleted user: " + $selectedUser.userName);
                $selectedUser = null;
                $userRoles = [];
                $activeMode = Modes.LIST;
            })
            .catch(e => toasts.error("Failed to delete user: " + e.error));
    }
</script>

{#if _.isEmpty($selectedUser)}
    <NoData>No user selected</NoData>
{:else}
    <h4>Are you sure you want to delete user: {$selectedUser.userName}?</h4>
    <br>
    <span>
        <button class="btn btn-danger"
                on:click={() => deleteUser()}>
            Delete
        </button>
        <button class="btn btn-skinny"
                on:click={() => $activeMode = Modes.DETAIL}>
            Cancel
        </button>
    </span>
{/if}