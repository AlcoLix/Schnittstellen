package Jira;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AureaMapping {

	private static ArrayList<Employee> employees; 
	private static ArrayList<Customer> customers; 
	
	public static String getCustomerNumber(String searchString) {
		if(customers == null) {
			init();
		}
		for (Customer customer : customers) {
			String[] split = customer.getJira().split(",");
			for (int i = 0; i < split.length; i++) {
				if(split[i].equalsIgnoreCase(searchString)) {
					return customer.getExtSchlüssel();
				}
			}
		}
		return "";
	}
	/**
	 * 
	 * @param searchString the JiraID of the employee
	 * @return the Aurea ID
	 */
	public static String getEmployeeNumber(String searchString) {
		if(employees==null) {
			init();
		}
		for (Employee employee : employees) {
			if(employee.getJira().equalsIgnoreCase(searchString)) {
				return employee.getKPSerNo();
			}
		}
		return "";
	}
	/**
	 * 
	 * @param searchString the JiraID of the employee
	 * @return the Aurea ID
	 */
	public static String getEmployeeName(String searchString) {
		if(employees==null) {
			init();
		}
		for (Employee employee : employees) {
			if(employee.getJira().equalsIgnoreCase(searchString)) {
				return employee.getNachname()+" "+employee.getVorname();
			}
		}
		return "";
	}
	
	private static void init() {
		try {
			customers = new ArrayList<Customer>();
			employees = new ArrayList<Employee>();
			BufferedReader reader = new BufferedReader(new  FileReader("Firmen_Mapping.csv"));
			String line = reader.readLine();
			//First line is the header and can be skipped
			while((line = reader.readLine())!=null) {
				customers.add(new Customer(line));
			}
			reader.close();
			reader = new BufferedReader(new FileReader("Mitarbeiter_Mapping.csv"));
			line = reader.readLine();
			//First line is the header and can be skipped
			while((line = reader.readLine())!=null) {
				employees.add(new Employee(line));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static class Employee{
		private String Vorname;
		private String Nachname;
		private String KPSerNo;
		private String Jira;
		private Employee(String csvLine) {
			String[] values = csvLine.split(";");
			setVorname(values[0]);
			setNachname(values[1]);
			setKPSerNo(values[2]);
			setJira(values[3]);
		}
		public String getVorname() {
			return Vorname;
		}
		public void setVorname(String vorname) {
			Vorname = vorname;
		}
		public String getNachname() {
			return Nachname;
		}
		public void setNachname(String nachname) {
			Nachname = nachname;
		}
		public String getKPSerNo() {
			return KPSerNo;
		}
		public void setKPSerNo(String kPSerNo) {
			KPSerNo = kPSerNo;
		}
		public String getJira() {
			return Jira;
		}
		public void setJira(String jira) {
			Jira = jira;
		}
	}
	private static class Customer{
		private String Firma;
		private String Kundennummer;
		private String StaNo;
		private String FirmenNr;
		private String ExtSystem;
		private String ExtSchlüssel;
		private String Jira;
		private Customer(String csvLine) {
			String[] values = csvLine.split(";");
			setFirma(values[0]);
			setKundennummer(values[1]);
			setStaNo(values[2]);
			setFirmenNr(values[3]);
			setExtSystem(values[4]);
			setExtSchlüssel(values[5]);
			if(values.length>6) {
				setJira(values[6]);
			} else {
				setJira("");
			}
		}
		public String getFirma() {
			return Firma;
		}
		public void setFirma(String firma) {
			Firma = firma;
		}
		public String getKundennummer() {
			return Kundennummer;
		}
		public void setKundennummer(String kundennummer) {
			Kundennummer = kundennummer;
		}
		public String getStaNo() {
			return StaNo;
		}
		public void setStaNo(String staNo) {
			StaNo = staNo;
		}
		public String getFirmenNr() {
			return FirmenNr;
		}
		public void setFirmenNr(String firmenNr) {
			FirmenNr = firmenNr;
		}
		public String getExtSystem() {
			return ExtSystem;
		}
		public void setExtSystem(String extSystem) {
			ExtSystem = extSystem;
		}
		public String getExtSchlüssel() {
			return ExtSchlüssel;
		}
		public void setExtSchlüssel(String extSchlüssel) {
			ExtSchlüssel = extSchlüssel;
		}
		public String getJira() {
			return Jira;
		}
		public void setJira(String jira) {
			Jira = jira;
		}
	}
}
