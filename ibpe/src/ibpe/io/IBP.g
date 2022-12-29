grammar IBP; 


options 
{
	language = Java;
	output = AST;
	backtrack = false;
}

@parser::header 
{
  package ibpe.io;
  import ibpe.model.*;
  import java.util.LinkedList;
  import java.util.HashSet;
  import java.util.HashMap;
  import java.util.Map;
  import java.util.Set;
  import org.eclipse.draw2d.geometry.Point;
  import org.eclipse.draw2d.geometry.Rectangle;
}

@lexer::header 
{
  package ibpe.io;
}

@lexer::members
{
private IErrorReporter errorReporter = null;
  
  public IErrorReporter getErrorReporter() 
  {
    int x = 1;
    return errorReporter;
  }
  
  public void setErrorReporter(IErrorReporter r) 
  {
    errorReporter = r;
  }
  
  @Override
  public void emitErrorMessage(String msg) 
  {
    assert errorReporter!=null;
    errorReporter.reportError(msg);
  }
}

@parser::members 
{
  private IErrorReporter errorReporter = null;
	
	public IErrorReporter getErrorReporter() 
	{
	  return errorReporter;
	}
	
	public void setErrorReporter(IErrorReporter r) 
	{
	  errorReporter = r;
	}
	
	@Override
	public void emitErrorMessage(String msg) 
	{
	  assert errorReporter!=null;
	  errorReporter.reportError(msg);
	}
	
	public void checkMatchingIds( Token id1, Token id2 )
	{
	  if (!id1.getText().equals(id2.getText())) 
	     emitErrorMessage("line "+id2.getLine()+":"+id2.getCharPositionInLine()+ 
	                      ", identifier '"+id2.getText()+"' does not match '"+id1.getText()+
	                      "' at line "+id1.getLine()+":"+id1.getCharPositionInLine());
	}
	
	// lists of created transitions and forks in the currently processed procedure
	private ArrayList<Transition> transitions; 
	private ArrayList<Fork> forks;

  // identifiers that should resolve to the transition's target 
	private HashMap<String,ArrayList<Transition>> unresolved; 
	
	protected void initTransitionIdTable()
	{
	   transitions = new ArrayList<Transition>();
	   forks = new ArrayList<Fork>();
	   unresolved = new HashMap<String,ArrayList<Transition>>();
	}
	
	protected void addToTransitionIdTable( Token id, Transition t )
	{
	   if (!unresolved.containsKey(id.getText()))
	       unresolved.put(id.getText(),new ArrayList<Transition>());
	   unresolved.get(id.getText()).add(t);
	}
	
	protected void resolveTransitionIds( Map<String,Node> ns )
	{
	   Set<String> resolved = new HashSet<String>();
	  
		  for (Map.Entry<String,ArrayList<Transition>> e: unresolved.entrySet())
		      if (ns.containsKey(e.getKey()))
		      {
		          GraphNode n = (GraphNode)ns.get(e.getKey());
		          for (Transition t : e.getValue())
		              t.setTarget(n);
		          resolved.add(e.getKey());
		      }
		      else
		          emitErrorMessage("reference to unknown situation '"+e.getKey()+"'");
		 for (String s : resolved)
	       unresolved.remove(s);
	}
}

// Throw exception upwards in rules 
@rulecatch { catch (RecognitionException e) { reportError(e); throw e; } }

	

context returns [ Context elem ]
: id1=ID COLON CONTEXT NEWLINE?
  BEGIN NEWLINE cts=context_content_list END id2=ID NEWLINE?
  { 
    checkMatchingIds($id1,$id2);
    $elem = new Context($id1.text,$cts.list); 
  }     
;
  
context_content_list returns [ LinkedList<Element> list ]
: c=context_content cs=context_content_list { $list = $cs.list; $list.addFirst($c.elem); }
| /* empty list */ { $list = new LinkedList<Element>(); }
;


context_content returns [ Element elem ]
: textrow SEMICOLON NEWLINE? { $elem = $textrow.elem; }
| procedure { $elem = $procedure.elem; }
;


