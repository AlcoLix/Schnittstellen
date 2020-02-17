package Jira.utils;

import java.util.Calendar;
import java.util.Date;

public class CalendarUtils {
	public static long getDifferenceIn(Calendar c1, Calendar c2, int field) {
		long millis = Math.abs(c2.getTimeInMillis()-c1.getTimeInMillis());
		return truncMillisTo(millis, field);
	}
	public static long getDifferenceIn(Date d1, Date d2, int field) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		return getDifferenceIn(c1, c2, field); 
	}
	public static String getDifference(long millis1, long millis2){
		long millis = Math.abs(millis1-millis2);
		int time = (int)(millis / 1000);
		return getStringToTimeInt(time,Calendar.SECOND);
	}

	public static long truncMillisTo(long millis, int field){
		switch(field){
		case Calendar.DAY_OF_MONTH:
		case Calendar.DAY_OF_YEAR:
			millis/=24;
		case Calendar.HOUR:
		case Calendar.HOUR_OF_DAY:
			millis/=60;
		case Calendar.MINUTE:
			millis/=60;
		case Calendar.SECOND:
			millis/=1000;
		}
		return millis;
	}
	/**
	 * increases the time and adjustes it in case the hour must be switched
	 * @param time The time must be in the format hh:mm
	 * @param amount The amount to be increased or decreased (negative in this case) in minutes
	 * @return the new time String
	 */
	public static String incrementTimeString(String time, int amount){
		String[] parts = time.split(":");
		int hours = Integer.parseInt(parts[0]);
		int minutes = Integer.parseInt(parts[1]);
		minutes += amount;
		while(minutes<0){
			minutes+=60;
			hours-=1;
		}
		while(minutes>=60){
			minutes-=60;
			hours+=1;
		}
		while(hours<0){
			hours+=24;
		}
		while(hours>23){
			hours-=24;
		}
		if(minutes<10){
			return hours+":0"+minutes;	
		}
		return hours+":"+minutes;
	}
	
	public static Date getNowAsDate(){
		return Calendar.getInstance().getTime();
	}
	
	public static Calendar getTimedAnniversary(Calendar c, int amount, int field) {
		Calendar val = (Calendar) c.clone();
		val.add(field, amount);
		return val;
	}
	public static String getStringToDateMonthWord(Date d, boolean year, boolean month,
			boolean day, boolean hour, boolean minute, boolean second, boolean millis){
		Calendar c = Calendar.getInstance();
		if(d != null){
			c.setTime(d);
		}
		return getStringToCalendarMonthWord(c, year, month, day, hour, minute, second, millis);
	}
	public static String getStringToCalendarMonthWord(Calendar c, boolean year, boolean month,
			boolean day, boolean hour, boolean minute, boolean second, boolean millis){
		if(c == null){
			c = Calendar.getInstance();
		}
		StringBuffer buf = new StringBuffer();
		if (day) {
			int d = c.get(Calendar.DAY_OF_MONTH);
			if(d<10){
				buf.append("0");
			}
			buf.append(d).append(".");
		}
		if (month) {
			int m = c.get(Calendar.MONTH) + 1;
			switch(m){
			case 1:
				buf.append("Januar");
				break;
			case 2:
				buf.append("Februar");
				break;
			case 3:
				buf.append("März");
				break;
			case 4:
				buf.append("April");
				break;
			case 5:
				buf.append("Mai");
				break;
			case 6:
				buf.append("Juni");
				break;
			case 7:
				buf.append("Juli");
				break;
			case 8:
				buf.append("August");
				break;
			case 9:
				buf.append("September");
				break;
			case 10:
				buf.append("Oktober");
				break;
			case 11:
				buf.append("November");
				break;
			case 12:
				buf.append("Dezember");
				break;
			}
			buf.append(" ");
		}
		if (year) {
			buf.append(c.get(Calendar.YEAR));
		}
		if (hour) {
			if (buf.length() > 0) {
				buf.append(" \t");
			}
			int h = c.get(Calendar.HOUR_OF_DAY);
			if(h<10){
				buf.append("0");
			}
			buf.append(h);
		}
		if (minute) {
			if (buf.length() > 0 && !hour) {
				buf.append(" \t");
			}
			if (hour) {
				buf.append(":");
			}
			int m = c.get(Calendar.MINUTE);
			if(m<10){
				buf.append("0");
			}
			buf.append(m);
		}
		if (second) {
			if (buf.length() > 0 && !hour && !minute) {
				buf.append(" \t");
			}
			if (hour || minute) {
				buf.append(":");
			}
			int s = c.get(Calendar.SECOND);
			if(s<10){
				buf.append("0");
			}
			buf.append(s);
		}
		if(millis){
			if (buf.length() > 0 && !hour && !minute &&! second) {
				buf.append(" \t");
			}
			if (second) {
				buf.append(".");
			}
			int m = c.get(Calendar.MILLISECOND);
			if(m<10){
				buf.append("000");
			}else if(m<100){
				buf.append("00");
			}else if(m<1000){
				buf.append("0");
			}
			buf.append(m);
		}
		return buf.toString();
	}
	public static String getStringToDate(Date d, boolean year, boolean month,
			boolean day, boolean hour, boolean minute, boolean second, boolean millis) {
		Calendar c = Calendar.getInstance();
		if(d!=null){
			c.setTime(d);
		}
		return getStringToCalendar(c, year, month, day, hour, minute, second, millis);
	}
	public static Date getDateToString(String s){
		if(StringUtils.isEmpty(s.trim())){
			return startOfDay(Calendar.getInstance()).getTime();
		}
		Calendar c = startOfDay(Calendar.getInstance());
		c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(("0"+s.substring(0,2)).trim()));
		c.set(Calendar.MONTH, Integer.parseInt(("0"+s.substring(3,5)).trim()+1));
		c.set(Calendar.YEAR, Integer.parseInt(("20"+s.substring(6)).trim()));
		if(c.after(Calendar.getInstance())){
			c.roll(Calendar.YEAR, -100);
		}
		return c.getTime();
	}
	public static Date getDateToYear(String s){
		if(StringUtils.isEmpty(s.trim())){
			return startOfDay(Calendar.getInstance()).getTime();
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 5);
		c.set(Integer.parseInt(s), 0, 1, 0, 0, 0);
		return c.getTime();
	}
	public static String getStringToCalendar(Calendar c, boolean year, boolean month,
			boolean day, boolean hour, boolean minute, boolean second, boolean millis) {
		if(c == null){
			c = Calendar.getInstance();
		}
		StringBuffer buf = new StringBuffer();
		if (day) {
			int d = c.get(Calendar.DAY_OF_MONTH);
			if(d<10){
				buf.append("0");
			}
			buf.append(d).append(".");
		}
		if (month) {
			int m = c.get(Calendar.MONTH) + 1;
			if(m<10){
				buf.append("0");
			}
			buf.append(m).append(".");
		}
		if (year) {
			buf.append(c.get(Calendar.YEAR));
		}
		if (hour) {
			if (buf.length() > 0) {
				buf.append(" \t");
			}
			int h = c.get(Calendar.HOUR_OF_DAY);
			if(h<10){
				buf.append("0");
			}
			buf.append(h);
		}
		if (minute) {
			if (buf.length() > 0 && !hour) {
				buf.append(" \t");
			}
			if (hour) {
				buf.append(":");
			}
			int m = c.get(Calendar.MINUTE);
			if(m<10){
				buf.append("0");
			}
			buf.append(m);
		}
		if (second) {
			if (buf.length() > 0 && !hour && !minute) {
				buf.append(" \t");
			}
			if (hour || minute) {
				buf.append(":");
			}
			int s = c.get(Calendar.SECOND);
			if(s<10){
				buf.append("0");
			}
			buf.append(s);
		}
		if(millis){
			if (buf.length() > 0 && !hour && !minute &&! second) {
				buf.append(" \t");
			}
			if (second) {
				buf.append(".");
			}
			int m = c.get(Calendar.MILLISECOND);
			if(m<10){
				buf.append("000");
			}else if(m<100){
				buf.append("00");
			}else if(m<1000){
				buf.append("0");
			}
			buf.append(m);
		}
		return buf.toString();
	}
	
	public static String getStringForDB(Date d, boolean time){
		Calendar c = Calendar.getInstance();
		if(d!=null){
			c.setTime(d);
		}
		return getStringForDB(c, time);
	}
	
	public static String getStringForDB(Calendar c, boolean time){
		StringBuffer buf = new StringBuffer();
		int month = (c.get(Calendar.MONTH)+1);
		int day = c.get(Calendar.DAY_OF_MONTH);
		buf.append(c.get(Calendar.YEAR)).append("-");
		if(month<10){
			buf.append("0");
		}
		buf.append(month).append("-");
		if(day<10){
			buf.append("0");
		}
		buf.append(day);
		if(buf.toString().equals("20-1-31")){
			//Catching some strange error...
			System.out.println(buf);
		}
		if(c.get(Calendar.HOUR_OF_DAY)>0){
			//Seems like we have time, too!
//			if(c.get(Calendar.HOUR_OF_DAY)==12&&c.get(Calendar.MINUTE)==0&&c.get(Calendar.SECOND)==0){
//				//Nah, a direct 12:00:00 is fishy
//			}else{
				buf.append(" ");
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int minutes = c.get(Calendar.MINUTE);
				if(hour<10){
					buf.append("0");
				}
				buf.append(hour).append(":");
				if(minutes<10){
					buf.append("0");
				}
				buf.append(minutes).append(":00");
//			}
		}
		return buf.toString();
	}
	
	public static String getStringToField(Calendar c, int field, int minLength){
		StringBuffer buf = new StringBuffer();
		int i = c.get(field);
		if(field == Calendar.MONTH){
			i++;
		}
		buf.append(i);
		while(buf.length()<minLength){
			buf.insert(0, 0);
		}
		return buf.toString();
	}

	/**
	 * @param time
	 *            The time as int.
	 * @param field
	 *            The Calendar field representing the form of the given time. <br>
	 *            Ranges from day to millisecond
	 * @return A String representing the given time in a user friendly form down
	 *         to milliseconds
	 */
	public static String getStringToTimeInt(long time, int field) {
		return getStringToTimeInt((int)time, field);
	}

	/**
	 * @param time
	 *            The time as int.
	 * @param field
	 *            The Calendar field representing the form of the given time. <br>
	 *            Ranges from day to millisecond
	 * @return A String representing the given time in a user friendly form down
	 *         to milliseconds
	 */
	public static String getStringToTimeInt(int time, int field) {
		StringBuffer buf = new StringBuffer();
		long val = getTimeInMillis(time, field);
		int	millis = (int)val%1000;
		val/=1000;
		int seconds = (int)val%60;
		val/=60;
		int minutes = (int)val&60;
		val/=60;
		int hours = (int)val%24;
		val/=24;
		int days = (int)val;
		if(days>0){
			buf.append(days).append(" Tage ");
		}
		if(hours>0||buf.length()>0){
			buf.append(hours).append(" Stunden ");
		}
		if(minutes>0||buf.length()>0){
			buf.append(minutes).append(" Minuten ");
		}
		if(seconds>0||millis>0){
			buf.append(seconds).append(" Sekunden ");
		}
		if(millis>0&&buf.length()>0){
			buf.append(millis).append(" Millisekunden ");
		}
		return buf.toString();
	}
	/**
	 * @param time
	 *            The time as int.
	 * @param field
	 *            The Calendar field representing the form of the given time. <br>
	 *            Ranges from day to millisecond
	 * @return The time in milliseconds
	 */
	public static long getTimeInMillis(int time, int field){
		long val = 0;
		val = time;
		switch (field) {
		case Calendar.DAY_OF_MONTH:
		case Calendar.DAY_OF_YEAR:
			val *= 24;
		case Calendar.HOUR_OF_DAY:
		case Calendar.HOUR:
			val *= 60;
		case Calendar.MINUTE:
			val *= 60;
		case Calendar.SECOND:
			val *= 1000;
		}
		return val;
	}

	public static boolean isToday(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return isToday(c);
	}

	public static boolean isToday(Calendar c) {
		Calendar now = Calendar.getInstance();
		if(now.get(Calendar.YEAR)!=c.get(Calendar.YEAR)){
			return false;
		}
		if(now.get(Calendar.MONTH)!=c.get(Calendar.MONTH)){
			return false;
		}
		if(now.get(Calendar.DAY_OF_MONTH)!=c.get(Calendar.DAY_OF_MONTH)){
			return false;
		}
		return true;
	}

	public static Date startOfDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return startOfDay(c).getTime();
	}
	public static Calendar startOfDay(Calendar c) {
		c.set(Calendar.MILLISECOND, 5);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR, 0);
		return c;
	}

	public static Date endOfDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return endOfDay(c).getTime();
	}
	public static Calendar endOfDay(Calendar c) {
		c.set(Calendar.MILLISECOND, 995);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.HOUR, 23);
		return c;
	}

	public static boolean isBefore(Date d, Date comparator) {
		Calendar c2 = Calendar.getInstance();
		c2.setTime(comparator);
		return isBefore(d, comparator);
	}
	public static boolean isBefore(Date d, Calendar comparator) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.before(comparator);
	}
	/**
	 * 
	 * @param d
	 * @param comparator
	 * @return true if d is later than comparator
	 */
	public static boolean isAfter(Date d, Date comparator) {
		Calendar c2 = Calendar.getInstance();
		c2.setTime(comparator);
		return isAfter(d, c2);
	}
	/**
	 * 
	 * @param d
	 * @param comparator
	 * @return true if d is later than comparator
	 */
	public static boolean isAfter(Date d, Calendar comparator) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.after(comparator);
	}
	public static boolean areDifferentDayAndNotNull(Date... d){
		boolean val = false;
		Date compare = null;
		for (int i = 0; i < d.length; i++) {
			if(compare==null){
				compare = d[i];
				val = true;
			}
		}
		return val;
	}
	public static boolean isSameDay(Date d1, Date d2){
		Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);
		Calendar c2 = Calendar.getInstance();
		c2.setTime(d2);
		return isSameDay(c1, c2);
	}
	public static boolean isSameDay(Calendar c1, Calendar c2){
		c1 = startOfDay(c1);
		c2 = startOfDay(c2);
		return Math.abs(c1.getTimeInMillis()-c2.getTimeInMillis())<60000;
	}
}
