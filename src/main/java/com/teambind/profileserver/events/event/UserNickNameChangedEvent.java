package com.teambind.profileserver.events.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNickNameChangedEvent extends Event{
	String userId;
	String nickName;
	
	public UserNickNameChangedEvent(String userId, String nickname) {
		super("user-nickname-changed");
		this.userId = userId;
		this.nickName = nickname;
	}
}
