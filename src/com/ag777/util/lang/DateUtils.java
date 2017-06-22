package com.ag777.util.lang;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: wanggz
 * Time: created at 2016/7/7.
 * Last modify: 2017/4/20.
 * MARK:
 * 巧妙的考勤统计:http://www.01happy.com/mysql-bit_count-bit_or/
 * SELECT year,month,BIT_COUNT(BIT_OR(1<<day)) AS days FROM t1
       GROUP BY year,month;
 */
public class DateUtils {

	public static final String DEFAULT_TEMPLATE = "yyyy-MM-dd";		//日期标准格式
	public static final String DEFAULT_TEMPLATE_TIME = "yyyy-MM-dd HH:mm:ss";	//时间标准格式
	public static final String DEFAULT_TEMPLATE_MONTH = "yyyy-MM";	//月份标准格式
	
	public static enum TimeUnit {	//时间单位枚举
		SECOND,MINUTE,HOUR,DAY,WEEK,MONTH,YEAR
	}
	
	private DateUtils(){}
	
	/*==============转换==============*/
	
	/**
	 * 字符串转DateTime
	 * @param date
	 * @param template
	 * @return
	 */
	public static DateTime convertToDateTime(String date, String template){
		DateTimeFormatter format = DateTimeFormat.forPattern(template);
		return DateTime.parse(date, format);
	}
	//重载
	public static DateTime convertToDateTime(String date){
		return convertToDateTime(date, DEFAULT_TEMPLATE_TIME);
	}
	
	/**
	 * LocalDate转DateTime
	 * @param ld
	 * @return
	 */
	public static DateTime convertToDateTime(LocalDate ld){
		return ld.toDateTimeAtStartOfDay();
	}
	
	/**
	 * 字符串转LocalDate
	 * @param date
	 * @param template
	 * @return
	 */
	public static LocalDate convertToLocalDate(String date, String template){
		DateTimeFormatter format = DateTimeFormat.forPattern(template);
		return LocalDate.parse(date, format);
	}
	//重载
	public static LocalDate convertToLocalDate(String date){
		return convertToLocalDate(date, DEFAULT_TEMPLATE);
	}
	
	/**
	 * DateTime转LocalDate
	 * @param dt
	 * @return
	 */
	public static LocalDate convertToLocalDate(DateTime dt){
		return dt.toLocalDate();
	}
	
	/**
	 * 获取某个时间的long型值
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public static long convertToLong(int year,int month,int day) {
		return new LocalDate(year,month,day).toDate().getTime();
	}
	
	/**
	 * 转换long型时间为字符串
	 * @param time
	 * @param template
	 * @return
	 */
	public static String convertToString(long time,String template) {
		return new DateTime(time).toString(template);
	}

	/**
	 * 转换Calendar型时间为字符串
	 * @param cld
	 * @param template
	 * @return
	 */
	public static String convertToString(Calendar cld,String template) {
		return new DateTime(cld).toString(template);
	}
	//重载
	public static String convertToString(java.util.Date date,String template) {
		return new DateTime(date).toString(template);
	}
	
	/**
	 * 转换字符串型时间为Calendar
	 * @param str
	 * @param template
	 * @return  失败返回null
	 */
	public static Calendar convertToCalendar(String str,String template) {
		DateTime dt = convertToDateTime(str, template);
		if(dt!=null){
			return dt.toCalendar(null);
		}
		return null;
	}
	//重载
	public static java.util.Date convertToDate(String str,String template) {
		DateTime dt = convertToDateTime(str, template);
		if(dt!=null){
			return dt.toDate();
		}
		return null;
	}
	
	/**
	 * 转换日期的显示格式
	 * @param date	日期
	 * @param template_src	源格式
	 * @param template_dest	目标格式
	 * @return
	 */
	public String convertFormat(String date, String template_src, String template_dest) {
		return convertToDateTime(date, template_src).toString(template_dest);
	}
	
