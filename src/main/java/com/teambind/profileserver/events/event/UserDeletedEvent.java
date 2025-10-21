package com.teambind.profileserver.events.event;


import lombok.Data;

@Data
public class UserDeletedEvent {
	String userId;
}
