<script>
    import {onMount} from "svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import {roleStore} from "../../svelte-stores/role-store";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import {simpleTermSearch, termSearch} from "../../common";

    export let roleId;

    let roleViewCall;

    $: roleViewCall = roleId && roleStore.getViewById(roleId);

    let role = null;
    let users = null;
    let searchQry = "";

    $: roleView = $roleViewCall?.data
    $: role = roleView?.role;
    $: users = _.orderBy(
        simpleTermSearch(
            roleView?.users,
            searchQry),
        d => d);
</script>



<PageHeader icon="user-circle"
            name="{role?.name}"
            small="Role">

    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.role.list">Roles</ViewLink>
            </li>
            <li>
                {role?.name}
            </li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">
        <div class="row">

            {#if role}
            <div class="col-sm-6">
                <h3>{role.name}</h3>
                <dl>
                    <dt>Name:</dt>
                    <dd>{role.name}</dd>
                    <dt>Description:</dt>
                    <dd>{role.description}</dd>
                    <dt>Key:</dt>
                    <dd><span class="force-wrap">{role.key}</span></dd>
                    <dt>Is Custom:</dt>
                    <dd>
                        <Icon name={role.isCustom ? "check" : "times"}/>
                        {role.isCustom ? "Custom role" : "System defined role"}
                        <div class="help-block">
                            Waltz ships with a set of custom roles which are needed for it's basic operation.
                            Installations of Waltz can register additional roles, these are flagged with the <em>is_custom</em> attribute.
                        </div>
                    </dd>
                </dl>
            </div>
            {/if}
            {#if users}
            <div class="col-sm-6">
                <h3>Users (#{users.length})</h3>
                <SearchInput bind:value={searchQry}
                             placeholder="Search for a user..."/>
                <div class="waltz-scroll-region-300">
                    <ul class="list-unstyled">
                        {#each users as user}
                            <li>{user}</li>
                        {/each}
                    </ul>
                </div>
            </div>
            {/if}
        </div>
    </div>
</div>


<style>
    dd {
        padding-bottom: 0.5em;
    }
</style>