package com.ag777.util.lang.exception;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.ag777.util.Utils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.collection.interf.ListFilter;

/**
 * 异常辅助类
 * 
 * @author ag777
 * Time: created at 2017/06/06. last modify at 2018/01/25.
 * Mark: 
 */
public class ExceptionHelper {

	private String workPackage;							//工作目录
	private List<String> excludePackageList;		//排除目录

	/*====================静态方法==================*/
	/**
	 * 功能：得到异常的堆栈信息。包括异常信息简述，以及详细堆栈信息。
	 * @param e 异常
	 * @return List<String>
	 */
	public static List<String> getStackTrace(Throwable e){
		List<String> errList=new LinkedList<String>();
		errList.add(e.toString());
		for(StackTraceElement line : e.getStackTrace()){
			errList.add(line.toString());
		}
		return errList;
	}
	
	/*====================构造函数==================*/
	public ExceptionHelper(String workPackage, List<String> excludePackageList) {
		this.workPackage = workPackage;
		if(excludePackageList != null) {
			this.excludePackageList = excludePackageList;
		} else {
			this.excludePackageList = new ArrayList<>();
		}
		
	}
	
	public ExceptionHelper(String workPackage) {
		this(workPackage, null);
	}
	
	/*====================外部方法==================*/

	/**
	 * 从抛出异常中提取对应类抛出的异常信息
	 * <p>
	 * 	遍历堆栈如果错误信息由clazz抛出，则拼接错误信息并返回
	 * </p>
	 * 
	 * @param throwable
	 * @param clazz
	 * @return
	 */
	public static String getErrMsg(Throwable throwable, Class<?> clazz) {
		if(throwable == null) {
			return null;
		}
		
		String errMsg = throwable.getMessage() != null?throwable.getMessage():throwable.toString();
		try {
			StackTraceElement[] s = throwable.getStackTrace();
			for (StackTraceElement stackTraceElement : s) {
				String clazzName = stackTraceElement.getClassName();	//类路径
				if(clazz == null || clazz.getName().equals(clazzName)) {
					errMsg = getErrMsg(errMsg, stackTraceElement);
					break;
				}
			}
		} catch(Exception e) {
			//不处理
		}
		return errMsg;
	}
	
	/**
	 * 从捕获的异常中提取错误信息
	 * <p>
	 * 	遍历堆栈如果错误信息由workPackageName抛出，且不再排除目录内,则拼接错误信息并返回
	 * </p>
	 * 
	 * @param throwable				异常
	 * @param workPackageName		该方法会从错误栈中查找第一条该包下找到的异常信息作为异常源
	 * @param excludePackageList	排除包
	 * @return
	 */
	public static String getErrMsg(Throwable throwable, String workPackageName,List<String> excludePackageList) {
		if(throwable == null) {
			return null;
		}
		
		String errMsg = throwable.getMessage() != null?throwable.getMessage():throwable.toString();
		try {
			
			excludePackageList = filterExcludePackages(workPackageName, excludePackageList);
			
//			if(throwable instanceof java.lang.NullPointerException) {	//空指针异常
				StackTraceElement[] s = throwable.getStackTrace();
				for (StackTraceElement stackTraceElement : s) {
					String clazzName = stackTraceElement.getClassName();	//类路径
					
					if(clazzName.contains(workPackageName)) {	//在工作路径下
						if(isUnderPackage(clazzName, excludePackageList)) {	//在排除路径下则算是错误源
							 continue;
						} else {
							errMsg = getErrMsg(errMsg, stackTraceElement);
							break;	//一定得记得，一般第一处就是错误源，没必要接着拼接下去了
						}
					}
					
				}
//			}	
		} catch(Exception e) {
			//不处理
		}
		return errMsg;
	}
	
	/**
	 * 从抛出的一场中提取错误信息(工作目录为app中的目录)
	 * @param throwable
	 * @param shouldExcludeUtilsPackage 是否排除该工具包的路径,如果不传这个参数则默认为true
	 * @return
	 */
	public String getErrMsg(Throwable throwable, boolean shouldExcludeUtilsPackage) {
		//是否需要排除改工具包的路径
		if(shouldExcludeUtilsPackage) {
			//排除路径集中添加工具包的根路径
			excludePackageList.add(Utils.getUtilsPackageName());
		}
		return getErrMsg(throwable, workPackage, excludePackageList);
	}
	
	/**
	 * 从抛出的一场中提取错误信息(工作目录为app中的目录，排除目录包含Utils类中的目录)
	 * @param throwable
	 * @return
	 */
	public String getErrMsg(Throwable throwable) {
		return getErrMsg(throwable, true);
	}
	
	/*====================工具方法==================*/
	/**
	 *  一次循环过滤掉无用的包名(规则，该包不在工作包下则移除)
	 * @param workPackageName
	 * @param excludePackages
	 * @return
	 */
	private static List<String> filterExcludePackages(final String workPackageName, List<String> excludePackages) {
		if(excludePackages == null || excludePackages.isEmpty()) {
			return excludePackages;
		}
		return ListUtils.removeByFilter(excludePackages, new ListFilter<String>() {

			@Override
			public boolean dofilter(String item) {
				if(item.contains(workPackageName)) {
					return false;
				}
				return true;
			}
		});
	}
	
	/**
	 * 判断目标包是否在列表中的包下
	 * @param targetPackage
	 * @param testPackageList
	 * @return
	 */
	private static boolean isUnderPackage(String targetPackage, List<String> testPackageList) {
		if(testPackageList == null || testPackageList.isEmpty()) {
			return false;
		}
		for (String  testPackage : testPackageList) {
			if(testPackage.contains(targetPackage)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 从错误栈中提取需要的信息
	 * @param errMsg
	 * @param stackTraceElement
	 * @return
	 */
	private static String getErrMsg(String errMsg, StackTraceElement stackTraceElement) {
//		String name = stackTraceElement.getFileName();			//异常发生的java文件名XXX.java
		int line = stackTraceElement.getLineNumber();			//异常相对于类所在行数
		String method = stackTraceElement.getMethodName();		//异常所属方法名
		String clazzName = stackTraceElement.getClassName();	//类路径
		return getErrMsg(errMsg, clazzName, method, line);
	}
	
	/**
	 * 拼接需要的信息
	 * @param errMsg
	 * @param clazzName
	 * @param method
	 * @param line
	 * @return
	 */
	private static String getErrMsg(String errMsg, String clazzName, String method, int line) {
		errMsg = errMsg.replaceAll("\"", "\\\\\"");	//转义双引号，为了外面的方法能转为map
		
		StringBuilder sb = new StringBuilder();
		sb.append('{')
			.append(" \"异常信息\": ")
			.append('"').append(errMsg).append('"')
			.append(',')
			.append(" \"异常发生位置\": ")
			.append('"').append(clazzName).append('"')
			.append(',')
			.append(" \"方法\": ")
			.append('"').append(method).append('"')
			.append(',')
			.append(" \"行数\": ")
			.append(line)
			.append(' ')
			.append('}');
		return sb.toString();
	}
	
}
