# INSTALLATION

update submodules and 
`cd parafin && lein install`

`lein npm install`

`mkdir -p ~/.nvim/rplugin/node && ln -s $PWD ~/.nvim/rplugin/node`

in vim
`:UpdateRemotePlugins`

# Problems 

Only binding to `*.xljs` files for testing

Something (maybe typing too fast) causes:

```
2015/11/12 01:13:43 [error @ call_set_error:743] 76515 - msgpack-rpc: Channel was closed by the client
```
