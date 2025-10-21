package com.teambind.profileserver.entity;


import com.teambind.profileserver.entity.attribute.UserGenres;
import com.teambind.profileserver.entity.attribute.UserInstruments;
import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
	@Builder.Default
	private List<UserGenres> userGenres = new ArrayList<>();
	
	@OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
	@Builder.Default
	private List<UserInstruments> userInstruments = new ArrayList<>();
	
	@OneToMany(mappedBy = "userInfo", cascade = CascadeType.ALL , orphanRemoval = true, fetch = FetchType.LAZY)
	private List<History> userHistory;
	
	
	public void addInstrument(InstrumentNameTable instrument) {
		if (instrument == null) return;
		if (this.userInstruments == null) this.userInstruments = new ArrayList<>();
		
		boolean exists = this.userInstruments.stream()
				.anyMatch(link -> link.getAttribute() != null
						&& link.getAttribute().getInstrumentId() == instrument.getInstrumentId());
		if (exists) return;
		
		UserInstruments link = new UserInstruments();
		link.setUserInfo(this);
		link.setAttribute(instrument);
		// 필요시 link.setId(new UserInstrumentKey(this.userId, instrument.getInstrumentId()));
		this.userInstruments.add(link);
	}
	public void addGenre(GenreNameTable genre) {
		if (genre == null) return;
		if (this.userGenres == null) this.userGenres = new ArrayList<>();
		
		boolean exists = this.userGenres.stream()
				.anyMatch(link -> link.getAttribute() != null
						&& link.getAttribute().getGenreId() == genre.getGenreId());
		if (exists) return;
		
		UserGenres link = new UserGenres();
		link.setUserInfo(this);
		link.setAttribute(genre);
		// 필요시 link.setId(new UserGenreKey(this.userId, genre.getGenreId()));
		this.userGenres.add(link);
	}
	
	public void addHistory(History history) {
		if (history == null) return;
		if (this.userHistory == null) this.userHistory = new ArrayList<>();
		this.userHistory.add(history);
		 history.setUserInfo(this);
	}
	public void removeGenre(GenreNameTable genre) {
		if (genre == null || this.userGenres == null) return;
		this.userGenres.removeIf(link -> {
			var attr = link.getAttribute();
			boolean match = attr != null && attr.getGenreId() == genre.getGenreId();
			if (match) link.setUserInfo(null);
			return match;
		});
	}
	
	public void removeInstrument(InstrumentNameTable instrument) {
		if (instrument == null || this.userInstruments == null) return;
		this.userInstruments.removeIf(link -> {
			var attr = link.getAttribute();
			boolean match = attr != null && attr.getInstrumentId() == instrument.getInstrumentId();
			if (match) link.setUserInfo(null);
			return match;
		});
	}
	
	
	
	public void clearInstruments() {
		if (this.userInstruments == null) return;
		this.userInstruments.forEach(link -> link.setUserInfo(null));
		this.userInstruments.clear();
	}
	
	public void clearGenres() {
		if (this.userGenres == null) return;
		this.userGenres.forEach(link -> link.setUserInfo(null));
		this.userGenres.clear();
	}
	
}
