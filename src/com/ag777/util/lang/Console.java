package com.ag777.util.lang;

import com.ag777.util.Utils;
import com.ag777.util.other.ExceptionHelper;

/**
 * @Description 控制台输出辅助类
 * @author wanggz
 * Time: created at 2017/6/6. last modify at 2017/6/13.
 * Mark: 
 */
public class Console {

	private static boolean devMode = true;
	
	/**
	 * getter/setter 控制是否打印日志
	 * @param devMode
	 */
	public static void setDevMode(boolean devMode) {
		Console.devMode = devMode;
	}
	public static boolean isDevMode() {
		return devMode;
	}
	
	/**
	 * 控制台打印信息
	 * @param obj
	 * @return
	 */
	public static String log(Object obj) {
		if(isDevMode()) {
			String msg = null;
			
			if(obj != null && obj instanceof String) {
				msg = (String) obj;
			} else {
				msg = Utils.jsonUtils().toJson(obj);
			}
			
			System.out.println(msg);
			return msg;
		} 
		return null;
	}
	
	/**
	 * 控制台打印异常信息
	 * @param throwable
	 * @param helper
	 * @return
	 */
	public static String log(Throwable throwable, ExceptionHelper helper) {
		if(isDevMode()) {
			String errMsg = helper.getErrMsg(throwable);
			System.err.println(errMsg);
			return errMsg;
		}
		return null;
	}
	
	public static void err(String msg) {
		System.err.println(msg);
	}
	
}
