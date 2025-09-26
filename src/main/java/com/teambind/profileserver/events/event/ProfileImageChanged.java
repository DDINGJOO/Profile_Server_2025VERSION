package com.teambind.profileserver.events.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileImageChanged {
    private String referenceId;
    private String imageUrl;

}
