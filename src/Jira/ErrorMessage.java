package Jira;

import java.util.ArrayList;

public class ErrorMessage {
	private final String error;
	private final AureaWorklog worklog;
	private static ArrayList<ErrorMessage> errors;
	
	public static void addError(String error, AureaWorklog worklog) {
		getErrorList().add(new ErrorMessage(error, worklog));
	}
	public static ArrayList<ErrorMessage> getErrorList(){
		if (errors == null) {
			errors = new ArrayList<ErrorMessage>();
		}
		return errors;
	}
	private ErrorMessage(String error, AureaWorklog worklog) {
		this.error = error;
		this.worklog = worklog;
	}
	public AureaWorklog getWorklog() {
		return worklog;
	}
	public String getError() {
		return error;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Fehler: ").append(getError()).append("\r\n");
		buf.append("Worklog: ").append(getWorklog().toString()).append("\r\n");
		return buf.toString();
	}
	public static String getErrorsAsString() {
		StringBuffer buf = new StringBuffer();
		for (ErrorMessage error : errors) {
			buf.append(error.getWorklog().getIssueKey()).append(":\r\n");
			buf.append(error.toString());
		}
		return buf.toString();
	}
}
