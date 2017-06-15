package james.sugden.engine.input.form;

public class EEventObject	/**Allows game objects to send events to the editor without knowing about the editor*/
{
	private EEventListener lst;
	
	public void setListener(EEventListener lst)
	{
		this.lst = lst;
	}
	
	/**Sends the event listener an event*/
	public void fireEvent(EEventType event)
	{
		if(lst != null)
			lst.fireEvent(event);
	}
	
	public static enum EEventType
	{
		OPEN_ANIMATION_EDITOR, OPEN_CODE_EDITOR, OPEN_DOCUMENTATION;
		
		public String data = null;
		
		public void packData(String data)
		{
			this.data = data;
		}
	}
}
