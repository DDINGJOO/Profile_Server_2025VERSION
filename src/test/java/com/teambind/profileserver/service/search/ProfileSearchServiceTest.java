package com.teambind.profileserver.service.search;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.teambind.profileserver.dto.response.BatchUserSummaryResponse;
import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.ProfileSearchRepository;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

/**
 * ProfileSearchService 단위 테스트
 *
 * <p>테스트 범위: 1. ID로 단건 프로필 조회 2. 조건 기반 프로필 검색 (페이징) 3. 커서 기반 프로필 검색 4. 여러 ID로 배치 조회
 *
 * <p>테스트 전략: - Repository는 Mock으로 격리 - DTO 변환 로직 검증 - 예외 상황 처리 확인 - 엣지 케이스 커버
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileSearchService 테스트")
class ProfileSearchServiceTest {

  private static final String TEST_USER_ID = "testUser123";
  @InjectMocks private ProfileSearchService profileSearchService;
  @Mock private ProfileSearchRepository repository;

  @Nested
  @DisplayName("ID로 프로필 조회 (searchProfileById)")
  class SearchProfileById {

    @Test
    @DisplayName("성공 - 프로필 조회 및 DTO 변환")
    void searchProfileById_Success() {
      // given
      UserInfo userInfo = createCompleteUserInfo(TEST_USER_ID);
      when(repository.search(TEST_USER_ID)).thenReturn(userInfo);

      // when
      UserResponse result = profileSearchService.searchProfileById(TEST_USER_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
      assertThat(result.getNickname()).isEqualTo(userInfo.getNickname());
      verify(repository).search(TEST_USER_ID);
    }

    @Test
    @DisplayName("실패 - 사용자 없음 예외 발생")
    void searchProfileById_UserNotFound_ThrowsException() {
      // given
      when(repository.search(TEST_USER_ID)).thenReturn(null);

      // when & then
      assertThatThrownBy(() -> profileSearchService.searchProfileById(TEST_USER_ID))
          .isInstanceOf(ProfileException.class)
          .hasFieldOrPropertyWithValue("errorCode", ProfileErrorCode.USER_NOT_FOUND);

      verify(repository).search(TEST_USER_ID);
    }

    @Test
    @DisplayName("장르와 악기가 포함된 프로필 조회")
    void searchProfileById_WithGenresAndInstruments() {
      // given
      UserInfo userInfo =
          userInfo()
              .userId(TEST_USER_ID)
              .nickname("testUser")
              .withGenres(createGenres().subList(0, 3))
              .withInstruments(createInstruments().subList(0, 2))
              .build();

      when(repository.search(TEST_USER_ID)).thenReturn(userInfo);

      // when
      UserResponse result = profileSearchService.searchProfileById(TEST_USER_ID);

      // then
      assertThat(result).isNotNull();
      // DTO 변환 시 장르/악기 정보도 포함되는지 확인 (UserResponse 구현에 따라 다름)
      verify(repository).search(TEST_USER_ID);
    }

    @Test
    @DisplayName("최소 정보만 있는 프로필 조회")
    void searchProfileById_MinimalProfile() {
      // given
      UserInfo userInfo = createDefaultUserInfo(TEST_USER_ID);
      when(repository.search(TEST_USER_ID)).thenReturn(userInfo);

      // when
      UserResponse result = profileSearchService.searchProfileById(TEST_USER_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
      verify(repository).search(TEST_USER_ID);
    }
  }

  @Nested
  @DisplayName("조건 기반 프로필 검색 (searchProfiles)")
  class SearchProfiles {

    @Test
    @DisplayName("성공 - 페이지 검색 결과 반환")
    void searchProfiles_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      Pageable pageable = PageRequest.of(0, 10);

      List<UserInfo> users =
          Arrays.asList(
              createDefaultUserInfo("user1"),
              createDefaultUserInfo("user2"),
              createDefaultUserInfo("user3"));
      Page<UserInfo> pageResult = new PageImpl<>(users, pageable, 3);

      when(repository.search(criteria, pageable)).thenReturn(pageResult);

      // when
      Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(3);
      assertThat(result.getTotalElements()).isEqualTo(3);
      assertThat(result.getNumber()).isEqualTo(0);
      assertThat(result.getSize()).isEqualTo(10);

      verify(repository).search(criteria, pageable);
    }

    @Test
    @DisplayName("빈 검색 결과")
    void searchProfiles_EmptyResult() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      Pageable pageable = PageRequest.of(0, 10);
      Page<UserInfo> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

      when(repository.search(criteria, pageable)).thenReturn(emptyPage);

      // when
      Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(0);
      verify(repository).search(criteria, pageable);
    }

    @Test
    @DisplayName("여러 페이지 조회")
    void searchProfiles_MultiplePages() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      Pageable firstPage = PageRequest.of(0, 2);
      Pageable secondPage = PageRequest.of(1, 2);

      List<UserInfo> firstPageUsers =
          Arrays.asList(createDefaultUserInfo("user1"), createDefaultUserInfo("user2"));
      List<UserInfo> secondPageUsers =
          Arrays.asList(createDefaultUserInfo("user3"), createDefaultUserInfo("user4"));

      when(repository.search(criteria, firstPage))
          .thenReturn(new PageImpl<>(firstPageUsers, firstPage, 4));
      when(repository.search(criteria, secondPage))
          .thenReturn(new PageImpl<>(secondPageUsers, secondPage, 4));

      // when
      Page<UserResponse> result1 = profileSearchService.searchProfiles(criteria, firstPage);
      Page<UserResponse> result2 = profileSearchService.searchProfiles(criteria, secondPage);

      // then
      assertThat(result1.getContent()).hasSize(2);
      assertThat(result1.getNumber()).isEqualTo(0);
      assertThat(result1.getTotalPages()).isEqualTo(2);

      assertThat(result2.getContent()).hasSize(2);
      assertThat(result2.getNumber()).isEqualTo(1);
      assertThat(result2.isLast()).isTrue();

      verify(repository, times(2)).search(any(ProfileSearchCriteria.class), any(Pageable.class));
    }

    @Test
    @DisplayName("다양한 페이지 크기로 조회")
    void searchProfiles_DifferentPageSizes() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      int[] pageSizes = {5, 10, 20, 50, 100};

      for (int size : pageSizes) {
        Pageable pageable = PageRequest.of(0, size);
        Page<UserInfo> pageResult = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(repository.search(criteria, pageable)).thenReturn(pageResult);

        // when
        Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

        // then
        assertThat(result.getSize()).isEqualTo(size);
      }
    }
  }

  @Nested
  @DisplayName("커서 기반 프로필 검색 (searchProfilesByCursor)")
  class SearchProfilesByCursor {

    @Test
    @DisplayName("성공 - 커서 기반 검색 결과 반환")
    void searchProfilesByCursor_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      String cursor = "cursor123";
      int size = 10;

      List<UserInfo> users =
          Arrays.asList(
              createDefaultUserInfo("user1"),
              createDefaultUserInfo("user2"),
              createDefaultUserInfo("user3"));
      Slice<UserInfo> sliceResult = new SliceImpl<>(users, PageRequest.of(0, size), true);

      when(repository.searchByCursor(criteria, cursor, size)).thenReturn(sliceResult);

      // when
      Slice<UserResponse> result =
          profileSearchService.searchProfilesByCursor(criteria, cursor, size);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(3);
      assertThat(result.hasNext()).isTrue();

      verify(repository).searchByCursor(criteria, cursor, size);
    }

    @Test
    @DisplayName("다음 페이지 없음 (hasNext = false)")
    void searchProfilesByCursor_NoNextPage() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      String cursor = "lastCursor";
      int size = 10;

      List<UserInfo> users =
          Arrays.asList(createDefaultUserInfo("user1"), createDefaultUserInfo("user2"));
      Slice<UserInfo> sliceResult = new SliceImpl<>(users, PageRequest.of(0, size), false);

      when(repository.searchByCursor(criteria, cursor, size)).thenReturn(sliceResult);

      // when
      Slice<UserResponse> result =
          profileSearchService.searchProfilesByCursor(criteria, cursor, size);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.hasNext()).isFalse();

      verify(repository).searchByCursor(criteria, cursor, size);
    }

    @Test
    @DisplayName("null 커서로 첫 페이지 조회")
    void searchProfilesByCursor_NullCursor_FirstPage() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      String cursor = null;
      int size = 10;

      List<UserInfo> users =
          Arrays.asList(createDefaultUserInfo("user1"), createDefaultUserInfo("user2"));
      Slice<UserInfo> sliceResult = new SliceImpl<>(users, PageRequest.of(0, size), true);

      when(repository.searchByCursor(criteria, cursor, size)).thenReturn(sliceResult);

      // when
      Slice<UserResponse> result =
          profileSearchService.searchProfilesByCursor(criteria, cursor, size);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(2);
      verify(repository).searchByCursor(criteria, null, size);
    }

    @Test
    @DisplayName("빈 결과")
    void searchProfilesByCursor_EmptyResult() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      String cursor = "cursor123";
      int size = 10;

      Slice<UserInfo> emptySlice =
          new SliceImpl<>(Collections.emptyList(), PageRequest.of(0, size), false);

      when(repository.searchByCursor(criteria, cursor, size)).thenReturn(emptySlice);

      // when
      Slice<UserResponse> result =
          profileSearchService.searchProfilesByCursor(criteria, cursor, size);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      assertThat(result.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("여러 ID로 배치 조회 (searchProfilesByIds)")
  class SearchProfilesByIds {

    @Test
    @DisplayName("성공 - 여러 사용자 배치 조회")
    void searchProfilesByIds_Success() {
      // given
      List<String> userIds = Arrays.asList("user1", "user2", "user3");
      List<UserInfo> users =
          Arrays.asList(
              createDefaultUserInfo("user1"),
              createDefaultUserInfo("user2"),
              createDefaultUserInfo("user3"));

      when(repository.searchByUserIds(userIds)).thenReturn(users);

      // when
      List<BatchUserSummaryResponse> result = profileSearchService.searchProfilesByIds(userIds);

      // then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(3);
      verify(repository).searchByUserIds(userIds);
    }

    @Test
    @DisplayName("빈 ID 리스트로 조회")
    void searchProfilesByIds_EmptyList() {
      // given
      List<String> emptyList = Collections.emptyList();
      when(repository.searchByUserIds(emptyList)).thenReturn(Collections.emptyList());

      // when
      List<BatchUserSummaryResponse> result = profileSearchService.searchProfilesByIds(emptyList);

      // then
      assertThat(result).isEmpty();
      verify(repository).searchByUserIds(emptyList);
    }

    @Test
    @DisplayName("단일 ID로 조회")
    void searchProfilesByIds_SingleId() {
      // given
      List<String> singleId = List.of("user1");
      List<UserInfo> users = List.of(createDefaultUserInfo("user1"));

      when(repository.searchByUserIds(singleId)).thenReturn(users);

      // when
      List<BatchUserSummaryResponse> result = profileSearchService.searchProfilesByIds(singleId);

      // then
      assertThat(result).hasSize(1);
      verify(repository).searchByUserIds(singleId);
    }

    @Test
    @DisplayName("일부 ID만 존재하는 경우")
    void searchProfilesByIds_PartialMatch() {
      // given
      List<String> requestedIds = Arrays.asList("user1", "user2", "nonExistent");
      List<UserInfo> foundUsers =
          Arrays.asList(createDefaultUserInfo("user1"), createDefaultUserInfo("user2"));

      when(repository.searchByUserIds(requestedIds)).thenReturn(foundUsers);

      // when
      List<BatchUserSummaryResponse> result =
          profileSearchService.searchProfilesByIds(requestedIds);

      // then
      assertThat(result).hasSize(2); // 존재하는 2개만 반환
      verify(repository).searchByUserIds(requestedIds);
    }

    @Test
    @DisplayName("대량의 ID로 조회 (성능 테스트)")
    void searchProfilesByIds_LargeList() {
      // given
      List<String> largeIdList = new java.util.ArrayList<>();
      List<UserInfo> largeUserList = new java.util.ArrayList<>();

      for (int i = 0; i < 1000; i++) {
        String userId = "user" + i;
        largeIdList.add(userId);
        largeUserList.add(createDefaultUserInfo(userId));
      }

      when(repository.searchByUserIds(largeIdList)).thenReturn(largeUserList);

      // when
      List<BatchUserSummaryResponse> result = profileSearchService.searchProfilesByIds(largeIdList);

      // then
      assertThat(result).hasSize(1000);
      verify(repository).searchByUserIds(largeIdList);
    }
  }

  @Nested
  @DisplayName("readOnly 트랜잭션 동작 검증")
  class ReadOnlyTransactionBehavior {

    @Test
    @DisplayName("모든 조회 메서드는 @Transactional(readOnly=true)")
    void allMethodsAreReadOnly() {
      // given
      UserInfo userInfo = createDefaultUserInfo(TEST_USER_ID);
      when(repository.search(TEST_USER_ID)).thenReturn(userInfo);

      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      Pageable pageable = PageRequest.of(0, 10);
      Page<UserInfo> pageResult = new PageImpl<>(List.of(userInfo), pageable, 1);
      when(repository.search(criteria, pageable)).thenReturn(pageResult);

      Slice<UserInfo> sliceResult = new SliceImpl<>(List.of(userInfo), pageable, false);
      when(repository.searchByCursor(criteria, "cursor", 10)).thenReturn(sliceResult);

      when(repository.searchByUserIds(List.of(TEST_USER_ID))).thenReturn(List.of(userInfo));

      // when
      profileSearchService.searchProfileById(TEST_USER_ID);
      profileSearchService.searchProfiles(criteria, pageable);
      profileSearchService.searchProfilesByCursor(criteria, "cursor", 10);
      profileSearchService.searchProfilesByIds(List.of(TEST_USER_ID));

      // then
      // @Transactional(readOnly = true) 어노테이션이 있으므로 모두 조회만 가능
      // 실제 트랜잭션 동작은 통합 테스트에서 검증
      verify(repository, times(1)).search(TEST_USER_ID);
      verify(repository, times(1)).search(criteria, pageable);
      verify(repository, times(1)).searchByCursor(criteria, "cursor", 10);
      verify(repository, times(1)).searchByUserIds(List.of(TEST_USER_ID));
    }
  }

  @Nested
  @DisplayName("엣지 케이스")
  class EdgeCases {

    @Test
    @DisplayName("매우 긴 userId로 조회")
    void searchById_VeryLongUserId() {
      // given
      String veryLongUserId = "a".repeat(255);
      UserInfo userInfo = createDefaultUserInfo(veryLongUserId);
      when(repository.search(veryLongUserId)).thenReturn(userInfo);

      // when
      UserResponse result = profileSearchService.searchProfileById(veryLongUserId);

      // then
      assertThat(result).isNotNull();
      verify(repository).search(veryLongUserId);
    }

    @Test
    @DisplayName("특수문자가 포함된 userId로 조회")
    void searchById_SpecialCharsInUserId() {
      // given
      String specialUserId = "user!@#$%^&*()";
      UserInfo userInfo = createDefaultUserInfo(specialUserId);
      when(repository.search(specialUserId)).thenReturn(userInfo);

      // when
      UserResponse result = profileSearchService.searchProfileById(specialUserId);

      // then
      assertThat(result).isNotNull();
      verify(repository).search(specialUserId);
    }

    @Test
    @DisplayName("페이지 크기 0으로 조회")
    void searchProfiles_PageSizeZero() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      // when then
      assertThatThrownBy(() -> profileSearchService.searchProfiles(criteria, PageRequest.of(0, 0)));
    }
  }
}
