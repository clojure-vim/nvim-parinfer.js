neovim plugin for parinfer
https://shaunlebron.github.io/parinfer

[Changelog](CHANGES.md)

# Installation

### Pre-requisites
[Install node.js](https://nodejs.org)

[Install latest neovim](https://github.com/neovim/neovim/wiki/Installing-Neovim)

[Install node-host](https://github.com/neovim/node-host)

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

### Airline integration

Parinfer mode (`indent` / `paren`) is displayed in airline by default, you can disable it with `let g:parinfer_airline_integration = 0`.

# Problems / troubleshooting

This does not seem to work well with paredit's insert mappings, I'd suggest turning it off `let g:paredit_mode = 0`

If you are using vim-sexp, I suggest turning off insert mode mappings `let g:sexp_enable_insert_mode_mappings = 0`

This is, maybe, not optimized enough - it reads and writes the entire buffer with each character change.

# Development

###  To build
`lein cljsbuild auto plugin`

### To test
In one shell:
`rlwrap lein figwheel fig-test`

In another shell:
`node target/out/tests.js`

If you want to see what the plugin is doing, you probably want to:
```
export NEOVIM_JS_DEBUG=/tmp/nvim-debug.log
tail -f ~/.nvimlog
tail -f /tmp/nvim-debug.log
```
