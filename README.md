neovim plugin for parinfer
https://shaunlebron.github.io/parinfer

# INSTALLATION

*When you clone, the local directory name must end in `.js`*

I'm using node 4.2.1 but it should work on 0.12. I don't know about node 5.0.0 however.

Follow installation instructions here: https://github.com/neovim/node-host
*[THERE IS A BUG IN THIS LIBRARY](https://github.com/neovim/node-client/issues/8) THAT CAUSES CRASHES*
*copy [this file](https://github.com/snoe/node-client/blob/master/index.js) this file to `~/.config/nvim/bundle/node-host/node_modules/neovim-client/`*

Inside this repo:

`npm install`

(on neovim HEAD https://github.com/neovim/neovim/wiki/Following-HEAD)

`mkdir -p ~/.config/nvim/rplugin/node && ln -s $PWD ~/.config/nvim/rplugin/node`

(on older neovim)

`mkdir -p ~/.nvim/rplugin/node && ln -s $PWD ~/.nvim/rplugin/node`

- in nvim `:UpdateRemotePlugins` you should see `remote/host: node host registered plugins ['nvim-parinfer.js']` 
- restart nvim
- infer pars

# [CHANGELOG](CHANGES.md)

# Problems / troubleshooting

Undo after normal mode changes doesn't work well because parinfer's changes go to the top of the stack and popping just causes parinfer to run again.

This does not seem to work well with paredit's insert mappings, I'd suggest turning it off `let g:paredit_mode = 0`
If you are using vim-sexp, I suggest turning off insert mode mappings `let g:sexp_enable_insert_mode_mappings = 0`

The plugin sometimes crashes, especially when adding an empty line as your first change, if you get an error restart vim and make a change elsewhere in the file first. (see node-host instructions above for a fix)

This is, maybe, not optimized enough - it reads and writes the entire buffer with each character change.

# Development

You need to update sub-modules and
`cd parinfer && lein install`

###  To build
`lein cljsbuild auto plugin`

### To test
`rlwrap lein figwheel fig-test`

If you want to see what the plugin is doing, you probably want to:
```
export NEOVIM_JS_DEBUG=/tmp/nvim-debug.log
tail -f ~/.nvimlog
tail -f /tmp/nvim-debug.log
```
