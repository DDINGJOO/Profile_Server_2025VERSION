package com.teambind.profileserver.events.event;

import lombok.Data;

@Data
public class UserNickNameChangedEvent {
	String userId;
	String nickName;
}