	/*==============遍历==============*/
	
	/**
	 * 获取一年内的所有月份列表
	 * @param year
	 * @return
	 */
	public static List<String> getMonthListOfYear(int year, String template){
		LocalDate now = new LocalDate();
		LocalDate ld = now.year().setCopy(year).monthOfYear().setCopy(1);

		List<String> list = new ArrayList<String>();
		while(ld.getYear() == year){
			list.add(ld.toString(template));
			ld = ld.plusMonths(1);
		}

		return list;
	}
	//重载
	public static List<String> getMonthListOfYear(int year){
		return getMonthListOfYear(year, DEFAULT_TEMPLATE_MONTH);
	}

	/**
	 * 遍历起止时间间固定间隔的时间(通用)
	 * @param start_date
	 * @param end_date
	 * @param template 
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示每次增加一秒
	 * @param viewer 可以为null
	 */
	public static void ergodiceDateList(String start_date,String end_date,String template,TimeUnit unit,Viewer<DateTime> viewer) {
		DateTime start = convertToDateTime(start_date, template);
		DateTime end = convertToDateTime(end_date, template);
		
		while(!start.isAfter(end)){
			if(viewer != null) {
				viewer.doView(new DateTime(start));	//查看副本以防影响遍历结果
			}
			
			start = plusToCopy(start, unit, 1);
		}
		
	}
	
	/**
	 * 获取起止时间间固定间隔的时间列表(通用)
	 * @param start_date
	 * @param end_date
	 * @param template_src
	 * @param template_dest
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示每次增加一秒
	 * @param filter 返回true则写入列表,为null则都写入列表
	 * @return
	 */
	public static List<String> getList(String start_date,String end_date,String template_src,String template_dest,TimeUnit unit, Filter<DateTime> filter) {
		List<String> list = new ArrayList<>();
		DateTime start = convertToDateTime(start_date, template_src);
		DateTime end = convertToDateTime(end_date, template_src);
		while(!start.isAfter(end)){
				
			if(filter == null || filter.doFilter(new DateTime(start))) {		//过滤 || 建立副本以防影响遍历结果
				String item = start.toString(template_dest);
				list.add(item);
			}
			
			start = plusToCopy(start, unit, 1);
		}
		
		return list;
	}
	
	/**
	 * 获取起止时间间固定间隔的时间列表(通用)
	 * @param start_date
	 * @param end_date
	 * @param template_src
	 * @param template_dest
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示每次增加一秒
	 * @param editor 返回null则表示不写入列表
	 * @return
	 */
	public static List<String> getList(String start_date,String end_date,String template_src,String template_dest,TimeUnit unit, Editor<DateTime> editor) {
		List<String> list = new ArrayList<>();
		DateTime start = convertToDateTime(start_date, template_src);
		DateTime end = convertToDateTime(end_date, template_src);
		while(!start.isAfter(end)){
			DateTime temp = new DateTime(start);	//建立副本以防影响遍历结果
			DateTime result = editor.doEdit(temp);
			if(result != null) {
				String item = result.toString(template_dest);
				list.add(item);
			}
			start = plusToCopy(start, unit, 1);
		}
		
		return list;
	}
	//重载
	public static List<String> getList(String start_date,String end_date,String template_src,String template_dest,TimeUnit unit) {
		return getList(start_date, end_date, template_src, template_dest, unit, (Filter<DateTime>)null);
	}
	
	/**
	 * 获取起止时间间固定间隔的时间列表(通用)
	 * @param start_date
	 * @param end_date
	 * @param template_src
	 * @param template_dest
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示每次增加一秒
	 * @param editor 返回null则表示不写入列表
	 * @return 列表项为editor1的返回值,null不加入列表
	 */
	public static <T>List<T> getList(String start_date,String end_date,String template_src,TimeUnit unit, Editor1<DateTime,T> editor) {
		List<T> list = new ArrayList<>();
		DateTime start = convertToDateTime(start_date, template_src);
		DateTime end = convertToDateTime(end_date, template_src);
		while(!start.isAfter(end)){
			DateTime temp = new DateTime(start);	//建立副本以防影响遍历结果
			T item = editor.doEdit(temp);
			if(item != null) {
				list.add(item);
			}
		}
		return list;
	}
	
