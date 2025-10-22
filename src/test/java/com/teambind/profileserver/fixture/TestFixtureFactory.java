package com.teambind.profileserver.fixture;

import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.History;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.UserGenres;
import com.teambind.profileserver.entity.attribute.UserInstruments;
import com.teambind.profileserver.entity.attribute.key.UserGenreKey;
import com.teambind.profileserver.entity.attribute.key.UserInstrumentKey;
import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 객체 생성 팩토리 클래스
 *
 * 목적:
 * 1. 테스트 데이터 생성 로직 중앙화
 * 2. 메인 코드 변경 시 테스트 수정 최소화
 * 3. 테스트 가독성 향상
 */
public class TestFixtureFactory {

    // ==================== UserInfo ====================

    /**
     * 기본 UserInfo 생성 (모든 필드 null/false)
     */
    public static UserInfo createDefaultUserInfo(String userId) {
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .nickname("testUser_" + userId)
                .profileImageUrl(null)
                .sex(null)
                .city(null)
                .introduction(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPublic(false)
                .isChatable(false)
                .userGenres(new ArrayList<>())
                .userInstruments(new ArrayList<>())
                .build();

        // userHistory는 @Builder.Default가 없어서 수동 초기화 필요
        userInfo.setUserHistory(new ArrayList<>());

        return userInfo;
    }

    /**
     * 완전한 UserInfo 생성 (모든 필드 채워짐)
     */
    public static UserInfo createCompleteUserInfo(String userId) {
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .nickname("completeUser_" + userId)
                .profileImageUrl("https://example.com/profile.jpg")
                .sex('M')
                .city("서울")
                .introduction("안녕하세요!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPublic(true)
                .isChatable(true)
                .userGenres(new ArrayList<>())
                .userInstruments(new ArrayList<>())
                .build();

        // userHistory는 @Builder.Default가 없어서 수동 초기화 필요
        userInfo.setUserHistory(new ArrayList<>());

        // 장르 추가
        userInfo.addGenre(createGenre(1, "Rock"));
        userInfo.addGenre(createGenre(2, "Jazz"));

        // 악기 추가
        userInfo.addInstrument(createInstrument(1, "Guitar"));
        userInfo.addInstrument(createInstrument(2, "Piano"));

        return userInfo;
    }

    /**
     * 커스텀 UserInfo 빌더 (필요한 필드만 설정)
     */
    public static UserInfoBuilder userInfo() {
        return new UserInfoBuilder();
    }

    public static GenreNameTable createGenre(int id, String name) {
        return GenreNameTable.builder()
                .genreId(id)
                .genreName(name)
                .build();
    }

    // ==================== GenreNameTable ====================

    public static List<GenreNameTable> createGenres() {
        return List.of(
                createGenre(1, "Rock"),
                createGenre(2, "Jazz"),
                createGenre(3, "Classical"),
                createGenre(4, "Pop"),
                createGenre(5, "Hip-Hop")
        );
    }

    public static InstrumentNameTable createInstrument(int id, String name) {
        return InstrumentNameTable.builder()
                .instrumentId(id)
                .instrumentName(name)
                .build();
    }

    // ==================== InstrumentNameTable ====================

    public static List<InstrumentNameTable> createInstruments() {
        return List.of(
                createInstrument(1, "Guitar"),
                createInstrument(2, "Piano"),
                createInstrument(3, "Drum"),
                createInstrument(4, "Bass"),
                createInstrument(5, "Violin")
        );
    }

    /**
     * 기본 ProfileUpdateRequest (닉네임만 변경)
     */
    public static ProfileUpdateRequest createBasicUpdateRequest() {
        return ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .chattable(false)
                .publicProfile(false)
                .build();
    }

    // ==================== ProfileUpdateRequest ====================

    /**
     * 완전한 ProfileUpdateRequest (모든 필드 포함)
     */
    public static ProfileUpdateRequest createCompleteUpdateRequest() {
        return ProfileUpdateRequest.builder()
                .nickname("completeNickname")
                .city("부산")
                .introduction("새로운 자기소개")
                .chattable(true)
                .publicProfile(true)
                .sex('F')
                .genres(List.of(1, 2, 3))
                .instruments(List.of(1, 2))
                .build();
    }

    /**
     * 커스텀 ProfileUpdateRequest 빌더
     */
    public static ProfileUpdateRequestBuilder updateRequest() {
        return new ProfileUpdateRequestBuilder();
    }

    public static UserGenres createUserGenre(UserInfo userInfo, GenreNameTable genre) {
        return UserGenres.builder()
                .id(new UserGenreKey(userInfo.getUserId(), genre.getGenreId()))
                .userInfo(userInfo)
                .genre(genre)
                .build();
    }

    public static UserInstruments createUserInstrument(UserInfo userInfo, InstrumentNameTable instrument) {
        return UserInstruments.builder()
                .id(new UserInstrumentKey(userInfo.getUserId(), instrument.getInstrumentId()))
                .userInfo(userInfo)
                .instrument(instrument)
                .build();
    }

    // ==================== UserGenres ====================

    public static History createHistory(String fieldName, String oldValue, String newValue) {
        return new History(fieldName, oldValue, newValue);
    }

    // ==================== UserInstruments ====================

    public static class UserInfoBuilder {
        private String userId = "testUser123";
        private String nickname = "testNickname";
        private String profileImageUrl = null;
        private Character sex = null;
        private String city = null;
        private String introduction = null;
        private Boolean isPublic = false;
        private Boolean isChatable = false;
        private List<GenreNameTable> genres = new ArrayList<>();
        private List<InstrumentNameTable> instruments = new ArrayList<>();

        public UserInfoBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserInfoBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserInfoBuilder profileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public UserInfoBuilder sex(Character sex) {
            this.sex = sex;
            return this;
        }

        public UserInfoBuilder city(String city) {
            this.city = city;
            return this;
        }

        public UserInfoBuilder introduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public UserInfoBuilder isPublic(Boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        public UserInfoBuilder isChatable(Boolean isChatable) {
            this.isChatable = isChatable;
            return this;
        }

        public UserInfoBuilder withGenres(List<GenreNameTable> genres) {
            this.genres = genres;
            return this;
        }

        public UserInfoBuilder withInstruments(List<InstrumentNameTable> instruments) {
            this.instruments = instruments;
            return this;
        }

        public UserInfo build() {
            UserInfo userInfo = UserInfo.builder()
                    .userId(userId)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .sex(sex)
                    .city(city)
                    .introduction(introduction)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isPublic(isPublic)
                    .isChatable(isChatable)
                    .userGenres(new ArrayList<>())
                    .userInstruments(new ArrayList<>())
                    .build();

            // userHistory는 @Builder.Default가 없어서 수동 초기화 필요
            userInfo.setUserHistory(new ArrayList<>());

            genres.forEach(userInfo::addGenre);
            instruments.forEach(userInfo::addInstrument);

            return userInfo;
        }
    }

    // ==================== History ====================

    public static class ProfileUpdateRequestBuilder {
        private String nickname = null;
        private String city = null;
        private String introduction = null;
        private boolean chattable = false;
        private boolean publicProfile = false;
        private Character sex = null;
        private List<Integer> genres = null;
        private List<Integer> instruments = null;

        public ProfileUpdateRequestBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public ProfileUpdateRequestBuilder city(String city) {
            this.city = city;
            return this;
        }

        public ProfileUpdateRequestBuilder introduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public ProfileUpdateRequestBuilder chattable(boolean chattable) {
            this.chattable = chattable;
            return this;
        }

        public ProfileUpdateRequestBuilder publicProfile(boolean publicProfile) {
            this.publicProfile = publicProfile;
            return this;
        }

        public ProfileUpdateRequestBuilder sex(Character sex) {
            this.sex = sex;
            return this;
        }

        public ProfileUpdateRequestBuilder genres(List<Integer> genres) {
            this.genres = genres;
            return this;
        }

        public ProfileUpdateRequestBuilder instruments(List<Integer> instruments) {
            this.instruments = instruments;
            return this;
        }

        public ProfileUpdateRequest build() {
            return ProfileUpdateRequest.builder()
                    .nickname(nickname)
                    .city(city)
                    .introduction(introduction)
                    .chattable(chattable)
                    .publicProfile(publicProfile)
                    .sex(sex)
                    .genres(genres)
                    .instruments(instruments)
                    .build();
        }
    }
}
