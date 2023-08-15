<script>
    import _ from "lodash";
    import {activeMode, Modes, selectedUser, userRoles} from "./user-management-store";
    import {userStore} from "../../svelte-stores/user-store";
    import toasts from "../../svelte-stores/toast-store";

    let userName = null;
    let password = null;

    let userCall;

    $: disabled = _.isEmpty(userName)
        || _.isEmpty(password)
        || _.isEmpty(userName.trim())
        || _.isEmpty(password.trim())

    function registerUser() {
        const newUser = {
            userName,
            password
        }

        const registerPromise = userStore.register(newUser);

        Promise.resolve(registerPromise)
            .then(r => {
                toasts.success("Successfully registered new user: " + userName)
                userCall = userStore.getByUserId(userName);
                Promise.resolve(userCall)
                    .then(r => r.subscribe(d => {
                        $selectedUser = d.data;
                        $userRoles = d.data?.roles
                    }))
                    .then(() => $activeMode = Modes.DETAIL);
            })
            .catch(e => toasts.error("Could not create new user: " + e.error))
    }

</script>

<h4>New User Registration</h4>
<div class="help-block">Enter a username and password for the new user below</div>

<div class="row">
    <label for="username"
           class="col-sm-2">
        User Name
    </label>

    <div class="col-sm-3">
        <input class="form-control"
               id="username"
               maxlength="255"
               placeholder="User name"
               bind:value={userName}/>
    </div>
</div>
<br>
<div class="row">
    <label for="password"
           class="col-sm-2">
        Password
    </label>

    <div class="col-sm-3">
        <input class="form-control"
               id="password"
               maxlength="255"
               placeholder="Password"
               bind:value={password}/>
    </div>
</div>

<br>
<div class="row">
    <div class="col-sm-2">
    </div>
    <div class="col-sm-3">
        <button class="btn btn-success"
                data-testid="submit-new-user-btn"
                on:click={() => registerUser()}
                disabled={disabled}>
            Register
        </button>
        <button class="btn btn-skinny"
                on:click={() => $activeMode = Modes.LIST}>
            Cancel
        </button>
    </div>
</div>