	/**
	 * 获取两个时间间隔(天)内的所有日期
	 * @param startDate 开始日期
	 * @param endDate	结束日期
	 * @param template	日期格式("yyyy-MM-dd")
	 * @return
	 */
	public static List<String> getDateList(String startDate,String endDate,String template_src, String template_dest){
		return getList(startDate, endDate, template_src, template_dest, TimeUnit.DAY);
	}
	//重载
	public static List<String> getDateList(String startDate,String endDate,String template){
		return getDateList(startDate, endDate, template, DEFAULT_TEMPLATE);
	}
	//重载
	public static List<String> getDateList(String startDate,String endDate){
		return getDateList(startDate, endDate,DEFAULT_TEMPLATE);
	}
	
	/**
	 * 获取两个时间间隔(天)内的所有日期(排除周末)
	 * @param startDate
	 * @param endDate
	 * @param template
	 * @return
	 */
	public static List<String> getDateListWithoutWeeken(String startDate,String endDate,String template_src, String template_dest){
		List<String> list = new ArrayList<>();

		LocalDate start = convertToLocalDate(startDate, template_src);
		LocalDate end = convertToLocalDate(endDate, template_src);
		while(!start.isAfter(end)){
			
			if(!isWeeken(start)) {	//非周末
				list.add(start.toString(template_dest));
			}
			start = start.plusDays(1);
		}

		return list;
	}
	//重载
	public static List<String> getDateListWithoutWeeken(String startDate,String endDate,String template){
		return getDateListWithoutWeeken(startDate, endDate, template, DEFAULT_TEMPLATE);
	}
	//重载
	public static List<String> getDateListWithoutWeeken(String startDate,String endDate){
		return getDateListWithoutWeeken(startDate, endDate, DEFAULT_TEMPLATE);
	}

	/**
	 * 获取两个时间间隔(天)内的周末日期
	 * @param startDate
	 * @param endDate
	 * @param template
	 * @return
	 */
	public static List<String> getWeekenDateList(String startDate,String endDate,String template_src, String template_dest){
		List<String> list = new ArrayList<>();

		LocalDate start = convertToLocalDate(startDate, template_src);
		LocalDate end = convertToLocalDate(endDate, template_src);
		while(!start.isAfter(end)){
			if(isWeeken(start)){	//周末
				list.add(start.toString(template_dest));	
			}
			start = start.plusDays(1);
		}
		return list;
	}
	//重载
	public static List<String> getWeekenDateList(String startDate,String endDate,String template){
		return getWeekenDateList(startDate, endDate, template, DEFAULT_TEMPLATE);
	}
	//重载
	public static List<String> getWeekenDateList(String startDate,String endDate){
		return getWeekenDateList(startDate, endDate,DEFAULT_TEMPLATE);
	}
	
	
	/**
	 * 获取两个时间间隔内的所有月份（包含首尾时间）
	 * @param startDate 开始日期
	 * @param endDate	结束日期
	 * @param template	日期格式("yyyy-MM-dd"),可以是("yyyy-MM")
	 * @return
	 */
	public static List<String> getMonthList(String startDate,String endDate,String template_src, String template_dest){
		List<String> list = new ArrayList<>();

		LocalDate start = convertToLocalDate(startDate, template_src).dayOfMonth().withMinimumValue();
		LocalDate end = convertToLocalDate(endDate, template_src).dayOfMonth().withMinimumValue();
		while(!start.isAfter(end)){
			list.add(start.toString(template_dest));
			start = start.plusMonths(1);
		}


		return list;
	}
	//重载
	public static List<String> getMonthList(String startDate,String endDate,String template){
		return getMonthList(startDate, endDate, template, DEFAULT_TEMPLATE_MONTH);
	}
	//重载,默认传进来的时间格式为DEFAULT_TEMPLATE(天)
	public static List<String> getMonthList(String startDate,String endDate){
		return getMonthList(startDate, endDate,DEFAULT_TEMPLATE);
	}
	
	
	/*==============获取==============*/
	
