package Jira;

import java.util.Date;

public class Task {

	private Date dueDate;
	private Date plannedDate;
	private String summary;
	private String assignee;
	private String responsible;
	private String timeSpent;
	private long timeSpentSeconds;
	private String timeEstimate;
	private long timeEstmateSeconds;
	private String timeEstimateRemaining;
	private long timeEstmateRemainingSeconds;
	private String issueKey;
	private boolean billable;
	private String ordernumber;
	private String orderposition;
	private String parent;
	private String epic;
	private String project;
	private String customer;
	private String issueType;
	
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public Date getPlannedDate() {
		return plannedDate;
	}
	public void setPlannedDate(Date plannedDate) {
		this.plannedDate = plannedDate;
	}
	public String getSummary() {
		return summary;
	}
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public String getResponsible() {
		return responsible;
	}
	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getTimeEstimate() {
		return timeEstimate;
	}
	public void setTimeEstimate(String timeEstimate) {
		this.timeEstimate = timeEstimate;
	}
	public long getTimeEstmateSeconds() {
		return timeEstmateSeconds;
	}
	public void setTimeEstmateSeconds(long timeEstmateSeconds) {
		this.timeEstmateSeconds = timeEstmateSeconds;
	}
	public String getTimeEstimateRemaining() {
		return timeEstimateRemaining;
	}
	public void setTimeEstimateRemaining(String timeEstimateRemaining) {
		this.timeEstimateRemaining = timeEstimateRemaining;
	}
	public long getTimeEstmateRemainingSeconds() {
		return timeEstmateRemainingSeconds;
	}
	public void setTimeEstmateRemainingSeconds(long timeEstmateRemainingSeconds) {
		this.timeEstmateRemainingSeconds = timeEstmateRemainingSeconds;
	}
	public String getTimeSpent() {
		return timeSpent;
	}
	public void setTimeSpent(String timeSpent) {
		this.timeSpent = timeSpent;
	}
	public long getTimeSpentSeconds() {
		return timeSpentSeconds;
	}
	public void setTimeSpentSeconds(long timeSpentSeconds) {
		this.timeSpentSeconds = timeSpentSeconds;
	}
	public String getIssueKey() {
		return issueKey;
	}
	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
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
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getEpic() {
		return epic;
	}
	public void setEpic(String epic) {
		this.epic = epic;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public boolean isBillable() {
		return billable;
	}
	public void setBillable(boolean billable) {
		this.billable = billable;
	}
	public String getCustomer() {
		return customer;
	}
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	public String getIssueType() {
		return issueType;
	}
	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}
}
