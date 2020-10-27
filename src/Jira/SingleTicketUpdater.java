package Jira;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import Jira.database.DatabaseConnection;
import Jira.utils.GuiUtils;


public class SingleTicketUpdater {

	private JFrame mainFrame;
	private JTextField ticketNo;

	public SingleTicketUpdater() {
		init();
	}
	private void init() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel center = new JPanel();
		mainFrame.add(center);
		JPanel top = new JPanel();
		top.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		mainFrame.add(top, BorderLayout.NORTH);
		JPanel bottom = new JPanel();
		bottom.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		mainFrame.add(bottom, BorderLayout.SOUTH);
		top.add(new JLabel("Übertragung ALLER Worklogs eines Tickets"));
		center.add(new JLabel("Bitte Jira-Ticketnummer eingeben"));
		ticketNo = new JTextField(15);
		center.add(ticketNo);
		JButton start = new JButton("Start");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});
		bottom.add(start);
		mainFrame.pack();
		GuiUtils.centerWindowOnScreen(mainFrame);
		mainFrame.setVisible(true);
	}
	private void start() {
		JiraApiHelper.getInstance().setBaseString("https://partsolution.atlassian.net/rest/api/latest/search");
		JiraApiHelper.getInstance().appendKeyValue("jql", "issuekey="+ticketNo.getText());
		JiraApiHelper.getInstance().appendKeyValue("fields", JiraApiHelper.FIELDS_FOR_WORKLOGS);	
		Hashtable<String, String> header = new Hashtable<String, String>();
		// Der Auth-Header mit API-Token in base64 encoding
		header.put("Authorization", "Basic RGVubmlzLnJ1ZW56bGVyQHBhcnQuZGU6WTJpZlp6dWpRYVZTZmR3RkFZMUMzQzE5");
		StringBuffer json = null;
		try {
			json = JiraApiHelper.getInstance().sendRequest("GET", header);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(json!=null) {
			JSONObject content = new JSONObject(json.toString());
			JSONArray issues = content.getJSONArray("issues");
			ArrayList<Worklog> retval = new ArrayList<Worklog>();
			for (Object object : issues) {
				JSONObject issue = (JSONObject) object;
				retval.addAll(JiraParser.parseWorklogsFromIssueObject(issue));
			}
			if(retval.size()==0) {
				JOptionPane.showConfirmDialog(mainFrame, "Keine Worklogs im Ticket gefunden", "Fehler", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			}else {
				if(JOptionPane.showConfirmDialog(mainFrame, retval.size()+" Worklogs gefunden. Übertragung beginnen?", "Übertragung beginnen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION) {
					ArrayList<AureaWorklog> aureaList =  JiraParser.parseWorklogList2AureaList(retval);
					DatabaseConnection.getInstance().connect();
					DatabaseConnection.getInstance().sendInsertOrUpdate4Aurea(aureaList);
				}
			}
		} else {
			JOptionPane.showConfirmDialog(mainFrame, "Ticketnummer nicht gefunden", "Fehler", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static void main(String[] args) {
		new SingleTicketUpdater();
	}
}
