package com.teambind.profileserver.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name ="user_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    @Id
    @Column(name ="id", nullable = false, unique = true)
    private String id;

    @Column(name="profile_image_url")
    private String profileImageUrl;

    @Column(name="sex")
    private Boolean sex;

    @Column(name="nickname", unique = true, nullable = false)
    private String nickname;

    @Column(name="location")
    private int location;
    @Version
    @Column(name="version")
    private int version;

    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="last_updated_at")
    private LocalDateTime updatedAt;

    @Column(name="is_public")
    private Boolean isPublic;

    @Column(name="is_chatable")
    private Boolean isChatable;

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserGenres> userChats;

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserInstments> userInstments;

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
    private List<History> userHistory;

}
