package Jira.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.MaskFormatter;

import Jira.JiraApiHelper;
import Jira.JiraParser;
import Jira.Worklog;

public class MainFrame {
	private String[] users;
	private String[] projects;
	private JFrame frame;
	private JLabel searchStringDisplay;
	private SettingsDialog settingsDialog;
	private JTable worklogTable;
	private ArrayList<Worklog> worklogList = new ArrayList<Worklog>();

	private void initFrame() {
		frame = new JFrame("Jira Auswertung");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/group/member");
		JiraApiHelper.getInstance().appendKeyValue("groupname", "jira-software-users");
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5"); 
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		users = JiraParser.parseUsers(json);

		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/project/search");
		// Der Auth-Header kann noch einmal verwendet werden 
		json = JiraApiHelper.getInstance().sendRequest("GET", header);
		projects = JiraParser.parseProjects(json);
	}
	
	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu data = new JMenu("Datei");
		menuBar.add(data);
		JMenuItem settings = new JMenuItem("Einstellungen");
		settings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_DOWN_MASK));
		settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openSettingsDialog();
			}
		});
		data.add(settings);
		JMenuItem search = new JMenuItem("Suchen");
		search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startSearch();
			}
		});
		data.add(search);
		JMenuItem save = new JMenuItem("CSV speichern");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK));
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportToFile();
			}
		});
		data.add(save);
		JMenuItem exit = new JMenuItem("Beenden");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,InputEvent.ALT_DOWN_MASK));
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		data.add(exit);
	}

	@SuppressWarnings("serial")
	private void initContent() {
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		JPanel top = new JPanel(new GridLayout(0, 1));
		panel.add(top, BorderLayout.NORTH);
		top.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		top.add(new JLabel("Suchbefehl:"));
		searchStringDisplay = new JLabel();
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
		JScrollPane scrollPane = new JScrollPane(worklogTable);
		center.add(scrollPane);
	}

	private void startSearch() {
		/*
		 * JiraApiHelper.getInstance().setBaseString(
		 * "https://partsolution.atlassian.net/rest/api/latest/search");
		 * JiraApiHelper.getInstance().appendKeyValue("jql",
		 * "project = SBOS AND text ~ \"KW Bader\"");
		 * JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
		 * JiraApiHelper.getInstance().appendKeyValue("fields", "worklog, key");
		 * Hashtable<String, String> header = new Hashtable<String, String>();
		 * header.put("Authorization",
		 * "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		 * //Der Auth-Header mit API-Token in base64 encoding
		 * JiraApiHelper.getInstance().sendRequest("GET", header);
		 */
		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
		JiraApiHelper.getInstance().appendKeyValue("jql", searchStringDisplay.getText());
		JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
		JiraApiHelper.getInstance().appendKeyValue("fields", "worklog, key,customfield_10030,customfield_10031");
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5"); 
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		worklogList = JiraParser.parseSearchResults(json);
		worklogTable.revalidate();
	}

	private void openSettingsDialog() {
		if (settingsDialog == null) {
			settingsDialog = new SettingsDialog();
		} else if (!settingsDialog.isVisible()) {
			settingsDialog.setVisible(true);
		}
	}

	private void exportToFile() {
		if(worklogList.size()>0) {
			StringBuffer buf = JiraParser.parseWorklogsToCsvString(worklogList);
			try {
				Calendar c = Calendar.getInstance();
				File f = new File(c.get(Calendar.YEAR)+"_"+c.get(Calendar.MONTH)+"_"+c.get(Calendar.DAY_OF_MONTH)+"_"+c.get(Calendar.SECOND)+".csv");
				FileWriter writer = new FileWriter(f);
				writer.write(buf.toString());
				writer.close();
				JOptionPane.showInternalMessageDialog(frame.getContentPane(), "Datei "+f.getPath()+" wurde gespeichert", "Datenexport", JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	private class SettingsDialog extends JDialog {
		private  JComboBox<String> project;
		private JTextField fromDate;
		private JTextField toDate;
		private JComboBox<String> user;

		private SettingsDialog() {
			super(frame, true);
			KeyAdapter listener = new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						dialogClosed();
						setVisible(false);
					}
				}
			};
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(new GridLayout(0, 2));
			add(new JLabel("Projekt"));
			project = new JComboBox<String>();
			project.addItem("Alle");
			for (int i = 0; i < projects.length; i++) {
				project.addItem(projects[i]);
			}
			project.addKeyListener(listener);
			add(project);
			add(new JLabel("Datum von (dd.mm.yyyy)"));
			try {
				fromDate = new JFormattedTextField(new MaskFormatter("##.##.####"));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			fromDate.addKeyListener(listener);
			add(fromDate);
			add(new JLabel("Datum bis (dd.mm.yyyy)"));
			try {
				toDate = new JFormattedTextField(new MaskFormatter("##.##.####"));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			toDate.addKeyListener(listener);
			add(toDate);
			add(new JLabel("Mitarbeiter"));
			user = new JComboBox<String>();
			user.addItem("Alle");
			for (int i = 0; i < users.length; i++) {
				user.addItem(users[i]);
			}
			user.addKeyListener(listener);
			add(user);
			pack();
			setVisible(true);
			this.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					dialogClosed();
				}
			});
		}
		private void dialogClosed() {
			boolean hasContent = false;
			StringBuffer buf = new StringBuffer();
			if (project.getSelectedIndex()>0) {
				if (hasContent) {
					buf.append(" and ");
				}
				buf.append("project = ").append("\"").append(project.getSelectedItem()).append("\"");
				hasContent = true;
			}
			if (fromDate.getText().matches("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d")) {
				String s = fromDate.getText();
				int days = Integer.parseInt(s.substring(0, 2));
				int months = Integer.parseInt(s.substring(3, 5));
				int year = Integer.parseInt(s.substring(6));
				if (days < 32 && months <13) { // no need to check if below zero, Text field only accepts numbers
					if (hasContent) {
						buf.append(" and ");
					}
					buf.append("worklogdate >= \"").append(year).append("/").append(months).append("/").append(days).append("\"");
					hasContent = true;
				}
			}
			if (toDate.getText().matches("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d")) {
				String s = toDate.getText();
				int days = Integer.parseInt(s.substring(0, 2));
				int months = Integer.parseInt(s.substring(3, 5));
				int year = Integer.parseInt(s.substring(6));
				if (days < 32 && months <13) { // no need to check if below zero, Text field only accepts numbers
					if (hasContent) {
						buf.append(" and ");
					}
					buf.append("worklogdate <= \"").append(year).append("/").append(months).append("/").append(days).append("\"");
					hasContent = true;
				}
			}
			if (user.getSelectedIndex()>0) {
				if (hasContent) {
					buf.append(" and ");
				}
				buf.append("worklogauthor = \"").append(user.getSelectedItem()).append("\"");
				hasContent = true;
			}
			// mandatory content
			if (hasContent) {
				buf.append(" and ");
			}
			buf.append("timespent > 0");
			hasContent = true;
			searchStringDisplay.setText(buf.toString());
		}
	}

	@SuppressWarnings("serial")
	private class WorklogTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 7;
		}

		@Override
		public int getRowCount() {
			return worklogList.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return worklogList.get(rowIndex).getUser();
			case 1:
				return worklogList.get(rowIndex).getDate();
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
			}
			return "";
		}
	}
}