procedure returns [ Procedure elem ]
@init { initTransitionIdTable(); }
: id1=ID signature COLON PROCEDURE NEWLINE?
  pre?
  post_list 
  variant 
  BEGIN NEWLINE
    decl_list
    situation_list
    trs?
  END id2=ID NEWLINE
  {
    checkMatchingIds($id1,$id2);
    
    if ($trs.transitions!=null)
    {
        assert($pre.elem!=null);
        $pre.elem.setChoiceType($trs.type);
        for (Transition t : $trs.transitions)
		        t.setSource($pre.elem);
	  }
        
    $elem = new Procedure($id1.text, 
                          $signature.list, 
                          $pre.elem, 
                          $post_list.list, 
                          $variant.elem,
                          $decl_list.list,
                          $situation_list.list,
                          transitions,forks);
    resolveTransitionIds($elem.getNamespace());
    for (Transition t : transitions)
    {
      // Transitions without target go to the anonymous
      // postcondition. This breaks if there are several
      // anonymous postconditions. 
        if (t.getTarget()==null)
        {
          for (Postcondition p:$elem.getPostconditions())
            if (p.isAnonymous())
            {
              t.setTarget(p);
              break;
            }
        }
    }
    
    // TODO: check for unresolved transitions
    assert unresolved.isEmpty();
    
    // TODO: check duplicate post, situation identifiers
    // TOOD: add initial transition
  }     
;


signature returns [ List<TextRow> list ] 
: LBRACKET NEWLINE decl_list RBRACKET { $list = $decl_list.list; }
| /* no args */ { $list =new LinkedList<TextRow>(); }
;


decl_list returns [LinkedList<TextRow> list ]
: a=textrow SEMICOLON NEWLINE? as=decl_list { $list = $as.list; $list.addFirst($a.elem); }
| /* empty list */ { $list = new LinkedList<TextRow>(); }
;


pre returns [ Precondition elem ] 
: PRE BEGIN NEWLINE?
    KWFLAG rectangle NEWLINE
    constraint_list
  END NEWLINE
  { 
    $elem = new Precondition($constraint_list.list);
    if ($rectangle.rect!=null) $elem.setBounds($rectangle.rect);    
  }
;

anon_post returns [ Postcondition elem ] 
: POST BEGIN NEWLINE?
    KWFLAG rectangle NEWLINE 
    constraint_list
  END NEWLINE 
  { 
    $elem = new Postcondition($constraint_list.list);
    $elem.setText("");
    if ($rectangle.rect!=null) $elem.setBounds($rectangle.rect);    
  }
 ;

named_post returns [ Postcondition elem ] 
: id1=ID COLON POST NEWLINE?
  BEGIN NEWLINE?
    KWFLAG rectangle NEWLINE 
    constraint_list
  END id2=ID NEWLINE
  { 
    checkMatchingIds($id1,$id2);
    $elem = new Postcondition($constraint_list.list);
    $elem.setText($id1.text); 
    if ($rectangle.rect!=null) $elem.setBounds($rectangle.rect);    
  }
;

post returns [ Postcondition elem ]
: anon_post { $elem = $anon_post.elem; }
| named_post { $elem = $named_post.elem; }
;

post_list returns [ LinkedList<Postcondition> list ]
: p=post ps=post_list { $list = $ps.list; $list.addFirst($p.elem); }
| /* empty list */ { $list = new LinkedList<Postcondition>(); }
;

situation_list returns [ LinkedList<Situation> list ]
: s=situation ss=situation_list { $list = $ss.list; $list.addFirst($s.elem); }
| /* empty list */ { $list = new LinkedList<Situation>(); }
;

situation returns [ Situation elem ]
: id1=ID COLON SITUATION NEWLINE? 
  BEGIN NEWLINE?
    KWFLAG rectangle NEWLINE
    declaration_list
    constraint_list
    variant
    situation_list
    trs?
  END id2=ID NEWLINE
  {
    checkMatchingIds($id1,$id2);
    $elem = new Situation($id1.text,$declaration_list.list,$constraint_list.list,$variant.elem,$situation_list.list);
    if ($rectangle.rect!=null) $elem.setBounds($rectangle.rect);
    if ($trs.transitions!=null) 
    {
        $elem.setChoiceType($trs.type);
        for (Transition t : $trs.transitions)
            t.setSource($elem);
    }    
  }
  ;
  /*
       [java] /home/aton4/mparsa/workspace/fi.imped.socos.IBPE/src/ibpe/io/IBP.g
     [java] ANTLR Parser Generator  Version 3.4
     [java] warning(200): /home/aton4/mparsa/workspace/fi.imped.socos.IBPE/src/ibpe/io/IBP.g:318:3: 
     [java] Decision can match input such as "STARSTAR {ANY..LBRACKET, POST..RBRACKET, SITUATION..WS} SEMICOLON NEWLINE" using multiple alternatives: 1, 2
     [java] As a result, alternative(s) 2 were disabled for that inpu
  */
constraint_list returns [LinkedList<TextRow> list ]
: c=constraint cs=constraint_list { $list = $cs.list; $list.addFirst($c.elem); }
| /* empty list */ { $list = new LinkedList<TextRow>(); }
;

constraint returns [TextRow elem]
: STAR textrow SEMICOLON NEWLINE { $elem = $textrow.elem; }
;

