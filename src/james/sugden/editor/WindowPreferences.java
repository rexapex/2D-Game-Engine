package james.sugden.editor;

import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class WindowPreferences
{
	/**Every component will use these background and foreground colours*/
	private static Color clrBackground, clrForeground;
	
	/**Attribute sets for the code editor*/
	private static SimpleAttributeSet setDefault, setKeyword, setAnnotation, setVariable, setMethod, setString;
	
	public final static void init(Color clrBackground, Color clrForeground, SimpleAttributeSet setDefault, SimpleAttributeSet setKeyword,
			SimpleAttributeSet setAnnotation, SimpleAttributeSet setVariable, SimpleAttributeSet setMethod, SimpleAttributeSet setString)
	{
		WindowPreferences.clrBackground = clrBackground;
		WindowPreferences.clrForeground = clrForeground;
		WindowPreferences.setDefault = setDefault;
		WindowPreferences.setKeyword = setKeyword;
		WindowPreferences.setAnnotation = setAnnotation;
		WindowPreferences.setVariable = setVariable;
		WindowPreferences.setMethod = setMethod;
		WindowPreferences.setString = setString;
	}
	
	/**Sets the house style to the default house style*/
	public static void initToDefault()
	{
		WindowPreferences.clrBackground = Color.WHITE;
		WindowPreferences.clrForeground = Color.BLACK;
		
		setKeyword = new SimpleAttributeSet();
	    StyleConstants.setBold(setKeyword, true);
	    StyleConstants.setForeground(setKeyword, Color.magenta);
	    
	    setVariable = new SimpleAttributeSet();
	    StyleConstants.setForeground(setVariable, Color.blue);
	    
	    setMethod = new SimpleAttributeSet();
	    StyleConstants.setForeground(setMethod, new Color(139, 90, 43));
	    
	    setAnnotation = new SimpleAttributeSet();
	    StyleConstants.setForeground(setAnnotation, new Color(34, 139, 34));
		
	    setString = new SimpleAttributeSet();
	    StyleConstants.setForeground(setString, Color.red);
	    
		setDefault = new SimpleAttributeSet();
	    StyleConstants.setForeground(setDefault, Color.black);
	}

	/**
	 * @return the clrBackground
	 */
	public static final Color getClrBackground() {
		return clrBackground;
	}

	/**
	 * @return the clrForeground
	 */
	public static final Color getClrForeground() {
		return clrForeground;
	}

	/**
	 * @return the setDefault
	 */
	public static final SimpleAttributeSet getSetDefault() {
		return setDefault;
	}

	/**
	 * @return the setKeyword
	 */
	public static final SimpleAttributeSet getSetKeyword() {
		return setKeyword;
	}

	/**
	 * @return the setAnnotation
	 */
	public static final SimpleAttributeSet getSetAnnotation() {
		return setAnnotation;
	}

	/**
	 * @return the setVariable
	 */
	public static final SimpleAttributeSet getSetVariable() {
		return setVariable;
	}

	/**
	 * @return the setMethod
	 */
	public static final SimpleAttributeSet getSetMethod() {
		return setMethod;
	}
	
	public static final SimpleAttributeSet getSetString() {
		return setString;
	}
}
