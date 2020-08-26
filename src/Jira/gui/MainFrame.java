package Jira.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import Jira.AureaWorklog;
import Jira.JiraApiHelper;
import Jira.JiraParser;
import Jira.Task;
import Jira.Worklog;
import Jira.scripting.Script;
import Jira.scripting.ScriptStep;
import Jira.utils.CalendarUtils;
import Jira.utils.GuiUtils;
import Jira.utils.StringUtils;

public class MainFrame {
	private String[] users;
	private String[] projects;
	private String[] epics;
	private JFrame frame;
	private JLabel searchStringDisplay;
	private SettingsDialog settingsDialog;
	private JTable worklogTable;
	private JTable taskTable;
	private JTable aureaTable;
	private ArrayList<Worklog> worklogList = new ArrayList<Worklog>();
	private ArrayList<AureaWorklog> aureaList = new ArrayList<AureaWorklog>();
	private ArrayList<Task> taskList = new ArrayList<Task>();
	private Thread searchThread;
	private searchMode currentSearchMode = searchMode.Worklog;
	private JPanel contentPanel;
	private JDialog debugDialog;
	private JTextArea debugText;
	
	private enum searchMode{
		Worklog, Task, Aurea;
	}

	private void initFrame() {
		frame = new JFrame("Jira Auswertung");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initValues();
		initMenuBar();
		initContent();
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * gets the values for ComboBoxes (users and projects) from the REST API
	 */
	private void initValues() {
		
		users = JiraApiHelper.getInstance().queryUsers();

		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/project/search");
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		projects = JiraParser.parseProjects(json);

		epics = JiraApiHelper.getInstance().queryEpics(null);
	}

	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu data = new JMenu("Datei");
		menuBar.add(data);
		JMenuItem settings = new JMenuItem("Einstellungen");
		settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openSettingsDialog();
			}
		});
		data.add(settings);
		JMenuItem search = new JMenuItem("Suche starten");
		search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startSearch();
			}
		});
		data.add(search);
		JMenuItem save = new JMenuItem("CSV speichern");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportToFile();
			}
		});
		data.add(save);
		JMenuItem exit = new JMenuItem("Beenden");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		data.add(exit);
		JMenu scripting = new JMenu("Automatisierung");
		menuBar.add(scripting);
		JMenuItem newScript = new JMenuItem("Neu erstellen");
		newScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openScriptDialog(true);
			}
		});
		scripting.add(newScript);
		JMenuItem editScript = new JMenuItem("ändern");
		editScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openScriptDialog(false);
			}
		});
		scripting.add(editScript);
		JMenuItem executeScript = new JMenuItem("Ausführen");
		executeScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openExecuteScriptDialog();
			}
		});
		scripting.add(executeScript);
		JMenuItem enableDebugging = new JMenuItem("Debug Modus");
		enableDebugging.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDebugging();
			}
		});
		scripting.add(enableDebugging);
	}

	private void initContent() {
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		JPanel top = new JPanel(new GridLayout(0, 1));
		panel.add(top, BorderLayout.NORTH);
		top.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		top.add(new JLabel("Suchbefehl:"));
		searchStringDisplay = new JLabel("timespent > 0");
		top.add(searchStringDisplay);
		contentPanel = new JPanel(new CardLayout());
		JPanel worklogPanel = initWorklogPanel();
		contentPanel.add(worklogPanel, "Worklog");
		JPanel taskPanel = initTaskPanel();
		contentPanel.add(taskPanel,"Task");
		JPanel aureaPanel = initAureaPanel();
		contentPanel.add(aureaPanel,"Aurea");
		panel.add(contentPanel, BorderLayout.CENTER);
	}
	@SuppressWarnings("serial")
	private JPanel initWorklogPanel(){
		JPanel worklogPanel = new JPanel();
		worklogTable = new JTable(new WorklogTableModel()) {

			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);

				try {
					tip = getValueAt(rowIndex, colIndex).toString();
				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty line
				}

				return tip;
			}
		};
		worklogTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		worklogTable.getColumnModel().getColumn(1).setPreferredWidth(125);
		worklogTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		worklogTable.getColumnModel().getColumn(3).setPreferredWidth(75);
		worklogTable.getColumnModel().getColumn(4).setPreferredWidth(75);
		worklogTable.getColumnModel().getColumn(5).setPreferredWidth(50);
		worklogTable.getColumnModel().getColumn(6).setPreferredWidth(50);
		worklogTable.getColumnModel().getColumn(7).setPreferredWidth(30);
		worklogTable.getColumnModel().getColumn(8).setPreferredWidth(200);
		worklogTable.getColumnModel().getColumn(9).setPreferredWidth(200);
		worklogTable.getColumnModel().getColumn(10).setPreferredWidth(75);
		worklogTable.getColumnModel().getColumn(11).setPreferredWidth(75);
		worklogTable.getColumnModel().getColumn(12).setPreferredWidth(75);
		worklogTable.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(worklogTable);
		scrollPane.setPreferredSize(new Dimension(1180, 600));
		scrollPane.setMinimumSize(new Dimension(1180, 600));
		
		//Initializing the row sorter
		TableRowSorter<WorklogTableModel> sorter = new TableRowSorter<WorklogTableModel>((WorklogTableModel)worklogTable.getModel());
		worklogTable.setRowSorter(sorter);
		sorter.setComparator(1, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				//Cut the date String into pieces and reassamble for correct default sorting
				StringBuffer s1 = new StringBuffer(o1.substring(6,10));
				s1.append(o1.substring(3, 5)).append(o1.substring(0,2)).append(o1.substring(10));
				StringBuffer s2 = new StringBuffer(o2.substring(6,10));
				s2.append(o2.substring(3, 5)).append(o2.substring(0,2)).append(o2.substring(10));
				return s1.toString().compareTo(s2.toString());
			}
		});
		worklogPanel.add(scrollPane);
		return worklogPanel;
	}
	
	@SuppressWarnings("serial")
	private JPanel initTaskPanel(){
		JPanel taskPanel = new JPanel();
		taskTable = new JTable(new TaskTableModel()) {

			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);

				try {
					tip = getValueAt(rowIndex, colIndex).toString();
				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty line
				}

				return tip;
			}
		};
		taskTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		taskTable.getColumnModel().getColumn(1).setPreferredWidth(125);
		taskTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		taskTable.getColumnModel().getColumn(3).setPreferredWidth(75);
		taskTable.getColumnModel().getColumn(4).setPreferredWidth(75);
		taskTable.getColumnModel().getColumn(5).setPreferredWidth(50);
		taskTable.getColumnModel().getColumn(6).setPreferredWidth(50);
		taskTable.getColumnModel().getColumn(7).setPreferredWidth(200);
		taskTable.getColumnModel().getColumn(8).setPreferredWidth(150);
		taskTable.getColumnModel().getColumn(9).setPreferredWidth(75);
		taskTable.getColumnModel().getColumn(10).setPreferredWidth(75);
		taskTable.getColumnModel().getColumn(11).setPreferredWidth(75);
		taskTable.getColumnModel().getColumn(12).setPreferredWidth(75);
		taskTable.getColumnModel().getColumn(13).setPreferredWidth(75);
		taskTable.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(taskTable);
		scrollPane.setPreferredSize(new Dimension(1150, 600));
		scrollPane.setMinimumSize(new Dimension(1150, 600));
		
		//Initializing the row sorter
		TableRowSorter<TaskTableModel> sorter = new TableRowSorter<TaskTableModel>((TaskTableModel)taskTable.getModel());
		taskTable.setRowSorter(sorter);
		Comparator<String> dateComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				//Cut the date String into pieces and reassamble for correct default sorting
				StringBuffer s1 = new StringBuffer(o1.substring(6,10));
				s1.append(o1.substring(3, 5)).append(o1.substring(0,2)).append(o1.substring(10));
				StringBuffer s2 = new StringBuffer(o2.substring(6,10));
				s2.append(o2.substring(3, 5)).append(o2.substring(0,2)).append(o2.substring(10));
				return s1.toString().compareTo(s2.toString());
			}
		};
		sorter.setComparator(5, dateComparator);
		sorter.setComparator(6, dateComparator);
		taskPanel.add(scrollPane);
		return taskPanel;
	}
	@SuppressWarnings("serial")
	private JPanel initAureaPanel(){
		JPanel aureaPanel = new JPanel();
		aureaTable = new JTable(new AureaTableModel()) {

			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);

				try {
					tip = getValueAt(rowIndex, colIndex).toString();
				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty line
				}

				return tip;
			}
		};
		aureaTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		aureaTable.getColumnModel().getColumn(1).setPreferredWidth(125);
		aureaTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		aureaTable.getColumnModel().getColumn(3).setPreferredWidth(75);
		aureaTable.getColumnModel().getColumn(4).setPreferredWidth(75);
		aureaTable.getColumnModel().getColumn(5).setPreferredWidth(50);
		aureaTable.getColumnModel().getColumn(6).setPreferredWidth(50);
		aureaTable.getColumnModel().getColumn(7).setPreferredWidth(30);
		aureaTable.getColumnModel().getColumn(8).setPreferredWidth(200);
		aureaTable.getColumnModel().getColumn(9).setPreferredWidth(200);
		aureaTable.getColumnModel().getColumn(10).setPreferredWidth(75);
		aureaTable.getColumnModel().getColumn(11).setPreferredWidth(75);
		aureaTable.getColumnModel().getColumn(12).setPreferredWidth(75);
		aureaTable.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(aureaTable);
		scrollPane.setPreferredSize(new Dimension(1180, 600));
		scrollPane.setMinimumSize(new Dimension(1180, 600));
		
		//Initializing the row sorter
		TableRowSorter<AureaTableModel> sorter = new TableRowSorter<AureaTableModel>((AureaTableModel)aureaTable.getModel());
		aureaTable.setRowSorter(sorter);
		sorter.setComparator(1, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				//Cut the date String into pieces and reassamble for correct default sorting
				StringBuffer s1 = new StringBuffer(o1.substring(6,10));
				s1.append(o1.substring(3, 5)).append(o1.substring(0,2)).append(o1.substring(10));
				StringBuffer s2 = new StringBuffer(o2.substring(6,10));
				s2.append(o2.substring(3, 5)).append(o2.substring(0,2)).append(o2.substring(10));
				return s1.toString().compareTo(s2.toString());
			}
		});
		aureaPanel.add(scrollPane);
		return aureaPanel;
	}

	private void startSearch() {
		if(searchThread!=null&&searchThread.isAlive()) {
			//Maybe we need a way to kill older search Threads, this should be done here
		}
		searchThread = new Thread() {
			@Override
			public void run() {
				String title = frame.getTitle();
				frame.setTitle(title+" ...Suche läuft...");
				if(currentSearchMode.equals(searchMode.Aurea)){
					JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/worklog/updated");
					JiraApiHelper.getInstance().appendKeyValue(searchStringDisplay.getText().split("=")[0], searchStringDisplay.getText().split("=")[1]);
					JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
					JiraApiHelper.getInstance().appendKeyValue("maxResults", "500");
				} else  {
					JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
					JiraApiHelper.getInstance().appendKeyValue("jql", searchStringDisplay.getText());
				}
				if(currentSearchMode.equals(searchMode.Task)) {
					JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_TASKS);
				}else if(currentSearchMode.equals(searchMode.Worklog)) {
					JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_WORKLOGS);	
				}
				Hashtable<String, String> header = new Hashtable<String, String>();
				// Der Auth-Header mit API-Token in base64 encoding
				header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
				StringBuffer json;
				if(currentSearchMode.equals(searchMode.Aurea)){
					json = JiraApiHelper.getInstance().sendRequest("GET", header);
				} else {
					json = JiraApiHelper.getInstance().sendRequest("GET", header);
				}
				if(currentSearchMode.equals(searchMode.Task)) {
					taskList = JiraParser.parseTaskSearchResults(json);
				} else if(currentSearchMode.equals(searchMode.Worklog)) {
					worklogList = JiraParser.parseWorklogSearchResults(json);
				} else if(currentSearchMode.equals(searchMode.Aurea)) {
					aureaList = JiraParser.parseAureaSearchResults(json);
				}
				if(currentSearchMode.equals(searchMode.Aurea)) {
					String nextPage;
					while((nextPage = JiraParser.nextPage(json)) != null) {
						JiraApiHelper.getInstance().setBaseString(nextPage);
						json = JiraApiHelper.getInstance().sendRequest("GET", header);
						aureaList.addAll(JiraParser.parseAureaSearchResults(json));
					}
				} else {
					int startAt = 0;
					while ((startAt = JiraParser.nextStartAt(json)) != -1) {
						JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
						JiraApiHelper.getInstance().appendKeyValue("jql", searchStringDisplay.getText());
						JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
						JiraApiHelper.getInstance().appendKeyValue("maxResults", "500");
						if(currentSearchMode.equals(searchMode.Task)) {
							JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_TASKS);
						}else if(currentSearchMode.equals(searchMode.Worklog)) {
							JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_WORKLOGS);	
						}
						JiraApiHelper.getInstance().appendKeyValue("startAt", String.valueOf(startAt));
						json = JiraApiHelper.getInstance().sendRequest("GET", header);
						if(currentSearchMode.equals(searchMode.Task)) {
							taskList.addAll(JiraParser.parseTaskSearchResults(json));
						}else if(currentSearchMode.equals(searchMode.Worklog)) {
							worklogList.addAll(JiraParser.parseWorklogSearchResults(json));
						}
					}
				}
				if (settingsDialog != null) {
					if(currentSearchMode.equals(searchMode.Task)) {
						//TODO
					}else if(currentSearchMode.equals(searchMode.Worklog)) {
						worklogList = settingsDialog.applyFiltersToWorklogList(worklogList);
					}
				}
				if(currentSearchMode.equals(searchMode.Task)) {
					taskTable.revalidate();
					((CardLayout) contentPanel.getLayout()).show(contentPanel, "Task");
				}else if(currentSearchMode.equals(searchMode.Worklog)) {
					worklogTable.revalidate();
					((CardLayout) contentPanel.getLayout()).show(contentPanel, "Worklog");
				}else if(currentSearchMode.equals(searchMode.Aurea)){
					aureaTable.revalidate();
					((CardLayout) contentPanel.getLayout()).show(contentPanel, "Aurea");
				}
				frame.setTitle(title);
			}
		};
		searchThread.start();
	}

	private void openSettingsDialog() {
		if (settingsDialog == null) {
			settingsDialog = new SettingsDialog();
		} else if (!settingsDialog.isVisible()) {
			settingsDialog.setVisible(true);
		}
	}
	
	private void openScriptDialog(boolean createNew) {
		new ScriptDialog(createNew);
	}
	private void openExecuteScriptDialog() {
		File f;
		String[] files = new String[0];
		try {
			f = new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if(f.isDirectory()&&f.exists()) {
				files = f.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".scr");
					}
				});
			}
			if(files.length==0) {
				files = f.getParentFile().list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".scr");
					}
				});
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if(files.length>0) {
			Object name = JOptionPane.showInputDialog(frame, "Bitte Scriptnamen auswählen", "", JOptionPane.INFORMATION_MESSAGE, null, files, files[0]).toString();
			if(name!=null) {
				Script.getScript(name.toString()).execute();
			}
		}else {
			JOptionPane.showMessageDialog(frame, "Keine Daten gefunden");
		}
	}
	
	private void openDebugging() {
		if(debugDialog != null) {
			debugDialog.setVisible(true);
		} else {
			debugDialog = new JDialog();
			debugText = new JTextArea();
			debugDialog.add(new JScrollPane(debugText));
			debugDialog.pack();
			debugDialog.setVisible(true);
		}
	}
	
	public static void addDebugLine(String text) {
		if(getInstance().debugText != null) {
			getInstance().debugText.append(text);
			getInstance().debugText.append("\r\n");
		}
	}

	private void exportToFile() {
		if(currentSearchMode.equals(searchMode.Worklog)) {
			if (worklogList.size() > 0) {
				String s = JOptionPane.showInputDialog(frame, "Dateiname angeben (ohne Endung)", "Exportdatei speichern",
						JOptionPane.QUESTION_MESSAGE);
				if (!StringUtils.isEmpty(s)) {
					File f = new File("latest.csv");
					JiraParser.writeWorklogsToFile(worklogList, f);
					f = new File(s.split("\\.")[0] + ".csv");
					JiraParser.writeWorklogsToFile(worklogList, f);
					try {
						JOptionPane.showInternalMessageDialog(frame.getContentPane(),
								"Datei " + f.getCanonicalPath() + " wurde gespeichert", "Datenexport",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else if(currentSearchMode.equals(searchMode.Task)) {
			if (taskList.size() > 0) {
				String s = JOptionPane.showInputDialog(frame, "Dateiname angeben (ohne Endung)", "Exportdatei speichern",
						JOptionPane.QUESTION_MESSAGE);
				if (!StringUtils.isEmpty(s)) {
					File f = new File("latest.csv");
					JiraParser.writeTasksToFile(taskList, f);
					f = new File(s.split("\\.")[0] + ".csv");
					JiraParser.writeTasksToFile(taskList, f);
					try {
						JOptionPane.showInternalMessageDialog(frame.getContentPane(),
								"Datei " + f.getCanonicalPath() + " wurde gespeichert", "Datenexport",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// ------------- Singleton Code only below
	private static MainFrame instance;

	private MainFrame() {
	}

	public static MainFrame getInstance() {
		if (instance == null) {
			instance = new MainFrame();
			instance.initFrame();
		}
		return instance;
	}

	@SuppressWarnings("serial")
	private class ScriptDialog extends JDialog {
		private Script script;
		private JTextField path;
		private int stepCount;
		private JTabbedPane tabbed;
		public ScriptDialog(boolean createNew) {
			super(frame, true);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			String name = "";
			if(createNew) {
				name = JOptionPane.showInputDialog(this, "Bitte Scriptnamen angeben","",JOptionPane.INFORMATION_MESSAGE);
			}else {
				File f;
				String[] files = new String[0];
				try {
					f = new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
					if(f.isDirectory()&&f.exists()) {
						files = f.list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return name.endsWith(".scr");
							}
						});
					}
					if(files.length==0) {
						files = f.getParentFile().list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return name.endsWith(".scr");
							}
						});
					}
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				if(files.length>0) {
					Object o = JOptionPane.showInputDialog(this, "Bitte Scriptnamen auswählen", "", JOptionPane.INFORMATION_MESSAGE, null, files, files[0]);
					if(o!=null) {
						name = o.toString();
					}
				}else {
					name = JOptionPane.showInputDialog(this, "Keine Daten gefunden, bitte neuen Scriptnamen angeben","",JOptionPane.INFORMATION_MESSAGE);
				}
			}
			if(!StringUtils.isEmpty(name)){
				name = name.split("\\.")[0];
				initContent(name);
				pack();
				setVisible(true);
				GuiUtils.centerDialogOnWindow(this, frame);
			}
		}
		private void initContent(String name) {
			script = Script.getScript(name);
			setLayout(new BorderLayout());
			tabbed = new JTabbedPane();
			stepCount = 0;
			for (ScriptStep step : script.getSteps()) {
				stepCount++;
				tabbed.addTab("Schritt "+stepCount, initTab(step)) ;
			}
			if(script.getSteps().size()==0) {
				addStep();
			}
			add(tabbed, BorderLayout.CENTER);
			JPanel buttonBar = new JPanel();
			add(buttonBar, BorderLayout.SOUTH);
			JButton add = new JButton("Schritt Hinzufügen");
			add.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addStep();
				}
			});
			buttonBar.add(add);
			JButton save = new JButton("Alle Schritte Speichern");
			save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					save();
				}
			});
			buttonBar.add(save);
			JButton execute = new JButton("Ausführen");
			execute.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					execute();
				}
			});
			buttonBar.add(execute);
		}
		private void save() {
			script.save();
		}
		private void execute() {
			script.execute();
		}
		private void addStep() {
			stepCount++;
			ScriptStep step = new ScriptStep();
			script.getSteps().add(step);
			tabbed.addTab("Schritt " + stepCount, initTab(step));
		}
		private JPanel initTab(final ScriptStep step) {
			JPanel tab = new JPanel();
			JComboBox<String> project;
			JSpinner fromDateOffset;
			JSpinner toDateOffset;
			JComboBox<String> snapToWeekMonthYear;
			JComboBox<String> offsetUnit;
			JComboBox<String> user;
			final JComboBox<String> epic = new JComboBox<String>();
			JTextField ordernumber;
			JTextField position;

			tab.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.insets.set(1, 1, 1, 1);
			tab.add(new JLabel("Projekt"), c);
			project = new JComboBox<String>();
			project.addItem("Alle");
			for (int i = 0; i < projects.length; i++) {
				project.addItem(projects[i]);
			}
			project.setSelectedItem(step.getProject());
			project.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						String selection = e.getItem().toString();
						if (e.getItem().equals("Alle")) {
							selection = "";
						}
						String[] epics = JiraApiHelper.getInstance().queryEpics(selection);
						epic.removeAllItems();
						epic.addItem("Alle");
						for (int i = 0; i < epics.length; i++) {
							epic.addItem(epics[i]);
						}
					}
					step.setProject(e.getItem().toString());
				}
			});
			c.gridy = 0;
			c.gridx = 1;
			tab.add(project, c);
			c.gridy = 1;
			c.gridx = 0;
			tab.add(new JLabel("von (Früher als Ausführungsdatum)"), c);
			fromDateOffset = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
			fromDateOffset.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					step.setRelativeStartDate((Integer)fromDateOffset.getModel().getValue());
				}
			});
			fromDateOffset.setValue(step.getRelativeStartDate());
			c.gridy = 1;
			c.gridx = 1;
			tab.add(fromDateOffset, c);
			c.gridy = 2;
			c.gridx = 0;
			tab.add(new JLabel("bis (Früher als Ausführungsdatum)"), c);
			toDateOffset = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
			toDateOffset.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					step.setRelativeEndDate((Integer)toDateOffset.getModel().getValue());
				}
			});
			toDateOffset.setValue(step.getRelativeEndDate());
			c.gridy = 2;
			c.gridx = 1;
			tab.add(toDateOffset, c);
			c.gridy = 3;
			c.gridx = 0;
			tab.add(new JLabel("gemessen in..."), c);
			offsetUnit = new JComboBox<String>() ;
			offsetUnit.addItem("Tage");
			offsetUnit.addItem("Monate");
			offsetUnit.addItem("Jahre");
			offsetUnit.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if("Jahre".equalsIgnoreCase(e.getItem().toString())){
						step.setDateOffsetUnit("year");
					} else if("Monate".equalsIgnoreCase(e.getItem().toString())){
						step.setDateOffsetUnit("month");
					} else {
						step.setDateOffsetUnit("day");
					}
				}
			});
			if(step.getDateOffsetUnit().equalsIgnoreCase("year")) {
				offsetUnit.setSelectedItem("Jahre");
			} else if(step.getDateOffsetUnit().equalsIgnoreCase("month")) {
				offsetUnit.setSelectedItem("Monate");
			} else {
				offsetUnit.setSelectedItem("Tage");
			}
			c.gridy = 3;
			c.gridx = 1;
			tab.add(offsetUnit, c);
			c.gridy = 4;
			c.gridx = 0;
			tab.add(new JLabel("Runden auf."), c);
			snapToWeekMonthYear = new JComboBox<String>() ;
			snapToWeekMonthYear.addItem("Nicht Runden");
			snapToWeekMonthYear.addItem("Wochen");
			snapToWeekMonthYear.addItem("Monate");
			snapToWeekMonthYear.addItem("Jahre");
			snapToWeekMonthYear.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if ("Jahre".equalsIgnoreCase(e.getItem().toString())){
						step.setSnapToWeekMonthYear("year");
					} else if ("Monate".equalsIgnoreCase(e.getItem().toString())){
						step.setSnapToWeekMonthYear("month");
					} else if ("Wochen".equalsIgnoreCase(e.getItem().toString())){
						step.setSnapToWeekMonthYear("week");
					} else {
						step.setSnapToWeekMonthYear("");
					}
				}
			});
			if(step.getSnapToWeekMonthYear().equalsIgnoreCase("year")) {
				snapToWeekMonthYear.setSelectedItem("Jahre");
			} else if(step.getSnapToWeekMonthYear().equalsIgnoreCase("month")) {
				snapToWeekMonthYear.setSelectedItem("Monate");
			} else if(step.getSnapToWeekMonthYear().equalsIgnoreCase("week")) {
				snapToWeekMonthYear.setSelectedItem("Wochen");
			} else {
				snapToWeekMonthYear.setSelectedItem("Nicht Runden");
			}
			c.gridy = 4;
			c.gridx = 1;
			tab.add(snapToWeekMonthYear, c);
			c.gridy = 5;
			c.gridx = 0;
			tab.add(new JLabel("Mitarbeiter"), c);
			user = new JComboBox<String>();
			user.addItem("Alle");
			for (int i = 0; i < users.length; i++) {
				user.addItem(users[i]);
			}
			user.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getItem().toString().equalsIgnoreCase("Alle")){
						step.setUser("");
					}else {	
						step.setUser(e.getItem().toString());
					}
				}
			});
			user.setSelectedItem(step.getUser());
			c.gridy = 5;
			c.gridx = 1;
			tab.add(user, c);
			c.gridy = 6;
			c.gridx = 0;
			tab.add(new JLabel("Epic"), c);
			epic.addItem("Alle");
			for (int i = 0; i < epics.length; i++) {
				epic.addItem(epics[i]);
			}
			epic.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(e.getItem().toString().equalsIgnoreCase("Alle")){
						step.setEpic("");
					}else {	
						step.setEpic(e.getItem().toString());
					}
				}
			});
			epic.setSelectedItem(step.getEpic());
			c.gridy = 6;
			c.gridx = 1;
			tab.add(epic, c);
			c.gridy = 7;
			c.gridx = 0;
			tab.add(new JLabel("Auftragsnummer"), c);
			c.gridy = 7;
			c.gridx = 1;
			ordernumber = new JTextField();
			tab.add(ordernumber, c);
			ordernumber.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					step.setOrdernumber(ordernumber.getText());
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					step.setOrdernumber(ordernumber.getText());
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					step.setOrdernumber(ordernumber.getText());
				}
			});
			ordernumber.setText(step.getOrdernumber());
			c.gridy = 8;
			c.gridx = 0;
			tab.add(new JLabel("Position"), c);
			c.gridy = 8;
			c.gridx = 1;
			position = new JTextField();
			tab.add(position, c);
			position.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					step.setOrderposition(position.getText());	
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					step.setOrderposition(position.getText());
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					step.setOrderposition(position.getText());
				}
			});
			position.setText(step.getOrderposition());
			c.gridy = 9;
			c.gridx = 0;
			tab.add(new JLabel("Pfad und Name für Exportdatei"), c);
			c.gridy = 9;
			c.gridx = 1;
			path = new JTextField();
			tab.add(path, c);
			path.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					step.setSavePath(path.getText());
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					step.setSavePath(path.getText());
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					step.setSavePath(path.getText());
				}
			});
			path.setText(step.getSavePath());
			return tab;
		}
	}

	@SuppressWarnings("serial")
	private class SettingsDialog extends JDialog {
		private JRadioButton worklogButton;
		private JRadioButton taskButton;
		private JRadioButton aureaButton;
		private ButtonGroup typeGroup;
		private JComboBox<String> project;
		private JDatePicker fromDate;
		private JDatePicker toDate;
		private JComboBox<String> user;
		private JComboBox<String> epic;
		private JTextField ordernumber;
		private JTextField position;

		private SettingsDialog() {
			super(frame, true);
			KeyAdapter listener = new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						dialogClosed();
						setVisible(false);
					}
				}
			};
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.insets.set(1, 1, 1, 1);
			add(new JLabel("Suchen nach"));
			c.gridx = 1;
			typeGroup = new ButtonGroup();
			worklogButton = new JRadioButton("Worklog",true);
			typeGroup.add(worklogButton);
			add(worklogButton,c);
			c.gridx = 2;
			taskButton = new JRadioButton("Ticket");
			typeGroup.add(taskButton);
			add(taskButton,c);
			c.gridx = 3;
			aureaButton = new JRadioButton("Aurea");
			typeGroup.add(aureaButton);
			add(aureaButton,c);
			c.gridy++;
			c.gridx= 0;
			add(new JLabel("Projekt"), c);
			project = new JComboBox<String>();
			project.addItem("Alle");
			for (int i = 0; i < projects.length; i++) {
				project.addItem(projects[i]);
			}
			project.addKeyListener(listener);
			project.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						String selection = e.getItem().toString();
						if (e.getItem().equals("Alle")) {
							selection = "";
						}
						String[] epics = JiraApiHelper.getInstance().queryEpics(selection);
						epic.removeAllItems();
						epic.addItem("Alle");
						for (int i = 0; i < epics.length; i++) {
							epic.addItem(epics[i]);
						}
					}
				}
			});
			c.gridx = 1;
			c.gridwidth = 3;
			add(project, c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			add(new JLabel("Datum von"), c);
			fromDate = new JDatePicker();
			fromDate.addKeyListener(listener);
			c.gridx = 1;
			c.gridwidth = 3;
			add(fromDate, c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			add(new JLabel("Datum bis"), c);
			toDate = new JDatePicker();
			toDate.addKeyListener(listener);
			c.gridx = 1;
			c.gridwidth = 3;
			add(toDate, c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			add(new JLabel("Mitarbeiter"), c);
			user = new JComboBox<String>();
			user.addItem("Alle");
			for (int i = 0; i < users.length; i++) {
				user.addItem(users[i]);
			}
			user.addKeyListener(listener);
			c.gridx = 1;
			c.gridwidth = 3;
			add(user, c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			add(new JLabel("Epic"), c);
			epic = new JComboBox<String>();
			epic.addItem("Alle");
			for (int i = 0; i < epics.length; i++) {
				epic.addItem(epics[i]);
			}
			epic.addKeyListener(listener);
			c.gridx = 1;
			c.gridwidth = 3;
			add(epic, c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			add(new JLabel("Auftragsnummer"), c);
			c.gridx = 1;
			c.gridwidth = 3;
			ordernumber = new JTextField();
			ordernumber.addKeyListener(listener);
			add(ordernumber, c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			add(new JLabel("Position"), c);
			c.gridx = 1;
			c.gridwidth = 3;
			position = new JTextField();
			position.addKeyListener(listener);
			add(position, c);
			pack();
			setVisible(true);
			GuiUtils.centerDialogOnWindow(this, frame);
			this.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					dialogClosed();
				}
			});
		}

		private void dialogClosed() {
			StringBuffer buf = new StringBuffer();
			boolean hasContent = false;
			if(taskButton.isSelected()) {
				currentSearchMode = searchMode.Task;
			} else if(worklogButton.isSelected()) {
				currentSearchMode = searchMode.Worklog;
			} else if(aureaButton.isSelected()) {
				currentSearchMode = searchMode.Aurea;
			}
			if (project.getSelectedIndex() > 0) {
				if (hasContent) {
					buf.append(" and ");
				}
				buf.append("project = ").append("\"").append(project.getSelectedItem()).append("\"");
				hasContent = true;
			}
			if (fromDate.getDate() != null) {
				Date d = fromDate.getDate();
				Calendar c = Calendar.getInstance();
				c.setTime(d);
				if (hasContent) {
					buf.append(" and ");
				}
				if(currentSearchMode.equals(searchMode.Task)){
					buf.append("DueDate");
					buf.append(" >= \"").append(CalendarUtils.getStringToCalendarForREST(c)).append("\"");
				}else if(currentSearchMode.equals(searchMode.Worklog)){
					buf.append("worklogdate");
					buf.append(" >= \"").append(CalendarUtils.getStringToCalendarForREST(c)).append("\"");
				}else if(currentSearchMode.equals(searchMode.Aurea)){
					buf.append("since");
					buf.append(" = ").append(String.valueOf(c.getTimeInMillis()));
				}
				hasContent = true;
			}
			if(!currentSearchMode.equals(searchMode.Aurea)) {
				if (toDate.getDate() != null) {
					Date d = toDate.getDate();
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					if (hasContent) {
						buf.append(" and ");
					}
					if(currentSearchMode.equals(searchMode.Task)){
						buf.append("DueDate");
					}else if(currentSearchMode.equals(searchMode.Worklog)){
						buf.append("worklogdate");
					}
					buf.append(" <= \"").append(CalendarUtils.getStringToCalendarForREST(c)).append("\"");
					hasContent = true;
				}
				if (user.getSelectedIndex() > 0) {
					if (hasContent) {
						buf.append(" and ");
					}
					if(currentSearchMode.equals(searchMode.Task)){
						buf.append("(assignee = \"").append(user.getSelectedItem()).append("\" or creator = \"").append(user.getSelectedItem()).append("\" or responsible = \"").append(user.getSelectedItem()).append("\")");
					}else if(currentSearchMode.equals(searchMode.Worklog)){
						buf.append("worklogauthor = \"").append(user.getSelectedItem()).append("\"");
					}
					hasContent = true;
				}
				if (epic.getSelectedIndex() > 0) {
					if (hasContent) {
						buf.append(" and ");
					}
					String epicKey = epic.getSelectedItem().toString();
					epicKey = epicKey.substring(0, epicKey.indexOf(" - "));
					buf.append("\"Epic Link\" = \"").append(epicKey).append("\"");
					hasContent = true;
				}
				if (!StringUtils.isEmpty(ordernumber.getText())) {
					if (hasContent) {
						buf.append(" and ");
					}
					buf.append("cf[10030] ~ ").append(ordernumber.getText()).append("");
					hasContent = true;
				}
				if (!StringUtils.isEmpty(position.getText())) {
					if (hasContent) {
						buf.append(" and ");
					}
					buf.append("cf[10031] ~ ").append(position.getText()).append("");
					hasContent = true;
				}
				if(currentSearchMode.equals(searchMode.Worklog)){
					// mandatory content for worklogs
					if (hasContent) {
						buf.append(" and ");
					}
					buf.append("timespent > 0");
					hasContent = true;
				}
			}
			searchStringDisplay.setText(buf.toString());
			startSearch();
		}

		/**
		 * since the api filters the tickets, not the worklogs, we need to apply our
		 * filter settings to the worklogList
		 * 
		 * @param list
		 */
		private ArrayList<Worklog> applyFiltersToWorklogList(ArrayList<Worklog> list) {
			String selectedUser = "";
			if (user.getSelectedIndex() > 0) {
				selectedUser = user.getSelectedItem().toString();
			}
			return JiraApiHelper.applyFiltersToWorklogList(list, fromDate.getDate(), toDate.getDate(), selectedUser);
		}
	}

	@SuppressWarnings("serial")
	private class WorklogTableModel extends AbstractTableModel {
		
		@Override
		public int getColumnCount() {
			return 13;
		}

		@Override
		public int getRowCount() {
			return worklogList.size();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(getRowCount()==0) {
				return Object.class;
			}
			return getValueAt(0, columnIndex).getClass();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return worklogList.get(rowIndex).getUser();
				case 1:
					SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
					return format.format(worklogList.get(rowIndex).getDate());
				case 2:
					return worklogList.get(rowIndex).getTimeSpent();
				case 3:
					return worklogList.get(rowIndex).getIssueKey();
				case 4:
					return worklogList.get(rowIndex).getComment();
				case 5:
					return worklogList.get(rowIndex).getOrdernumber();
				case 6:
					return worklogList.get(rowIndex).getOrderposition();
				case 7:
					return  worklogList.get(rowIndex).isBillable();
				case 8:
					return worklogList.get(rowIndex).getCustomer();
				case 9:
					return worklogList.get(rowIndex).getSummary();
				case 10:
					return worklogList.get(rowIndex).getProject();
				case 11:
					return worklogList.get(rowIndex).getEpic();
				case 12:
					return worklogList.get(rowIndex).getParent();
			}
			return "";
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Mitarbeiter";
			case 1:
				return "Datum";
			case 2:
				return "Zeit";
			case 3:
				return "Ticket";
			case 4:
				return "Bemerkung";
			case 5:
				return "Auftrag";
			case 6:
				return "Position";
			case 7:
				return "Abrechenbar";
			case 8:
				return "Kunde";
			case 9:
				return "Ticketname";
			case 10:
				return "Projekt";
			case 11:
				return "Epic";
			case 12:
				return "Mutterticket";
			}
			return "";
		}
	}
	@SuppressWarnings("serial")
	private class TaskTableModel extends AbstractTableModel {
		
		@Override
		public int getColumnCount() {
			return 14;
		}

		@Override
		public int getRowCount() {
			return taskList.size();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(getRowCount()==0) {
				return Object.class;
			}
			return getValueAt(0, columnIndex).getClass();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			switch(columnIndex) {
			case 0:
				return taskList.get(rowIndex).getProject();
			case 1:
				return taskList.get(rowIndex).getIssueKey();
			case 2:
				return taskList.get(rowIndex).getSummary();
			case 3:
				return taskList.get(rowIndex).getAssignee();
			case 4:
				return taskList.get(rowIndex).getResponsible();
			case 5:
				if(taskList.get(rowIndex).getDueDate()!=null) {
					return format.format(taskList.get(rowIndex).getDueDate());
				}
				return"";
			case 6:
				if(taskList.get(rowIndex).getPlannedDate()!=null) {
					return format.format(taskList.get(rowIndex).getPlannedDate());
				}
			return"";
			case 7:
				return taskList.get(rowIndex).getTimeSpent();
			case 8:
				return taskList.get(rowIndex).getTimeEstimate();
			case 9:
				return taskList.get(rowIndex).getOrdernumber();
			case 10:
				return taskList.get(rowIndex).getOrderposition();
			case 11:
				return taskList.get(rowIndex).isBillable();
			case 12:
				return taskList.get(rowIndex).getEpic();
			case 13:
				return taskList.get(rowIndex).getParent();
			}
			return "";
		}

		@Override
		public String getColumnName(int column) {
			switch(column){
			case 0:
				return "Projekt";
			case 1:
				return "Nummer";
			case 2:
				return "Titel";
			case 3:
				return "Zugewiesen";
			case 4:
				return "Verantwortlicher";
			case 5:
				return "Fälligkeit";
			case 6:
				return "geplantes Datum";
			case 7:
				return "gebuchte Zeit";
			case 8:
				return "geplante Zeit";
			case 9:
				return "Auftragsnummer";
			case 10:
				return "Auftragsposition";
			case 11:
				return "Abrechenbar";
			case 12:
				return "Epic";
			case 13:
				return "Mutterticket";
			}
			return "";
		}
	}
	@SuppressWarnings("serial")
	private class AureaTableModel extends AbstractTableModel {
		
		@Override
		public int getColumnCount() {
			return 14;
		}

		@Override
		public int getRowCount() {
			return aureaList.size();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(getRowCount()==0) {
				return Object.class;
			}
			return getValueAt(0, columnIndex).getClass();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			switch(columnIndex) {
			case 0:
				return aureaList.get(rowIndex).getCustomer();
			case 1:
				return aureaList.get(rowIndex).getCustomerID();
			case 2:
				return aureaList.get(rowIndex).getUser();
			case 3:
				return aureaList.get(rowIndex).getUserID();
			case 4:
				return aureaList.get(rowIndex).getOrdernumber();
			case 5:
				return aureaList.get(rowIndex).getOrderposition();
			case 6:
				if(aureaList.get(rowIndex).getDate()!=null) {
					return dateFormat.format(aureaList.get(rowIndex).getDate());
				}
				return"";
			case 7:
				aureaList.get(rowIndex).getPaymentType();
			case 8:
				aureaList.get(rowIndex).getPaymentMethod();
			case 9:
				if(aureaList.get(rowIndex).getStartTime()!=null) {
					return timeFormat.format(aureaList.get(rowIndex).getStartTime());
				}
				return "";
			case 10:
				if(aureaList.get(rowIndex).getEndTime()!=null) {
					return timeFormat.format(aureaList.get(rowIndex).getEndTime());
				}
				return "";
			case 11:
				aureaList.get(rowIndex).getIssueKey();
			case 12:
				aureaList.get(rowIndex).getDisplayText();
			case 13:
				aureaList.get(rowIndex).getTeam();
			}
			return "";
		}

		@Override
		public String getColumnName(int column) {
			switch(column){
			case 0:
				return "Kunde";
			case 1:
				return "Kunden-ID";
			case 2:
				return "Bearbeiter";
			case 3:
				return "Bearbeiter-ID";
			case 4:
				return "Auftragsnummer";
			case 5:
				return "Auftragsposition";
			case 6:
				return "Datum";
			case 7:
				return "Verrechnungstyp";
			case 8:
				return "Verrechnungsmethode";
			case 9:
				return "Start";
			case 10:
				return "Ende";
			case 11:
				return "Jira-TicketNr";
			case 12:
				return "Text";
			case 13:
				return "Teamzugehörigkeit";
			}
			return "";
		}
	}
}
