/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballcat.desensitize.util;

import java.util.PriorityQueue;

import org.ballcat.desensitize.DesensitizationHandlerHolder;
import org.ballcat.desensitize.enums.RegexDesensitizationTypeEnum;
import org.ballcat.desensitize.enums.SlideDesensitizationTypeEnum;
import org.ballcat.desensitize.handler.RegexDesensitizationHandler;
import org.ballcat.desensitize.handler.SimpleDesensitizationHandler;
import org.ballcat.desensitize.handler.SixAsteriskDesensitizationHandler;
import org.ballcat.desensitize.handler.SlideDesensitizationHandler;

/**
 * 脱敏工具类
 * <ul>
 * <li>支持常用类型的脱敏：如姓名、身份证、银行卡号、手机号、密码、加密密文、座机、邮箱、地址、IP等</li>
 * <li>支持自定义前后保留多少位脱敏，可自定义脱敏占位符</li>
 * <li>支持基于自定义规则的脱敏：如指定"3-6，8，10-"表示第4，5，6，7，9，11以及11之后的位使用加密字符替换</li>
 * </ul>
 *
 * @author evil0th Create on 2024/4/12
 */
public final class DesensitizedUtil {

	private DesensitizedUtil() {
	}

	/**
	 * 中文姓名只显示第一个姓和最后一个汉字（单名则只显示最后一个汉字），其他隐藏为星号 <pre>
	 *     DesensitizedUtil.maskChineseName("张梦") = "*梦"
	 *     DesensitizedUtil.maskChineseName("张小梦") = "张*梦"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskChineseName(String input) {
		if (invalidText(input)) {
			return input;
		}
		return maskString(input, input.length() > 2 ? 1 : 0, 1);
	}

	/**
	 * 身份证(18位或者15位)显示前六位, 四位，其他隐藏。 <pre>
	 *     DesensitizedUtil.maskIdCard("43012319990101432X") = "430123********432X"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskIdCard(String input) {
		if (invalidText(input)) {
			return input;
		}
		SlideDesensitizationHandler slideHandler = DesensitizationHandlerHolder.getSlideDesensitizationHandler();
		return slideHandler.handle(input, SlideDesensitizationTypeEnum.ID_CARD_NO);
	}

	/**
	 * 固定电话显示后四位，其他隐藏。 <pre>
	 *     DesensitizedUtil.maskPhone("89898976") = "8***8976"
	 *     DesensitizedUtil.maskPhone("01089898976") = "0108***8976"
	 *     DesensitizedUtil.maskPhone("010-89898976") = "010-****8976"
	 * </pre>
	 * <p>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskPhone(String input) {
		if (invalidText(input)) {
			return input;
		}
		String temp = input.trim();
		int tail = Math.min(4, temp.length());
		if (temp.indexOf("-") > 0) {
			int frontCharNum = temp.indexOf('-') + 1;
			return maskString(input, frontCharNum, tail);
		}
		return maskString(input, temp.length() > 8 ? 4 : 1, tail);
	}

	/**
	 * 移动电话前三位，后四位，其他隐藏，比如 <pre>
	 * DesensitizedUtil.maskMobile("13812345678") = "138******10"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskMobile(String input) {
		if (invalidText(input)) {
			return input;
		}
		SlideDesensitizationHandler slideHandler = DesensitizationHandlerHolder.getSlideDesensitizationHandler();
		return slideHandler.handle(input, SlideDesensitizationTypeEnum.PHONE_NUMBER);
	}

	/**
	 * 地址脱敏，只显示到地区，不显示详细地址 <pre>
	 * DesensitizedUtil.maskAddress("北京市西城区金城坊街2号") = "北京市西城区******"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskAddress(String input) {
		if (invalidText(input)) {
			return input;
		}
		return maskString(input, 6, 0);
	}

	/**
	 * 电子邮箱脱敏，邮箱前缀最多显示前1字母，前缀其他隐藏，用星号代替，@及后面的地址显示 <pre>
	 * DesensitizedUtil.maskMail("test.demo@qq.com") = "t****@qq.com"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskMail(String input) {
		if (invalidText(input)) {
			return input;
		}
		RegexDesensitizationHandler regexHandler = DesensitizationHandlerHolder.getRegexDesensitizationHandler();
		return regexHandler.handle(input, RegexDesensitizationTypeEnum.EMAIL);
	}

	/**
	 * 银行卡号脱敏，显示前六位后四位 <pre>
	 * DesensitizedUtil.maskBankAccount("62226000000043211234") = "622260**********1234"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskBankAccount(String input) {
		if (invalidText(input)) {
			return input;
		}
		SlideDesensitizationHandler slideHandler = DesensitizationHandlerHolder.getSlideDesensitizationHandler();
		return slideHandler.handle(input, SlideDesensitizationTypeEnum.BANK_CARD_NO);
	}

	/**
	 * 密码脱敏，用******代替 <pre>
	 * DesensitizedUtil.maskPassword(password) = "******"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskPassword(String input) {
		if (invalidText(input)) {
			return input;
		}
		SimpleDesensitizationHandler simpleHandler = DesensitizationHandlerHolder
			.getSimpleHandler(SixAsteriskDesensitizationHandler.class);
		return simpleHandler.handle(input);
	}

	/**
	 * IPv脱敏，支持IPv4和IPv6 <pre>
	 * DesensitizedUtil.maskIP("192.168.2.1") = "192.*.*.*"
	 * DesensitizedUtil.maskIP("2001:0db8:02de:0000:0000:0000:0000:0e13") = "2001:*:*:*:*:*:*:*"
	 * DesensitizedUtil.maskIP("2001:db8:2de:0000:0000:0000:0000:e13") = "2001:*:*:*:*:*:*:*"
	 * DesensitizedUtil.maskIP("2001:db8:2de:0:0:0:0:e13") = "2001:*:*:*:*:*:*:*"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskIP(String input) {
		if (invalidText(input) || input.length() < 8) {
			return input;
		}
		int index = input.indexOf(".");
		if (index > 0) {
			return input.substring(0, index) + ".*.*.*";
		}
		index = input.indexOf(":");
		if (index > 0) {
			return input.substring(0, index) + ":*:*:*:*:*:*:*";
		}
		return input;
	}

	/**
	 * 密文脱敏，前3后2，中间替换为 4个 *
	 *
	 * <pre>
	 * DesensitizedUtil.maskKey("0000000123456q34") = "000****34"
	 * </pre>
	 * @param input 待处理的文本
	 * @return 屏蔽后的文本
	 */
	public static String maskKey(String input) {
		if (invalidText(input)) {
			return input;
		}
		RegexDesensitizationHandler regexHandler = DesensitizationHandlerHolder.getRegexDesensitizationHandler();
		return regexHandler.handle(input, RegexDesensitizationTypeEnum.ENCRYPTED_PASSWORD);
	}

