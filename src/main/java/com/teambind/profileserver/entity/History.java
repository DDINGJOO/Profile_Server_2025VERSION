package com.teambind.profileserver.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
// 서비스 기반 버전 정보 하면 역전 현상 막을 수 있음.
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
	
	public History(String fieldName, String oldVal, String newVal) {
		this.fieldName = fieldName;
		this.oldVal = oldVal;
		this.newVal = newVal;
		this.updatedAt = LocalDateTime.now();
	}



}
