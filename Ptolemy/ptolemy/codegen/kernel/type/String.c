/***declareBlock***/
typedef char* StringToken;
/**/

/***funcDeclareBlock***/
Token String_new(char* s);
/**/

/***newBlock***/
// make a new integer token from the given value.
Token String_new(char* s) {
    Token result;
    result.type = TYPE_String;
    result.payload.String = strdup(s);
    return result;
}
/**/

/***deleteBlock***/
Token String_delete(Token token, ...) {   
    free(token.payload.String);    
}    
/**/

/***equalsBlock***/
Token String_equals(Token this, ...) {
    va_list argp; 
    Token otherToken; 
    va_start(argp, this);
    otherToken = va_arg(argp, Token);

    va_end(argp);
    return Boolean_new(!strcmp(this.payload.String, otherToken.payload.String));
}
/**/



/***printBlock***/
Token String_print(Token this, ...) {
    printf("\"%s\"", this.payload.String);
}
/**/

/***toStringBlock***/
Token String_toString(Token this, ...) {
    // Guarrantee to return a new string.
    char* result = (char*) malloc(sizeof(char) * (3 + strlen(this.payload.String)));
    sprintf(result, "\"%s\"", this.payload.String);
    return String_new(result);
}
/**/

/***addBlock***/
Token String_add(Token this, ...) {
    va_list argp; 
    va_start(argp, this);
    Token otherToken = va_arg(argp, Token);
	
    char* result = (char*) malloc(sizeof(char) * (1 + strlen(this.payload.String) + strlen(otherToken.payload.String)));
    strcpy(result, this.payload.String);
    strcat(result, otherToken.payload.String);

    va_end(argp);
    return String_new(result);
}
/**/

/***substractBlock***/
Token String_substract(Token this, ...) {
    fprintf(stderr, "String_substract not supported");
    exit(1);
}
/**/

/***multiplyBlock***/
Token String_multiply(Token this, ...) {
    fprintf(stderr, "String_multiply not supported");
    exit(1);
}
/**/

/***divideBlock***/
Token String_divide(Token this, ...) {
    fprintf(stderr, "String_divide not supported");
    exit(1);
}
/**/

/***negateBlock***/
Token String_negate(Token this, ...) {
}	
/**/

/***zeroBlock***/
Token String_zero(Token token, ...) {
    return String_new("");
}
/**/

/***oneBlock***/
Token String_one(Token this, ...) {
    fprintf(stderr, "String_one not supported");
    exit(1);
}
/**/

/***cloneBlock***/
Token String_clone(Token this, ...) {
    return String_new(this.payload.String);
}
/**/



------------------ static functions --------------------------------------

/***convertBlock***/
Token String_convert(Token token, ...) {
    char* stringPointer;
	
    switch (token.type) {
#ifdef TYPE_Boolean
    case TYPE_Boolean:
        stringPointer = BooleantoString(token.payload.Boolean);
        break;
#endif

#ifdef TYPE_Int
    case TYPE_Int:
        stringPointer = InttoString(token.payload.Int);
        break;
#endif

#ifdef TYPE_Double
    case TYPE_Double:
        stringPointer = DoubletoString(token.payload.Double);
        break;
#endif

    default:
        // FIXME: not finished
        fprintf(stderr, "String_convert(): Conversion from an unsupported type. (%d)\n", token.type);
        break;
    }
    token.payload.String = stringPointer;
    token.type = TYPE_String;
    return token;
}    
/**/

