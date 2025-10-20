package com.teambind.profileserver.service.update;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teambind.profileserver.dto.request.HistoryUpdateRequest;
import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.service.history.UserProfileHistoryService;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileUpdateService 테스트")
class ProfileUpdateServiceTest {

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private InstrumentNameTableRepository instrumentNameTableRepository;

    @Mock
    private GenreNameTableRepository genreNameTableRepository;

    @Mock
    private UserProfileHistoryService historyService;

    @InjectMocks
    private ProfileUpdateService profileUpdateService;

    private UserInfo userInfo;
    private GenreNameTable genre1;
    private GenreNameTable genre2;
    private GenreNameTable genre3;
    private GenreNameTable genre4;
    private InstrumentNameTable instrument1;
    private InstrumentNameTable instrument2;
    private InstrumentNameTable instrument3;
    private InstrumentNameTable instrument4;

    @BeforeEach
    void setUp() {
        userInfo = UserInfo.builder()
                .userId("testUser")
                .nickname("testNickname")
                .city("Seoul")
                .introduction("Hello")
                .sex('M')
                .build();

        genre1 = GenreNameTable.builder()
                .genreId(1)
                .genreName("Rock")
                .build();

        genre2 = GenreNameTable.builder()
                .genreId(2)
                .genreName("Jazz")
                .build();

        genre3 = GenreNameTable.builder()
                .genreId(3)
                .genreName("Classical")
                .build();

        genre4 = GenreNameTable.builder()
                .genreId(4)
                .genreName("Pop")
                .build();

        instrument1 = InstrumentNameTable.builder()
                .instrumentId(1)
                .instrumentName("Guitar")
                .build();

        instrument2 = InstrumentNameTable.builder()
                .instrumentId(2)
                .instrumentName("Piano")
                .build();

        instrument3 = InstrumentNameTable.builder()
                .instrumentId(3)
                .instrumentName("Drum")
                .build();

        instrument4 = InstrumentNameTable.builder()
                .instrumentId(4)
                .instrumentName("Bass")
                .build();
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - 성공")
    void updateProfile_Success() {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .city("Busan")
                .introduction("New Introduction")
                .chattable(true)
                .publicProfile(true)
                .sex('F')
                .genres(Arrays.asList(1, 2))
                .instruments(Arrays.asList(1, 2))
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(userInfoRepository.existsByNickname("newNickname")).thenReturn(false);
        when(genreNameTableRepository.getReferenceById(1)).thenReturn(genre1);
        when(genreNameTableRepository.getReferenceById(2)).thenReturn(genre2);
        when(instrumentNameTableRepository.getReferenceById(1)).thenReturn(instrument1);
        when(instrumentNameTableRepository.getReferenceById(2)).thenReturn(instrument2);
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfile(userId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("newNickname");
        assertThat(result.getCity()).isEqualTo("Busan");
        assertThat(result.getIntroduction()).isEqualTo("New Introduction");
        assertThat(result.getSex()).isEqualTo('F');
        assertThat(result.getIsChatable()).isTrue();
        assertThat(result.getIsPublic()).isTrue();

        verify(userInfoRepository).findById(userId);
        verify(userInfoRepository).existsByNickname("newNickname");
        verify(userInfoRepository).save(userInfo);
        verify(historyService).saveAllHistory(any(), any());
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - 닉네임 중복으로 실패")
    void updateProfile_DuplicateNickname_Fail() {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("existingNickname")
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(userInfoRepository.existsByNickname("existingNickname")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> profileUpdateService.updateProfile(userId, request))
                .isInstanceOf(ProfileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_ALREADY_EXISTS);

        verify(userInfoRepository).findById(userId);
        verify(userInfoRepository).existsByNickname("existingNickname");
        verify(userInfoRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - 사용자를 찾을 수 없음")
    void updateProfile_UserNotFound_Fail() {
        // given
        String userId = "nonExistentUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileUpdateService.updateProfile(userId, request))
                .isInstanceOf(ProfileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userInfoRepository).findById(userId);
        verify(userInfoRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - null 필드는 업데이트하지 않음")
    void updateProfile_NullFields_NotUpdated() {
        // given
        String userId = "testUser";
        String originalCity = userInfo.getCity();
        String originalIntroduction = userInfo.getIntroduction();
        Character originalSex = userInfo.getSex();

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .city(null) // null이므로 업데이트하지 않음
                .introduction(null) // null이므로 업데이트하지 않음
                .sex(null) // null이므로 업데이트하지 않음
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(userInfoRepository.existsByNickname("newNickname")).thenReturn(false);
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfile(userId, request);

        // then
        assertThat(result.getNickname()).isEqualTo("newNickname");
        assertThat(result.getCity()).isEqualTo(originalCity); // 변경되지 않음
        assertThat(result.getIntroduction()).isEqualTo(originalIntroduction); // 변경되지 않음
        assertThat(result.getSex()).isEqualTo(originalSex); // 변경되지 않음
    }

    @Test
    @DisplayName("프로필 전체 업데이트 - 성공")
    void updateProfileAll_Success() {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .city("Busan")
                .introduction("New Introduction")
                .chattable(true)
                .publicProfile(true)
                .sex('F')
                .genres(Arrays.asList(1, 2))
                .instruments(Arrays.asList(1, 2))
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(userInfoRepository.existsByNickname("newNickname")).thenReturn(false);
        when(genreNameTableRepository.getReferenceById(1)).thenReturn(genre1);
        when(genreNameTableRepository.getReferenceById(2)).thenReturn(genre2);
        when(instrumentNameTableRepository.getReferenceById(1)).thenReturn(instrument1);
        when(instrumentNameTableRepository.getReferenceById(2)).thenReturn(instrument2);
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfileAll(userId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("newNickname");
        assertThat(result.getCity()).isEqualTo("Busan");
        assertThat(result.getIntroduction()).isEqualTo("New Introduction");
        assertThat(result.getSex()).isEqualTo('F');
        assertThat(result.getIsChatable()).isTrue();
        assertThat(result.getIsPublic()).isTrue();

        verify(userInfoRepository).findById(userId);
        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("프로필 전체 업데이트 - 모든 필드를 교체")
    void updateProfileAll_ReplaceAllFields() {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .city("Busan")
                .introduction("Updated")
                .chattable(false)
                .publicProfile(false)
                .sex('F')
                .genres(Arrays.asList(1))
                .instruments(Arrays.asList(2))
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(userInfoRepository.existsByNickname("newNickname")).thenReturn(false);
        when(genreNameTableRepository.getReferenceById(1)).thenReturn(genre1);
        when(instrumentNameTableRepository.getReferenceById(2)).thenReturn(instrument2);
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfileAll(userId, request);

        // then
        assertThat(result.getNickname()).isEqualTo("newNickname");
        assertThat(result.getCity()).isEqualTo("Busan");
        assertThat(result.getIntroduction()).isEqualTo("Updated");
        assertThat(result.getSex()).isEqualTo('F');
        assertThat(result.getIsChatable()).isFalse();
        assertThat(result.getIsPublic()).isFalse();

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 - 성공")
    void updateProfileImage_Success() {
        // given
        String userId = "testUser";
        String imageUrl = "https://example.com/image.jpg";

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfileImage(userId, imageUrl);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProfileImageUrl()).isEqualTo(imageUrl);

        verify(userInfoRepository).findById(userId);
        verify(userInfoRepository).save(userInfo);
        verify(historyService).saveAllHistory(any(), any());
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 - 히스토리 저장 실패")
    void updateProfileImage_HistorySaveFail() {
        // given
        String userId = "testUser";
        String imageUrl = "https://example.com/image.jpg";

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(historyService.saveAllHistory(any(), any())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> profileUpdateService.updateProfileImage(userId, imageUrl))
                .isInstanceOf(ProfileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HISTORY_UPDATE_FAILED);

        verify(userInfoRepository).findById(userId);
    }

    // ========== 연관관계 변경 테스트 (부분 업데이트 - PATCH) ==========

    @Test
    @DisplayName("부분 업데이트 - 장르 일부 제거 (1,2,3 → 1,2)")
    void updateProfile_RemoveSomeGenres() {
        // given
        String userId = "testUser";

        // 초기 상태: 장르 1,2,3 보유
        userInfo.addGenre(genre1);
        userInfo.addGenre(genre2);
        userInfo.addGenre(genre3);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .genres(Arrays.asList(1, 2)) // 3 제거
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        // 제거할 장르 3만 호출됨
        when(genreNameTableRepository.getReferenceById(3)).thenReturn(genre3);

        // when
        UserInfo result = profileUpdateService.updateProfile(userId, request);

        // then
        assertThat(result.getUserGenres()).hasSize(2);
        assertThat(result.getUserGenres().stream()
                .map(ug -> ug.getAttribute().getGenreId()))
                .containsExactlyInAnyOrder(1, 2);

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("부분 업데이트 - 악기 일부 추가 (1,2 → 1,2,3)")
    void updateProfile_AddSomeInstruments() {
        // given
        String userId = "testUser";

        // 초기 상태: 악기 1,2 보유
        userInfo.addInstrument(instrument1);
        userInfo.addInstrument(instrument2);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .instruments(Arrays.asList(1, 2, 3)) // 3 추가
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        // 추가할 악기 3만 호출됨
        when(instrumentNameTableRepository.getReferenceById(3)).thenReturn(instrument3);

        // when
        UserInfo result = profileUpdateService.updateProfile(userId, request);

        // then
        assertThat(result.getUserInstruments()).hasSize(3);
        assertThat(result.getUserInstruments().stream()
                .map(ui -> ui.getAttribute().getInstrumentId()))
                .containsExactlyInAnyOrder(1, 2, 3);

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("부분 업데이트 - 장르 일부 교체 (1,2,3 → 1,2,4)")
    void updateProfile_ReplaceSomeGenres() {
        // given
        String userId = "testUser";

        // 초기 상태: 장르 1,2,3 보유
        userInfo.addGenre(genre1);
        userInfo.addGenre(genre2);
        userInfo.addGenre(genre3);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .genres(Arrays.asList(1, 2, 4)) // 3 제거, 4 추가
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        // 제거할 장르 3과 추가할 장르 4만 호출됨
        when(genreNameTableRepository.getReferenceById(3)).thenReturn(genre3);
        when(genreNameTableRepository.getReferenceById(4)).thenReturn(genre4);

        // when
        UserInfo result = profileUpdateService.updateProfile(userId, request);

        // then
        assertThat(result.getUserGenres()).hasSize(3);
        assertThat(result.getUserGenres().stream()
                .map(ug -> ug.getAttribute().getGenreId()))
                .containsExactlyInAnyOrder(1, 2, 4);

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("부분 업데이트 - 악기 전체 삭제 (1,2,3 → [])")
    void updateProfile_RemoveAllInstruments() {
        // given
        String userId = "testUser";

        // 초기 상태: 악기 1,2,3 보유
        userInfo.addInstrument(instrument1);
        userInfo.addInstrument(instrument2);
        userInfo.addInstrument(instrument3);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .instruments(Arrays.asList()) // 전체 삭제
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        // 제거할 악기 1,2,3이 호출됨
        when(instrumentNameTableRepository.getReferenceById(1)).thenReturn(instrument1);
        when(instrumentNameTableRepository.getReferenceById(2)).thenReturn(instrument2);
        when(instrumentNameTableRepository.getReferenceById(3)).thenReturn(instrument3);

        // when
        UserInfo result = profileUpdateService.updateProfile(userId, request);

        // then
        assertThat(result.getUserInstruments()).isEmpty();

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("부분 업데이트 - 장르 null이면 변경하지 않음")
    void updateProfile_NullGenres_NotChanged() {
        // given
        String userId = "testUser";

        // 초기 상태: 장르 1,2 보유
        userInfo.addGenre(genre1);
        userInfo.addGenre(genre2);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .genres(null) // null이므로 변경하지 않음
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));

        // when
        UserInfo result = profileUpdateService.updateProfile(userId, request);

        // then
        assertThat(result.getUserGenres()).hasSize(2);
        assertThat(result.getUserGenres().stream()
                .map(ug -> ug.getAttribute().getGenreId()))
                .containsExactlyInAnyOrder(1, 2);

        verify(userInfoRepository).save(userInfo);
    }

    // ========== 연관관계 변경 테스트 (전체 업데이트 - PUT) ==========

    @Test
    @DisplayName("전체 업데이트 - 장르 전체 교체 (1,2,3 → 4)")
    void updateProfileAll_ReplaceAllGenres() {
        // given
        String userId = "testUser";

        // 초기 상태: 장르 1,2,3 보유
        userInfo.addGenre(genre1);
        userInfo.addGenre(genre2);
        userInfo.addGenre(genre3);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("testNickname")
                .genres(Arrays.asList(4)) // 전체 교체
                .instruments(Arrays.asList())
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(genreNameTableRepository.getReferenceById(4)).thenReturn(genre4);
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfileAll(userId, request);

        // then
        assertThat(result.getUserGenres()).hasSize(1);
        assertThat(result.getUserGenres().stream()
                .map(ug -> ug.getAttribute().getGenreId()))
                .containsExactly(4);

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("전체 업데이트 - 악기 빈 리스트에서 추가 ([] → 1,2,3)")
    void updateProfileAll_AddInstrumentsToEmpty() {
        // given
        String userId = "testUser";

        // 초기 상태: 악기 없음
        assertThat(userInfo.getUserInstruments()).isEmpty();

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("testNickname")
                .genres(Arrays.asList())
                .instruments(Arrays.asList(1, 2, 3)) // 빈 상태에서 추가
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(instrumentNameTableRepository.getReferenceById(1)).thenReturn(instrument1);
        when(instrumentNameTableRepository.getReferenceById(2)).thenReturn(instrument2);
        when(instrumentNameTableRepository.getReferenceById(3)).thenReturn(instrument3);
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfileAll(userId, request);

        // then
        assertThat(result.getUserInstruments()).hasSize(3);
        assertThat(result.getUserInstruments().stream()
                .map(ui -> ui.getAttribute().getInstrumentId()))
                .containsExactlyInAnyOrder(1, 2, 3);

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("전체 업데이트 - 장르와 악기 동시 변경 (1,2 → 3,4 / 1,2 → 3,4)")
    void updateProfileAll_ReplaceGenresAndInstrumentsTogether() {
        // given
        String userId = "testUser";

        // 초기 상태: 장르 1,2 / 악기 1,2 보유
        userInfo.addGenre(genre1);
        userInfo.addGenre(genre2);
        userInfo.addInstrument(instrument1);
        userInfo.addInstrument(instrument2);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("testNickname")
                .genres(Arrays.asList(3, 4)) // 전체 교체
                .instruments(Arrays.asList(3, 4)) // 전체 교체
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));
        when(genreNameTableRepository.getReferenceById(3)).thenReturn(genre3);
        when(genreNameTableRepository.getReferenceById(4)).thenReturn(genre4);
        when(instrumentNameTableRepository.getReferenceById(3)).thenReturn(instrument3);
        when(instrumentNameTableRepository.getReferenceById(4)).thenReturn(instrument4);
        when(historyService.saveAllHistory(any(), any())).thenReturn(true);

        // when
        UserInfo result = profileUpdateService.updateProfileAll(userId, request);

        // then
        assertThat(result.getUserGenres()).hasSize(2);
        assertThat(result.getUserGenres().stream()
                .map(ug -> ug.getAttribute().getGenreId()))
                .containsExactlyInAnyOrder(3, 4);

        assertThat(result.getUserInstruments()).hasSize(2);
        assertThat(result.getUserInstruments().stream()
                .map(ui -> ui.getAttribute().getInstrumentId()))
                .containsExactlyInAnyOrder(3, 4);

        verify(userInfoRepository).save(userInfo);
    }

    @Test
    @DisplayName("전체 업데이트 - 모든 장르/악기 삭제 (1,2,3 → [])")
    void updateProfileAll_RemoveAllGenresAndInstruments() {
        // given
        String userId = "testUser";

        // 초기 상태: 장르 1,2,3 / 악기 1,2,3 보유
        userInfo.addGenre(genre1);
        userInfo.addGenre(genre2);
        userInfo.addGenre(genre3);
        userInfo.addInstrument(instrument1);
        userInfo.addInstrument(instrument2);
        userInfo.addInstrument(instrument3);

        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("testNickname")
                .genres(Arrays.asList()) // 전체 삭제
                .instruments(Arrays.asList()) // 전체 삭제
                .chattable(true)
                .publicProfile(true)
                .build();

        when(userInfoRepository.findById(userId)).thenReturn(Optional.of(userInfo));

        // when
        UserInfo result = profileUpdateService.updateProfileAll(userId, request);

        // then
        assertThat(result.getUserGenres()).isEmpty();
        assertThat(result.getUserInstruments()).isEmpty();

        verify(userInfoRepository).save(userInfo);
    }
}
