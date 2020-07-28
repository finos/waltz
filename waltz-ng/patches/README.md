# Patches

This directory contains patches to dependencies listed in the `package.json` file.

For more information about the patching process see: [patch-package](https://github.com/ds300/patch-package)

## Patched libs

### bigeval

We use [`bigeval`](https://github.com/aviaryan/BigEval.js) for evaluating dynamic expressions without 
using the unsafe `eval` function.  However, there is a security hole in the package as it will search 
the global namespace if it cannot resolve function references.  The patch allows for an (optional)
root context to be passed in which replaces the default global context.  

The fork/branch: [davidwatkins73/bigeval/bigeval-unroot](https://github.com/davidwatkins73/BigEval.js/tree/bigeval-unroot)
holds the modified code outside of the patch file.