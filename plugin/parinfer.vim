augroup ClojureParinfer
  autocmd FileType clojure
        \ :autocmd! ClojureParinfer BufEnter <buffer> 
        \ :let lines = ParinferIndent("enter") | if !empty(lines) | call setline(1,lines) | endif | unlet lines

  autocmd FileType clojure
        \ :autocmd! TextChanged,TextChangedI <buffer>
        \ :try | silent undojoin | catch | endtry | let lines = ParinferIndent("changed") | if !empty(lines) | call setline(1,lines) | endif | unlet lines
augroup END
