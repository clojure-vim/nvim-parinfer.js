if !exists('g:parinfer_mode')
  let g:parinfer_mode = "indent"
endif

function! s:indentparen()
  if has('nvim') && g:parinfer_mode != "off"
    try
      silent undojoin
    catch
    endtry
    let l:saved_mode = g:parinfer_mode
    let g:parinfer_mode = "paren"
    let l:lines = ParinferIndent()
    let g:parinfer_mode = l:saved_mode
    if !empty(lines)
      call setline(1,lines)
    endif
  endif
endfunction

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
        \ :call <SID>indentparen()

  autocmd FileType clojure
        \ :autocmd! ClojureParinfer TextChanged,TextChangedI <buffer>
        \ :call <SID>indent()
augroup END

if (exists('g:parinfer_airline_integration') ? g:parinfer_airline_integration : 1)
  function! ParinferAirline(...)
    if &filetype == "clojure"
      let w:airline_section_a = g:airline_section_a . ' %{g:parinfer_mode}'
    endif
  endfunction

  try
    call airline#add_statusline_func('ParinferAirline')
  catch
  endtry
endif
