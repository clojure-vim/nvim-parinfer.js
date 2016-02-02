#0.4.2
- run paren mode indent on bufenter

#0.4.1
- fix airline guard

#0.4.0
- Updated to the lastest parinfer javascript api.

#0.3.2
- Updated for node-host api change, you need to update node-host plugin and `npm install --production` inside the bundle then `:UpdateRemotePlugins`

#0.3.1
- Added  paren-mode support (`let g:parinfer_mode = "paren"`).
- Added airline modeline indicator.
- You need to run `:UpdateRemotePlugins`.

#0.3.0
- Move to only running parinfer in node-host, put vimmy stuff like undojoin in `plugin/parinfer.vim`
- You need to run `:UpdateRemotePlugins`.
- You need to make sure the plugin is installed with vundle/pathogen or link in `plugin`

#0.2.4
- Change commands to non-sync versions since it seems impossible to synchronize the rpc calls anyhow
- You need to run `:UpdateRemotePlugins`.

#0.2.3
- Turn on plugin in all `.clj*` files - you need to run `:UpdateRemotePlugins`.

#0.2.2
- Flag to turn off plugin - you need to run `:UpdateRemotePlugins`.

#0.2.1
- Better undo handling.

#0.2.0
- Moved library to rplugin/node you can now install/update with Vundle.

#0.1.3
- Bump neovim-client version. 
- IMPORTANT you need to `cd ~/.config/nvim/bundle/node-host && npm upgrade` to fix a bug in the plugin host that caused the parinfer to crash (should use `neovim-client 1.0.6` dependency)

#0.1.2
- Run paren-mode format on initial buffer load; stops badly formatted files from getting munged 

#0.1.1
- Significant speedup with proper diff algorithm 
- Point to node-host crash fix in README

#0.1.0
- First release