declaration_list returns [LinkedList<TextRow> list ]
: i=declaration il=declaration_list { $list = $il.list; $list.addFirst($i.elem); }
| /* empty list */ { $list = new LinkedList<TextRow>(); }
;

declaration returns [TextRow elem]
: MINUS textrow SEMICOLON NEWLINE { $elem = $textrow.elem; }
;

variant returns [ TextRow elem ]
: STARSTAR textrow SEMICOLON? NEWLINE { $elem = $textrow.elem; }
| /* no variant */ { $elem = null; }
;

trs returns [ Point point, String type, LinkedList<Transition> transitions ]
: t=(CHOICE|IF) (NEWLINE? KWFLAG pt=point)? NEWLINE 
    transition_list
  (ENDCHOICE|ENDIF) NEWLINE
  {
    $point = $pt.point; // may be null!
    $type = $t.getText().toUpperCase(); 
    $transitions = $transition_list.list;
  }
;

/*
call returns [ Point point, String call, LinkedList<Transition> transitions]
: CALL sss+=(~(SEMICOLON|NEWLINE))+ SEMICOLON? NEWLINE (NEWLINE? KWFLAG pt=point)? NEWLINE 
	transition_list
  ENDCALL NEWLINE
  {
    $point = $pt.point; // may be null!
    $call = "";
    for (Object t : $sss)
       $call += ((Token)t).getText();
    $transitions = $transition_list.list;
  }
;
*/

call returns [ Point point, String call, LinkedList<Transition> transitions]
: CALL t = textstring SEMICOLON? NEWLINE (NEWLINE? KWFLAG pt=point)? NEWLINE 
	transition_list
  ENDCALL NEWLINE
  {
    $point = $pt.point; // may be null!
    $call = t.t;
    $transitions = $transition_list.list;
  }
;


fork returns [Fork elem]
: ifchoice { $elem = $ifchoice.elem; }
| multicall { $elem = $multicall.elem; }
;


ifchoice returns [IFChoice elem]
: trs
{
    IFChoice f = new IFChoice($trs.point);
    f.setChoiceType($trs.type);
    forks.add(f);
    for (Transition t : $trs.transitions)
        t.setSource(f);
    $elem = f;
}
;

multicall returns [MultiCall elem]
: call
{
    MultiCall f = new MultiCall($call.point);
    f.setText($call.call);
    forks.add(f);
    for (Transition t : $call.transitions)
       t.setSource(f);
    $elem = f;
   
}
;

transition_decl_list returns [LinkedList<Element> list ]
: (a=textrow SEMICOLON NEWLINE? | b=proof) as=transition_decl_list 
  { 
    $list = $as.list;
    if($a.elem!=null) 
    {
      $list.addFirst($a.elem);
    }
    else if($b.elem!=null) 
    {
      $list.addFirst($b.elem);
    }    
  }
| /* empty list */ { $list = new LinkedList<Element>(); }
;


transition returns [ Transition elem ]
: KWFLAG (srcpt=point)? LBRACKET waypoints=point_list RBRACKET (trgpt=point)? NEWLINE
  KWFLAG lblpt=point NEWLINE 
  decls=transition_decl_list
  (fork | (GOTO gotoid=ID NEWLINE | EXIT exitid=ID NEWLINE | EXIT NEWLINE ))
  {
    $elem = new Transition($srcpt.point,$trgpt.point,$waypoints.list,$lblpt.point,$decls.list);
    transitions.add($elem);
    if ($fork.elem!=null)
    	$elem.setTarget($fork.elem);
    if ($gotoid!=null)
      addToTransitionIdTable($gotoid,$elem);
    if ($exitid!=null)
      addToTransitionIdTable($exitid,$elem);
  }
;


proof returns [Proof elem]
: PROOF NEWLINE decls=decl_list ENDPROOF NEWLINE
{
  $elem=new Proof();

  $elem.setText("");

  for(TextRow row: $decls.list)
  {
    $elem.setText($elem.getText()+row.getText()+"\n");
  }

};


transition_list returns [LinkedList<Transition> list ]
: t=transition ts=transition_list { $list = $ts.list; $list.addFirst($t.elem); }
| /* empty list */ { $list = new LinkedList<Transition>(); }
;

textstring returns [ String t ]
: (~(SEMICOLON|NEWLINE))* { $t = $text.trim(); }
;


textrow returns [ TextRow elem ]
: t = textstring { $elem = new TextRow($t.t); }
;

rectangle returns [ Rectangle rect ]
: LBRACKET x=INTEGER y=INTEGER width=INTEGER height=INTEGER RBRACKET 
  {
    $rect = new Rectangle(Integer.parseInt($x.text),
                          Integer.parseInt($y.text),
                          Integer.parseInt($width.text),
                          Integer.parseInt($height.text));
  } ;


