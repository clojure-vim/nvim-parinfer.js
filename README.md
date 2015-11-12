neovim plugin for parinfer
https://shaunlebron.github.io/parinfer

# INSTALLATION

Follow installation instructions here: https://github.com/neovim/node-host

`npm install`

(on neovim HEAD)

`mkdir -p ~/.config/nvim/rplugin/node && ln -s $PWD ~/.config/nvim/rplugin/node`

(on older neovim HEAD)

`mkdir -p ~/.nvim/rplugin/node && ln -s $PWD ~/.nvim/rplugin/node`

- in nvim `:UpdateRemotePlugins` you should see `remote/host: node host registered plugins ['nvim-parinfer.js']` 
- restart nvim
- infer pars

# Problems / troubleshooting

This does not seem to work well with paredit, I'd suggest turning it off `let g:paredit_mode = 0`

Very early, you probably want to `export NEOVIM_JS_DEBUG=/tmp/nvim-debug.log`

```
tail -f ~/.nvimlog
tail -f /tmp/nvim-debug.log
```

Only binding to `*.cljs` files for testing

