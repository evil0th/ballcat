package com.hccake.extend.ding.talk.message;

import cn.hutool.json.JSONObject;
import com.hccake.extend.ding.talk.enums.MessageTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author lingting  2020/6/10 22:13
 */
@Getter
@Setter
@Accessors(chain = true)
public class DingTalkLinkMessage extends AbstractDingTalkMessage {
	/**
	 * 文本
	 */
	private String text;
	/**
	 * 标题
	 */
	private String title;
	/**
	 * 图片url
	 */
	private String picUrl;
	/**
	 * 消息链接
	 */
	private String messageUrl;

	@Override
	public MessageTypeEnum getType() {
		return MessageTypeEnum.LINK;
	}

	@Override
	public JSONObject json() {
		return new JSONObject().put(
				"link",
				new JSONObject()
						.put("text", text)
						.put("title", title)
						.put("picUrl", picUrl)
						.put("messageUrl", messageUrl)
		);
	}
}