	/**
	 * 获取当前时间
	 * @param tempalte 如"yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static String getNow(String tempalte){
		return new DateTime().toString(tempalte);
	}

	/**
	 * 获取今天日期
	 * @return
	 */
	public static String getToday(){
		return getNow(DEFAULT_TEMPLATE);
	}
	
	
	/**
	 * 获取某天的开始时间00:00:00
	 * @param date
	 * @param template
	 * @return
	 */
	public static String getBeginOfDay(String date,String template) {
		return getMinimumToCopy(convertToDateTime(date, template), TimeUnit.DAY)
				.toString(DEFAULT_TEMPLATE_TIME);
	}
	//重载
	public static String getBeginOfDay(String date) {
		return getBeginOfDay(date,DEFAULT_TEMPLATE);
	}
	
	/**
	 * 获取某天的结束时间23:59:59
	 * @param date
	 * @param template
	 * @return
	 */
	public static String getEndOfDay(String date,String template) {
		return getMaximumToCopy(convertToDateTime(date, template), TimeUnit.DAY)
				.toString(DEFAULT_TEMPLATE_TIME);
	}
	//重载
	public static String getEndOfDay(String date) {
		return getEndOfDay(date,DEFAULT_TEMPLATE);
	}
	
	/**
	 * 判断日期是否为周末
	 * @param ld
	 * @return
	 */
	public static boolean isWeeken(LocalDate ld) {
		return ld.getDayOfWeek() == 6||ld.getDayOfWeek() == 7;
	}
	//重载
	public static boolean isWeeken(String date, String template) {
		LocalDate ld = convertToLocalDate(date,template);
		return isWeeken(ld);
	}
	//重载
	public static boolean isWeeken(String date) {
		return isWeeken(date, DEFAULT_TEMPLATE);
	}
	
	/**
	 * 时间增长
	 * @param time
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示每次增加一秒
	 * @param step 步长,可以为负数
	 * @return
	 */
	public static DateTime plusToCopy(DateTime time, TimeUnit unit, int step) {	//没做空指针判断
		if(unit == TimeUnit.SECOND) {
			return time.plusSeconds(step);
		}else if(unit == TimeUnit.MINUTE) {
			return time.plusMinutes(step);
		}else if(unit == TimeUnit.HOUR) {
			return time.plusHours(step);
		}else if(unit == TimeUnit.DAY) {
			return time.plusDays(step);
		}else if(unit == TimeUnit.WEEK) {
			return time.plusWeeks(step);
		}else if(unit == TimeUnit.MONTH) {
			return time.plusMonths(step);
		}else if(unit == TimeUnit.YEAR) {
			return time.plusYears(step);
		}
		return new DateTime(time);
	}
	
	/**
	 * 日期增长,单位写了不支持的(如:秒),则复制源数据返回
	 * @param time
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示每次增加一秒
	 * @param step 步长,可以为负数
	 * @return
	 */
	public static LocalDate plusToCopy(LocalDate time, TimeUnit unit, int step) {
		if(unit == TimeUnit.DAY) {
			return time.plusDays(step);
		}else if(unit == TimeUnit.WEEK) {
			return time.plusWeeks(step);
		}else if(unit == TimeUnit.MONTH) {
			return time.plusMonths(step);
		}else if(unit == TimeUnit.YEAR) {
			return time.plusYears(step);
		}
		return new LocalDate(time);
	}
	
	/**====================================日期比较===================================**/
	
