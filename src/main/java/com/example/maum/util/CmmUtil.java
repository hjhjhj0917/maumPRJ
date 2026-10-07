package com.example.maum.util;

public class CmmUtil {
	public static String nvl(String str, String chg_str) {
		String res;

		if (str == null) {
			res = chg_str;
		} else if (str.equals("")) {
			res = chg_str;
		} else {
			res = str;
		}
		return res;
	}

	public static String nvl(String str){
		return nvl(str,"");
	}

	// 로그에 로그인 아이디를 그대로 남기지 않도록 앞 두 글자만 보이게 가림 (예: us***)
	public static String maskUserId(String userId){
		if (userId == null || userId.isEmpty()) {
			return "";
		}
		if (userId.length() <= 2) {
			return "***";
		}
		return userId.substring(0, 2) + "***";
	}

	// 로그에 이메일을 그대로 남기지 않도록 앞 두 글자와 도메인만 보이게 가림 (예: te***@example.com)
	public static String maskEmail(String email){
		if (email == null || email.isEmpty()) {
			return "";
		}
		int at = email.indexOf('@');
		if (at < 0) {
			return "***";
		}
		if (at <= 2) {
			return "***" + email.substring(at);
		}
		return email.substring(0, 2) + "***" + email.substring(at);
	}
}
