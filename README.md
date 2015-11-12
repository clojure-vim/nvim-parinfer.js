neovim plugin for parinfer
https://shaunlebron.github.io/parinfer

# INSTALLATION

update submodules and 
`cd parafin && lein install`

`npm install`

`mkdir -p ~/.nvim/rplugin/node && ln -s $PWD ~/.nvim/rplugin/node`

in vim
`:UpdateRemotePlugins`

# Problems 

Very early, you probably want to `export NEOVIM_JS_DEBUG=/tmp/nvim-debug.log`

Only binding to `*.cljs` files for testing

