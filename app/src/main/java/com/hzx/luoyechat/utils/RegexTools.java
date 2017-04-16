package com.hzx.luoyechat.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
/**
 * 验证邮箱、手机的类
 * @author huangzx
 *
 */
public class RegexTools {

	public static boolean checkPwd(String pwd) {
		pwd = pwd.trim();
		if (TextUtils.isEmpty(pwd)) 
			return false;
		
		int length = pwd.length();
		return (length>=6&&length<=18);
	}
	
	/** 验证邮箱 */
	public static boolean isEmail(String email) {
		String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		return match(check, email);
	}

	/** 验证数字 */
	public static boolean isNumber(String str,int begin,int end) {
		String regex = "^[0-9]{%d,%d}$";
		regex = String.format(regex, begin,end);
		return match(regex, str);
	}
	/** 验证数字 */
	public static boolean isNumber(String str) {
		String regex = "^[0-9]*$";
		return match(regex, str);
	}

	/** 验证手机号码 */
	public static boolean isMobile(String mobiles) {
		String regex = "^((13[0-9])|(15[^4,\\D])|(17[6-8])|(18[^4]))\\d{8}$";
		return match(regex, mobiles);
	}
	

	private static boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}
}
