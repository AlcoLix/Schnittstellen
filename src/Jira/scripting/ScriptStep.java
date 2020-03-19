package Jira.scripting;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlTransient;

import Jira.utils.CalendarUtils;
import Jira.utils.StringUtils;

public class ScriptStep {
	private String savePath;
	private String project;
	private String dateOffsetUnit;
	private int relativeStartDate;
	private int relativeEndDate;
	private String snapToWeekMonthYear;
	private String user;
	private String epic;
	private String ordernumber;
	private String orderposition;
	
	public ScriptStep() {
	}
	
	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getDateOffsetUnit() {
		return dateOffsetUnit;
	}

	public void setDateOffsetUnit(String dateOffsetUnit) {
		this.dateOffsetUnit = dateOffsetUnit;
	}

	public int getRelativeStartDate() {
		return relativeStartDate;
	}

	public void setRelativeStartDate(int relativeStartDate) {
		this.relativeStartDate = relativeStartDate;
	}

	public int getRelativeEndDate() {
		return relativeEndDate;
	}

	public void setRelativeEndDate(int relativeEndDate) {
		this.relativeEndDate = relativeEndDate;
	}

	public String getSnapToWeekMonthYear() {
		return snapToWeekMonthYear;
	}

	public void setSnapToWeekMonthYear(String snapToWeekMonthYear) {
		this.snapToWeekMonthYear = snapToWeekMonthYear;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getEpic() {
		return epic;
	}

	public void setEpic(String epic) {
		this.epic = epic;
	}

	public String getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getOrderposition() {
		return orderposition;
	}

	public void setOrderposition(String orderposition) {
		this.orderposition = orderposition;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	
	@XmlTransient
	public String getSearchString() {
		StringBuffer buf = new StringBuffer();
		boolean hasContent = false;
		if (!StringUtils.isEmpty(getProject())) {
			if (hasContent) {
				buf.append(" and ");
			}
			buf.append("project = ").append("\"").append(getProject()).append("\"");
			hasContent = true;
		}
		if (!StringUtils.isEmpty(getDateOffsetUnit())&&getRelativeStartDate()!=0) {
			Calendar c = Calendar.getInstance();
			if(getDateOffsetUnit().equalsIgnoreCase("year")) {
				c.add(Calendar.YEAR, -getRelativeStartDate());
			} else if(getDateOffsetUnit().equalsIgnoreCase("month")) {
				c.add(Calendar.MONTH, -getRelativeStartDate());
			} else {
				c.add(Calendar.DAY_OF_MONTH, -getRelativeStartDate());
			}
			if (hasContent) {
				buf.append(" and ");
			}
			if (!StringUtils.isEmpty(getSnapToWeekMonthYear())){
				if(getSnapToWeekMonthYear().equalsIgnoreCase("week")) {
					while(c.get(Calendar.DAY_OF_WEEK)!=Calendar.MONDAY) {
						c.add(Calendar.DAY_OF_MONTH, -1);
					}
				} else if(getSnapToWeekMonthYear().equalsIgnoreCase("month")) {
					c.set(Calendar.DAY_OF_MONTH, 1);
				} else if(getSnapToWeekMonthYear().equalsIgnoreCase("year")) {
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.MONTH, 0);
				}
			}
			buf.append("worklogdate >= \"").append(CalendarUtils.getStringToCalendarForREST(c)).append("\"");
			hasContent = true;
		}
		if (!StringUtils.isEmpty(getDateOffsetUnit())&&getRelativeEndDate()!=0) {
			Calendar c = Calendar.getInstance();
			if(getDateOffsetUnit().equalsIgnoreCase("year")) {
				c.add(Calendar.YEAR, -getRelativeEndDate());	
			} else if(getDateOffsetUnit().equalsIgnoreCase("month")) {
				c.add(Calendar.MONTH, -getRelativeEndDate());	
			}else {
				c.add(Calendar.DAY_OF_MONTH, -getRelativeEndDate());	
			}
			if (hasContent) {
				buf.append(" and ");
			}
			if (!StringUtils.isEmpty(getSnapToWeekMonthYear())){
				if(getSnapToWeekMonthYear().equalsIgnoreCase("week")) {
					while(c.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY) {
						c.add(Calendar.DAY_OF_MONTH, +1);
					}
				} else if(getSnapToWeekMonthYear().equalsIgnoreCase("month")) {
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.add(Calendar.MONTH, 1);
					c.add(Calendar.DAY_OF_MONTH, -1);
				} else if(getSnapToWeekMonthYear().equalsIgnoreCase("year")) {
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.MONTH, 11);
					c.add(Calendar.YEAR, 1);
					c.add(Calendar.DAY_OF_MONTH, -1);
				}
			}
			buf.append("worklogdate <= \"").append(CalendarUtils.getStringToCalendarForREST(c)).append("\"");
			hasContent = true;
		}
		if (!StringUtils.isEmpty(getUser())) {
			if (hasContent) {
				buf.append(" and ");
			}
			buf.append("worklogauthor = \"").append(getUser()).append("\"");
			hasContent = true;
		}
		if (!StringUtils.isEmpty(getEpic())) {
			if (hasContent) {
				buf.append(" and ");
			}
			String epicKey = getEpic();
			epicKey = epicKey.substring(0, epicKey.indexOf(" - "));
			buf.append("\"Epic Link\" = \"").append(epicKey).append("\"");
			hasContent = true;
		}
		if (!StringUtils.isEmpty(getOrdernumber())) {
			if (hasContent) {
				buf.append(" and ");
			}
			buf.append("cf[10030] ~ \"").append(getOrdernumber()).append("\"");
			hasContent = true;
		}
		if (!StringUtils.isEmpty(getOrderposition())) {
			if (hasContent) {
				buf.append(" and ");
			}
			buf.append("cf[10031] ~ \"").append(getOrderposition()).append("\"");
			hasContent = true;
		}
		// mandatory content
		if (hasContent) {
			buf.append(" and ");
		}
		buf.append("timespent > 0");
		hasContent = true;
		return buf.toString();
	}
}
