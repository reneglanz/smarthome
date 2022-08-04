package de.core.task;

import de.core.CoreException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CronPattern {
	private static Map<String, Integer> dayAlias = new HashMap<>();

	private static Map<String, Integer> monthAlias = new HashMap<>();

	Matcher minute;

	Matcher hour;

	Matcher day;

	Matcher month;

	Matcher weekday;

	static {
		dayAlias.put("SUN", Integer.valueOf(1));
		dayAlias.put("MON", Integer.valueOf(2));
		dayAlias.put("TUE", Integer.valueOf(3));
		dayAlias.put("WED", Integer.valueOf(4));
		dayAlias.put("THU", Integer.valueOf(5));
		dayAlias.put("FRI", Integer.valueOf(6));
		dayAlias.put("SAT", Integer.valueOf(7));
		monthAlias.put("JAN", Integer.valueOf(0));
		monthAlias.put("FEB", Integer.valueOf(1));
		monthAlias.put("MAR", Integer.valueOf(2));
		monthAlias.put("APR", Integer.valueOf(3));
		monthAlias.put("MAY", Integer.valueOf(4));
		monthAlias.put("JUN", Integer.valueOf(5));
		monthAlias.put("JUL", Integer.valueOf(6));
		monthAlias.put("AUG", Integer.valueOf(7));
		monthAlias.put("SEP", Integer.valueOf(8));
		monthAlias.put("OCT", Integer.valueOf(9));
		monthAlias.put("NOV", Integer.valueOf(10));
		monthAlias.put("DEC", Integer.valueOf(11));
	}

	private static class Parser {
		public static final String RANGE = "-";

		public static final String EVERY = "/";

		public static final String LIST = ",";

		public static final int[] MAX = new int[] { 59, 23, 31, 12, 7 };

		public static CronPattern parse(String pattern) throws CoreException {
			CronPattern result = new CronPattern();
			String[] token = pattern.split(" ");
			if (token.length == 5) {
				result.minute = parseToken(token[0], 0);
				result.hour = parseToken(token[1], 1);
				result.day = parseToken(token[2], 2);
				result.month = parseToken(token[3], 3);
				result.weekday = parseToken(token[4], 4);
			} else {
				CoreException.throwCoreException("Invalid cron pattern" + pattern);
			}
			return result;
		}

		private static CronPattern.Matcher parseToken(String token, int index) throws CoreException {
			int iEvery = token.indexOf(EVERY);
			int iRange = token.indexOf(RANGE);
			int iList = token.indexOf(LIST);
			try {
				if ("*".equals(token))
					return new CronPattern.AllMatcher();
				if (iEvery != -1 && iEvery != token.length()) {
					String value = token.substring(iEvery + 1, token.length());
					int ivalue = CronPattern.getValue(index, value);
					List<Integer> list = new ArrayList<>();
					int i;
					for (i = 0; i < MAX[index]; i += ivalue)
						list.add(Integer.valueOf(i));
					return new CronPattern.ArrayMatcher(list);
				}
				if (iRange != -1 && iRange != token.length()) {
					int start = CronPattern.getValue(index, token.substring(0, iRange));
					int end = CronPattern.getValue(index, token.substring(iRange + 1, token.length()));
					List<Integer> list = new ArrayList<>();
					for (int i = start; i <= end && i < MAX[index]; i++)
						list.add(Integer.valueOf(i));
					return new CronPattern.ArrayMatcher(list);
				}
				if (iList != -1 && iList != token.length()) {
					String[] entries = token.split(",");
					List<Integer> list = new ArrayList<>();
					for (int i = 0; i < entries.length; i++)
						list.add(Integer.valueOf(CronPattern.getValue(index, entries[i])));
					return new CronPattern.ArrayMatcher(list);
				}
				return new CronPattern.ArrayMatcher(
						Collections.singletonList(Integer.valueOf(CronPattern.getValue(index, token))));
			} catch (NumberFormatException nfe) {
				CoreException.throwCoreException("Invalid token for index " + index);
				return null;
			}
		}
	}

	public static int getValue(int index, String value) {
		Integer val = null;
		if (index == 4) {
			val = dayAlias.get(value);
		} else if (index == 3) {
			val = monthAlias.get(value);
		}
		if (val == null)
			return Integer.parseInt(value);
		return val.intValue();
	}

	public static interface Matcher {
		boolean match(int param1Int);
	}

	public static class AllMatcher implements Matcher {
		public boolean match(int value) {
			return true;
		}
	}

	public static class ArrayMatcher implements Matcher {
		List<Integer> values;

		public ArrayMatcher(List<Integer> values) {
			this.values = values;
		}

		public boolean match(int value) {
			for (Iterator<Integer> iterator = this.values.iterator(); iterator.hasNext();) {
				int i = ((Integer) iterator.next()).intValue();
				if (i == value)
					return true;
			}
			return false;
		}
	}

	public long next() {
		long now = System.currentTimeMillis() + 60000L;
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(now);
		cal.set(13, 0);
		int min = cal.get(12);
		int h = cal.get(11);
		int d = cal.get(5);
		int month = cal.get(2);
		int y = cal.get(1);
		if (match(now))
			return cal.getTimeInMillis();
		while (true) {
			while (this.minute.match(min)) {
				if (h > 23) {
					h = 0;
					d++;
				}
				if (this.hour.match(h)) {
					if (d > 31) {
						d = 1;
						month++;
					}
					if (month > 11) {
						month = 0;
						y++;
						continue;
					}
					if (this.day.match(d)) {
						if (this.month.match(month)) {
							cal.set(12, min);
							cal.set(11, h);
							cal.set(5, d);
							cal.set(2, month);
							cal.set(1, y);
							cal.set(13, 0);
							int d2 = cal.get(7);
							if (!this.weekday.match(d2)) {
								d++;
								h = 0;
								min = 0;
								if (d > 31) {
									d = 1;
									month++;
									if (month > 11) {
										month = 0;
										y++;
									}
								}
								continue;
							}
							return cal.getTimeInMillis();
						}
						month++;
						d = 1;
						h = 0;
						min = 0;
						continue;
					}
					d++;
					h = 0;
					min = 0;
					continue;
				}
				h++;
				min = 0;
			}
			min++;
			if (min > 59) {
				min = 0;
				h++;
			}
		}
		// return cal.getTimeInMillis();
	}

	public boolean match(long time) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		cal.set(13, 0);
		int min = cal.get(12);
		int h = cal.get(10);
		int d = cal.get(5);
		int month = cal.get(2);
		int w = cal.get(7);
		return (this.minute.match(min) && this.hour.match(h) && this.day.match(d) && this.month.match(month)
				&& this.weekday.match(w));
	}

	public static CronPattern get(String pattern) throws CoreException {
		return Parser.parse(pattern);
	}
}
