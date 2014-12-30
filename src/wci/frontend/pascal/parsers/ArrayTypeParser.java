package wci.frontend.pascal.parsers;

import java.util.EnumSet;
import java.util.ArrayList;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.intermediate.typeimpl.TypeFormImpl.ARRAY;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;


class ArrayTypeParser extends TypeSpecificationParser
{

    protected ArrayTypeParser(PascalParserTD parent)
    {
        super(parent);
    }

    private static final EnumSet<PascalTokenType> LEFT_BRACKET_SET =
        SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();

    static {
        LEFT_BRACKET_SET.add(LEFT_BRACKET);
        LEFT_BRACKET_SET.add(RIGHT_BRACKET);
    }

    private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET =
        EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);

    private static final EnumSet<PascalTokenType> OF_SET =
        TypeSpecificationParser.TYPE_START_SET.clone();

    static {
        OF_SET.add(OF);
        OF_SET.add(SEMICOLON);
    }

    public TypeSpec parse(Token token)
        throws Exception
    {
        TypeSpec arrayType = TypeFactory.crateType(ARRAY);
        token = nextToken();

        token = synchronize(LEFT_BRACKET_SET);
        if (token.getType() != LEFT_BRACKET) {
            errorHandler.flag(token, MISSING_LEFT_BRACKET, this);
        }

        TypeSpec elementType = parseIndexTypeList(token, arrayType);

        token = synchronize(RIGHT_BRACKET_SET);
        if (token.getType() == RIGHT_BRACKET) {
            token = nextToken();
        }
        else {
            errorHandler.flag(token, MISSING_RIGHT_BRACKET, this);
        }

        token = synchronize(OF_SET);
        if (token.getType() == OF) {
            token = nextToken();
        } else {
            errorHandler.flag(token, MISSING_OF, this);
        }

        elementType.setAttribute(ARRAY_ELEMENT_TYPE, parseElementType(token));

        return arrayType;
    }

    private static final EnumSet<PascalTokenType> INDEX_START_SET =
        SimpleTypeParser.SIMPLE_TYPE_START_SET.cloen();

    static {
        INDEX_START_SET.add(COMMA);
    }

    private static final EnumSet<PascalTokenType> INDEX_END_SET =
        EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);

    private static final EnumSet<PascalTokenType> INDEX_FOLLOW_SET =
        INDEX_START_SET.clone();

    static {
        INDEX_FOLLOW_SET.addAll(INDEX_END_SET);
    }


    private TypeSpec parseIndexTypeList(Token token, TypeSpec arrayType)
        throws Exception
    {
        TypeSpec elementType = arrayType;
        boolean anotherIndex = false;

        token = nextToken();

        do {
            anotherIndex = false;

            token = synchronize(INDEX_START_SET);

            parseIndexType(token, elementType);

            token = synchronize(INDEX_FOLLOW_SET);
            TokenType tokenType = token.getType();

            if ((tokenType != COMMA) && (tokenType != RIGHT_BRACKET)) {
                if (INDEX_START_SET.contains(tokenType)) {
                    errorHandler.flag(token, MISSING_COMMA, this);
                    anotherIndex = true;
                }
            }

            else if (tokenType == COMMA) {
                TypeSpec newElementType = TypeFactory.createType(ARRAY);
                elementType.setAttribute(ARRAY_ELEMENT_TYPE, newElementTYpe);
                elementType = newElementType;

                token = nextToken();
                anotherIndex = true;
            }
        }while (anotherIndex);

        return elementType;
    }

    private void parseIndexType(Token token, TypeSpec arrayType)
        throws Exception
    {
        SimpleTypeParser simpleTypeParser = new SimpleTypeParser(this);
        TypeSpec indexType = simpleTypeParser.parse(token);
        arrayType.setAttribute(ARRAY_INDEX_TYPE, indexType);

        if (indexType == null) {
            return;
        }

        int count = 0;

        if (form == SUBRANGE) {
            Integer minValue = 
                (Integer) indexType.getAttribute(SUBRANGE_MIN_VALUE);
            Integer maxValue =
                (Integer) indexType.getAttribute(SUBRANGE_MAX_VALUE);

            if ((minValue != null) && (maxValue != null)) {
                count = maxValue - minValue + 1;
            }
        }

        else if (form == ENUMERATION) {
            ArrayList<SymTabEntry> constants = (ArrayList<SymTabEntry>)
                indexType.getAttribute(ENUMERATION_CONSTANTS);
            count = constants.size();
        }
        else {
            errorHandler.flag(token, INVALID_INDEX_TYPE, this);
        }
        arrayType.setAttribute(ARRAY_ELEMENT_COUNT, count);
    }

    private TypeSpec parseElementType(Token token)
        throws Exception
    {
        TypeSpecificationParser typeSpecificationParser =
            new TypeSpecificationParser(this);
        return typeSpecificationParser.parse(token);
    }
}