name : ID;


point returns [ Point point ]
: LBRACKET x=INTEGER y=INTEGER RBRACKET 
  {
  	$point = new Point(Integer.parseInt($x.text),Integer.parseInt($y.text));
  }
;


point_list returns [ LinkedList<Point> list ]
: p=point ps=point_list { $list = $ps.list; $list.addFirst($p.point); }
| /* empty list */ { $list = new LinkedList<Point>(); }
;


/// FRAGMENT RULES ///

context_contents_fragment returns [ SequenceFragment frag ]
: context_content_list { $frag = $context_content_list.list.size()>0 ? new SequenceFragment($context_content_list.list) : null; }
;

diagram_fragment returns [ DiagramFragment frag ]
@init { initTransitionIdTable(); }
: pre?
  post_list
  situation_list
  trs? 
{ 
  if ($pre.elem!=null || $post_list.list.size()>0 || $situation_list.list.size()>0)
  {
      if ($trs.transitions!=null)
		      for (Transition t : $trs.transitions)
		      {
		          assert($pre.elem!=null);
		          t.setSource($pre.elem);
		          transitions.add(t);
		      }
      Map<String,Node> ns = new HashMap<String,Node>();
      for (Postcondition p : $post_list.list) ns.put(p.getText(),p);
      for (Situation s : $situation_list.list) ns.put(s.getText(),s);
      resolveTransitionIds(ns);
      Set<Transition> arcs = new HashSet<Transition>(transitions);
      for (Transition t:transitions)
          if (t.getTarget()==null)
              arcs.remove(t);
      
      $frag = new DiagramFragment($pre.elem,$post_list.list,$situation_list.list,arcs,forks);
  }
  else
      $frag = null;
}
;

/// TOKENS ///

KWFLAG    : '%:' ;

CONTEXT  : ('C'|'c')('O'|'o')('N'|'n')('T'|'t')('E'|'e')('X'|'x')('T'|'t');

PROCEDURE : ('P'|'p')('R'|'r')('O'|'o')('C'|'c')('E'|'e')('D'|'d')('U'|'u')('R'|'r')('E'|'e') ;

SITUATION : ('S'|'s')('I'|'i')('T'|'t')('U'|'u')('A'|'a')('T'|'t')('I'|'i')('O'|'o')('N'|'n') ;

BEGIN     : ('B'|'b')('E'|'e')('G'|'g')('I'|'i')('N'|'n') ;

PRE       : ('P'|'p')('R'|'r')('E'|'e') ;

POST      : ('P'|'p')('O'|'o')('S'|'s')('T'|'t') ;

ENDCHOICE : ('E'|'e')('N'|'n')('D'|'d')('C'|'c')('H'|'h')('O'|'o')('I'|'i')('C'|'c')('E'|'e') ;

ENDIF     : ('E'|'e')('N'|'n')('D'|'d')('I'|'i')('F'|'f') ;

ENDCALL   : ('E'|'e')('N'|'n')('D'|'d')('C'|'c')('A'|'a')('L'|'l')('L'|'l');

END       : ('E'|'e')('N'|'n')('D'|'d') ;

GOTO      : ('G'|'g')('O'|'o')('T'|'t')('O'|'o') ;

EXIT      : ('E'|'e')('X'|'x')('I'|'i')('T'|'t') ;

PROOF     : ('P'|'p')('R'|'r')('O'|'o')('O'|'o')('F'|'f');

ENDPROOF  : ('E'|'e')('N'|'n')('D'|'d')('P'|'p')('R'|'r')('O'|'o')('O'|'o')('F'|'f');

IF        : ('I'|'i')('F'|'f') ;

CALL      : ('C'|'c')('A'|'a')('L'|'l')('L'|'l') ;

CHOICE    : ('C'|'c')('H'|'h')('O'|'o')('I'|'i')('C'|'c')('E'|'e') ;

ID        : ( 'a'..'z' | 'A'..'Z')( 'a'..'z' | 'A'..'Z' | '0'..'9' | '_')* ;

INTEGER   : ('-')?( '0'..'9')+ ;

NEWLINE   : ( ( '\u000C' )?( '\r' )? '\n' )+ ;

WS        : ( ' ' | '\t' | '\u000C' )  { $channel = HIDDEN; } ;

STARSTAR  : '**';

STAR      : '*';

MINUS	  : '-';

COLON     : ':' ;

SEMICOLON : ';' ;

LBRACKET  : '[' ;

RBRACKET  : ']' ;

ANY : ('\u0000'..'\uFFFF');
