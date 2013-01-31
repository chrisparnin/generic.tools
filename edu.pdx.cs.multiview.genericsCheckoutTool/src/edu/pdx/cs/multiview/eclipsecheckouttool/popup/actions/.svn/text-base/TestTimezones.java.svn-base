package edu.pdx.cs.multiview.eclipsecheckouttool.popup.actions;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TestTimezones {

	public static void main(String[] args) {
		Timestamp predaylightSavings = new Timestamp(1172625369000l);//2007-02-27 17:16:09.0
		
		test(predaylightSavings);
		
		int oneMonthInMillis = 1000*60*60*24*50000000;
		Timestamp postDaylightSavings = new Timestamp(1172625369000l+oneMonthInMillis);//2007-03-18 16:19:27.912
		
		test(postDaylightSavings);
	}

	private static void test(Timestamp fromDb) {
		Timestamp old = new Timestamp(fromDb.getTime()-60*60*1000*8);
		Timestamp nu = localTime(fromDb);
		 
		System.out.println("original:       "+fromDb);
		System.out.println("old conversion: "+old);
		System.out.println("new conversion: "+nu);
		System.out.println("equal? "+old.compareTo(nu));
	}
	
	/*
	 * Take a timestamp in UTC and convert it into Pacific Time
	 * (subtract 8 or 7 hours, depending on daylight savings
	 */
	private static Timestamp localTime(Timestamp timeInUTC){
		
		
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(timeInUTC.getTime());
		 cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		 return new Timestamp(cal.getTimeInMillis()+
		   cal.get(Calendar.DST_OFFSET) + 
		   cal.get(Calendar.ZONE_OFFSET));
		
		
//		System.out.println(timeInUTC);
//		
//		System.out.println(timeInUTC);
//		Calendar gc = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//		gc.setTimeZone(TimeZone.getTimeZone("GMT"));
//		gc.setTimeInMillis(timeInUTC.getTime());
//		
//		System.out.println(gc.getTime());
//		
//		return timeInUTC;
//		
//		gc.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
//		
//		System.out.println(gc.get(Calendar.HOUR_OF_DAY));
//		
//		return timeInUTC;
		
//		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, Locale.ENGLISH);
//		//SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
//		format.setTimeZone(TimeZone.getTimeZone("GMT"));		
//		String stringVal = format.format(timeInUTC);
//		
//		System.out.println(timeInUTC);
//		System.out.println(stringVal);
//		
//		return Timestamp.valueOf(stringVal);
		

		
		
//		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST"));
//		calendar.setTimeInMillis(timeInUTC.getTime());
//		//calendar.setTimeZone(TimeZone.getDefault());		
//		return new Timestamp(calendar.getTimeInMillis());
	}
}
