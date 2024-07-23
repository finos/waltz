<script>
    import {createEventDispatcher} from "svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";

    const Modes = {
        SHOW_USERS: "SHOW_USERS",
        SHOW_ACTIONS: "SHOW_ACTIONS"
    }

    export let selectedRole;

    let mode = Modes.SHOW_ACTIONS;
    let assignedUsers = [];

    const dispatch = createEventDispatcher();

    function onCancel() {
        dispatch("cancel");
    }


    function onShowUsers() {
        assignedUsers = [];
        mode = Modes.SHOW_USERS;
    }

</script>

<dl>
    <dt>Name:</dt>
    <dd>{selectedRole.name}</dd>
    <dt>Description:</dt>
    <dd>{selectedRole.description}</dd>
    <dt>Key:</dt>
    <dd><span class="force-wrap">{selectedRole.key}</span></dd>
    <dt>Is Custom:</dt>
    <dd>
        <Icon name={selectedRole.isCustom ? "check" : "times"}/>
        {selectedRole.isCustom ? "Custom role" : "System defined role"}
        <div class="help-block">
            Waltz ships with a set of custom roles which are needed for it's basic operation.
            Installations of Waltz can register additional roles, these are flagged with the <em>is_custom</em> attribute.
        </div>
    </dd>
</dl>

{#if mode === Modes.SHOW_ACTIONS}
<details open>
    <summary>Actions</summary>
    <menu class="list-unstyled">
        <li>
            <ViewLink state="main.role.view"
                      ctx={{id: selectedRole.id}}>
                Show details
            </ViewLink>
            <div class="help-block">Retrieve all users assigned to this role</div>
        </li>
    </menu>
</details>
{/if}



<button class="btn-link"
        on:click={onCancel}>
    Cancel
</button>


<style>
    dd {
        padding-bottom: 0.5em;
    }
</style>