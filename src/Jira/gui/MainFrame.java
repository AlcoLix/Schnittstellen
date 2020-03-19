package Jira.gui;

import java.awt.BorderLayout;
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
import java.io.FileWriter;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import Jira.JiraApiHelper;
import Jira.JiraParser;
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
	private ScriptDialog scriptDialog;
	private JTable worklogTable;
	private ArrayList<Worklog> worklogList = new ArrayList<Worklog>();
	private Thread searchThread;

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
		JMenuItem editScript = new JMenuItem("�ndern");
		editScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openScriptDialog(false);
			}
		});
		scripting.add(editScript);
		JMenuItem executeScript = new JMenuItem("Ausf�hren");
		executeScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openExecuteScriptDialog();
			}
		});
		scripting.add(executeScript);
	}

	@SuppressWarnings("serial")
	private void initContent() {
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		JPanel top = new JPanel(new GridLayout(0, 1));
		panel.add(top, BorderLayout.NORTH);
		top.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		top.add(new JLabel("Suchbefehl:"));
		searchStringDisplay = new JLabel("timespent > 0");
		top.add(searchStringDisplay);
		JPanel center = new JPanel();
		panel.add(center, BorderLayout.CENTER);
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
		worklogTable.getColumnModel().getColumn(7).setPreferredWidth(200);
		worklogTable.getColumnModel().getColumn(8).setPreferredWidth(200);
		worklogTable.getColumnModel().getColumn(9).setPreferredWidth(75);
		worklogTable.getColumnModel().getColumn(10).setPreferredWidth(75);
		worklogTable.getColumnModel().getColumn(11).setPreferredWidth(75);
		worklogTable.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(worklogTable);
		scrollPane.setPreferredSize(new Dimension(1150, 600));
		scrollPane.setMinimumSize(new Dimension(1150, 600));
		
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
		center.add(scrollPane);
	}

	private void startSearch() {
		if(searchThread!=null&&searchThread.isAlive()) {
			//Maybe we need a way to kill older search Threads, this should be done here
		}
		searchThread = new Thread() {
			@Override
			public void run() {
				String title = frame.getTitle();
				frame.setTitle(title+" ...Suche l�uft...");
				JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
				JiraApiHelper.getInstance().appendKeyValue("jql", searchStringDisplay.getText());
				JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
				JiraApiHelper.getInstance().appendKeyValue("maxResults", "500");
				JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_TASKS);
				Hashtable<String, String> header = new Hashtable<String, String>();
				// Der Auth-Header mit API-Token in base64 encoding
				header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
				StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
				worklogList = JiraParser.parseSearchResults(json);
				int startAt = 0;
				while ((startAt = JiraParser.nextStartAt(json)) != -1) {
					JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
					JiraApiHelper.getInstance().appendKeyValue("jql", searchStringDisplay.getText());
					JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
					JiraApiHelper.getInstance().appendKeyValue("maxResults", "500");
					JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_TASKS);
					JiraApiHelper.getInstance().appendKeyValue("startAt", String.valueOf(startAt));
					json = JiraApiHelper.getInstance().sendRequest("GET", header);
					worklogList.addAll(JiraParser.parseSearchResults(json));
				}
				if (settingsDialog != null) {
					worklogList = settingsDialog.applyFiltersToWorklogList(worklogList);
				}
				worklogTable.revalidate();
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
		scriptDialog = new ScriptDialog(createNew);
	}
	private void openExecuteScriptDialog() {
		
	}

	private void exportToFile() {
		if (worklogList.size() > 0) {
				String s = JOptionPane.showInputDialog(frame, "Dateiname angeben (ohne Endung)", "Exportdate speichern",
						JOptionPane.QUESTION_MESSAGE);
				if (!StringUtils.isEmpty(s)) {
					File f = new File("latest.csv");
					writeWorklogsToFile(worklogList, f);
					f = new File(s.split("\\.")[0] + ".csv");
					writeWorklogsToFile(worklogList, f);
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
	
	private void writeWorklogsToFile(ArrayList<Worklog> worklogs, File f) {
		try {
			StringBuffer buf = JiraParser.parseWorklogsToCsvString(worklogList);
			FileWriter writer = new FileWriter(f, false);
			writer.write(buf.toString());
			writer.close();
			writer = new FileWriter(f, false);
			writer.write(buf.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ------------- Singleton Code only below
	private static MainFrame instance;

	private MainFrame() {
		initFrame();
	}

	public static MainFrame getInstance() {
		if (instance == null) {
			instance = new MainFrame();
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
			String name;
			if(createNew) {
				name = JOptionPane.showInputDialog(this, "Bitte Scriptnamen angeben","",JOptionPane.INFORMATION_MESSAGE);
			}else {
				File f;
				String[] files = new String[0];
				try {
					f = new File(main.Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
					files = f.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							name.endsWith(".scr");
							return false;
						}
					});
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				if(files.length>0) {
					name = JOptionPane.showInputDialog(this, "Bitte Scriptnamen ausw�hlen", "", JOptionPane.INFORMATION_MESSAGE, null, files, files[0]).toString();
				}else {
					name = JOptionPane.showInputDialog(this, "Keine Daten gefunden, bitte neuen Scriptnamen angeben","",JOptionPane.INFORMATION_MESSAGE);
				}
			}
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
			JButton add = new JButton("Schritt Hinzuf�gen");
			add.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addStep();
				}
			});
			buttonBar.add(add);
			JButton save = new JButton("Speichern");
			save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					save();
				}
			});
			buttonBar.add(save);
			JButton execute = new JButton("Ausf�hren");
			execute.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					execute();
				}
			});
			buttonBar.add(execute);
			pack();
			setVisible(true);
			GuiUtils.centerDialogOnWindow(this, frame);
		}
		private void save() {
			script.save();
		}
		private void execute() {
			
		}
		private void addStep() {
			stepCount++;
			ScriptStep step = new ScriptStep();
			script.getSteps().add(step);
			tabbed.addTab("Schritt " + stepCount, initTab(step));
		}
		private JPanel initTab(ScriptStep step) {
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
			c.gridy = 0;
			c.gridx = 1;
			tab.add(project, c);
			c.gridy = 1;
			c.gridx = 0;
			tab.add(new JLabel("von (Fr�her als Ausf�hrungsdatum)"), c);
			fromDateOffset = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
			c.gridy = 1;
			c.gridx = 1;
			tab.add(fromDateOffset, c);
			c.gridy = 2;
			c.gridx = 0;
			tab.add(new JLabel("bis (Fr�her als Ausf�hrungsdatum)"), c);
			toDateOffset = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
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
			c.gridy = 8;
			c.gridx = 0;
			tab.add(new JLabel("Position"), c);
			c.gridy = 8;
			c.gridx = 1;
			position = new JTextField();
			tab.add(position, c);
			c.gridy = 9;
			c.gridx = 0;
			tab.add(new JLabel("Pfad und Name f�r Exportdatei"), c);
			c.gridy = 9;
			c.gridx = 1;
			path = new JTextField();
			tab.add(path, c);		
			return tab;
		}
	}

	@SuppressWarnings("serial")
	private class SettingsDialog extends JDialog {
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
			c.gridy = 0;
			c.gridx = 1;
			add(project, c);
			c.gridy = 1;
			c.gridx = 0;
			add(new JLabel("Datum von"), c);
			fromDate = new JDatePicker();
			fromDate.addKeyListener(listener);
			c.gridy = 1;
			c.gridx = 1;
			add(fromDate, c);
			c.gridy = 2;
			c.gridx = 0;
			add(new JLabel("Datum bis"), c);
			toDate = new JDatePicker();
			toDate.addKeyListener(listener);
			c.gridy = 2;
			c.gridx = 1;
			add(toDate, c);
			c.gridy = 3;
			c.gridx = 0;
			add(new JLabel("Mitarbeiter"), c);
			user = new JComboBox<String>();
			user.addItem("Alle");
			for (int i = 0; i < users.length; i++) {
				user.addItem(users[i]);
			}
			user.addKeyListener(listener);
			c.gridy = 3;
			c.gridx = 1;
			add(user, c);
			c.gridy = 4;
			c.gridx = 0;
			add(new JLabel("Epic"), c);
			epic = new JComboBox<String>();
			epic.addItem("Alle");
			for (int i = 0; i < epics.length; i++) {
				epic.addItem(epics[i]);
			}
			epic.addKeyListener(listener);
			c.gridy = 4;
			c.gridx = 1;
			add(epic, c);
			c.gridy = 5;
			c.gridx = 0;
			add(new JLabel("Auftragsnummer"), c);
			c.gridy = 5;
			c.gridx = 1;
			ordernumber = new JTextField();
			ordernumber.addKeyListener(listener);
			add(ordernumber, c);
			c.gridy = 6;
			c.gridx = 0;
			add(new JLabel("Position"), c);
			c.gridy = 6;
			c.gridx = 1;
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
				buf.append("worklogdate >= \"").append(CalendarUtils.getStringToCalendarForREST(c)).append("\"");
				hasContent = true;
			}
			if (toDate.getDate() != null) {
				Date d = toDate.getDate();
				Calendar c = Calendar.getInstance();
				c.setTime(d);
				if (hasContent) {
					buf.append(" and ");
				}
				buf.append("worklogdate <= \"").append(CalendarUtils.getStringToCalendarForREST(c)).append("\"");
				hasContent = true;
			}
			if (user.getSelectedIndex() > 0) {
				if (hasContent) {
					buf.append(" and ");
				}
				buf.append("worklogauthor = \"").append(user.getSelectedItem()).append("\"");
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
			// mandatory content
			if (hasContent) {
				buf.append(" and ");
			}
			buf.append("timespent > 0");
			hasContent = true;
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
			ArrayList<Worklog> retval = new ArrayList<Worklog>(list.size());
			for (Worklog worklog : list) {
				boolean keep = true;
				if (fromDate.getDate() != null) {
					Date d = CalendarUtils.startOfDay(fromDate.getDate());
					if (worklog.getDate().before(d)&&!CalendarUtils.isSameDay(d, worklog.getDate())) {
						keep = false;
					}
				}
				if (toDate.getDate() != null) {
					Date d = CalendarUtils.endOfDay(toDate.getDate());
					if (worklog.getDate().after(d)&&!CalendarUtils.isSameDay(d, worklog.getDate())) {
						keep = false;
					}
				}
				if (user.getSelectedIndex() > 0) {
					if (!worklog.getUser().equalsIgnoreCase(user.getSelectedItem().toString())) {
						keep = false;
					}
				}
				if (keep) {
					retval.add(worklog);
				}
			}
			return retval;
		}
	}

	@SuppressWarnings("serial")
	private class WorklogTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 12;
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
				return worklogList.get(rowIndex).getCustomer();
			case 8:
				return worklogList.get(rowIndex).getSummary();
			case 9:
				return worklogList.get(rowIndex).getProject();
			case 10:
				return worklogList.get(rowIndex).getEpic();
			case 11:
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
				return "Kunde";
			case 8:
				return "Ticketname";
			case 9:
				return "Projekt";
			case 10:
				return "Epic";
			case 11:
				return "Mutterticket";
			}
			return "";
		}
	}
}
