/*** preinitBlock ***/
int $actorSymbol(i);
Token $actorSymbol(valueArray);
/**/

/*** fireBlock ***/
$actorSymbol(valueArray) = $new(Array($val(arrayLength), 0));
for ($actorSymbol(i) = 0;
     $actorSymbol(i) < $val(arrayLength);
     $actorSymbol(i)++) {
    Array_set($actorSymbol(valueArray),
            $actorSymbol(i),
            $ref((Token)input, $actorSymbol(i)));
}	
$ref(output) = 
        $actorSymbol(valueArray);
/**/
