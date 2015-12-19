let g:parinfer_mode = "indent"

function! s:indent()
  if has('nvim') && g:parinfer_mode != "off"
    try
      silent undojoin
    catch
    endtry
    let l:lines = ParinferIndent()
    if !empty(lines)
      call setline(1,lines)
    endif
  endif
endfunction

augroup ClojureParinfer
  autocmd FileType clojure
        \ :autocmd! ClojureParinfer BufEnter <buffer>
        \ :call <SID>indent()

  autocmd FileType clojure
        \ :autocmd! ClojureParinfer TextChanged,TextChangedI <buffer>
        \ :call <SID>indent()
augroup END

if (exists('g:loaded_airline') && g:loaded_airline)
  function! ParinferAirline(...)
    if &filetype == "clojure"
      let w:airline_section_a = g:airline_section_a . ' %{g:parinfer_mode}'
    endif
  endfunction

  call airline#add_statusline_func('ParinferAirline')
endif
