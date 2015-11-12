neovim plugin for parinfer
https://shaunlebron.github.io/parinfer

# INSTALLATION

update submodules and 
`cd parinfer && lein install`

`npm install`

(on neovim HEAD)

`mkdir -p ~/.config/nvim/rplugin/node && ln -s $PWD ~/.config/nvim/rplugin/node`

(on older neovim HEAD)

`mkdir -p ~/.nvim/rplugin/node && ln -s $PWD ~/.nvim/rplugin/node`

- in nvim `:UpdateRemotePlugins` you should see `remote/host: node host registered plugins ['nvim-parinfer.js']` 
- restart nvim
- infer pars

# Problems / troubleshooting

Very early, you probably want to `export NEOVIM_JS_DEBUG=/tmp/nvim-debug.log`

```
tail -f ~/.nvimlog
tail -f /tmp/nvim-debug.log
```

Only binding to `*.cljs` files for testing

