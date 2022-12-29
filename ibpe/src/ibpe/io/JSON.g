grammar JSON;

options {
  language = Java;
  output = AST;
  }

@header {
package ibpe.io;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
}

@lexer::header {
package ibpe.io;
}

// Optional step: Disable automatic error recovery
@members { 
protected void mismatch(IntStream input, int ttype, BitSet follow) 
throws RecognitionException 
{ 
throw new MismatchedTokenException(ttype, input); 
} 
public Object recoverFromMismatchedSet(IntStream input, 
RecognitionException e, 
BitSet follow) 
throws RecognitionException 
{ 
throw e; 
}

    private Object extractNumber(Token numberToken, Token exponentToken) {
        String numberBody = numberToken.getText();
        String exponent = (exponentToken == null) ? null : exponentToken.getText().substring(1); // remove the 'e' prefix if there
        boolean isReal = numberBody.indexOf('.') >= 0 || exponent != null;
        if (!isReal) {
            return new Integer(numberBody);
        } else {
            double result = Double.parseDouble(numberBody);
            if (exponent != null) {
                result = result * Math.pow(10.0f, Double.parseDouble(exponent));
            }
            return new Double(result);
        }
    }
    
    private String extractString(Token token) {
        // StringBuffers are an efficient way to modify strings
        StringBuffer sb = new StringBuffer(token.getText());
        // Process character escapes
        int startPoint = 1; // skip initial quotation mark
        for (;;) {
            int slashIndex = sb.indexOf("\\", startPoint); // search for a single backslash
            if (slashIndex == -1) break;
            // Else, we have a backslash
            char escapeType = sb.charAt(slashIndex + 1);
            switch (escapeType) {
                case'u':
                    // Unicode escape.
                    String unicode = extractUnicode(sb, slashIndex);
                    sb.replace(slashIndex, slashIndex + 6, unicode); // backspace
                    break; // back to the loop

                    // note: Java's character escapes match JSON's, which is why it looks like we're replacing
                // "\b" with "\b". We're actually replacing 2 characters (slash-b) with one (backspace).
                case 'b':
                    sb.replace(slashIndex, slashIndex + 2, "\b"); // backspace
                    break;

                case 't':
                    sb.replace(slashIndex, slashIndex + 2, "\t"); // tab
                    break;

                case 'n':
                    sb.replace(slashIndex, slashIndex + 2, "\n"); // newline
                    break;

                case 'f':
                    sb.replace(slashIndex, slashIndex + 2, "\f"); // form feed
                    break;

                case 'r':
                    sb.replace(slashIndex, slashIndex + 2, "\r"); // return
                    break;

                case '\'':
                    sb.replace(slashIndex, slashIndex + 2, "\'"); // single quote
                    break;

                case '\"':
                    sb.replace(slashIndex, slashIndex + 2, "\""); // double quote
                    break;

                case '\\':
                    sb.replace(slashIndex, slashIndex + 2, "\\"); // backslash
                    break;
                    
                case '/':
                    sb.replace(slashIndex, slashIndex + 2, "/"); // solidus
                    break;

            }
            startPoint = slashIndex+1;

        }

        // remove surrounding quotes
        sb.deleteCharAt(0);
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private String extractUnicode(StringBuffer sb, int slashIndex) {
        // Gather the 4 hex digits, convert to an integer, translate the number to a unicode char, replace
        String result;
        String code = sb.substring(slashIndex + 2, slashIndex + 6);
        int charNum = Integer.parseInt(code, 16); // hex to integer
        // There's no simple way to go from an int to a unicode character.
        // We'll have to pass this through an output stream writer to do
        // the conversion.
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
            osw.write(charNum);
            osw.flush();
            result = baos.toString("UTF-8"); // Thanks to Silvester Pozarnik for the tip about adding "UTF-8" here
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

	public static Object parseString( String s ) throws RecognitionException
	{
		CharStream in = new ANTLRStringStream(s);
	    JSONLexer lexer = new JSONLexer(in);
	    UnbufferedTokenStream tokens = new UnbufferedTokenStream(lexer);
        JSONParser parser = new JSONParser(tokens);
        JSONParser.value_return r = parser.value();
        return r.result;
	}
} 
// Alter code generation so catch-clauses get replace with 
// this action. 
@rulecatch { 
catch (RecognitionException e) { 
throw e; 
} 
} 


json returns  [Object result]
  : value { $result = $value.result; }
  ;
  
value returns [Object result]
  : String { $result = extractString($String); } 
  | Number {Pattern.matches("(0|(-?[1-9]\\d*))(\\.\\d+)?", $Number.getText())}? Exponent? { $result = extractNumber($Number, $Exponent); }
  | '{' members '}' { $result = $members.result; }
  | '[' elements ']' { $result = $elements.result; }
  | 'true' { $result = Boolean.TRUE; }
  | 'false' {$result = Boolean.FALSE; }
  | 'null' {$result = null; }
  ;

  
elements returns [List result]
  @init { $result = new ArrayList(); }
  : v=value { $result.add(0,$v.result); } ( ',' vs=elements { $result.addAll($vs.result); } )?
  | /* empty list */
  ;
  

members returns [Map result]
  @init { $result = new HashMap(); }
  : String ':' value {$result.put(extractString($String),$value.result);} ( ',' vs=members { $result.putAll($vs.result); } )? 
  | /* empty object */
  ;
   
Number  : '-'? Digit+ ( '.' Digit+)?;

Exponent: ('e'|'E') '-'? ('0'..'9') Digit*;

String  : 
  '"' ( EscapeSequence | ~('\u0000'..'\u001f' | '\\' | '\"') )* '"'
  ;

WS: (' '|'\n'|'\r'|'\t')+ {skip();} ; // ignore whitespace 

fragment EscapeSequence
      : '\\' (UnicodeEscape |'b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\'|'\/')
      ;

fragment UnicodeEscape
  : 'u' HexDigit HexDigit HexDigit HexDigit
  ;

fragment HexDigit
  : '0'..'9' | 'A'..'F' | 'a'..'f'
  ;

fragment Digit
  : '0'..'9'
  ;