	/**
	 * 替换任意字符串 <pre>
	 * DesensitizedUtil.maskString("43012319990101432X", 6, 4, "^_^") = "430123^_^432X"
	 * </pre>
	 * @param input 输入字符串
	 * @param head 头部保留长度
	 * @param tail 尾部保留长度
	 * @param text 替换结果字符
	 * @return 屏蔽后的文本
	 */
	public static String maskString(String input, int head, int tail, String text) {
		if (invalidText(input)) {
			return input;
		}
		if (head + tail >= input.length()) {
			return input;
		}
		String replacement = "$1" + text + "$2";
		String regex = "(\\S{" + head + "})\\S{" + (input.length() - head - tail) + "}(\\S{" + tail + "})";
		return input.replaceAll(regex, replacement);
	}

	/**
	 * 替换任意字符串 <pre>
	 * DesensitizedUtil.maskString("Hello World", 2, 3, "#") = "He######rld"
	 * </pre>
	 * @param input 输入字符串
	 * @param head 头部保留长度
	 * @param tail 尾部保留长度
	 * @param symbol 替换结果字符
	 * @return 屏蔽后的文本
	 */
	public static String maskString(String input, int head, int tail, char symbol) {
		if (invalidText(input)) {
			return input;
		}
		SlideDesensitizationHandler slideHandler = DesensitizationHandlerHolder.getSlideDesensitizationHandler();
		if (head + tail >= input.length()) {
			return input;
		}
		return slideHandler.handle(input, head, tail, String.valueOf(symbol));
	}

