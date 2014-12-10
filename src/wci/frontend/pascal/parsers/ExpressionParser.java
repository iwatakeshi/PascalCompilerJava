package wci.frontend.pascal.parsers;

import java.util.EnumSet;
import java.util.HashMap;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalTokenType.NOT;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;

public class ExpressionParser extends StatementParser
{
    public ExpressionParser(PascalParserTD parent)
    {
      super(parent);
    }

    private static final EnumSet<PascalTokenType> REL_OPS = EnumSet.of(GREATER_THAN, GREATER_EQUALS);

    private static final HashMap<PascalTokenType, ICodeNodeType> REL_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();

    static{
      REL_OPS_MAP.put(EQUALS, EQ);
      REL_OPS_MAP.put(NOT_EQUALS, NE);
      REL_OPS_MAP.put(LESS_THAN, LT);
      REL_OPS_MAP.put(LESS_EQUALS, LE);
      REL_OPS_MAP.put(GREATER_THAN, GT);
      REL_OPS_MAP.Put(GREATER_EQUALS, GE);
    };

    private ICodeNode parseExpression(Token token)
      throws Exception
    {
      ICodeNode rootNode = parseSimpleExpression(token);

      token = currentToken();
      TokenType tokenType = token.getType();

      if (REL_OPS.contains(tokenType)) {
        ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);

        ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);

        opNode.addChild(rootNode);
        token = nextToken();

        opNode.addChild(parseSimpleExpression(token));

        rootNode = opNode;
       }
      return rootNode;
     }

     private static final EnumSet<PascalTokenType> ADD_OPS = EnumSet.of(PLUS, MMINUS, PascalTokenType.OR);

     static {
       ADD_OPS_OPS_MAP.put(PLUS, ADD);
       ADD_OPS_OPS_MAP.put(MINUS, SUBTRACT);
       ADD_OPS_OPS_MAP.put(PascalTokenType.OR, ICodeNodeTypeImpl.OR);
     };

     private ICodeNode parseSimpleExpression(Token token)
       throws Exception
     {
       TokenType signType = null;

       TokenType tokenType = token.getType();

       if ((tokenType == PLUS) || (tokenType == MINUS)) {
         signType = tokenType;
         token = nextToken();
       }

       ICodeNode rootNode = parseTerm(token);

       if (signType == MINUS) {

         ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);

         negateNode.addChild(rootNode);
         rootNode = negateNode;
       }

       token = currentToken();
       tokenType = token.getType();

       while (ADD_OPS.contains(tokenType)) {
        ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(tokenType);
        ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
        opNode.addChild(rootNode);

        token = nextToken();

        opNode.addChild(parseTerm(token));

        rootNode = opNode;

       token = currentToken();

        tokenType = token.getType();

        }

      return rootNode;
   }


   private static final EnumSet<PascalTokenType> MULT_OPS = EnumSet.of(STAR, SLASH, DIV, PascalTokenType.MOD, PascalTokenType.AND);

   private static final HashMap<PascalTokenType, ICodeNodeType> MULT_OPS_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();

   static {
     MULT_OPS_OPS_MAP.put(STAR, MULTIPLY);
     MULT_OPS_OPS_MAP.put(SLASH, FLOAT_DIVIDE);
     MULT_OPS_OPS_MAP.put(DIV, INTEGER_DIVIDE);
     MULT_OPS_OPS_MAP.put(PascalTokenType.MOD, ICodeNodeTypeImpl.MOD);
     MULT_OPS_OPS_MAP.put(PascalTokenType.AND, ICodeNodeTypeImpl.AND);
   };

   private ICodeNode parseTerm(Token token)
     throws Exception
   {
     ICodeNode rootNode = parseFactor(token);

     token = currentToken();

     TokenType tokenType = token.getType();

     while (MULT_OPS.contains(tokenType)) {

       ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(tokenType);

       ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
       opNode.addChild(rootNode);

       token = nextToken();

       opNode.addChild(parseFactor(token));

       rootNode = opNode;
       
       token = currentToken();
       tokenType = token.getType();
     }

    return rootNode;

  }


  private ICodeNode parseFactor(Token token)
    throws Exception
  {

    TokenType tokenType = token.getType();

    ICodeNode rootNode = null;

    switch ((PascalTokenType) tokenType) {
      case IDENTIFIER: {
      
        String name = token.getText().toLowerCase();

        SymTabEntry id = symTabStack.lookup(name);

        if (id == null) {
          errorHanlder.flag(token, IDENTIFIER_UNDEFINED, this);
          id = symTabStack.enterLocal(name);

        }

        rootNode = ICodeFactory.createICodeNode(VARIABLE);
        rootNode.setAttribute(ID, id);
        id.appendLineNumber(token.getLineNumber());

        token = nextToken();
        break;
       }

       case INTEGER: {
         rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);

         rootNode.setAttribute(VALUE, token.getValue());
         token = nextToken();
         break;
       }

       case REAL: {
         rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
         rootNode.setAttribute(VALUE, token.getValue());
         token = nextToken();
         break;
       }
       
       case STRING: {
         String value = (String) token.getValue();
         
         rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
         rootNode.setAttribute(VALUE, value);

         token = nextToken();
         break;
       }

       case NOT: {
         token = nextToken();
    
         rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);

         rootNode.addChild(parseFactor(token));
         break;
       }

       case LEFT_PAREN: {
         token = nextToken();

         rootNode = parseExpression(token);

         token = currentToken();

         if (token.getType() == RIGHT_PAREN) {
            token = nextToken();
         } else {
            errorHanlder.flag(token, MISSING_RIGHT_PAREN, this);
         }

         break;
       }

       default: {
         errorHandler.flag(token, UNEXPECTED_TOKEN, this);
         break;
       }

     }
    return rootNode;
   }
}

       
        
  
     
     
    

