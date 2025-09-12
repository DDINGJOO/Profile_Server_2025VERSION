package com.teambind.profileserver.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "profile_update_history")
public class History {
    @Id
    @Column(name ="history_id")
    private Long historyId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name ="old_val")
    private String oldVal;
    @Column(name ="new_val")
    private String newVal;
    @Column(name ="field_name")
    private String fieldName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserInfo userInfo;


    @Version
    @Column(name="version")
    private int version;


}
