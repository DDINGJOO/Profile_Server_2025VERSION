package com.teambind.profileserver.entity;


import com.teambind.profileserver.entity.attribute.UserGenres;
import com.teambind.profileserver.entity.attribute.UserInstruments;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Entity
@Table(name ="user_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    @Id
    @Column(name ="user_id")
    private String userId;

    @Column(name="profile_image_url")
    private String profileImageUrl;

    @Column(name="sex")
    private Character sex;

    @Column(name="nickname", unique = true, nullable = false)
    private String nickname;

    @Column(name="city")
    private String city;
    @Version
    @Column(name="version")
    private int version;
	
	@Column(name="introduction")
	private String introduction;

    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="last_updated_at")
    private LocalDateTime updatedAt;

    @Column(name="is_public")
    private Boolean isPublic;

    @Column(name="is_chatable")
    private Boolean isChatable;

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserGenres> userGenres;

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserInstruments> userInstruments;

    @OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
    private List<History> userHistory;


}
