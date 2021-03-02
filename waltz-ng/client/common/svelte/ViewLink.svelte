<script>
    import _ from "lodash";

    export let state;
    export let title = null;
    export let ctx = {};

    let path;

    const routes = {
        "main": {
            path: () => "",
            title: "Home page"
        },
        "main.system.list": {
            path: () => "system/list",
            title: "System Administration"
        },
        "main.app.view": {
            path: ctx => `application/${ctx.id}`,
            title: "Application View"
        }
    };

    $: {
        path = _.get(routes, [state, "path"], () => "")(ctx);
        title = title || _.get(routes, [state, "title"], state);
    }

</script>

<a href={path}
   {title}>
    <slot></slot>
</a>
