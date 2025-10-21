package com.teambind.profileserver.events.event;

import lombok.Data;

@Data
public class UserNickNameChangedEvent {
	String userId;
	String nickName;
	
	public UserNickNameChangedEvent(String userId, String nickname) {
		this.userId = userId;
		this.nickName = nickname;
	}
}
