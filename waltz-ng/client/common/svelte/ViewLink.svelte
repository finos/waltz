<script>

    export let state;
    export let title = null;

    let path;

    const routes = {
        "main": {
            path: "",
            title: "Home page"
        },
        "main.system.list": {
            path: "system/list",
            title: "System Administration"
        }
    };

    $: {
        path = _.get(routes, [state, "path"], "/");
        title = title || _.get(routes, [state, "title"], state);
    }

</script>

<a href={path}
   {title}>
    <slot></slot>
</a>
