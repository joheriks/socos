package ibpe.io;

import org.eclipse.swt.dnd.*;

public class Clipboard {
	
	private static Clipboard clipboard = new Clipboard();
	
	public static Clipboard getClipboard() {
		return clipboard;
	}
	
	public void setContentsFromFragment( Fragment<?> f )
	{
		org.eclipse.swt.dnd.Clipboard board = new org.eclipse.swt.dnd.Clipboard(null);
		
		StringBuffer sb = new StringBuffer();
		Serializer s = new Serializer(sb);
		s.serialize(f);
		
		board.setContents(new Object[]{sb.toString()}, new Transfer[]{TextTransfer.getInstance()});

		board.dispose(); // Required after every operation on the clipboard is done.
	}
	
	public String getContents()
	{				
		org.eclipse.swt.dnd.Clipboard board = new org.eclipse.swt.dnd.Clipboard(null);
	    String textData = (String)board.getContents(TextTransfer.getInstance());
		board.dispose();
		return textData;
	}
	

	public boolean hasPossibleContents()
	{
		org.eclipse.swt.dnd.Clipboard board = new org.eclipse.swt.dnd.Clipboard(null);
		TransferData[] tds = board.getAvailableTypes();
		
		boolean retval = false;
		for (TransferData td : tds)	retval |= TextTransfer.getInstance().isSupportedType(td);
		board.dispose();
		return retval;
	}
}