	//target 日期是否在 compare 日期之前
	public static boolean isBefore(LocalDate target,LocalDate compare) {
		if(target.compareTo(compare) == -1) {	//1 大于 0 等于 -1 小于
			return true;
		}
		return false;
	}
	
	//target 日期是否在 compare 日期之后
	public static boolean isAfter(LocalDate target,LocalDate compare) {
		if(target.compareTo(compare) == 1) {	//1 大于 0 等于 -1 小于
			return true;
		}
		return false;
	}
	
	//target 日期是否不在 compare 日期之前(大等于)
	public static boolean isNotBefore(LocalDate target,LocalDate compare) {
		if(target.compareTo(compare) == -1) {	//1 大于 0 等于 -1 小于
			return false;
		}
		return true;
	}
	
	//target 日期是否不在 compare 日期之后(小等于)
	public static boolean isNotAfter(LocalDate target,LocalDate compare) {
		if(target.compareTo(compare) == 1) {	//1 大于 0 等于 -1 小于
			return false;
		}
		return true;
	}
	
	/**
	 * 获取两个时间差(通用)
	 * @param start_time
	 * @param end_time
	 * @param template
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示计算另个时间差多少秒
	 * @return
	 */
	public static int between(DateTime start, DateTime end, TimeUnit unit) {
		if(unit == TimeUnit.SECOND) {
			return Seconds.secondsBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getSeconds();
		}else if(unit == TimeUnit.MINUTE) {
			return Minutes.minutesBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getMinutes();
		}else if(unit == TimeUnit.HOUR) {
			return Hours.hoursBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getHours();
		}else if(unit == TimeUnit.DAY) {
			return Days.daysBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getDays();
		}else if(unit == TimeUnit.WEEK) {
			return Weeks.weeksBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getWeeks();
		}else if(unit == TimeUnit.MONTH) {
			return Months.monthsBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getMonths();
		}else if(unit == TimeUnit.YEAR) {
			return Years.yearsBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getYears();
		}
		return 0;
	}
	/**
	 * 获取两个时间差(通用)
	 * @param start_time
	 * @param end_time
	 * @param template
	 * @param unit 时间单位/遍历的步长,如TimeUnit.SECONDS表示计算另个时间差多少秒
	 * @return
	 */
	public static int between(LocalDate start, LocalDate end, TimeUnit unit) {
		if(unit == TimeUnit.DAY) {
			return Days.daysBetween(start,
					end).getDays();	//已经是最小化时间了没必要再做处理了
		}else if(unit == TimeUnit.WEEK) {
			return Weeks.weeksBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getWeeks();
		}else if(unit == TimeUnit.MONTH) {
			return Months.monthsBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getMonths();
		}else if(unit == TimeUnit.YEAR) {
			return Years.yearsBetween(getMinimumToCopy(start, unit),
					getMinimumToCopy(end, unit)).getYears();
		}
		return 0;
	}
	
	//重载
	public static int between(String start_time,String end_time,String template,TimeUnit unit) {
		
		if(unit == TimeUnit.DAY || unit == TimeUnit.WEEK || unit == TimeUnit.MONTH || unit == TimeUnit.YEAR) {
			LocalDate start = convertToLocalDate(start_time, template);
			LocalDate end = convertToLocalDate(end_time, template);
			return between(start, end, unit);
		}else {
			DateTime start = convertToDateTime(start_time, template);	
			DateTime end = convertToDateTime(end_time, template);
			return between(start, end, unit);
		}
		
		
	}
	
	
	/**
	 * 清空某个时间单位底下的时间(最小值),递归清空
	 * @param dt
	 * @param unit
	 * @return
	 */
	public static DateTime getMinimumToCopy(DateTime dt, TimeUnit unit) {
		if(unit == TimeUnit.SECOND) {
			return dt.millisOfSecond().withMinimumValue();	//最终根递归节点
		}else if(unit == TimeUnit.MINUTE) {
			return getMinimumToCopy(dt.secondOfMinute().withMinimumValue(), TimeUnit.SECOND);
		}else if(unit == TimeUnit.HOUR) {
			return getMinimumToCopy(dt.minuteOfHour().withMinimumValue(), TimeUnit.MINUTE);
		}else if(unit == TimeUnit.DAY) {
			return dt.millisOfDay().withMinimumValue();		//最终根递归节点
		}else if(unit == TimeUnit.WEEK) {
			return getMinimumToCopy(dt.dayOfWeek().withMinimumValue(), TimeUnit.DAY);
		}else if(unit == TimeUnit.MONTH) {
			return getMinimumToCopy(dt.dayOfMonth().withMinimumValue(), TimeUnit.DAY);
		}else if(unit == TimeUnit.YEAR) {
			return getMinimumToCopy(dt.dayOfYear().withMinimumValue(), TimeUnit.DAY);
		}
		return new DateTime(dt);
	}
	
