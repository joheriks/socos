package tests.model;

import ibpe.IBPEditor;
import ibpe.model.Module;
import ibpe.model.Partitioner;
import ibpe.model.Procedure;
import ibpe.model.TextElement;
import ibpe.model.TextRow;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.gef.EditPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestModel extends TestCase {  
	private Module module;

	
	private TextRow leaf;
	@Before
	protected void setUp(){
		
		EditPart test = IBPEditor.moduleEditPart;

		module = new Module("testModule");
		leaf = new TextRow();
		Procedure procedure = new Procedure(module);
		module.add(procedure);
		procedure.setName("testProcedure");		
		procedure.setArguments("VALRES x:int");
		
	}

	@Test
	public void testStuff(){
		assertEquals("testModule", module.getName());
		Procedure proc = (Procedure) module.getStatementArray().get(0);
		assertEquals("testProcedure", proc.getName());
		assertEquals("VALRES x:int", proc.getArguments());		
	}

	@After
	public void cleanTest(){
		
	}
	
	@Ignore
	public void Partitioner(){
	
		String newText = "IF THEN ELSE %mumin"; 
		leaf.setName(newText);
		assertEquals(newText, leaf.getName());
		ArrayList<TextElement> list = new ArrayList<TextElement>();
		Partitioner.searchForNonWhiteSpace(newText, list);


		for(int i = 0; i < list.size();i++)

			System.out.println(list.get(i).getText());
		assertEquals(list.size(), 7);
	}
	
}

