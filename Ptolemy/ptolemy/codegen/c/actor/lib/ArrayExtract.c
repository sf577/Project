/*** preinitBlock ***/
int $actorSymbol(i);
boolean $actorSymbol(doDelete);
/**/

/*** initBlock ***/
$actorSymbol(doDelete) = false;
/**/


/*** fireBlock ***/
if ($actorSymbol(doDelete)) {
    Array_delete($ref(output));
}

$ref(output) = Array_new($val(outputArrayLength), 0);
	
for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(destinationPosition); $actorSymbol(i)++) {
    $ref(output).payload.Array->elements[$actorSymbol(i)] = $tokenFunc(Array_get($ref(input), 0)::zero());
}
for (; $actorSymbol(i) < $val(destinationPosition) + $val(extractLength); $actorSymbol(i)++) {
    $ref(output).payload.Array->elements[$actorSymbol(i)] = Array_get($ref(input), $val(sourcePosition) + $actorSymbol(i) - $val(destinationPosition));
}
for (; $actorSymbol(i) < $val(outputArrayLength); $actorSymbol(i)++) {
    $ref(output).payload.Array->elements[$actorSymbol(i)] = $tokenFunc(Array_get($ref(input), 0)::zero());
}
/**/