	/**
	 * 清空某个时间单位底下的时间(最小值),递归清空
	 * @param dt
	 * @param unit
	 * @return
	 */
	public static LocalDate getMinimumToCopy(LocalDate ld, TimeUnit unit) {
		if(unit == TimeUnit.WEEK) {
			return ld.dayOfWeek().withMinimumValue();
		}else if(unit == TimeUnit.MONTH) {
			return ld.dayOfMonth().withMinimumValue();
		}else if(unit == TimeUnit.YEAR) {
			return ld.dayOfYear().withMinimumValue();
		}
		return new LocalDate(ld);
	}
	
	/**
	 * 最大化某个时间单位底下的时间(最小值),递归赋值,比如23点底下的最大时间为23:59:59.999
	 * @param dt
	 * @param unit
	 * @return
	 */
	public static DateTime getMaximumToCopy(DateTime dt, TimeUnit unit) {
		if(unit == TimeUnit.SECOND) {
			return dt.millisOfSecond().withMaximumValue();	//最终根递归节点
		}else if(unit == TimeUnit.MINUTE) {
			return getMinimumToCopy(dt.secondOfMinute().withMaximumValue(), TimeUnit.SECOND);
		}else if(unit == TimeUnit.HOUR) {
			return getMinimumToCopy(dt.minuteOfHour().withMaximumValue(), TimeUnit.MINUTE);
		}else if(unit == TimeUnit.DAY) {
			return dt.millisOfDay().withMaximumValue();		//最终根递归节点
		}else if(unit == TimeUnit.WEEK) {
			return getMinimumToCopy(dt.dayOfWeek().withMaximumValue(), TimeUnit.DAY);
		}else if(unit == TimeUnit.MONTH) {
			return getMinimumToCopy(dt.dayOfMonth().withMaximumValue(), TimeUnit.DAY);
		}else if(unit == TimeUnit.YEAR) {
			return getMinimumToCopy(dt.dayOfYear().withMaximumValue(), TimeUnit.DAY);
		}
		return new DateTime(dt);
	}
	
	/**
	 * 最大化某个时间单位底下的时间(最小值),递归赋值,比如1月份最大时间为1-31
	 * @param dt
	 * @param unit
	 * @return
	 */
	public static LocalDate getMaximumToCopy(LocalDate ld, TimeUnit unit) {
		if(unit == TimeUnit.WEEK) {
			return ld.dayOfWeek().withMaximumValue();
		}else if(unit == TimeUnit.MONTH) {
			return ld.dayOfMonth().withMaximumValue();
		}else if(unit == TimeUnit.YEAR) {
			return ld.dayOfYear().withMaximumValue();
		}
		return new LocalDate(ld);
	}
	
