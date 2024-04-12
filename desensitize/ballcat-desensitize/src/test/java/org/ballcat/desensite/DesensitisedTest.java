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

package org.ballcat.desensite;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ballcat.desensitize.DesensitizationHandlerHolder;
import org.ballcat.desensitize.enums.RegexDesensitizationTypeEnum;
import org.ballcat.desensitize.enums.SlideDesensitizationTypeEnum;
import org.ballcat.desensitize.handler.RegexDesensitizationHandler;
import org.ballcat.desensitize.handler.SimpleDesensitizationHandler;
import org.ballcat.desensitize.handler.SixAsteriskDesensitizationHandler;
import org.ballcat.desensitize.handler.SlideDesensitizationHandler;
import org.ballcat.desensitize.json.JsonDesensitizeSerializerModifier;
import org.ballcat.desensitize.util.DesensitizedUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Hccake 2021/1/23
 */
@Slf4j
class DesensitisedTest {

	@Test
	public void testUtils() {
		assertEquals("430123******431", DesensitizedUtil.maskString("430123990101431", 6, 3));
		assertEquals("430123********432X", DesensitizedUtil.maskString("43012319990101432X", 6, 4));
		assertEquals("430123ABCD432X", DesensitizedUtil.maskString("43012319990101432X", 6, 4, "ABCD"));
		assertEquals("张*丰", DesensitizedUtil.maskChineseName("张三丰"));
		assertEquals("430123********432X", DesensitizedUtil.maskIdCard("43012319990101432X"));
		assertEquals("8***8976", DesensitizedUtil.maskPhone("89898976"));
		assertEquals("0108***8976", DesensitizedUtil.maskPhone("01089898976"));
		assertEquals("010-****8976", DesensitizedUtil.maskPhone("010-89898976"));
		assertEquals("138******78", DesensitizedUtil.maskMobile("13812345678"));
		assertEquals("北京市西城区******", DesensitizedUtil.maskAddress("北京市西城区金城坊街2号"));
		assertEquals("t****@qq.com", DesensitizedUtil.maskMail("test.demo@qq.com"));
		assertEquals("622260**********1234", DesensitizedUtil.maskBankAccount("62226000000043211234"));
		assertEquals("******", DesensitizedUtil.maskPassword(UUID.randomUUID().toString()));
		assertEquals("000****34", DesensitizedUtil.maskKey("0000000123456q34"));
		assertEquals("192.*.*.*", DesensitizedUtil.maskIP("192.168.2.1"));
		assertEquals("2001:*:*:*:*:*:*:*", DesensitizedUtil.maskIP("2001:0db8:02de:0000:0000:0000:0000:0e13"));
		assertEquals("2001:*:*:*:*:*:*:*", DesensitizedUtil.maskIP("2001:db8:2de:0:0:0:0:e13"));
		assertEquals("4*01***99*********", DesensitizedUtil.maskString("43012319990101432X", "1", "4-6", "9-"));
		assertEquals("4-01---99---------",
				DesensitizedUtil.maskString("43012319990101432X", '-', false, "1", "4-6", "9-"));
		assertEquals("-3--231--90101432X",
				DesensitizedUtil.maskString("43012319990101432X", '-', true, "1", "4-6", "9-"));
	}

	@Test
	void testSimple() {
		// 获取简单脱敏处理器
		SimpleDesensitizationHandler desensitizationHandler = DesensitizationHandlerHolder
			.getSimpleHandler(SixAsteriskDesensitizationHandler.class);
		String origin = "你好吗？"; // 原始字符串
		String target = desensitizationHandler.handle(origin); // 替换处理
		assertEquals("******", target);
	}

	@Test
	void testRegex() {
		// 获取正则脱敏处理器
		RegexDesensitizationHandler desensitizationHandler = DesensitizationHandlerHolder
			.getRegexDesensitizationHandler();
		String origin = "12123124213@qq.com"; // 原始字符串
		String regex = "(^.)[^@]*(@.*$)"; // 正则表达式
		String replacement = "$1****$2"; // 占位替换表达式
		String target1 = desensitizationHandler.handle(origin, regex, replacement); // 替换处理
		assertEquals("1****@qq.com", target1);

		// 内置的正则脱敏类型
		String target2 = desensitizationHandler.handle(origin, RegexDesensitizationTypeEnum.EMAIL);
		assertEquals("1****@qq.com", target2);
	}

	@Test
	void testSlide() {
		// 获取滑动脱敏处理器
		SlideDesensitizationHandler desensitizationHandler = DesensitizationHandlerHolder
			.getSlideDesensitizationHandler();
		String origin = "15805516789"; // 原始字符串
		String target1 = desensitizationHandler.handle(origin, 3, 2); // 替换处理
		assertEquals("158******89", target1);

		String target2 = desensitizationHandler.handle(origin, SlideDesensitizationTypeEnum.PHONE_NUMBER); // 替换处理
		assertEquals("158******89", target2);
	}

	@Test
	void testJackson() throws Exception {
		TestUtils.resetEnv();

		// 指定DesensitizeHandler 若ignore方法为true 则忽略脱敏 false 则启用脱敏
		JsonDesensitizeSerializerModifier modifier = new JsonDesensitizeSerializerModifier((fieldName) -> {
			log.info("当前字段名称{}", fieldName);
			return false;
		});
		// 不指定 实现类 默认使用脱敏规则
		// JsonSerializerModifier modifier = new JsonSerializerModifier();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializerFactory(objectMapper.getSerializerFactory().withSerializerModifier(modifier));
		DesensitizationUser user = new DesensitizationUser().setEmail("chengbohua@foxmail.com")
			.setUsername("xiaoming")
			.setPassword("admina123456")
			.setPhoneNumber("15800000000")
			.setTestField("这是测试属性")
			.setCustomDesensitize("test");
		String value = objectMapper.writeValueAsString(user);
		log.info("脱敏后的数据：{}", value);

		String expected = "{\"username\":\"xiaoming\",\"password\":\"adm****56\",\"email\":\"c****@foxmail.com\",\"phoneNumber\":\"158******00\",\"testField\":\"TEST-这是测试属性\",\"customDesensitize\":\"test\"}";
		assertEquals(expected, value);
	}

}
