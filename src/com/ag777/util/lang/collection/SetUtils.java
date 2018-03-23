package com.ag777.util.lang.collection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 有关 <code>Set</code> 集合工具类。
 * 
 * @author ag777
 * @version create on 2018年03月23日,last modify at 2018年03月23日
 */
public class SetUtils {

	private SetUtils() {}
	
	public static <T>Set<T> newSet() {
		return newHashSet();
	}
	
	public static <T>HashSet<T> newHashSet() {
		return CollectionAndMapUtils.newHashSet();
	}
	
	
	/**
	 * 根据分隔符拆分Set获取字符串
	 * @param set
	 * @param separator
	 * @return
	 */
	public static <T>String toString(Set<T> set, String separator) {
		if(CollectionAndMapUtils.isEmpty(set)) {
			return "";
		}
		StringBuilder sb = null;
		Iterator<T> it = set.iterator();
		while (it.hasNext()) { 
			if(sb == null) {
				sb = new StringBuilder();
			} else if(separator != null) {
				sb.append(separator);
			}
			T item = it.next();  
			if(item != null) {
				sb.append(item.toString());
			} else {
				sb.append("null");
			}
		} 
		return sb.toString();
 	}
}