	/*----日期统计----*/
	/**
	 * 转换日期集合为统计字符串（可以给考勤统计模块用）
	 * 例子:传{"2017-06-20","2017-06-22"},"2017-06-20", "2017-06-23",返回1010
	 * 实现方式:二进制位与
	 * @param dateList
	 * @param startDateStr
	 * @param endDateStr
	 * @return
	 */
	public static String dateStatistics(List<String> dateList, String startDateStr,String endDateStr) {
		LocalDate startDate = convertToLocalDate(startDateStr);
		LocalDate endDate = convertToLocalDate(endDateStr);
		long interval = between(startDate, endDate, TimeUnit.DAY);	//首尾日期差
		long num = 1<<interval;
		
		for (String date : dateList) {
			if(date != null) {
				int dayBetween = between(startDate, convertToLocalDate(date), TimeUnit.DAY);
				if(dayBetween>-1) {
					num = num | (1<<dayBetween);
				}
			}
		}
		String statisticsStr = Long.toBinaryString(num);	//十进制转二进制
		StringBuilder sb = new StringBuilder(statisticsStr);
		if(!dateList.contains(endDate)) {	//不包含最后一天
			sb.replace(0, 1, "0");	//替换第一个字符为0(也就是清空enddate对应的日期数据)
		}
		return sb.reverse().toString();
	}
	
	public static String dateStatistics(List<String> dateList, List<String> holidayList, String startDateStr,String endDateStr) {
		LocalDate startDate = convertToLocalDate(startDateStr);
		LocalDate endDate = convertToLocalDate(endDateStr);
		long interval = between(startDate, endDate, TimeUnit.DAY);	//首尾日期差
		long num = 1<<(interval*3);
		
		for (String date : dateList) {
			if(date != null) {
				int dayBetween = between(startDate, convertToLocalDate(date), TimeUnit.DAY)*3;
				if(dayBetween>-1) {
					num = num | (1<<dayBetween);
				}
			}
		}
		for (String holiday : holidayList) {
			if(holiday != null) {
				int dayBetween = between(startDate, convertToLocalDate(holiday), TimeUnit.DAY)*3;
				if(dayBetween>-1) {
					num = num | (2<<dayBetween);
				}
			}
		}
		String statisticsStr = Long.toOctalString(num);	//十进制转八进制
		StringBuilder sb = new StringBuilder(statisticsStr);
		if(!dateList.contains(endDate)) {	//不包含最后一天
			sb.replace(0, 1, "0");	//替换第一个字符为0(也就是清空enddate对应的日期数据)
		}
		return sb.reverse().toString();
	}
	
	/**====================================配合控件===================================**/
	
	public static class DateRange {
		private static final String SEPARATOR = " 至 ";
		/**
		 * 配合前端daterange控件,拆分date_range字符串，填入map
		 * @param date_range 如'2016-07-07 至 2016-07-08'
		 * @param params 
		 */
		public static void fillDateRange(String date_range,String separator, String template, Map<String, Object> params){
			String[] date_group = getDateRange(date_range, separator, template);
			params.put("start_date", date_group[0]);
			params.put("end_date", date_group[1]);
		}
		
		//重载
		public static void fillDateRange(String date_range, Map<String, Object> params){
			String[] date_group = getDateRange(date_range);
			params.put("start_date", date_group[0]);
			params.put("end_date", date_group[1]);
		}
		
		/**
		 * 获取时间区间数组
		 * 返回[开始时间,结束时间]
		 */
		public static String[] getDateRange(String date_range,String separator, String template){
			String[] date_group = date_range.split(separator);
			String start_date = date_group[0];
			String end_date = date_group[1];
			if(!template.equals("yyyy-MM-dd")) {
				start_date = convertToLocalDate(start_date,template).toString(DEFAULT_TEMPLATE);
				end_date = convertToLocalDate(end_date, template).toString(DEFAULT_TEMPLATE);
			}
			date_group[0] = start_date;
			date_group[1] = end_date;
			return date_group;
		}
		//重载
		/**
		 * 获取时间区间数组
		 */
		public static String[] getDateRange(String date_range){
			return date_range.split(SEPARATOR);
		}
		/**
		 * 通过区间获取时间列表
		 */
		public static List<String> getDateList(String date_range,String separator, String template) {
			String[] group = getDateRange(date_range,separator,template);
			String startDate = group[0];
			String endDate = group[1];
			return DateUtils.getDateList(startDate, endDate);
		}
		//重载
		public static List<String> getDateList(String date_range) {
			String[] group = getDateRange(date_range);
			String startDate = group[0];
			String endDate = group[1];
			return DateUtils.getDateList(startDate, endDate);
		}
		
