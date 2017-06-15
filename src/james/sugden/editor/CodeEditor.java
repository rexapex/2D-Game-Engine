package james.sugden.editor;

import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.scripts.Script;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;

public class CodeEditor	//Static methods used to create a code editor with text highlighting
{
	private static String[] keywords =
		{
		    "assert",
		    "abstract", "break",
		    "case", "catch", "class",
		    "const", "continue", "default", "do",
		    "else", "extends", "final",
		    "finally","for", "goto",
		    "if", "implements", "import",
		    "instanceof", "interface",
		    "native", "new", "package",
		    "private", "protected", "public",
		    "return", "static", "super",
		    "switch", "synchronized", "this",
		    "throw", "throws", "transient",
		    "try", "void", "volatile", "while"
		};
	
	private static String[] primitives =
		{
		   "boolean", "byte", "char", "double", "float", "long", "short", "int"
		};
	
	public static void createCodeEditor(GameObject gameObject, JTabbedPane pnlCodeEditor)
	{
		if(gameObject != null)
		{
			pnlCodeEditor.removeAll();
			
			//JToolBar toolbar = new JToolBar();
			//toolbar.setFloatable(false);
			//JButton btnSave = new JButton("Save");
			//toolbar.add(btnSave);
			
			for(Component component : gameObject.getComponents())
			{
				if(component instanceof Script)
				{
					File fle = ((Script)component).getScriptFile();
					
					JPanel pnlScript = new JPanel(new BorderLayout());
					JScrollPane sclScript = new JScrollPane(pnlScript);
					sclScript.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					sclScript.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					
					JTextPane txtCode = new JTextPane();
					txtCode.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
					try {
						if(fle != null && fle.exists())
						{
							try {
								if(fle != null && fle.exists())
									loadScript(txtCode, fle);
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
						}
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					
					Document doc = txtCode.getStyledDocument();
					
					txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
					
					txtCode.addFocusListener(new FocusListener()
					{
						public void focusGained(FocusEvent e) {}
						
						public void focusLost(FocusEvent e)	//Save the script automatically if the user doesn't press the save button
						{
							try {
								saveScript(txtCode, fle);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					});
					
					txtCode.addKeyListener(new KeyListener()
					{
						private StringBuilder word = new StringBuilder();
						private boolean lineAnnotation = false;
						private  boolean string = false;
						
						public void keyTyped(KeyEvent e)
						{
							try {
								onCharType(txtCode, word, e.getKeyChar(), lineAnnotation, string, doc);
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
						}
						
						public void keyPressed(KeyEvent e) {}
						public void keyReleased(KeyEvent e) {}
					});
					
					/*btnSave.addActionListener(e ->
					{
						try {
							saveScript(txtCode, fle);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					});*/
					
					//pnlScript.add(toolbar, BorderLayout.NORTH);
					pnlScript.add(txtCode, BorderLayout.CENTER);
					
					pnlCodeEditor.addTab(component.getIdentificationName(), sclScript);
				}
			}
		}
	}
	
	
	
	
	
	

	
	
	
	
	
	
	
	private static void loadScript(JTextPane txtCode, File fleScript) throws IOException, BadLocationException	//Load a script into a text pane
	{
		JTextPane txtRead = new JTextPane();
		BufferedReader rdrCode = new BufferedReader(new FileReader(fleScript));
		txtRead.read(rdrCode, "file.txt");	//Read the script into a buffer text pane
		
	    Document doc = txtCode.getStyledDocument();
	    
	    txtCode.setFont(new Font("Consolas", Font.PLAIN, 12));
		
	    boolean lineAnnotation = false;
	    boolean string = false;
	    boolean variable = false;
	    
	    ArrayList<String> variables = new ArrayList<>();	//A list of the names of variables so they can be coloured
	    
	    txtRead.getText().replaceAll("\r", "");
	    
	    String word = "";
	    for(char c : txtRead.getDocument().getText(0, txtRead.getDocument().getLength()).toCharArray())	//Split text into individual characters
	    {
	    	if(Character.isJavaIdentifierPart(c))	//Add the character to the word if it is a java identifier or if it is in quotes
	    	{
	    		word += c;
	    	} else if(c == '\"' || c == '\'')	//Adds the character in the string font if it is contained in quotes
	    	{
	    		word += c;
	    		if(!string)
	    		{
	    			string = true;
	    			variable = false;
	    		}
	    		else
	    		{
		    		doc.insertString(doc.getLength(), word, WindowPreferences.getSetString());
		    		txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
		    		string = false;
		    		word = "";
	    		}
	    	} else if(c == '/')	//Determine whether the user is entering an annotation or a piece of code
	    	{
	    		word += c;
	    		if(word.equals("//"))
	    		{
	    			lineAnnotation = true;
	    			string = false;
	    			variable = false;
	    		}
	    	} else if(string || lineAnnotation)	//Add every character to the word if inside quotes or an annotation
	    	{
	    		word += c;
	    		if(lineAnnotation && c == '\n')	//If the user enters a new line set line annotation to false and add the annotation
	    		{
	    			doc.insertString(doc.getLength(), word, WindowPreferences.getSetAnnotation());
	    			txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
	    			lineAnnotation = false;
	    			variable = false;
	    			word = "";
	    		}
	    	} else	//Otherwise end the word and test if it needs to be highlighted
	    	{
	    		if(Arrays.asList(primitives).contains(word))
	    		{
	        		doc.insertString(doc.getLength(), word, WindowPreferences.getSetKeyword());
	        		txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
	        		variable = true;
	    		} else if(Arrays.asList(keywords).contains(word))
	    		{
	        		doc.insertString(doc.getLength(), word, WindowPreferences.getSetKeyword());
	        		txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
	        		variable = false;
	    		} else if(c == '(')	//Highlighting methods
	    		{
	    			doc.insertString(doc.getLength(), word, WindowPreferences.getSetMethod());
	    			txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
	    		}
	    		else
	    		{
	    			if(variable)
	    			{
	    				doc.insertString(doc.getLength(), word, WindowPreferences.getSetVariable());
	    				txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
	    				variables.add(word);
	    			}
	    			else
	    			{
	    				if(variables.contains(word))	//If the word is a variable which has already been defined
	    				{
	    					doc.insertString(doc.getLength(), word, WindowPreferences.getSetVariable());
	    					txtCode.setCharacterAttributes(WindowPreferences.getSetDefault(), true);
	    				}
	    				else
	    					doc.insertString(doc.getLength(), word, WindowPreferences.getSetDefault());
	    			}
	    		}
	    		doc.insertString(doc.getLength(), Character.toString(c), WindowPreferences.getSetDefault());
	    		word = "";
	    	}
	    	
	    	if(c == ';')	//End of the line so no more variables can be defined
	    	{
	    		variable = false;
	    	} 
	    }
		
		rdrCode.close();
	}
	
	
	
	
	
	
	private static void saveScript(JTextPane txtCode, File fleScript) throws IOException, BadLocationException
	{
		BufferedWriter wrtCode = new BufferedWriter(new FileWriter(fleScript));
		
		wrtCode.write(txtCode.getDocument().getText(0, txtCode.getDocument().getLength()));
		
		wrtCode.close();
	}
	
	
	
	
	
	
	private static void onCharType(JTextPane txtCode, StringBuilder word, char c, boolean lineAnnotation, boolean string, Document doc) throws BadLocationException
	{
		/*if(Character.isJavaIdentifierPart(c))	//Add the character to the word if it is a java identifier or if it is in quotes
		{
			word.append(c);
		} else if(c == '\"' || c == '\'')	//Adds the character in the string font if it is contained in quotes
		{
			word.append(c);
			if(!string)
				string = true;
			else
			{
				doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
				doc.insertString(txtCode.getCaretPosition(), word.toString(), HouseStyle.getSetString());
	    		string = false;
	    		word.delete(0, word.length());
			}
		} else if(c == '/')
		{
			word.append(c);
			if(word.equals("//"))
			{
				lineAnnotation = true;
				string = false;
				System.out.println("Annotaiton found " + word);
			}
		} else if(string || lineAnnotation)	//Add every character to the word if inside quotes or an annotation
		{
			word.append(c);
			if(lineAnnotation && c == '\n')	//If the user enters a new line set line annotation to false and add the annotation
			{
				doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
				doc.insertString(txtCode.getCaretPosition(), word.toString(), HouseStyle.getSetAnnotation());
				lineAnnotation = false;
	    		word.delete(0, word.length());
			}
		} else	//Otherwise end the word and test if it needs to be highlighted
		{
			System.out.println(word);
			if(Arrays.asList(keywords).contains(String.valueOf(word)))
			{
				doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
				doc.insertString(txtCode.getCaretPosition(), String.valueOf(word), HouseStyle.getSetKeyword());
				txtCode.setCharacterAttributes(HouseStyle.getSetDefault(), true);
			} else if(Arrays.asList(primitives).contains(String.valueOf(word)))
    		{
				doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
        		doc.insertString(txtCode.getCaretPosition(), String.valueOf(word), HouseStyle.getSetKeyword());
        		txtCode.setCharacterAttributes(HouseStyle.getSetDefault(), true);
    		} else
			{
				doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
				doc.insertString(txtCode.getCaretPosition(), String.valueOf(word), HouseStyle.getSetDefault());
			}
			//doc.insertString(txtCode.getCaretPosition(), Character.toString(c), HouseStyle.getSetDefault());
			word.delete(0, word.length());
		}*/
	}
}




/*	private static void onCharType(JTextPane txtCode, StringBuilder word, char c, boolean lineAnnotation, boolean string, Document doc) throws BadLocationException
{
	if(Character.isJavaIdentifierPart(c))	//Add the character to the word if it is a java identifier or if it is in quotes
	{
		word.append(c);
	} else if(c == '\"' || c == '\'')	//Adds the character in the string font if it is contained in quotes
	{
		word.append(c);
		if(!string)
			string = true;
		else
		{
			doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
			doc.insertString(txtCode.getCaretPosition(), word.toString(), HouseStyle.getSetString());
    		string = false;
    		word.delete(0, word.length());
		}
	} else if(c == '/')
	{
		word.append(c);
		if(word.equals("//"))
		{
			lineAnnotation = true;
			string = false;
			System.out.println("Annotaiton found " + word);
		}
	} else if(string || lineAnnotation)	//Add every character to the word if inside quotes or an annotation
	{
		word.append(c);
		if(lineAnnotation && c == '\n')	//If the user enters a new line set line annotation to false and add the annotation
		{
			doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
			doc.insertString(txtCode.getCaretPosition(), word.toString(), HouseStyle.getSetAnnotation());
			lineAnnotation = false;
    		word.delete(0, word.length());
		}
	} else	//Otherwise end the word and test if it needs to be highlighted
	{
		if(Arrays.asList(keywords).contains(word))
		{
			doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
			doc.insertString(txtCode.getCaretPosition(), word.toString(), HouseStyle.getSetKeyword());
		} else
		{
			doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
			doc.insertString(txtCode.getCaretPosition(), word.toString(), HouseStyle.getSetDefault());
		}
		doc.insertString(doc.getLength(), Character.toString(c), HouseStyle.getSetDefault());
		word.delete(0, word.length());
	}
}*/








//Old Coe
/*if(gameObject != null)
{
	pnlCodeEditor.removeAll();
	
	JToolBar toolbar = new JToolBar();
	toolbar.setFloatable(false);
	JButton btnSave = new JButton("Save");
	toolbar.add(btnSave);
	
	for(Component component : gameObject.getComponents())
	{
		if(component instanceof Script)
		{
			String[] keywords =
				{
				    "assert",
				    "abstract", "boolean", "break", "byte",
				    "case", "catch", "char", "class",
				    "const", "continue", "default", "do",
				    "double", "else", "extends", "final",
				    "finally", "float", "for", "goto",
				    "if", "implements", "import",
				    "instanceof", "int", "interface",
				    "long", "native", "new", "package",
				    "private", "protected", "public",
				    "return", "short", "static", "super",
				    "switch", "synchronized", "this",
				    "throw", "throws", "transient",
				    "try", "void", "volatile", "while"
				};
			
			File fle = ((Script)component).getScriptFile();
			
			JPanel pnlScript = new JPanel(new BorderLayout());
			JScrollPane sclScript = new JScrollPane(pnlScript);
			sclScript.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sclScript.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			
			JTextPane txtCode = new JTextPane();
		//	JTextPane txtLoad = new JTextPane();	//Load to this unstyled txtpane
			try {
				if(fle != null && fle.exists())
				{
					BufferedReader rdrCode = new BufferedReader(new FileReader(fle));
					txtCode.read(rdrCode, "file.txt");
					rdrCode.close();
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			//Tab Size
			/*TabStop[] tabs = new TabStop[4];
		    	tabs[0] = new TabStop(5, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE);
		        tabs[1] = new TabStop(5, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
		        tabs[2] = new TabStop(5, TabStop.ALIGN_CENTER, TabStop.LEAD_NONE);
		        tabs[3] = new TabStop(5, TabStop.ALIGN_DECIMAL, TabStop.LEAD_NONE);
		        TabSet tabset = new TabSet(tabs);

		        StyleContext sc = StyleContext.getDefaultStyleContext();
		        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
		        StyleConstants.TabSet, tabset);
		        txtCode.setParagraphAttributes(aset, false);
		        txtLoad.setParagraphAttributes(aset, false);
	        
			SimpleAttributeSet setKeyword = HouseStyle.getSetKeyword();
			//SimpleAttributeSet setAnnotation = HouseStyle.getSetAnnotation();
			SimpleAttributeSet setDefault = HouseStyle.getSetDefault();
		    
		    Document doc = txtCode.getStyledDocument();
		    
		  /*  if(fle != null)	//Load the script file and colour the words accordingly
			{
				try {
					BufferedReader bufferedReader = new BufferedReader(new FileReader(fle));
					txtLoad.read(bufferedReader, "file.java");
					String[] lines = txtLoad.getText().split(System.lineSeparator());
					for(String line : lines)
					{
						//boolean annotation = false;
						String[] words = line.split(" ");
						for(String word : words)
						{
							boolean isDefault = true;
							for(String keyword : keywords)
							{
								/*if(word.trim().startsWith("//"))
								{
									annotation = true;
								}
								if(annotation)
								{
									try {
										doc.insertString(doc.getLength(), word + " ", setAnnotation);
										isDefault = false;
										break;
									} catch (BadLocationException e1) {
										e1.printStackTrace();
									}
								}
								if(word.trim().equals(keyword))
								{
									try {
										doc.insertString(doc.getLength(), word + " ", setKeyword);
										isDefault = false;
										break;
									} catch (BadLocationException e1) {
										e1.printStackTrace();
									}
								}
							}
							if(isDefault)
							{
								try {
									doc.insertString(doc.getLength(), word + " ", setDefault);
								} catch (BadLocationException e1) {
									e1.printStackTrace();
								}
							}
						}
						try {
							doc.remove(doc.getLength()-1, 1);
							doc.insertString(doc.getLength(), System.lineSeparator(), setDefault);
							//annotation = false;
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			txtCode.setCharacterAttributes(setDefault, true);
			
			txtCode.addKeyListener(new KeyListener()
			{
				private String word = "";
				//private boolean annotation;
				//private List<String> variables = new ArrayList<>();
				
				public void keyTyped(KeyEvent e)
				{
					char c = e.getKeyChar();
					/*if(annotation)				//Annotation highlighting
					{
						try {
							doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
							doc.insertString(txtCode.getCaretPosition(), word, setAnnotation);
						} catch (BadLocationException e1) {
							e1.printStackTrace();
						}
						if(c == '\r' || c == '\n')
						{
							annotation = false;
						}
					}
					else if(c == '/')
					{
						String origWord = word;
						checkNewWord(c);
						if(origWord == "//")
							annotation = true;
						else if(origWord == "")
							word = "/";
						else if(origWord.substring(origWord.length()-1) == "/")
							word = "//";
						else
							word = "/";
					}
					if(Character.isJavaIdentifierPart(c))
						word += c;
					else if(c == ' ' || c == '.' || c == '{' || c == '}' || c == '(' || c == ')' || c == '[' || c == ']' || c == '\t')
						checkNewWord(c);
					else if(c == '\r' || c == '\n')
					{
						//annotation = false;
						checkNewWord(c);
					}
					else if(c == '\b')
						word = word.substring(0, word.length()-2);
				}
				
				public void keyPressed(KeyEvent e) {}
				public void keyReleased(KeyEvent e) {}
				
				private void checkNewWord(char c)
				{
					for(String keyword : keywords)
					{
						if(keyword.equals(word))
						{
							try {
								if(c == ' ')
								{
									doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
									doc.insertString(txtCode.getCaretPosition(), word, setKeyword);
								} else if(c == '\r' || c == '\n')
								{
									doc.remove(txtCode.getCaretPosition()-word.length()-1, word.length()+1);
									doc.insertString(txtCode.getCaretPosition(), word + "\r\n", setKeyword);
								} else if(c == '.' || c == '{' || c == '}' || c == '(' || c == ')' || c == '[' || c == ']' || c == '/')
								{
									doc.remove(txtCode.getCaretPosition()-word.length(), word.length());
									doc.insertString(txtCode.getCaretPosition(), word, setKeyword);
								} else if(c == '\t')
								{
									doc.remove(txtCode.getCaretPosition()-word.length()-1, word.length()+1);
									doc.insertString(txtCode.getCaretPosition(), word, setKeyword);
								}
								txtCode.setCharacterAttributes(setDefault, true);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							break;
						}
					}
					
					word = "";
				}
			});
			
			btnSave.addActionListener(e ->
			{
				try {
					if(fle != null && fle.exists())
						txtCode.write(new BufferedWriter(new FileWriter(fle)));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
			
			pnlScript.add(toolbar, BorderLayout.NORTH);
			pnlScript.add(txtCode, BorderLayout.CENTER);
			
			pnlCodeEditor.addTab(component.getIdentificationName(), sclScript);
		}
	}
}*/
