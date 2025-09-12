package com.teambind.profileserver.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "instments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInstments {

    @ManyToOne
    @JoinColumn(name = "user_info_id")
    @Id
    private UserInfo user;


    @JoinColumn(name = "instment_id")
    private int instmentId;


    @ManyToOne
    private UserInfo userInfo;


}
