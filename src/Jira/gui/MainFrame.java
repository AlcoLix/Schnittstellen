package Jira.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.MaskFormatter;

import Jira.JiraApiHelper;
import Jira.JiraParser;
import Jira.Worklog;

public class MainFrame {
	private JFrame frame;
	private JLabel searchStringDisplay;
	private SettingsDialog settingsDialog;
	private JTable worklogTable;
	private ArrayList<Worklog> worklogList = new ArrayList<Worklog>();
	
	private void initFrame() {
		frame = new JFrame("Jira Auswertung");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		initMenuBar();
		initContent();
		frame.pack();
		frame.setVisible(true);
	}
	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu data = new JMenu("Datei");
		menuBar.add(data);
		JMenuItem settings = new JMenuItem("Einstellungen");
		settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openSettingsDialog();
			}
		});
		data.add(settings);
		JMenuItem search = new JMenuItem("Suchen");
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startSearch();
			}
		});
		data.add(search);
		JMenuItem exit = new JMenuItem("Beenden");
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
		JPanel top = new JPanel();
		panel.add(top, BorderLayout.NORTH);
		top.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		top.add(new JLabel("Suchbefehl:"));
		searchStringDisplay = new JLabel();
		top.add(searchStringDisplay);
		JPanel center = new JPanel();
		panel.add(center, BorderLayout.CENTER);
		worklogTable = new JTable(new WorklogTableModel()){

            //Implement table cell tool tips.           
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    tip = getValueAt(rowIndex, colIndex).toString();
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            }
        };
		JScrollPane scrollPane = new JScrollPane(worklogTable);
		center.add(scrollPane);
	}
	private void startSearch() {
		/*
		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
		JiraApiHelper.getInstance().appendKeyValue("jql", "project = SBOS AND text ~ \"KW Bader\"");
		JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
		JiraApiHelper.getInstance().appendKeyValue("fields", "worklog, key");
		Hashtable<String, String> header = new Hashtable<String, String>();
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5"); //Der Auth-Header mit API-Token in base64 encoding
		JiraApiHelper.getInstance().sendRequest("GET", header);
		*/
		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
		JiraApiHelper.getInstance().appendKeyValue("jql", searchStringDisplay.getText());
		JiraApiHelper.getInstance().appendKeyValue("validateQuery", "warn");
		JiraApiHelper.getInstance().appendKeyValue("fields", "worklog, key,customfield_10030,customfield_10031");
		Hashtable<String, String> header = new Hashtable<String, String>();
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5"); //Der Auth-Header mit API-Token in base64 encoding
		StringBuffer json = JiraApiHelper.getInstance().sendRequest("GET", header);
		worklogList = JiraParser.parse(json);
		worklogTable.revalidate();
	}
	private void openSettingsDialog() {
		if(settingsDialog == null ) {
			settingsDialog = new SettingsDialog();
		}else if (!settingsDialog.isVisible()) {
			settingsDialog.setVisible(true);
		}
	}
	//------------- Singleton Code only below
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
	private class SettingsDialog extends JDialog{
		private JTextField project;
		private JTextField fromDate;
		private JTextField toDate;
		private JTextField user;

		private SettingsDialog() {
			super(frame,true);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(new GridLayout(0, 2));
			add(new JLabel("Projektschlüssel"));
			project = new JTextField();
			add(project);
			add(new JLabel("Datum von (dd.mm.yyyy)"));
			try {
				fromDate = new JFormattedTextField(new MaskFormatter("##.##.####"));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			add(fromDate);
			add(new JLabel("Datum bis (dd.mm.yyyy)"));
			try {
				toDate = new JFormattedTextField(new MaskFormatter("##.##.####"));
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			add(toDate);
			add(new JLabel("Mitarbeiter"));
			user = new JTextField();
			add(user);
			pack();
			setVisible(true);
			this.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					boolean hasContent = false;
					StringBuffer buf = new StringBuffer();
					if (project.getText().length()>0) {
						if(hasContent) {
							buf.append(" and ");
						}
						buf.append("project = ").append(project.getText());
						hasContent =true; 
					}
					if (user.getText().length()>0) {
						if(hasContent) {
							buf.append(" and ");
						}
						buf.append("worklogauthor = ").append(user.getText());
						hasContent =true; 
					}
					//mandatory content
					if(hasContent) {
						buf.append(" and ");
					}
					buf.append("timespent > 0");
					hasContent =true;
					searchStringDisplay.setText(buf.toString());
				}
			});
		}
	}
	@SuppressWarnings("serial")
	private class WorklogTableModel extends AbstractTableModel{
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
			switch(columnIndex) {
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
			switch(column) {
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
