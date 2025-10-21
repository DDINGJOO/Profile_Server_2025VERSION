package com.teambind.profileserver.events.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileCreateRequest extends Event{
    private String userId;
    private String provider;
	
	
	public ProfileCreateRequest(String userId, String provider) {
		super("profile-created");
	}
	
}
