package Jira;

import java.util.Date;

import Jira.utils.StringUtils;

public class AureaWorklog {
	private String worklogID;
	private String user;
	private String userID;
	private String customer;
	private String customerID;
	private String ordernumber;
	private String orderposition;
	private Date date;
	private String paymentType;
	private String paymentMethod;
	private Date startTime;
	private Date endTime;
	private String issueKey;
	private String comment;
	private String summary;
	private String project;
	private String team;
	private String timeSpent;
	private long timeSpentSeconds;
	private boolean billable;
	private String parent;
	private String epic;
	private Date create;
	private Date update;
	
	public Date getCreate() {
		return create;
	}
	public void setCreate(Date create) {
		this.create = create;
	}
	public Date getUpdate() {
		return update;
	}
	public void setUpdate(Date update) {
		this.update = update;
	}
	public String getWorklogID() {
		return worklogID;
	}
	public void setWorklogID(String worklogID) {
		this.worklogID = worklogID;
	}
	
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
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
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getCustomer() {
		return customer;
	}
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	public String getComment() {
		return comment;
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
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
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
	public boolean isBillable() {
		return billable;
	}
	public void setBillable(boolean billable) {
		this.billable = billable;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getCustomerID() {
		return customerID;
	}
	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}
	public String getDisplayText() {
		if(StringUtils.isEmpty(getComment())) {
			return getSummary();
		}
		return getSummary()+"\r\n"+getComment();
	}
}
