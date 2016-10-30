if !exists('g:parinfer_mode')
  let g:parinfer_mode = "indent"
endif

if !exists('g:parinfer_preview_cursor_scope')
  let g:parinfer_preview_cursor_scope = 0
endif

try
  silent! call repeat#set('')
catch
endtry

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

function! s:parinferShiftCmd(vis, left) range
  if a:vis && a:left
    let l:shift_op = "norm! gv<"
  elseif a:vis
    let l:shift_op = "norm! gv>"
  elseif a:left
    let l:shift_op = a:firstline.",".a:lastline."norm! <<"
  else
    let l:shift_op = a:firstline.",".a:lastline."norm! >>"
  endif

  call ParinferShift(l:shift_op, a:firstline, a:lastline)
endfunction

function! s:repeat(name, count)
  if exists('*repeat#set')
    call repeat#set(a:name, a:count)
  endif
endfunction

function! s:process(event)
  let l:position = getpos('.')
  let l:event = { "event": a:event,
                \ "position": [l:position[1] - 1, l:position[2] - 1],
                \ "lines": getline(1,line('$')),
                \ "mode": g:parinfer_mode,
                \ "preview_cursor_scope": g:parinfer_preview_cursor_scope }
  let l:result = ParinferTextChangedHandler(l:event)
  call setline(1, l:result["lines"])
  call cursor(l:result["position"][0] + 1, l:result["position"][1] + 1)
endfunction

noremap <silent> <Plug>ParinferShiftVisLeft
      \ :call <SID>parinferShiftCmd(1, 1)<CR>
      \ :call <SID>repeat("\<Plug>ParinferShiftVisLeft", v:count)<CR>
noremap <silent> <Plug>ParinferShiftVisRight
      \ :call <SID>parinferShiftCmd(1, 0)<CR>
      \ :call <SID>repeat("\<Plug>ParinferShiftVisRight", v:count)<CR>

noremap <silent> <Plug>ParinferShiftNormLeft
      \ :call <SID>parinferShiftCmd(0, 1)<CR>
      \ :call <SID>repeat("\<Plug>ParinferShiftNormLeft", v:count)<CR>
noremap <silent> <Plug>ParinferShiftNormRight
      \ :call <SID>parinferShiftCmd(0, 0)<CR>
      \ :call <SID>repeat("\<Plug>ParinferShiftNormRight", v:count)<CR>

augroup Parinfer
  autocmd FileType clojure,scheme,lisp,racket
        \ :autocmd! Parinfer BufEnter <buffer>
        \ :call <SID>indentparen()

  autocmd FileType clojure,scheme,lisp,racket
        \ :autocmd! Parinfer TextChanged <buffer>
        \ :call <SID>process("TextChanged")
  autocmd FileType clojure,scheme,lisp,racket
        \ :autocmd! Parinfer TextChangedI <buffer>
        \ :call <SID>process("TextChangedI")

  autocmd FileType clojure,scheme,lisp,racket :vmap <buffer> >  <Plug>ParinferShiftVisRight
  autocmd FileType clojure,scheme,lisp,racket :vmap <buffer> <  <Plug>ParinferShiftVisLeft
  autocmd FileType clojure,scheme,lisp,racket :nmap <buffer> >> <Plug>ParinferShiftNormRight
  autocmd FileType clojure,scheme,lisp,racket :nmap <buffer> << <Plug>ParinferShiftNormLeft
augroup END

if (exists('g:parinfer_airline_integration') ? g:parinfer_airline_integration : 1)
  function! ParinferAirline(...)
    if &filetype =~ '.*\(clojure\|scheme\|lisp\|racket\).*'
      let w:airline_section_a = g:airline_section_a . ' %{g:parinfer_mode}'
    endif
  endfunction

  try
    call airline#add_statusline_func('ParinferAirline')
  catch
  endtry
endif
