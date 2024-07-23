<script>
    import _ from "lodash";
    import {termSearch} from "../../common";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import {createEventDispatcher} from "svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";

    const dispatch = createEventDispatcher();

    export let roles;

    let searchQry = "";
    let displayRoles = [];

    function onSelectRole(role) {
        dispatch("select", role);
    }

    $: displayRoles = _.isEmpty(searchQry)
        ? roles
        : termSearch(
            roles,
            searchQry,
            ["name", "description", "key"]);

</script>


{#if _.isNil(roles)}
    <h3>Loading....</h3>
{:else}

    <SearchInput bind:value={searchQry}
                 placeholder="Search for a role..."/>

    <div class="waltz-scroll-region-700">
        <table class="table table-condensed small table-striped table-hover fixed-table">
            <thead>
            <tr>
                <th style="width: 30%">Name</th>
                <th style="width: 40%">Description</th>
                <th style="width: 30%">Key</th>
            </tr>
            </thead>
            <tbody>
            {#each _.orderBy(displayRoles, d => d.name) as row}
            <tr on:click={() => onSelectRole(row)}>
                <td>
                    <ViewLink state="main.role.view"
                              ctx={{id: row.id}}>
                        {row.name}
                    </ViewLink>
                    <Icon name={row.isCustom ? "fw": "lock"}/>
                </td>
                <td>{row.description}</td>
                <td>{row.key}</td>
                <td></td>
            </tr>
            {/each}
            </tbody>
        </table>
    </div>
{/if}