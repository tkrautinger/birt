/* Generated By:JavaCC: Do not edit this line. XDepthParser.java */
package  org.eclipse.birt.report.model.util.xpathparser;

import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;

/**
 * Parses the depths of xpath. 
 *
 * @version $Revision: 1.2 $ $Date: 2007/01/17 02:06:29 $
 */

public class XDepthParser implements XDepthParserConstants {
        private List depths = new ArrayList();

        // ******** the type of current element *****

        static final int INVALID = 0;
        static final int ELEMENT = 1;
        static final int SLOT = 2;
        static final int PROPERTY = 4;
        static final int UNKNOWN = 8;

        private int currentValueType = UNKNOWN;

        public XDepthParser( String input )
        {
                this( new StringReader( input ) );
        }


        /**
	 * Returns the depth.
	 *  
	 */

        public List getDepthInfo( )
        {
                return depths;
        }

        class DepthInfo
         {
                private String propName;
                private String propValue;

                private int index;

                private String tagName;

                private int valueType = UNKNOWN;

                DepthInfo(String tagName, Object[] props)
                {
                        this.tagName = tagName;
                        this.index = index;
                        if (props instanceof String[])
                        {
                                propName = (String) props[0];
                                propValue = (String) props[1];
                        }
                        else if (props instanceof Integer[])
                        {
                                index = ((Integer) props[0]).intValue() - 1;
                        }
                }

                int getIndex()
                {
                        return index;
                }

                String getPropertyName()
                {
                        return propName;
                }

                String getPropertyValue()
                {
                        return propValue;
                }

                String getTagName()
                {
                        return tagName;
                }

                private void setValueType( int valueType )
                {
                        this.valueType = valueType;
                }

                int getValueType( )
                {
                        return valueType;
                }
         }

/**
 * Deals with the shorthand property font.
 */
  final public void parse() throws ParseException {
        Token t1;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SEPARATOR:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      jj_consume_token(SEPARATOR);
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 0:
      case 16:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case 16:
          jj_consume_token(16);
          break;
        default:
          jj_la1[1] = jj_gen;
          ;
        }
        jj_consume_token(0);
        break;
      case PROPPREFIX:
        PureProperty();
        break;
      case NAME:
        Depth();
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
  }

  final public void Depth() throws ParseException {
        Object[] props = null;
        int index = -1;

        Token t1;
    t1 = jj_consume_token(NAME);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LEFTB:
      props = Property();
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
                DepthInfo tmpDepth = new DepthInfo(t1.image, props);
                tmpDepth.setValueType(currentValueType);
                depths.add(tmpDepth);
                {if (true) return;}
  }

  final public void PureProperty() throws ParseException {
        Token t1;
    String[] props = {"name", null};
    jj_consume_token(PROPPREFIX);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NAME:
      t1 = jj_consume_token(NAME);
      break;
    case PROPNAME:
      t1 = jj_consume_token(PROPNAME);
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(0);
            props[1] = t1.image;
                DepthInfo tmpDepth = new DepthInfo(null, props);
                tmpDepth.setValueType(PROPERTY);
                depths.add(tmpDepth);
                {if (true) return;}
  }

  final public Object[] Property() throws ParseException {
        Token t2;
        Object[] rets;
        int index;
    jj_consume_token(LEFTB);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PROPPREFIX:
      jj_consume_token(PROPPREFIX);
                        rets = PropertyPair();
                        {if (true) return rets;}
      break;
    case INDEX:
      index = Index();
                        rets    = new Integer[2];
                        rets[0] = new Integer(index);
                        {if (true) return rets;}
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public String[] PropertyPair() throws ParseException {
        String[] rets = {null, null};
    rets[0] = PropertyName();
    jj_consume_token(EQUAL);
    jj_consume_token(QUOTE);
    rets[1] = PropertyValue();
    jj_consume_token(QUOTE);
    jj_consume_token(RIGHTB);
          {if (true) return rets;}
    throw new Error("Missing return statement in function");
  }

  final public String PropertyName() throws ParseException {
        Token t1;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PROPNAME:
      t1 = jj_consume_token(PROPNAME);
                currentValueType = PROPERTY;
                {if (true) return t1.image;}
      break;
    case PROPID:
      t1 = jj_consume_token(PROPID);
                currentValueType = ELEMENT ;
                {if (true) return t1.image;}
      break;
    case PROPSLOTNAME:
      t1 = jj_consume_token(PROPSLOTNAME);
                currentValueType = SLOT;
                {if (true) return t1.image;}
      break;
    case NAME:
      t1 = jj_consume_token(NAME);
                currentValueType = INVALID;
                {if (true) return t1.image;}
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public String PropertyValue() throws ParseException {
        Token t1;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INDEX:
      t1 = jj_consume_token(INDEX);
                {if (true) return t1.image;}
      break;
    case NAME:
      t1 = jj_consume_token(NAME);
                {if (true) return t1.image;}
      break;
    default:
      jj_la1[7] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public int Index() throws ParseException {
        Token t1;
        int value = -1;
    t1 = jj_consume_token(INDEX);
          value = Integer.parseInt(t1.image);
    jj_consume_token(RIGHTB);
           {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  public XDepthParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[8];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x40,0x10000,0x18003,0x10,0x8800,0x4002,0xb800,0xc000,};
   }

  public XDepthParser(java.io.InputStream stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new XDepthParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  public XDepthParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new XDepthParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  public XDepthParser(XDepthParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  public void ReInit(XDepthParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 8; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[17];
    for (int i = 0; i < 17; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 8; i++) {
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
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
