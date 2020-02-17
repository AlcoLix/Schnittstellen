package Jira.utils;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

public class StringUtils {
	private static Font[] fonts;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static String pad(int side, int resultingLength, String padding, String s){
		StringBuffer buf = new StringBuffer(s);
		while(buf.length()<resultingLength){
			if(side == LEFT){
				buf.insert(0, padding);
			}else if(side == RIGHT){
				buf.append(padding);
			}
		}
		return buf.toString();
	}
	public static String makeCapital(String s){
		StringBuffer buf = new StringBuffer(s);
		buf.setCharAt(0, java.lang.Character.toUpperCase(buf.charAt(0)));
		return buf.toString();
	}

	public static boolean isEmpty(String s) {
		if(s==null){
			return true;
		}
		if(s.length()==0){
			return true;
		}
		return false;
	}
	public static boolean areEmpty(String... s){
		for (int i = 0; i < s.length; i++) {
			if(!isEmpty(s[i])){
				return false;
			}
		}
		return true;
	}
	public static boolean areNotEmpty(String... s){
		for (int i = 0; i < s.length; i++) {
			if(isEmpty(s[i])){
				return false;
			}
		}
		return true;
	}
	public static String cut(String s, int i) {
		if(s.length()<=i){
			return s;
		}
		StringBuffer buf = new StringBuffer(s);
		while(buf.length()>i){
			buf.deleteCharAt(buf.length()-1);
		}
		return buf.toString();
	}
	public static int count(String where, String what) {
		int val = 0;
		int index = where.indexOf(what);
		while(index!=-1){
			val++;
			index = where.indexOf(what, index+1);
		}
		return val;
	}
	public static int secureParse(String s) {
		try{
			return Integer.parseInt(numbersOnly(s));
		} catch(Exception e){
		}
		return 0;
	}
	public static String numbersOnly(String s) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			if(s.charAt(i)==','||s.charAt(i)=='.'||(s.charAt(i)>='0'&&s.charAt(i)<='9')){
				buf.append(s.charAt(i));
			}
		}
		return buf.toString();
	}
	public static String toCurrency(String s) {
		s = numbersOnly(s);
		if(StringUtils.isEmpty(s)){
			return "0,00";
		}
		s = s.replace(".", ",");
		if(s.lastIndexOf(",")!=s.indexOf(",")&&s.indexOf(",")!=-1){
			String[] subs = s.split(",");
			for (int i = 0; i < subs.length; i++) {
				if(i == subs.length-1){
					s += ",";
				}
				s += subs[i];
			}
		}
		String val;
		if(s.indexOf(",")==-1){
			val = s+",00";
		}else{
			String[] subs = s.split(",");
			subs[0] = String.valueOf(Integer.parseInt(subs[0]));
			if(subs[1].length()==2){
				val = subs[0]+","+subs[1];
			}else if(subs[1].length()<2){
				val = subs[0]+","+pad(RIGHT, 2, "0", subs[1]);
			}else{
				val = subs[0]+subs[1].substring(0,subs[1].length()-2)+","+subs[1].substring(subs[1].length()-2);
			}
		}
		return val;
	}
	/**
	 * Searches the s for Occurences of filter, wildcard * is accepted
	 * @param s the String to Filter
	 * @param filter the filter String
	 * @return true if the String contains the filter
	 */
	public static boolean filter(String s, String filter) {
		char[] c = {0x2A};
		String wildcard = new String(c);
		boolean startWildcard = filter.startsWith(wildcard);
		String[] filters = filter.replace(wildcard, "wildcardssssss").split("wildcardssssss");
		ArrayList<String> f = new ArrayList<String>();
		for (int i = 0; i < filters.length; i++) {
			if(!isEmpty(filters[i])){
				f.add(filters[i]);
			}
		}
		filters = new String[f.size()];
		filters = f.toArray(filters);
		return performFilter(s, filters, startWildcard);
	}
	private static boolean performFilter(String s, String[] filters,
			boolean startWildcard) {
		if (!startWildcard){
			if(!s.toLowerCase().startsWith(filters[0].toLowerCase())){
				return false;
			}
		}
		for (int i = 0; i < filters.length; i++) {
			if(!s.toLowerCase().contains(filters[i].toLowerCase())){
				return false;
			}
		}
		return true;
	}
	public static Font[] getFonts(){
		if (fonts == null) {
			fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		}
		return fonts;
	}
	public static String[] getFontNames(){
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}
	public static Font getFontForName(String name){
		Font[] fonts = getFonts();
		for (int i = 0; i < fonts.length; i++) {
			if(fonts[i].getFamily().equals(name)){
				if(fonts[i].getStyle()==Font.PLAIN){
					return fonts[i];
				}
			}
		}
		return null;
	}
	public static int getFontStyleForText(String style) {
		if(style.toLowerCase().contains("fett")){
			if(style.toLowerCase().contains("kursiv")){
				return Font.BOLD | Font.ITALIC;
			}
			return Font.BOLD;
		}
		if(style.toLowerCase().contains("kursiv")){
			return Font.ITALIC;
		}
		return Font.PLAIN;
	}
	public static boolean endsWith(String s, String... tokens){
		for (int i = 0; i < tokens.length; i++) {
			if(s.endsWith(tokens[i])){
				return true;
			}
		}
		return false;
	}
	public static boolean endsWithIgnoreCase(String s, String... tokens){
		s = s.toLowerCase();
		for (int i = 0; i < tokens.length; i++) {
			if(s.endsWith(tokens[i].toLowerCase())){
				return true;
			}
		}
		return false;
	}

	public static boolean startsWith(String s, String... tokens){
		for (int i = 0; i < tokens.length; i++) {
			if(s.startsWith(tokens[i])){
				return true;
			}
		}
		return false;
	}
	public static boolean startsWithIgnoreCase(String s, String... tokens){
		s = s.toLowerCase();
		for (int i = 0; i < tokens.length; i++) {
			if(s.startsWith(tokens[i].toLowerCase())){
				return true;
			}
		}
		return false;
	}
	public static boolean containsAny(String s, String... tokens){
		for (int i = 0; i < tokens.length; i++) {
			if(s.contains(tokens[i])){
				return true;
			}
		}
		return false;
	}

	public static boolean containsAnyIgnoreCase(String s, String... tokens){
		s = s.toLowerCase();
		for (int i = 0; i < tokens.length; i++) {
			if(s.contains(tokens[i].toLowerCase())){
				return true;
			}
		}
		return false;
	}
	/**
	 * 
	 * @param compare
	 * @param cases
	 * @return false if any value of cases is equal to compare (case sensitive, trimmed), true otherwise
	 */
	public static boolean notIn(String compare, String... cases) {
		for (int i = 0; i < cases.length; i++) {
			if(cases[i].trim().equals(compare.trim())){
				return false;
			}
		}
		return true;
	}
	/**
	 * 
	 * @param compare
	 * @param cases
	 * @return true if any value of cases is equal to compare (case sensitive, trimmed), false otherwise
	 */
	public static boolean in(String compare, String... cases) {
		for (int i = 0; i < cases.length; i++) {
			if(cases[i].trim().equals(compare.trim())){
				return true;
			}
		}
		return false;
	}
}