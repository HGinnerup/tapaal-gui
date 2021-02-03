/* ArcExpressionParser.java */
/* Generated By:JavaCC: Do not edit this line. ArcExpressionParser.java */
package dk.aau.cs.model.CPN.ArcExpressionParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;import java.util.Vector;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ProductType;import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.CPN.Color;


public class ArcExpressionParser implements ArcExpressionParserConstants {

        private static final String ERROR_PARSING_QUERY_MESSAGE = "TAPAAL countered an error trying to parse the expression";
    private static ColorType colorType;
    private static TimedArcPetriNetNetwork network;
        public static ArcExpression parse(String expression, ColorType inputPlaceColorType, TimedArcPetriNetNetwork inputNetwork) throws ParseException {
            colorType = inputPlaceColorType;
            network = inputNetwork;
                ArcExpressionParser parser = new ArcExpressionParser(new StringReader(expression));
                return parser.AddExpression();
        }
    public static ArcExpression parseNumberOfExpression(String expression, ColorType inputPlaceColorType, TimedArcPetriNetNetwork inputNetwork) throws ParseException {
        colorType = inputPlaceColorType;
        network = inputNetwork;
        ArcExpressionParser parser = new ArcExpressionParser(new StringReader(expression));
        return parser.NumberOfExpression();
    }

