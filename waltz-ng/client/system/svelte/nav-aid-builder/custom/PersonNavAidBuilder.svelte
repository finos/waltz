<script>
    import {model} from "./builderStore";
    import BuilderControl from "./control/BuilderControl.svelte";
    import PersonNavAid from "./PersonNavAid.svelte";

    let viz = null;

    function prettyHTML(html) {
        if (_.isNil(html)) {
            return "";
        }
        const tab = "    ";
        let result = "";
        let indent= "";

        html
            .split(/>\s*</)
            .forEach(function(element) {
                if (element.match( /^\/\w/ )) {
                    indent = indent.substring(tab.length);
                }

                result += indent + "<" + element + ">\r\n";

                if (element.match( /^<?\w[^>]*[^\/]$/ ) && !element.startsWith("input")  ) {
                    indent += tab;
                }
            });

        return result.substring(1, result.length - 3);
    }
</script>

<div class="row">
    <div class="col-sm-8">
        <div bind:this={viz}>
            <PersonNavAid/>
        </div>
    </div>
    <div class="col-sm-4"
         style="padding-left: 10px">
        <BuilderControl/>
    </div>
</div>

<hr>

<div class="row">
    <div class="col-md-6">
        <pre>{prettyHTML(viz?.innerHTML)}</pre>
    </div>
    <div class="col-md-6">
        <pre>{JSON.stringify($model, "", 2)}</pre>
    </div>
</div>
