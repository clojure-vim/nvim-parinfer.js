if !exists('g:parinfer_mode')
  let g:parinfer_mode = "indent"
endif

if !exists('g:parinfer_preview_cursor_scope')
  let g:parinfer_preview_cursor_scope = 0
endif

if !exists('g:parinfer_shift_norm_right_map')
  let g:parinfer_shift_norm_right_map = '>>'
endif

if !exists('g:parinfer_shift_norm_left_map')
  let g:parinfer_shift_norm_left_map = '<<'
endif

if !exists('g:parinfer_shift_vis_right_map')
  let g:parinfer_shift_vis_right_map = '>'
endif

if !exists('g:parinfer_shift_vis_left_map')
  let g:parinfer_shift_vis_left_map = '<'
endif

try
  silent! call repeat#set('')
catch
endtry

function! s:toggleMode()
  if g:parinfer_mode == "indent"
    let g:parinfer_mode = "paren"
  else
    let g:parinfer_mode = "indent"
  endif
endfunction

function! s:turnOff()
  let g:parinfer_mode = "off"
endfunction

command! ParinferToggleMode call <SID>toggleMode()
command! ParinferOff call <SID>turnOff()

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
  if has('nvim') && g:parinfer_mode != "off"
    let l:event = { "event": a:event,
                  \ "position": getpos('.'),
                  \ "lines": getline(1,line('$')),
                  \ "mode": g:parinfer_mode,
                  \ "preview_cursor_scope": g:parinfer_preview_cursor_scope }
    let l:result = ParinferProcessEvent(l:event)
    if !empty(l:result)
      for [l:n, l:ls] in l:result["patch"]
        try
          silent undojoin
        catch
        endtry
        call setline(l:n, l:ls)
      endfor
      call setpos('.', l:result["position"])
    endif
  endif
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
  autocmd FileType clojure,scheme,lisp,racket,hy
        \ :autocmd! Parinfer BufEnter <buffer>
        \ :call <SID>process("BufEnter")
  autocmd FileType clojure,scheme,lisp,racket,hy
        \ :autocmd! Parinfer TextChanged <buffer>
        \ :call <SID>process("TextChanged")
  autocmd FileType clojure,scheme,lisp,racket,hy
        \ :autocmd! Parinfer TextChangedI <buffer>
        \ :call <SID>process("TextChangedI")

  autocmd FileType clojure,scheme,lisp,racket,hy :exec 'nmap <buffer> ' . g:parinfer_shift_vis_right_map . ' <Plug>ParinferShiftVisRight'
  autocmd FileType clojure,scheme,lisp,racket,hy :exec 'nmap <buffer> ' . g:parinfer_shift_vis_left_map . ' <Plug>ParinferShiftVisLeft'
  autocmd FileType clojure,scheme,lisp,racket,hy :exec 'nmap <buffer> ' . g:parinfer_shift_norm_right_map . ' <Plug>ParinferShiftNormRight'
  autocmd FileType clojure,scheme,lisp,racket,hy :exec 'nmap <buffer> ' . g:parinfer_shift_norm_left_map . ' <Plug>ParinferShiftNormLeft'
augroup END

if (exists('g:parinfer_airline_integration') ? g:parinfer_airline_integration : 1)
  function! ParinferAirline(...)
    if &filetype =~ '.*\(clojure\|scheme\|lisp\|racket\|hy\).*'
      let w:airline_section_a = g:airline_section_a . ' %{g:parinfer_mode}'
    endif
  endfunction

  try
    call airline#add_statusline_func('ParinferAirline')
  catch
  endtry
endif
