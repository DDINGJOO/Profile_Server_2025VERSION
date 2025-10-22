package com.teambind.profileserver.events.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileImageChanged extends Event{
    private String referenceId;
    private String imageUrl;
	
	public ProfileImageChanged(String referenceId, String imageUrl) {
		super("profile-image-changed");
		this.referenceId = referenceId;
		this.imageUrl = imageUrl;
	}
}
