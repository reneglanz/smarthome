package de.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public final class SunriseSunset {
  public static final double SUN_ALTITUDE_SUNRISE_SUNSET = -0.833D;
  
  public static final double SUN_ALTITUDE_CIVIL_TWILIGHT = -6.0D;
  
  public static final double SUN_ALTITUDE_NAUTICAL_TWILIGHT = -12.0D;
  
  public static final double SUN_ALTITUDE_ASTRONOMICAL_TWILIGHT = -18.0D;
  
  private static final int JULIAN_DATE_2000_01_01 = 2451545;
  
  private static final double CONST_0009 = 9.0E-4D;
  
  private static final double CONST_360 = 360.0D;
  
  private static final long MILLISECONDS_IN_DAY = 86400000L;
  
  public enum DayPeriod {
    DAY, CIVIL_TWILIGHT, NAUTICAL_TWILIGHT, ASTRONOMICAL_TWILIGHT, NIGHT;
  }
  
  private static class SolarEquationVariables {
    final double n;
    
    final double m;
    
    final double lambda;
    
    final double jtransit;
    
    final double delta;
    
    private SolarEquationVariables(double n, double m, double lambda, double jtransit, double delta) {
      this.n = n;
      this.m = m;
      this.lambda = lambda;
      this.jtransit = jtransit;
      this.delta = delta;
    }
  }
  
  public static double getJulianDate(Calendar gregorianDate) {
    TimeZone tzUTC = TimeZone.getTimeZone("UTC");
    Calendar gregorianDateUTC = Calendar.getInstance(tzUTC);
    gregorianDateUTC.setTimeInMillis(gregorianDate.getTimeInMillis());
    int year = gregorianDateUTC.get(1);
    int month = gregorianDateUTC.get(2) + 1;
    int day = gregorianDateUTC.get(5);
    int a = (14 - month) / 12;
    int y = year + 4800 - a;
    int m = month + 12 * a - 3;
    int julianDay = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
    int hour = gregorianDateUTC.get(11);
    int minute = gregorianDateUTC.get(12);
    int second = gregorianDateUTC.get(13);
    return julianDay + (hour - 12.0D) / 24.0D + minute / 1440.0D + second / 86400.0D;
  }
  
  public static Calendar getGregorianDate(double julianDate) {
    int DAYS_PER_4000_YEARS = 146097;
    int DAYS_PER_CENTURY = 36524;
    int DAYS_PER_4_YEARS = 1461;
    int DAYS_PER_5_MONTHS = 153;
    int J = (int)(julianDate + 0.5D);
    int j = J + 32044;
    int g = j / 146097;
    int dg = j % 146097;
    int c = (dg / 36524 + 1) * 3 / 4;
    int dc = dg - c * 36524;
    int b = dc / 1461;
    int db = dc % 1461;
    int a = (db / 365 + 1) * 3 / 4;
    int da = db - a * 365;
    int y = g * 400 + c * 100 + b * 4 + a;
    int m = (da * 5 + 308) / 153 - 2;
    int d = da - (m + 4) * 153 / 5 + 122;
    int year = y - 4800 + (m + 2) / 12;
    int month = (m + 2) % 12;
    int day = d + 1;
    double dayFraction = julianDate + 0.5D - J;
    int hours = (int)(dayFraction * 24.0D);
    int minutes = (int)((dayFraction * 24.0D - hours) * 60.0D);
    int seconds = (int)(dayFraction * 24.0D * 3600.0D - (hours * 3600 + minutes * 60) + 0.5D);
    Calendar gregorianDateUTC = Calendar.getInstance(
        TimeZone.getTimeZone("UTC"));
    gregorianDateUTC.set(1, year);
    gregorianDateUTC.set(2, month);
    gregorianDateUTC.set(5, day);
    gregorianDateUTC.set(11, hours);
    gregorianDateUTC.set(12, minutes);
    gregorianDateUTC.set(13, seconds);
    gregorianDateUTC.set(14, 0);
    Calendar gregorianDate = Calendar.getInstance();
    gregorianDate.setTimeInMillis(gregorianDateUTC.getTimeInMillis());
    return gregorianDate;
  }
  
  public static Calendar[] getCivilTwilight(Calendar day, double latitude, double longitude) {
    return getSunriseSunset(day, latitude, longitude, -6.0D);
  }
  
  public static Calendar[] getNauticalTwilight(Calendar day, double latitude, double longitude) {
    return getSunriseSunset(day, latitude, longitude, -12.0D);
  }
  
  public static Calendar[] getAstronomicalTwilight(Calendar day, double latitude, double longitude) {
    return getSunriseSunset(day, latitude, longitude, -18.0D);
  }
  
  public static Calendar[] getSunriseSunset(Calendar day, double latitude, double longitude) {
    return getSunriseSunset(day, latitude, longitude, -0.833D);
  }
  
  private static SolarEquationVariables getSolarEquationVariables(Calendar day, double longitude) {
    longitude = -longitude;
    double julianDate = getJulianDate(day);
    double nstar = julianDate - 2451545.0D - 9.0E-4D - longitude / 360.0D;
    double n = Math.round(nstar);
    double jstar = 2451545.0009D + longitude / 360.0D + n;
    double m = Math.toRadians((357.5291D + 0.98560028D * (jstar - 2451545.0D)) % 360.0D);
    double c = 1.9148D * Math.sin(m) + 0.02D * Math.sin(2.0D * m) + 3.0E-4D * Math.sin(3.0D * m);
    double lambda = Math.toRadians((Math.toDegrees(m) + 102.9372D + c + 180.0D) % 360.0D);
    double jtransit = jstar + 0.0053D * Math.sin(m) - 0.0069D * Math.sin(2.0D * lambda);
    double delta = Math.asin(Math.sin(lambda) * 
        Math.sin(Math.toRadians(23.439D)));
    return new SolarEquationVariables(n, m, lambda, jtransit, delta);
  }
  
  public static Calendar[] getSunriseSunset(Calendar day, double latitude, double longitude, double sunAltitude) {
    SolarEquationVariables solarEquationVariables = getSolarEquationVariables(day, longitude);
    longitude = -longitude;
    double latitudeRad = Math.toRadians(latitude);
    double omega = Math.acos((Math.sin(Math.toRadians(sunAltitude)) - 
        Math.sin(latitudeRad) * Math.sin(solarEquationVariables.delta)) / 
        Math.cos(latitudeRad) * Math.cos(solarEquationVariables.delta));
    if (Double.isNaN(omega))
      return null; 
    double jset = 2451545.0009D + (Math.toDegrees(omega) + longitude) / 360.0D + solarEquationVariables.n + 0.0053D * Math.sin(solarEquationVariables.m) - 0.0069D * Math.sin(2.0D * solarEquationVariables.lambda);
    double jrise = solarEquationVariables.jtransit - jset - solarEquationVariables.jtransit;
    Calendar gregRiseUTC = getGregorianDate(jrise);
    Calendar gregSetUTC = getGregorianDate(jset);
    Calendar gregRise = Calendar.getInstance(day.getTimeZone());
    gregRise.setTimeInMillis(gregRiseUTC.getTimeInMillis());
    Calendar gregSet = Calendar.getInstance(day.getTimeZone());
    gregSet.setTimeInMillis(gregSetUTC.getTimeInMillis());
    return new Calendar[] { gregRise, gregSet };
  }
  
  public static Calendar getSolarNoon(Calendar day, double latitude, double longitude) {
    SolarEquationVariables solarEquationVariables = getSolarEquationVariables(day, longitude);
    double latitudeRad = Math.toRadians(latitude);
    double omega = Math.acos((Math.sin(Math.toRadians(-0.833D)) - 
        Math.sin(latitudeRad) * Math.sin(solarEquationVariables.delta)) / 
        Math.cos(latitudeRad) * Math.cos(solarEquationVariables.delta));
    if (Double.isNaN(omega))
      return null; 
    Calendar gregNoonUTC = getGregorianDate(solarEquationVariables.jtransit);
    Calendar gregNoon = Calendar.getInstance(day.getTimeZone());
    gregNoon.setTimeInMillis(gregNoonUTC.getTimeInMillis());
    return gregNoon;
  }
  
  public static boolean isDay(double latitude, double longitude) {
    Calendar now = Calendar.getInstance();
    return isDay(now, latitude, longitude);
  }
  
  public static boolean isDay(Calendar calendar, double latitude, double longitude) {
    Calendar[] sunriseSunset = getSunriseSunset(calendar, latitude, longitude);
    if (sunriseSunset == null) {
      int month = calendar.get(2);
      if (latitude > 0.0D) {
        if (month >= 3 && month <= 10)
          return true; 
        return false;
      } 
      if (month >= 3 && month <= 10)
        return false; 
      return true;
    } 
    Calendar sunrise = sunriseSunset[0];
    Calendar sunset = sunriseSunset[1];
    return (calendar.after(sunrise) && calendar.before(sunset));
  }
  
  public static boolean isNight(double latitude, double longitude) {
    Calendar now = Calendar.getInstance();
    return isNight(now, latitude, longitude);
  }
  
  public static boolean isNight(Calendar calendar, double latitude, double longitude) {
    Calendar[] astronomicalTwilight = getAstronomicalTwilight(calendar, latitude, longitude);
    if (astronomicalTwilight == null) {
      int month = calendar.get(2);
      if (latitude > 0.0D) {
        if (month >= 3 && month <= 10)
          return false; 
        return true;
      } 
      if (month >= 3 && month <= 10)
        return true; 
      return false;
    } 
    Calendar dawn = astronomicalTwilight[0];
    Calendar dusk = astronomicalTwilight[1];
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
    format.setTimeZone(calendar.getTimeZone());
    return (calendar.before(dawn) || calendar.after(dusk));
  }
  
  public static boolean isCivilTwilight(double latitude, double longitude) {
    Calendar today = Calendar.getInstance();
    return isCivilTwilight(today, latitude, longitude);
  }
  
  public static boolean isCivilTwilight(Calendar calendar, double latitude, double longitude) {
    Calendar[] sunriseSunset = getSunriseSunset(calendar, latitude, longitude);
    if (sunriseSunset == null)
      return false; 
    Calendar[] civilTwilight = getCivilTwilight(calendar, latitude, longitude);
    if (civilTwilight == null)
      return false; 
    return ((calendar.after(sunriseSunset[1]) && calendar.before(civilTwilight[1])) || (calendar
      .after(civilTwilight[0]) && calendar.before(sunriseSunset[0])));
  }
  
  public static boolean isNauticalTwilight(double latitude, double longitude) {
    Calendar today = Calendar.getInstance();
    return isNauticalTwilight(today, latitude, longitude);
  }
  
  public static boolean isNauticalTwilight(Calendar calendar, double latitude, double longitude) {
    Calendar[] civilTwilight = getCivilTwilight(calendar, latitude, longitude);
    if (civilTwilight == null)
      return false; 
    Calendar[] nauticalTwilight = getNauticalTwilight(calendar, latitude, longitude);
    if (nauticalTwilight == null)
      return false; 
    return ((calendar.after(civilTwilight[1]) && calendar.before(nauticalTwilight[1])) || (calendar
      .after(nauticalTwilight[0]) && calendar.before(civilTwilight[0])));
  }
  
  public static boolean isAstronomicalTwilight(double latitude, double longitude) {
    Calendar today = Calendar.getInstance();
    return isAstronomicalTwilight(today, latitude, longitude);
  }
  
  public static boolean isAstronomicalTwilight(Calendar calendar, double latitude, double longitude) {
    Calendar[] nauticalTwilight = getNauticalTwilight(calendar, latitude, longitude);
    if (nauticalTwilight == null)
      return false; 
    Calendar[] astronomicalTwilight = getAstronomicalTwilight(calendar, latitude, longitude);
    if (astronomicalTwilight == null)
      return false; 
    return ((calendar.after(nauticalTwilight[1]) && calendar.before(astronomicalTwilight[1])) || (calendar
      .after(astronomicalTwilight[0]) && calendar.before(nauticalTwilight[0])));
  }
  
  public static boolean isTwilight(double latitude, double longitude) {
    Calendar today = Calendar.getInstance();
    return isTwilight(today, latitude, longitude);
  }
  
  public static boolean isTwilight(Calendar calendar, double latitude, double longitude) {
    return (isCivilTwilight(calendar, latitude, longitude) || 
      isNauticalTwilight(calendar, latitude, longitude) || 
      isAstronomicalTwilight(calendar, latitude, longitude));
  }
  
  public static DayPeriod getDayPeriod(Calendar calendar, double latitude, double longitude) {
    if (isDay(calendar, latitude, longitude))
      return DayPeriod.DAY; 
    if (isCivilTwilight(calendar, latitude, longitude))
      return DayPeriod.CIVIL_TWILIGHT; 
    if (isNauticalTwilight(calendar, latitude, longitude))
      return DayPeriod.NAUTICAL_TWILIGHT; 
    if (isAstronomicalTwilight(calendar, latitude, longitude))
      return DayPeriod.ASTRONOMICAL_TWILIGHT; 
    if (isNight(calendar, latitude, longitude))
      return DayPeriod.NIGHT; 
    return DayPeriod.NIGHT;
  }
  
  public static long getDayLength(Calendar calendar, double latitude, double longitude) {
    Calendar[] sunriseSunset = getSunriseSunset(calendar, latitude, longitude);
    if (sunriseSunset == null) {
      int month = calendar.get(2);
      if (latitude > 0.0D) {
        if (month >= 3 && month <= 10)
          return 86400000L; 
        return 0L;
      } 
      if (month >= 3 && month <= 10)
        return 0L; 
      return 86400000L;
    } 
    return sunriseSunset[1].getTimeInMillis() - sunriseSunset[0].getTimeInMillis();
  }
  
  public static void main(String[] args) {
    Calendar[] cal = getSunriseSunset(new GregorianCalendar(), 52.31515439196772D, 13.199242070658022D);
    System.err.println("Sunrise: " + cal[0].getTime().toString());
    System.err.println("Sunset: " + cal[1].getTime().toString());
  }
}