  final public ArcExpression AddExpression() throws ParseException {ArcExpression currentChild;
        Vector<ArcExpression> constituents = new Vector<ArcExpression>();
    currentChild = SubExpression();
constituents.add(currentChild);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case PLUS:{
        ;
        break;
        }
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      jj_consume_token(PLUS);
      currentChild = SubExpression();
constituents.add(currentChild);
    }
{if ("" != null) return constituents.size() == 1 ? currentChild : new AddExpression(constituents);}
    throw new Error("Missing return statement in function");
}

  final public ArcExpression SubExpression() throws ParseException {ArcExpression left = null;
        ArcExpression right = null;
    left = ScalarExpression();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case SUB:{
      jj_consume_token(SUB);
      right = ScalarExpression();
      break;
      }
    default:
      jj_la1[1] = jj_gen;
      ;
    }
{if ("" != null) return right == null ? left : new SubtractExpression(left,right);}
    throw new Error("Missing return statement in function");
}

  final public ArcExpression ScalarExpression() throws ParseException {ArcExpression currentChild;
        Token number;
    if (jj_2_1(2147483647)) {
      number = jj_consume_token(NUM);
      jj_consume_token(MULT);
      currentChild = AddExpression();
{if ("" != null) return new ScalarProductExpression(Integer.parseInt(number.image),currentChild);}
    } else {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NUM:
      case 10:{
        currentChild = term();
{if ("" != null) return currentChild;}
        break;
        }
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
}

  final public ArcExpression term() throws ParseException {ArcExpression childExpression;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case NUM:{
      childExpression = NumberOfExpression();
{if ("" != null) return childExpression;}
      break;
      }
    case 10:{
      jj_consume_token(10);
      childExpression = AddExpression();
      jj_consume_token(11);
{if ("" != null) return childExpression;}
      break;
      }
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

  final public NumberOfExpression NumberOfExpression() throws ParseException {Token number;
        ColorExpression expr;
    number = jj_consume_token(NUM);
    jj_consume_token(12);
    expr = ColorExpression();
{if ("" != null) return new NumberOfExpression(Integer.parseInt(number.image), new Vector<>(Arrays.asList(expr)));}
    throw new Error("Missing return statement in function");
}

  final public ColorExpression ColorExpression() throws ParseException {ColorExpression subexpression;

    Vector<ColorExpression> expressions = new Vector<ColorExpression>();
    ArrayList<String> succPreds= new ArrayList<String>();
    Token name;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 10:{
      jj_consume_token(10);
      subexpression = Element();
expressions.add(subexpression);
      label_2:
      while (true) {
        jj_consume_token(13);
        subexpression = Element();
expressions.add(subexpression);
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 13:{
          ;
          break;
          }
        default:
          jj_la1[4] = jj_gen;
          break label_2;
        }
      }
      jj_consume_token(11);
      label_3:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 14:
        case 15:{
          ;
          break;
          }
        default:
          jj_la1[5] = jj_gen;
          break label_3;
        }
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 14:{
          jj_consume_token(14);
succPreds.add("++");
          break;
          }
        case 15:{
          jj_consume_token(15);
succPreds.add("--");
          break;
          }
        default:
          jj_la1[6] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
ColorExpression cexpr = new TupleExpression(expressions);
      //assumes single level producttypes
      boolean colorTypeExists = false;
      if(colorType instanceof ProductType){
          if(((ProductType)colorType).containsTypes(cexpr.getColorTypes())){
            colorTypeExists = true;
          }
      } else{
          {if (true) throw new ParseException("This arc can only output colors of the type " + colorType.getName() + " which is not a product type.");}
      }

      if(!colorTypeExists){
          {if (true) throw new ParseException("The color type on the input place and the given color " + cexpr.toString() + " does not match.");}
      }
      for(String s : succPreds){
           if(s.equals("++")){
               cexpr = new SuccessorExpression(cexpr);
           } else{
               cexpr = new PredecessorExpression(cexpr);
           }
      }

      {if ("" != null) return cexpr;}
      break;
      }
    default:
      jj_la1[7] = jj_gen;
      if (jj_2_2(2147483647)) {
        name = jj_consume_token(IDENT);
        jj_consume_token(16);
if(name.toString().equals(colorType.getName())){
                {if ("" != null) return new AllExpression(colorType);}
            } else{
              {if (true) throw new ParseException("The colortype " + name.toString() + " does not match the colortype of the input place");}
            }
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case IDENT:{
          subexpression = Element();
{if ("" != null) return subexpression;}
          break;
          }
        default:
          jj_la1[8] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
    throw new Error("Missing return statement in function");
}

  final public ColorExpression Element() throws ParseException {Token name;
    Vector<String> succPreds= new Vector<String>();
    if (jj_2_3(2147483647)) {
      name = jj_consume_token(IDENT);
      jj_consume_token(16);
ColorType constituentColorType = null;
                  boolean typeContained = false;
                  if(colorType instanceof ProductType){
                      for( ColorType ct : ((ProductType)colorType).getColorTypes()){
                          if(ct.getName().equals( name.toString())){
                              constituentColorType = ct;
                          }
                      }
                  }
                  if(constituentColorType == null){
                      {if (true) throw new ParseException("We could not find the colortype: " + name.toString());}
                  }
                  {if ("" != null) return new AllExpression(constituentColorType);}
    } else {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case IDENT:{
        name = jj_consume_token(IDENT);
        label_4:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case 14:
          case 15:{
            ;
            break;
            }
          default:
            jj_la1[9] = jj_gen;
            break label_4;
          }
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case 14:{
            jj_consume_token(14);
succPreds.add("++");
            break;
            }
          case 15:{
            jj_consume_token(15);
succPreds.add("--");
            break;
            }
          default:
            jj_la1[10] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
        }
ColorExpression cexpr = null;

        Color c = network.getColorByName(name.toString());
        if(c != null){
            cexpr = new UserOperatorExpression(c);

            if(!colorType.contains(c)){
                {if (true) throw new ParseException("The color \"" + c.getColorName() + "\" is not contained in the color type \"" + colorType.getName() + "\"");}
            }
        } else if(network.getVariableByName(name.toString()) != null){
            VariableExpression varexpr = new VariableExpression(network.getVariableByName(name.toString()));
            ColorType variableColorType = varexpr.getVariable().getColorType();
            if(colorType instanceof ProductType){
                if(variableColorType instanceof ProductType && !variableColorType.equals(colorType)){
                    {if (true) throw new ParseException("The variable \"" + name.toString() + "\" does not match the colortype \"" + colorType.getName() + "\" and we do not allow nested tuples");}
                }else if(variableColorType instanceof ProductType){
                    cexpr = varexpr;
                } else if(!(variableColorType instanceof ProductType)){
                    for( ColorType ct : ((ProductType)colorType).getColorTypes()){
                        if(ct.equals(variableColorType)){
                          cexpr = varexpr;
                        }
                    }
                    if(cexpr == null){
                        {if (true) throw new ParseException("The color type of variable \"" + name.toString() + "\" could not be found in the input colortype \"" + colorType.getName() + "\".");}
                    }
                }
            }else{
                if(colorType.equals(variableColorType)){
                    cexpr = varexpr;
                } else{
                    {if (true) throw new ParseException("The color type of variable \"" + name.toString() + "\" does not match the input colortype \"" + colorType.getName() + "\".");}
                }
            }
        } else{
            {if (true) throw new ParseException("Could not parse " + name + " as the name could not be found");}
        }
        for(String s : succPreds){
            if(s.equals("++")){
                cexpr = new SuccessorExpression(cexpr);
            } else{
                cexpr = new PredecessorExpression(cexpr);
            }
        }

        {if ("" != null) return cexpr;}
        break;
        }
      default:
        jj_la1[11] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
}

  private boolean jj_2_1(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_1()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_2()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_3()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_3_3()
 {
    if (jj_scan_token(IDENT)) return true;
    if (jj_scan_token(16)) return true;
    return false;
  }

  private boolean jj_3R_Element_178_6_18()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_3()) {
    jj_scanpos = xsp;
    if (jj_3R_Element_195_7_21()) return true;
    }
    return false;
  }

  private boolean jj_3R_ColorExpression_136_141_22()
 {
    if (jj_scan_token(14)) return true;
    return false;
  }

  private boolean jj_3R_ColorExpression_136_141_20()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_ColorExpression_136_141_22()) {
    jj_scanpos = xsp;
    if (jj_3R_ColorExpression_136_170_23()) return true;
    }
    return false;
  }

  private boolean jj_3R_AddExpression_75_9_5()
 {
    if (jj_3R_SubExpression_90_9_6()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_AddExpression_78_17_7()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_ColorExpression_170_7_17()
 {
    if (jj_3R_Element_178_6_18()) return true;
    return false;
  }

  private boolean jj_3R_NumberOfExpression_120_9_14()
 {
    if (jj_scan_token(NUM)) return true;
    if (jj_scan_token(12)) return true;
    if (jj_3R_ColorExpression_136_5_15()) return true;
    return false;
  }

  private boolean jj_3R_Element_195_23_25()
 {
    if (jj_scan_token(14)) return true;
    return false;
  }

  private boolean jj_3R_Element_195_23_24()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_Element_195_23_25()) {
    jj_scanpos = xsp;
    if (jj_3R_Element_195_52_26()) return true;
    }
    return false;
  }

  private boolean jj_3_2()
 {
    if (jj_scan_token(IDENT)) return true;
    if (jj_scan_token(16)) return true;
    return false;
  }

  private boolean jj_3R_term_111_9_11()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_term_111_9_12()) {
    jj_scanpos = xsp;
    if (jj_3R_term_112_7_13()) return true;
    }
    return false;
  }

  private boolean jj_3R_term_111_9_12()
 {
    if (jj_3R_NumberOfExpression_120_9_14()) return true;
    return false;
  }

  private boolean jj_3R_term_112_7_13()
 {
    if (jj_scan_token(10)) return true;
    if (jj_3R_AddExpression_75_9_5()) return true;
    if (jj_scan_token(11)) return true;
    return false;
  }

  private boolean jj_3R_ColorExpression_136_170_23()
 {
    if (jj_scan_token(15)) return true;
    return false;
  }

  private boolean jj_3R_ScalarExpression_104_11_10()
 {
    if (jj_3R_term_111_9_11()) return true;
    return false;
  }

  private boolean jj_3R_ColorExpression_136_70_19()
 {
    if (jj_scan_token(13)) return true;
    if (jj_3R_Element_178_6_18()) return true;
    return false;
  }

  private boolean jj_3R_ScalarExpression_103_9_8()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_1()) {
    jj_scanpos = xsp;
    if (jj_3R_ScalarExpression_104_11_10()) return true;
    }
    return false;
  }

  private boolean jj_3_1()
 {
    if (jj_scan_token(NUM)) return true;
    if (jj_scan_token(MULT)) return true;
    if (jj_3R_AddExpression_75_9_5()) return true;
    return false;
  }

  private boolean jj_3R_SubExpression_92_17_9()
 {
    if (jj_scan_token(SUB)) return true;
    if (jj_3R_ScalarExpression_103_9_8()) return true;
    return false;
  }

  private boolean jj_3R_Element_195_7_21()
 {
    if (jj_scan_token(IDENT)) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_Element_195_23_24()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_Element_195_52_26()
 {
    if (jj_scan_token(15)) return true;
    return false;
  }

  private boolean jj_3R_SubExpression_90_9_6()
 {
    if (jj_3R_ScalarExpression_103_9_8()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_SubExpression_92_17_9()) jj_scanpos = xsp;
    return false;
  }

  private boolean jj_3R_AddExpression_78_17_7()
 {
    if (jj_scan_token(PLUS)) return true;
    if (jj_3R_SubExpression_90_9_6()) return true;
    return false;
  }

  private boolean jj_3R_ColorExpression_136_5_15()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_ColorExpression_136_5_16()) {
    jj_scanpos = xsp;
    if (jj_3_2()) {
    jj_scanpos = xsp;
    if (jj_3R_ColorExpression_170_7_17()) return true;
    }
    }
    return false;
  }

  private boolean jj_3R_ColorExpression_136_5_16()
 {
    if (jj_scan_token(10)) return true;
    if (jj_3R_Element_178_6_18()) return true;
    Token xsp;
    if (jj_3R_ColorExpression_136_70_19()) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_ColorExpression_136_70_19()) { jj_scanpos = xsp; break; }
    }
    if (jj_scan_token(11)) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_ColorExpression_136_141_20()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  /** Generated Token Manager. */
  public ArcExpressionParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[12];
  static private int[] jj_la1_0;
  static {
	   jj_la1_init_0();
	}
	private static void jj_la1_init_0() {
	   jj_la1_0 = new int[] {0x2,0x4,0x410,0x410,0x2000,0xc000,0xc000,0x400,0x20,0xc000,0xc000,0x20,};
	}
  final private JJCalls[] jj_2_rtns = new JJCalls[3];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public ArcExpressionParser(java.io.InputStream stream) {
	  this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ArcExpressionParser(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source = new ArcExpressionParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 12; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
	  ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 12; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public ArcExpressionParser(java.io.Reader stream) {
	 jj_input_stream = new SimpleCharStream(stream, 1, 1);
	 token_source = new ArcExpressionParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 12; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
	if (jj_input_stream == null) {
	   jj_input_stream = new SimpleCharStream(stream, 1, 1);
	} else {
	   jj_input_stream.ReInit(stream, 1, 1);
	}
	if (token_source == null) {
 token_source = new ArcExpressionParserTokenManager(jj_input_stream);
	}

	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 12; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public ArcExpressionParser(ArcExpressionParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 12; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(ArcExpressionParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 12; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
	 Token oldToken;
	 if ((oldToken = token).next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 if (token.kind == kind) {
	   jj_gen++;
	   if (++jj_gc > 100) {
		 jj_gc = 0;
		 for (int i = 0; i < jj_2_rtns.length; i++) {
		   JJCalls c = jj_2_rtns[i];
		   while (c != null) {
			 if (c.gen < jj_gen) c.first = null;
			 c = c.next;
		   }
		 }
	   }
	   return token;
	 }
	 token = oldToken;
	 jj_kind = kind;
	 throw generateParseException();
  }

  @SuppressWarnings("serial")
  static private final class LookaheadSuccess extends Error {
    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }
  static private final LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
	 if (jj_scanpos == jj_lastpos) {
	   jj_la--;
	   if (jj_scanpos.next == null) {
		 jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
	   } else {
		 jj_lastpos = jj_scanpos = jj_scanpos.next;
	   }
	 } else {
	   jj_scanpos = jj_scanpos.next;
	 }
	 if (jj_rescan) {
	   int i = 0; Token tok = token;
	   while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
	   if (tok != null) jj_add_error_token(kind, i);
	 }
	 if (jj_scanpos.kind != kind) return true;
	 if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
	 return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
	 if (token.next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 jj_gen++;
	 return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
	 Token t = token;
	 for (int i = 0; i < index; i++) {
	   if (t.next != null) t = t.next;
	   else t = t.next = token_source.getNextToken();
	 }
	 return t;
  }

  private int jj_ntk_f() {
	 if ((jj_nt=token.next) == null)
	   return (jj_ntk = (token.next=token_source.getNextToken()).kind);
	 else
	   return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
	 if (pos >= 100) {
		return;
	 }

	 if (pos == jj_endpos + 1) {
	   jj_lasttokens[jj_endpos++] = kind;
	 } else if (jj_endpos != 0) {
	   jj_expentry = new int[jj_endpos];

	   for (int i = 0; i < jj_endpos; i++) {
		 jj_expentry[i] = jj_lasttokens[i];
	   }

	   for (int[] oldentry : jj_expentries) {
		 if (oldentry.length == jj_expentry.length) {
		   boolean isMatched = true;

		   for (int i = 0; i < jj_expentry.length; i++) {
			 if (oldentry[i] != jj_expentry[i]) {
			   isMatched = false;
			   break;
			 }

		   }
		   if (isMatched) {
			 jj_expentries.add(jj_expentry);
			 break;
		   }
		 }
	   }

	   if (pos != 0) {
		 jj_lasttokens[(jj_endpos = pos) - 1] = kind;
	   }
	 }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
	 jj_expentries.clear();
	 boolean[] la1tokens = new boolean[17];
	 if (jj_kind >= 0) {
	   la1tokens[jj_kind] = true;
	   jj_kind = -1;
	 }
	 for (int i = 0; i < 12; i++) {
	   if (jj_la1[i] == jj_gen) {
		 for (int j = 0; j < 32; j++) {
		   if ((jj_la1_0[i] & (1<<j)) != 0) {
			 la1tokens[j] = true;
		   }
		 }
	   }
	 }
	 for (int i = 0; i < 17; i++) {
	   if (la1tokens[i]) {
		 jj_expentry = new int[1];
		 jj_expentry[0] = i;
		 jj_expentries.add(jj_expentry);
	   }
	 }
	 jj_endpos = 0;
	 jj_rescan_token();
	 jj_add_error_token(0, 0);
	 int[][] exptokseq = new int[jj_expentries.size()][];
	 for (int i = 0; i < jj_expentries.size(); i++) {
	   exptokseq[i] = jj_expentries.get(i);
	 }
	 return new ParseException(token, exptokseq, tokenImage);
  }

  private boolean trace_enabled;

/** Trace enabled. */
  final public boolean trace_enabled() {
	 return trace_enabled;
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
	 jj_rescan = true;
	 for (int i = 0; i < 3; i++) {
	   try {
		 JJCalls p = jj_2_rtns[i];

		 do {
		   if (p.gen > jj_gen) {
			 jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
			 switch (i) {
			   case 0: jj_3_1(); break;
			   case 1: jj_3_2(); break;
			   case 2: jj_3_3(); break;
			 }
		   }
		   p = p.next;
		 } while (p != null);

		 } catch(LookaheadSuccess ls) { }
	 }
	 jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
	 JJCalls p = jj_2_rtns[index];
	 while (p.gen > jj_gen) {
	   if (p.next == null) { p = p.next = new JJCalls(); break; }
	   p = p.next;
	 }

	 p.gen = jj_gen + xla - jj_la; 
	 p.first = token;
	 p.arg = xla;
  }

  static final class JJCalls {
	 int gen;
	 Token first;
	 int arg;
	 JJCalls next;
  }

}
