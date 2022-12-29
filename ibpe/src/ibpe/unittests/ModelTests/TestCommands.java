package ibpe.unittests.ModelTests;

import junit.framework.TestCase;

public class TestCommands extends TestCase{
	
	// TODO: Disabled unit tests while refactoring
	
	/*
	private Context module;
	private CreateProcedureCommand procCommand;
	private CreateSituationCommand sitCommand;
	private CreateNewLeafCommand leafCommand;
	private ChangeNameCommand changeNameCommand;
	private ChangeStatementCommand changeStatementCommand;
	private CopyCommand copyCommand;
	private PasteCommand pasteCommand;
	private DeleteCommand deleteCommand;
	private MoveCommand moveCommand;
	
	protected void setUp(){
		module = new Context("testModule");
		procCommand = new CreateProcedureCommand();
		sitCommand = new CreateSituationCommand();
		leafCommand = new CreateNewLeafCommand();
		changeNameCommand = new ChangeNameCommand();
		changeStatementCommand = new ChangeStatementCommand();
		copyCommand = new CopyCommand();
		pasteCommand = new PasteCommand();
		deleteCommand =  new DeleteCommand();
		moveCommand = new MoveCommand();
	}
	
	public void testCommands(){
		
		procCommand.setCreateIn(module);
		procCommand.execute();
		List<Element> l = module.getStatementArray();	
		assertEquals(true, (l.get(0) instanceof Procedure));
		
		Procedure proc = (Procedure)l.get(0);
	
		sitCommand.setCreateIn(proc);
		sitCommand.execute();
		
		l = proc.getTextContainer().getStatementArray();
		l.addAll( proc.getBoxContainer().getStatementArray() );
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		assertEquals(3,l.size());
		
		sitCommand.undo();
		
		l = proc.getTextContainer().getStatementArray();
		l.addAll( proc.getBoxContainer().getStatementArray() );
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Placeholder));
				
		sitCommand.redo();		
		l = proc.getTextContainer().getStatementArray();
		l.addAll( proc.getBoxContainer().getStatementArray() );
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		assertEquals(3,l.size());
				
		Situation sit =  (Situation) l.get(1); // l.get(0) == place holder
		leafCommand.setCreateIn(sit);
		leafCommand.execute();		
		l = sit.getTextContainer().getStatementArray();
		l.addAll( sit.getBoxContainer().getStatementArray() );
		assertEquals(true, (l.get(0) instanceof TextRow));
		assertEquals(true, (l.get(1) instanceof Placeholder));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		
		TextRow leaf = (TextRow) l.get(0);
		sit.setText("nametobechanged");
		changeNameCommand.setModel(sit);
		changeNameCommand.setText("newName");
		changeNameCommand.execute();
		assertEquals("newName", sit.getText());
		
		leaf.setText("somethingtochange");
		changeStatementCommand.setModel(leaf);
		changeStatementCommand.setText("INVARIANT 0 <= i AND i <= len(a);");
		changeStatementCommand.execute();
		assertEquals("INVARIANT 0 <= i AND i <= len(a);", leaf.getText());
		
		copyCommand.addElement(proc);
		copyCommand.execute();
		pasteCommand.setCreateIn(module);
		pasteCommand.execute();
		
		l = module.getStatementArray();
		assertEquals(3,l.size());
		assertEquals(true, (l.get(0) instanceof Procedure));
		assertEquals(true, (l.get(1) instanceof Procedure));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		
		pasteCommand.undo();
		l = module.getStatementArray();		
		assertEquals(2,l.size());
		assertEquals(true, (l.get(0) instanceof Procedure));
		assertEquals(true, (l.get(1) instanceof Placeholder));
		
		pasteCommand.redo();		
		l = module.getStatementArray();		
		assertEquals(3,l.size());
		assertEquals(true, (l.get(0) instanceof Procedure));
		assertEquals(true, (l.get(1) instanceof Procedure));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		
		
		deleteCommand.setDeleteAt(proc);
		deleteCommand.execute();
		l = module.getStatementArray();
		assertEquals(2,l.size());
		assertEquals(true, (l.get(0) instanceof Procedure));
		assertEquals(true, (l.get(1) instanceof Placeholder));
			
		procCommand.setCreateIn(module);
		procCommand.execute();
		
		l = module.getStatementArray();
		assertEquals(true, (l.get(0) instanceof Procedure));
		assertEquals(true, (l.get(1) instanceof Procedure));
		
		Procedure destProc = (Procedure) l.get(1);
		proc = (Procedure) l.get(0);
				
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );
		assertEquals(3, l.size());
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		
		moveCommand.setAppendTo(sit, destProc);
		moveCommand.execute();
		
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );
		assertEquals(3,l.size());
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Placeholder));
	
		deleteCommand.setDeleteAt(sit);
		deleteCommand.execute();
		
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );
		assertEquals(2,l.size());
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Placeholder));			
		
		sitCommand.setCreateIn(destProc);
		sitCommand.execute();
		
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );	
		assertEquals(3,l.size());
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		
		sitCommand.setCreateIn(destProc);
		sitCommand.execute();
		
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );	
		assertEquals(4,l.size());
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Situation));
		assertEquals(true, (l.get(3) instanceof Placeholder));
		
		Situation s1 = (Situation) l.get(1);
		Situation s2 = (Situation) l.get(2);
	
		deleteCommand.setDeleteAt(s2);
		deleteCommand.execute();		
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );	
		assertEquals(3,l.size());
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Placeholder));
		
		copyCommand = new CopyCommand();
		copyCommand.addElement(s1);
		copyCommand.execute();
		pasteCommand.setCreateIn(destProc);
		pasteCommand.execute();
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );	
		assertEquals(4,l.size());
		assertEquals(true, (l.get(0) instanceof Placeholder));
		assertEquals(true, (l.get(1) instanceof Situation));
		assertEquals(true, (l.get(2) instanceof Situation));
		assertEquals(true, (l.get(3) instanceof Placeholder));

		sitCommand.setInsertAt(destProc);
		sitCommand.execute();
		l = destProc.getTextContainer().getStatementArray();
		l.addAll( destProc.getBoxContainer().getStatementArray() );	
		assertEquals(4,l.size());
		
	}
		
	protected void tearDown(){
		module = null;
		
	}
	*/
}