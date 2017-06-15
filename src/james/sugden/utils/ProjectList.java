package james.sugden.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import james.sugden.file_handling.ProjectFileHandler;

public class ProjectList extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private ArrayList<ProjectOpenedListener> projectListeners;
	
	public ProjectList()
	{
		this.projectListeners = new ArrayList<>();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setAlignmentX(LEFT_ALIGNMENT);
	}
	
	public final void addElement(final Project project)
	{
		JPanel pnlProject = new JPanel(new BorderLayout());
		
		final JLabel lblName;
		JLabel lblPath, lblVersion, lblDateCreated;
		
		lblName = new JLabel(project.strName);
		lblPath = new JLabel(project.fleProject.getAbsolutePath());
		lblVersion = new JLabel(project.strEngineVersion);
		lblDateCreated = new JLabel(project.strDateCreated);
		
		lblName.setForeground(Color.GRAY);
		lblName.setFont(new Font("Corbel Bold Italic", Font.PLAIN, 28));
		
		JPanel pnlLeft, pnlRight;
		pnlLeft = new JPanel();
		pnlRight = new JPanel();
		pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
		pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
		
		pnlLeft.add(lblName, BorderLayout.NORTH);
		pnlLeft.add(lblPath, BorderLayout.SOUTH);
		pnlRight.add(lblVersion, BorderLayout.NORTH);
		pnlRight.add(lblDateCreated, BorderLayout.SOUTH);
		
		pnlProject.add(pnlLeft, BorderLayout.WEST);
		pnlProject.add(pnlRight, BorderLayout.EAST);
		
		pnlProject.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		pnlProject.setMaximumSize(new Dimension(800, 75));
		
		pnlProject.addMouseListener(new MouseListener()
				{
					@Override
					public void mouseClicked(MouseEvent e) {}
					@Override
					public void mousePressed(MouseEvent e) {}
					
					@Override
					public void mouseReleased(MouseEvent e)
					{
						if(e.getButton() == MouseEvent.BUTTON3)
							project.edit = false;
						
						for(ProjectOpenedListener projectListener : projectListeners)
						{
							projectListener.projectOpened(project);
						}
					}
					
					@Override
					public void mouseEntered(MouseEvent e)
					{
						lblName.setForeground(new Color(0.4f, 0.72f, 1.0f));
					}
					
					@Override
					public void mouseExited(MouseEvent e)
					{
						lblName.setForeground(Color.GRAY);
					}
				});
		
		this.add(pnlProject);
	}
	
	public final void addElements(ArrayList<Project> projects)
	{
		for(Project project : projects)
		{
			addElement(project);
		}
	}
	
	public final void addNewProjectButton()
	{
		JPanel pnlProject = new JPanel(new BorderLayout());
		
		final JLabel lblNewProject = new JLabel("+ Create New Project");
		lblNewProject.setForeground(Color.GREEN);
		lblNewProject.setFont(new Font("Corbel Bold Italic", Font.PLAIN, 28));
		
		pnlProject.add(lblNewProject, BorderLayout.WEST);
		
		pnlProject.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnlProject.setMaximumSize(new Dimension(800, 50));
		
		pnlProject.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				File dirProjects = new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "JPGames/Projects");
				
				String projectName = JOptionPane.showInputDialog("Enter a Name for the Project");
				
				if(projectName == null)
					return;
				
				//DateFormat df = new SimpleDateFormat("yy.MM.dd");
				//Date dateobj = new Date();
				//String dateCreated = df.format(dateobj);
				String timeStamp = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
				
				File fleNewProject = new File(dirProjects.getAbsolutePath() + File.separator + projectName);
				//if(!fleNewProject.exists())
				//		fleNewProject.mkdirs();
				try
				{
					ProjectFileHandler.createProject(fleNewProject);
				} catch (IOException e1)
				{
					e1.printStackTrace();
					return;
				}
				
				Project newProject = new Project(ProjectFileHandler.version, fleNewProject.getName(), fleNewProject.getPath(), timeStamp);

				for(ProjectOpenedListener projectListener : projectListeners)
				{
					projectListener.projectOpened(newProject);
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e)
			{
				lblNewProject.setForeground(new Color(0.4f, 0.72f, 1.0f));
			}
			
			@Override
			public void mouseExited(MouseEvent e)
			{
				lblNewProject.setForeground(Color.GREEN);
			}
		});
		
		this.add(pnlProject);
	}
	
	public final void addProjectOpenedListener(ProjectOpenedListener listener)
	{
		projectListeners.add(listener);
	}
	
	public abstract class ProjectOpenedListener
	{
		public abstract void projectOpened(Project project);
	}
	
	public class Project
	{
		public String strEngineVersion;
		public String strName;
		public String strDateCreated;
		public File fleProject;
		public boolean edit;
		
		public Project(String ver, String name, String path, String dateCreated)
		{
			strEngineVersion = ver;
			strName = name;
			strDateCreated = dateCreated;
			fleProject = new File(path);
			edit = true;
		}
	}
}








