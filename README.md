neovim plugin for parinfer
https://shaunlebron.github.io/parinfer

# INSTALLATION

*When you clone, the local directory name must end in `.js`*

I'm using node 4.2.1 but it should work on 0.12. I don't know about node 5.0.0 however.

Follow installation instructions here: https://github.com/neovim/node-host

Inside this repo:

`npm install`

(on neovim HEAD https://github.com/neovim/neovim/wiki/Following-HEAD)

`mkdir -p ~/.config/nvim/rplugin/node && ln -s $PWD ~/.config/nvim/rplugin/node`

(on older neovim)

`mkdir -p ~/.nvim/rplugin/node && ln -s $PWD ~/.nvim/rplugin/node`

- in nvim `:UpdateRemotePlugins` you should see `remote/host: node host registered plugins ['nvim-parinfer.js']` 
- restart nvim
- infer pars

# Problems / troubleshooting

This does not seem to work well with paredit, I'd suggest turning it off `let g:paredit_mode = 0`

There's a problem with open strings, parens are inserted as you type.

This is, maybe, not optimized enough - it reads and writes the entire buffer with each character change.

Very early, you probably want to `export NEOVIM_JS_DEBUG=/tmp/nvim-debug.log`

```
tail -f ~/.nvimlog
tail -f /tmp/nvim-debug.log
```


