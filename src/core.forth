: nip swap drop ;
: over >r dup r> swap ;
: 2dup over over ;
: 2drop drop drop ;
: rot >r swap r> swap ;
: -rot swap >r swap r> ;
: tuck swap over ;
: != = not ;
: >= < not ;
: <= swap < not ;
: > swap < ;
: 1+ 1 + ;
: if immediate ['] jmp#f , here 0 , ;
: else immediate ['] jmp , here 0 , swap here over - swap ! ;
: then immediate here over - swap ! ;
: begin immediate here ;
: while immediate ['] jmp#f , here 0 , ;
: repeat immediate swap ['] jmp , here - ,  here over - swap ! ;
: until immediate ['] jmp#f , here - , ;
: do immediate ['] swap , ['] >r , ['] >r , 0 here ;
: unloop r> r> r> 2drop >r ;
: loop immediate
    ['] r> , ['] 1+ , ['] >r ,
    ['] i , ['] j , ['] >= ,
    ['] jmp#f , here - ,
    here over - swap !
    ['] unloop , ;
: min 2dup < if drop else nip then ;
: max 2dup < if nip else drop then ;