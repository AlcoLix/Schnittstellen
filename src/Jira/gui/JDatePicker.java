package Jira.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Jira.utils.CalendarUtils;
import Jira.utils.GuiUtils;


public class JDatePicker extends JButton implements ActionListener  {
	private static final long serialVersionUID = 1L;
	private Date date;
	private JDialog calendarPopup;

	public JDatePicker() {
		this(null);
	}

	public JDatePicker(Date d) {
		super();
		if(d!=null) {
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.set(Calendar.HOUR_OF_DAY,0);
			setDate(c.getTime());
		}else {
			setDate(null);
		}
		addActionListener(this);
	}

	public void setDate(Date d) {
		this.date = d;
		if(d != null) {
			setText(CalendarUtils.getStringToDate(d, true, true, true, false,
					false, false, false));	
		} else {
			setText("-");
		}
	}

	public Date getDate() {
		return date;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (calendarPopup != null) {
			calendarPopup.setVisible(false);
		}
		Window master = GuiUtils.getParentDialogElement(this);
		calendarPopup = new JDialog(master);
		calendarPopup.setContentPane(new CalendarDialogPanel());
		calendarPopup.setUndecorated(true);
		calendarPopup.setAlwaysOnTop(true);
		calendarPopup.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				calendarPopup.setVisible(false);
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {

			}
		});
		calendarPopup.pack();
		calendarPopup.setVisible(true);
		GuiUtils.centerWindowOnWindow(master, calendarPopup);
	}

	private class CalendarDialogPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		private JLabel currentMonthYear;
		private Calendar current;
		private int currentMonth;
		private int currentYear;
		GridBagConstraints c = new GridBagConstraints();

		public CalendarDialogPanel() {
			current = Calendar.getInstance();
			if(getDate()!=null) {
				current.setTime(getDate());
			}else {
				current.set(Calendar.HOUR_OF_DAY,0);
			}
			setBorder(BorderFactory.createEtchedBorder());
			setLayout(new GridBagLayout());
			initContents();
		}
		private void initContents(){
			removeAll();
			// first the Flip-Buttons
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			JButton yearDownButton = new JButton("<<");
			yearDownButton.addActionListener(this);
			add(yearDownButton, c);
			c.gridx = 1;
			JButton monthDownButton = new JButton("<");
			monthDownButton.addActionListener(this);
			add(monthDownButton, c);
			c.gridx = 2;
			c.gridwidth = 2;
			currentMonthYear = new JLabel(CalendarUtils
					.getStringToCalendarMonthWord(current, true, true, false,
							false, false, false, false));
			add(currentMonthYear, c);
			c.gridwidth = 1;
			c.gridx = 4;
			JButton clearButton = new JButton("X");
			clearButton.addActionListener(this);
			add(clearButton,c);
			c.gridx = 5;
			JButton monthUpButton = new JButton(">");
			monthUpButton.addActionListener(this);
			add(monthUpButton, c);
			c.gridx = 6;
			JButton yearUpButton = new JButton(">>");
			yearUpButton.addActionListener(this);
			add(yearUpButton, c);
			// Now the Days
			c.gridy = 1;
			c.gridx = 0;
			add(new JLabel("Mo"), c);
			c.gridy = 1;
			c.gridx = 1;
			add(new JLabel("Di"), c);
			c.gridy = 1;
			c.gridx = 2;
			add(new JLabel("Mi"), c);
			c.gridy = 1;
			c.gridx = 3;
			add(new JLabel("Do"), c);
			c.gridy = 1;
			c.gridx = 4;
			add(new JLabel("Fr"), c);
			c.gridy = 1;
			c.gridx = 5;
			add(new JLabel("Sa"), c);
			c.gridy = 1;
			c.gridx = 6;
			add(new JLabel("So"), c);
			// Now the buttons for the days
			fillDayButtons();

		}

		private void fillDayButtons() {
			Calendar myCurrent = (Calendar)current.clone();
			c.gridy = 1;
			myCurrent.set(Calendar.DAY_OF_MONTH, 1);
			currentMonth = myCurrent.get(Calendar.MONTH);
			currentYear = myCurrent.get(Calendar.YEAR);
			while (myCurrent.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
				myCurrent.set(Calendar.DAY_OF_MONTH, myCurrent
						.get(Calendar.DAY_OF_MONTH) - 1);
			}
			do {
				c.gridy++;
				for (int i = 0; i < 7; i++) {
					c.gridx = i;
					JButton b = new JButton(String.valueOf(myCurrent
							.get(Calendar.DAY_OF_MONTH)));
					if (myCurrent.get(Calendar.MONTH) != currentMonth) {
						b.setEnabled(false);
					} else {
						b.addActionListener(this);
					}
					add(b, c);
					myCurrent.set(Calendar.DAY_OF_MONTH, myCurrent
							.get(Calendar.DAY_OF_MONTH) + 1);
				}
			} while (myCurrent.get(Calendar.MONTH) == currentMonth);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("<<")) {
				current.set(Calendar.YEAR, current.get(Calendar.YEAR) - 1);
				initContents();
				calendarPopup.pack();
			} else if (e.getActionCommand().equals("<")) {
				current.set(Calendar.MONTH, current.get(Calendar.MONTH) - 1);
				initContents();
				calendarPopup.pack();
			} else if (e.getActionCommand().equals(">")) {
				current.set(Calendar.MONTH, current.get(Calendar.MONTH) + 1);
				initContents();
				calendarPopup.pack();
			} else if (e.getActionCommand().equals(">>")) {
				current.set(Calendar.YEAR, current.get(Calendar.YEAR) + 1);
				initContents();
				calendarPopup.pack();
			} else if(e.getActionCommand().equals("X")) {
				setDate(null);
				calendarPopup.setVisible(false);
			} else {
				int day = Integer.parseInt(e.getActionCommand());
				Calendar c = Calendar.getInstance();
				c.clear();
				c.set(Calendar.MONTH, currentMonth);
				c.set(Calendar.YEAR, currentYear);
				c.set(Calendar.DAY_OF_MONTH, day);
				setDate(c.getTime());
				calendarPopup.setVisible(false);
			}
		}
	}
}