	/**
	 * 替换任意字符串
	 *
	 * <pre>
	 *     DesensitizedUtil.maskString("Hello World", 2, 3) = "He******rld"
	 * </pre>
	 * @param input 输入字符串
	 * @param head 头部保留长度
	 * @param tail 尾部保留长度
	 * @return 屏蔽后的文本
	 */
	public static String maskString(String input, int head, int tail) {
		return maskString(input, head, tail, '*');
	}

	/**
	 * 基于规则的替换字符串 <pre>
	 *     DesensitizedUtil.maskString("43012319990101432X", "1", "4-6", "9-")) = "4*01***99*********"
	 * </pre>
	 * @param input 输入字符串
	 * @param rule 规则。<br>
	 * @return 脱敏字符串
	 */
	public static String maskString(String input, String... rule) {
		return maskString(input, '*', false, Rule.analysis(rule));
	}

	/**
	 * 基于规则的替换字符串 <pre>
	 *     DesensitizedUtil.maskString("43012319990101432X", '-', false, "1", "4-6", "9-")) = "4-01---99---------"
	 *     DesensitizedUtil.maskString("43012319990101432X", '-', true, "1", "4-6", "9-")) = "-3--231--90101432X"
	 * </pre>
	 * @param input 输入字符串
	 * @param rule 规则。{@link DesensitizedUtil.Rule}
	 * @param symbol 符号，默认*
	 * @param reverse 是否反转规则
	 * @return 脱敏字符串
	 */
	public static String maskString(String input, char symbol, boolean reverse, String... rule) {
		return maskString(input, symbol, reverse, Rule.analysis(rule));
	}

	private static String maskString(String input, char symbol, boolean reverse, Rule rule) {
		if (invalidText(input)) {
			return input;
		}
		char[] clearChars = input.toCharArray();
		for (int i = 0; i < clearChars.length; ++i) {
			if (reverse ^ rule.isIn(i)) {
				clearChars[i] = symbol;
			}
		}
		return new String(clearChars);
	}

	/**
	 * 判断是否无效字符串
	 * @param text 字符串
	 * @return true-无效字符串
	 */
	private static boolean invalidText(String text) {
		return null == text || text.isEmpty();
	}

	/**
	 * 脱敏规则
	 * <p>
	 * 1. 下标从0开始<br>
	 * 2. 单个下标：数字<br>
	 * 3. 范围下标：数字1-数字2/数字1-<br>
	 * 4. 多个规则合并：规则1，规则2<br>
	 * 如：3-6，8，10- ，表示第4，5，6，7，9，11以及11之后的位使用加密字符替换
	 */
	public static final class Rule {

		private final PriorityQueue<Integer> indexQueue;

		private boolean endless;

		private Rule() {
			this.indexQueue = new PriorityQueue<>((o1, o2) -> o2 - o1);
			this.endless = false;
		}

		/**
		 * 解析规则
		 * @param rules 规则列表
		 * @return rule
		 */
		static Rule analysis(String... rules) {
			Rule rule = new Rule();
			if (null == rules) {
				return rule;
			}
			for (String ruleStr : rules) {
				if (invalidText(ruleStr)) {
					continue;
				}
				String[] groups = ruleStr.split(",");
				for (String group : groups) {
					if (invalidText(group)) {
						continue;
					}
					String[] items = group.split("-");
					if (items.length == 1) {
						rule.addIndex(items[0]);
					}
					else {
						rule.addIndex(items[0], items[items.length - 1]);
					}
					rule.endless |= group.endsWith("-");
				}
			}
			return rule;
		}

		void addIndex(String index) {
			addIndex(Integer.parseInt(index));
		}

		void addIndex(String startIndex, String endIndex) {
			addIndex(Integer.parseInt(startIndex), Integer.parseInt(endIndex));
		}

		void addIndex(int index) {
			if (isIn(index)) {
				return;
			}
			this.indexQueue.add(index);
		}

		void addIndex(int startIndex, int endIndex) {
			for (int index = startIndex; index <= endIndex; ++index) {
				addIndex(index);
			}
		}

		boolean isIn(int index) {
			if (this.indexQueue.contains(index)) {
				return true;
			}
			if (!this.indexQueue.isEmpty() && this.endless) {
				return this.indexQueue.peek() < index;
			}
			return false;
		}

		@Override
		public String toString() {
			return "Rule{" + "index=" + this.indexQueue + ", endless=" + this.endless + "}";
		}

	}

}
