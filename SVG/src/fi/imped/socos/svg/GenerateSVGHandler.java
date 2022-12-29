package fi.imped.socos.svg;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ibpe.IBPEditor;
import ibpe.part.*;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.export.GraphicsSVG;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.w3c.dom.Element;


public class GenerateSVGHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}
	
	
	protected IBPEditor getActiveIBPEditor()
	{
		IEditorPart ep = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		return (ep instanceof IBPEditor) ? (IBPEditor)ep : null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IBPEditor editor = getActiveIBPEditor();
		List<AbstractIBPEditPart<?> > parts = getSelectedEditParts();
		if (parts.size()<1)
			parts.add((ContextPart)editor.getGraphicalViewer().getFocusEditPart());

		IEditorInput i = editor.getEditorInput();
		String fname = null;
		String dirname = null;
		if (i instanceof FileStoreEditorInput && URIUtil.isFileURI(((FileStoreEditorInput)i).getURI()))
		{
			fname = ((FileStoreEditorInput)i).getName();
			((FileStoreEditorInput)i).getURI().getPath();
			File f = URIUtil.toFile(((FileStoreEditorInput)i).getURI());
			dirname = f.getParentFile().toString();
		}
		else if (i instanceof FileEditorInput)
		{
			IFile f = ((FileEditorInput) i).getFile();
			dirname = f.getParent().getLocation().addTrailingSeparator().toString();
			fname = f.getName();
		}
		else
		{
			dirname = "/tmp";
			fname = editor.getContext().getText() + ".svg";
		}
		
		if (fname==null)
			return null;

		fname = fname.replaceAll("\\.ibp\\z",".svg");
		if (!fname.endsWith(".svg"))
			fname = fname + ".svg";

		FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),SWT.SAVE);
	    dialog.setFilterNames(new String[] { "SVG Files", "All Files (*.*)" });
	    dialog.setFilterExtensions(new String[] { "*.svg", "*.*" });
	    dialog.setFilterPath(dirname);
	    dialog.setFileName(fname);
	    fname = dialog.open();
		if (fname!=null)
			generateSVG(parts,fname);
		return null;
	}
	
	
	protected List<AbstractIBPEditPart<?> > getSelectedEditParts()
	{
		ArrayList<AbstractIBPEditPart<?> > parts = new ArrayList<AbstractIBPEditPart<?> >();
		IBPEditor editor = getActiveIBPEditor();
		if (editor!=null)
			for (Object p : editor.getGraphicalViewer().getSelectedEditParts())
				if (p instanceof AbstractIBPEditPart<?>)
					parts.add((AbstractIBPEditPart<?>)p);
		return parts;
	}
	
	
	protected void generateSVG( List<AbstractIBPEditPart<?> > parts, String filename )
	{
		Rectangle viewBox = parts.get(0).getFigure().getBounds();
		for (AbstractIBPEditPart<?> p : parts)
		{
			System.out.println("Part: "+p.getFigure().getBounds());
			viewBox = viewBox.union(p.getFigure().getBounds());
		}
		
		//viewBox.translate(-viewBox.x(),-viewBox.y());
		
		System.out.println("Box: "+viewBox);
		
		GraphicsSVG graphics = GraphicsSVG.getInstance(viewBox);
		OutputStream out = null;
		
		try {
			out = new FileOutputStream(filename);
				
			for (AbstractIBPEditPart<?> p : parts)
			{
				viewBox = viewBox.union(p.getFigure().getBounds());
				p.getFigure().paint(graphics);
			}
			//Rectangle viewBox = root.getBounds().getCopy();

			// paint figure
		    //root.paint(graphics);
		 
		    Element svgRoot = graphics.getRoot();
		 
		    // Define the view box
		    svgRoot.setAttributeNS(null,
		      "viewBox", String.valueOf(viewBox.x) + " " +
		      String.valueOf(viewBox.y) + " " +
		      String.valueOf(viewBox.width) + " " +
		      String.valueOf(viewBox.height));
		 
		    // Write the document to the stream
		    Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		 
		    DOMSource source = new DOMSource(svgRoot);
		    StreamResult result = new StreamResult(out);
		    transformer.transform(source, result);
		  } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    graphics.dispose();
		    try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }

	}

}
