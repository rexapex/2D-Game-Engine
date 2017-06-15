package james.sugden.engine.input.form;

import james.sugden.engine.input.form.EEventObject.EEventType;

public interface EEventListener
{
	public void fireEvent(EEventType event);
}
