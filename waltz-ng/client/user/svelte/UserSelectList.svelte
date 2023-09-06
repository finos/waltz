<script>

    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import {userStore} from "../../svelte-stores/user-store";
    import _ from "lodash";
    import {termSearch} from "../../common";
    import NoData from "../../common/svelte/NoData.svelte";
    import {selectedUser, activeMode, Modes, userRoles, searchQry} from "./user-management-store";
    import Icon from "../../common/svelte/Icon.svelte";

    $: usersCall = userStore.findAll(true);
    $: users = _.orderBy($usersCall?.data, d => d.userName);

    $: displayedUsers = _.isEmpty($searchQry)
        ? users
        : termSearch(users, $searchQry, ["userName"]);

    function selectUser(user) {
        $selectedUser = user;
        $userRoles = user.roles;
        $activeMode = Modes.DETAIL;
    }

</script>

<p>
    Use the search below to select a user and edit their roles or
    <button class="btn btn-skinny"
            data-testid="add-user-btn"
            on:click={() => $activeMode = Modes.ADD}>
        <Icon name="plus"/> add a new user
    </button>.
</p>
<SearchInput bind:value={$searchQry}
             placeholder="Search for a user..."/>
<br>
{#if _.size(displayedUsers) > 100}
    <NoData type="info">
        <Icon name="exclamation-triangle"/> There are too many results to show, please use the search to filter the list
    </NoData>
{:else}
    <div class:waltz-scroll-region-350={_.size(displayedUsers) > 10}>
        <table class="table table-condensed small table-hover">
            <thead>
                <tr>
                    <th>Username</th>
                </tr>
            </thead>
            <tbody>
            {#each displayedUsers as user}
                <tr class="clickable"
                    on:click={() => selectUser(user)}>
                    <td>
                        <button class="btn btn-skinny"
                                on:click={() => selectUser(user)}>
                            {user.userName}
                        </button>
                    </td>
                </tr>
            {:else}
                <tr>
                    <td>
                        <NoData>There are no users to display</NoData>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
{/if}