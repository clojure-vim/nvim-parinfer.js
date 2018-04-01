# DEPRECATED 2018-04-01

Please use https://github.com/eraserhd/parinfer-rust

@eraserhd has built a much better version of this plugin, that works in vim and neovim, without the trouble of remote plugins, and implements new parinfer features like smart mode.

## Postmortem

Today [node-host](https://github.com/neovim/node-host) has been merged into neovim but both the host and [node-client](https://github.com/neovim/node-client) libraries had a number of bugs early on that made it difficult for users of this plugin to get work done since bug fixes involved coordination with up to 5 different library authors and repositories. When node-host was merged into neovim I was able to update this plugin to work for me but it seems that others are having real problems getting it to work in their setups.

Another difficulty that has lessened been recently, is writing node.js compatible scripts with clojurescript. Working with npm modules as a script is something that Lumo is tackling but it's unclear if Lumo can be used with node-host. The CLJS core team is still working towards better compatibility with the node platform but my understanding is that a lot of the work needs to be done google closure.

I still believe that clojure/script is a great language for writing tools for clojure. [rewrite-cljs](https://github.com/rundis/rewrite-cljs) and [rewrite-clj](https://github.com/xsc/rewrite-clj) are top notch libraries that make manipulating your source code a breeze. However the approach taken here is built on too many transitive dependencies and too many moving pieces to be a realiable enough tool.

---

neovim plugin for parinfer
https://shaunlebron.github.io/parinfer

[Changelog](CHANGES.md)

# Installation

### Pre-requisites
[Install node.js](https://nodejs.org)

[Install neovim HEAD](https://github.com/neovim/neovim/wiki/Installing-Neovim)

[Install node-client](https://github.com/neovim/node-client) `npm install -g neovim`

### Vundle

Using Vundle, add this to your vundle .config/nvim/init.vim section:

```
Plugin 'snoe/nvim-parinfer.js'
```

### Inside nvim
- `:PluginInstall`
- `:UpdateRemotePlugins` you should see `remote/host: node host registered plugins ['nvim-parinfer.js']` 
- *restart* nvim
- infer pars

### Using it

You can `let g:parinfer_mode = "off"` to turn off the plugin.

You can `let g:parinfer_mode = "indent"` to switch to indent-mode.

You can `let g:parinfer_mode = "paren"` to switch to paren-mode.

Indent (`>>`) and dedent (`<<`) mappings have been made to dynamically align elements to tabstops that will change the structure of the code.
Default mappings can be overridden:
```VimL
let g:parinfer_shift_norm_right_map = '<space>>'
let g:parinfer_shift_norm_left_map = '<space><'
let g:parinfer_shift_vis_right_map = 'g>'
let g:parinfer_shift_vis_left_map = 'g<'
```

You can enable Parinfers previewCursorScope option with `let g:parinfer_preview_cursor_scope = 1` (default is 0)

  previewCursorScope off `let g:parinfer_preview_cursor_scope = 0`:

  ```clj
  (let [foo 1
        bar 2]
        |
    (+ foo bar))
  ```

  previewCursorScope on `let g:parinfer_preview_cursor_scope = 1`:

  ```clj
  (let [foo 1
        bar 2
        |]
    (+ foo bar))
  ```
### Airline integration

Parinfer mode (`indent` / `paren`) is displayed in airline by default, you can disable it with `let g:parinfer_airline_integration = 0`.

# Problems / troubleshooting

This does not seem to work well with paredit's insert mappings, I'd suggest turning it off `let g:paredit_mode = 0`

If you are using vim-sexp, I suggest turning off insert mode mappings `let g:sexp_enable_insert_mode_mappings = 0`

This is, maybe, not optimized enough - it reads and writes the entire buffer with each character change.

# Development

###  To build
`lein do npm install, cljsbuild auto plugin`

If you want to see what the plugin is doing, you probably want to:
```
export NVIM_NODE_LOG_FILE=/tmp/nvim-debug.log
tail -f /tmp/nvim-debug.log
```

Tests can be run in a watch by starting two separate terminals:

1. `$ rlwrap lein figwheel`
2. `$ node target/out/tests.js`