		/**
		 * 将map中yyyy-MM-dd格式的起止时间转换为yyyy-MM-dd HH:mm:ss的起止时间
		 * @param params
		 */
		public static void convertStartDateAndEndDate(Map<String, Object> params) {
			String reg = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
			if(params.containsKey("start_date") && ((String)params.get("start_date")).matches(reg)) {	//格式为yyyy-MM-dd
				params.put("start_date", 
						DateUtils.convertToDateTime((String) params.get("start_date"), "yyyy-MM-dd")
						.secondOfDay().withMinimumValue().toString("yyyy-MM-dd HH:mm:ss"));
			}
			if(params.containsKey("end_date") && ((String)params.get("end_date")).matches(reg)) {	//格式为yyyy-MM-dd
				params.put("end_date", 
						DateUtils.convertToDateTime((String) params.get("end_date"), "yyyy-MM-dd")
						.secondOfDay().withMaximumValue().toString("yyyy-MM-dd HH:mm:ss"));
			}
		}
	}
	
	public static class Date {
		/**
		 * 
		 * @param date yyyy-MM-dd
		 * @return
		 */
		public static Map<String, Object> getStartTimeAndEndTime(String date) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			if(date == null) {
				return params;
			}
			params.put("start_date", getStartDate(date));
			params.put("end_date", getEndDate(date));
			return params;
		}
		
		/**
		 * 根据map中date的key填充start_date和end_date
		 * @param date
		 * @param params
		 * @return
		 */
		public static Map<String, Object> fillStartTimeAndEndTime(String date,Map<String, Object> params) {
			if(date == null) {
				return params;
			}
			params.put("start_date", getStartDate(date));
			params.put("end_date", getEndDate(date));
			return params;
		}
		
		/**
		 * 获取开始时间
		 * @param date yyyy-MM-dd
		 */
		private static String getStartDate(String date) {
			return date+" 00:00:00";
		}
		
		/**
		 * 获取结束时间
		 * @param date yyyy-MM-dd
		 */
		private static String getEndDate(String date) {
			return date+" 23:59:59";
		}
	}
	
	/**====================================辅助类===================================**/
	
	public static interface Viewer<T> {
		void doView(T item);
	}
	
	public static interface Filter<T> {
		boolean doFilter(T item);		//匹配则返回true
	}
	
	public static interface Editor<T> extends Editor1<T, T> {
		T doEdit(T item);		
	}
	
	public static interface Editor1<T, V> {
		V doEdit(T item);		
	}
	
	public static void main(String[] args) {
//		List<String> list = getList("2017-04-01", "2017-04-14", "yyyy-MM-dd", "yyyy-MM-dd", TimeUnit.DAY,(Filter<DateTime>)null);
//		for (String date : list) {
//			System.out.println(date);
//		}
//		System.out.println(convertToLocalDate("2017-02-03").toString(DEFAULT_TEMPLATE_TIME));
//		System.out.println(convertToDateTime("23:01","mm:ss").toString(DEFAULT_TEMPLATE_TIME));
		LocalDateTime ldt = new LocalDateTime();
		
		System.out.println(ldt.toString(DEFAULT_TEMPLATE_TIME));
		System.out.println(DateUtils.getMinimumToCopy(
				DateUtils.convertToDateTime("23:02","mm:ss"),
				TimeUnit.MINUTE).toString("mm:ss"));
	}
}
