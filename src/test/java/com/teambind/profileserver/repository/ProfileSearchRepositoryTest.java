package com.teambind.profileserver.repository;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.assertj.core.api.Assertions.*;

import com.teambind.profileserver.config.TestConfig;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import com.teambind.profileserver.utils.InitTableMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileSearchRepository 테스트
 *
 * <p>테스트 전략: 1. @SpringBootTest로 전체 Context 로드 (QueryDSL 설정 포함) 2. 실제 DB 사용하여 QueryDSL 쿼리 검증 3. 다양한
 * 검색 조건 조합 테스트 4. 페이징 및 커서 기반 페이징 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("ProfileSearchRepository 테스트")
class ProfileSearchRepositoryTest {

  @Autowired private ProfileSearchRepository profileSearchRepository;

  @Autowired private UserInfoRepository userInfoRepository;

  @Autowired private InitTableMapper initTableMapper;

  @BeforeEach
  void setUp() {
    // @Transactional이 롤백을 처리하므로 데이터 격리 보장
    // initTableMapper.initializeTables()는 DB를 읽으므로 호출하지 않음

    // 테스트용 장르/악기/지역 데이터를 정적 맵에 직접 설정
    InitTableMapper.genreNameTable = new java.util.HashMap<>();
    InitTableMapper.instrumentNameTable = new java.util.HashMap<>();
    InitTableMapper.locationNamesTable = new java.util.HashMap<>();

    createGenres().forEach(genre -> InitTableMapper.genreNameTable.put(genre.getGenreId(), genre));
    createInstruments()
        .forEach(
            instrument ->
                InitTableMapper.instrumentNameTable.put(instrument.getInstrumentId(), instrument));

    InitTableMapper.locationNamesTable.put("SEOUL", "서울");
    InitTableMapper.locationNamesTable.put("BUSAN", "부산");
    InitTableMapper.locationNamesTable.put("DAEGU", "대구");
  }

  @Nested
  @DisplayName("단일 사용자 검색 테스트")
  class SearchByIdTests {

    @Test
    @DisplayName("성공 - userId로 사용자 검색")
    void search_ById_Success() {
      // given
      UserInfo userInfo = createDefaultUserInfo("searchUser1");
      userInfo.setCity("SEOUL");
      userInfoRepository.save(userInfo);

      // when
      UserInfo found = profileSearchRepository.search("searchUser1");

      // then
      assertThat(found).isNotNull();
      assertThat(found.getUserId()).isEqualTo("searchUser1");
      assertThat(found.getCity()).isEqualTo("SEOUL");
    }

    @Test
    @DisplayName("성공 - 존재하지 않는 userId로 검색 시 null 반환")
    void search_ById_NotFound_ReturnsNull() {
      // when
      UserInfo found = profileSearchRepository.search("nonExistentUser");

      // then
      assertThat(found).isNull();
    }

    @Test
    @DisplayName("성공 - 장르/악기가 포함된 사용자 검색")
    void search_ById_WithCollections_Success() {
      // given
      UserInfo userInfo = createDefaultUserInfo("userWithCollections");
      userInfo.addGenre(InitTableMapper.genreNameTable.get(1));
      userInfo.addInstrument(InitTableMapper.instrumentNameTable.get(1));
      userInfoRepository.save(userInfo);

      // when
      UserInfo found = profileSearchRepository.search("userWithCollections");

      // then
      assertThat(found).isNotNull();
      assertThat(found.getUserGenres()).hasSize(1);
      assertThat(found.getUserInstruments()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("조건별 검색 테스트 (페이징)")
  class SearchByCriteriaTests {

    @BeforeEach
    void setUpTestData() {
      // 다양한 조건의 테스트 데이터 생성
      UserInfo user1 = createDefaultUserInfo("user1");
      user1.setNickname("alice");
      user1.setCity("SEOUL");
      user1.setSex('F');
      user1.addGenre(InitTableMapper.genreNameTable.get(1)); // Rock
      user1.addInstrument(InitTableMapper.instrumentNameTable.get(1)); // Guitar
      userInfoRepository.save(user1);

      UserInfo user2 = createDefaultUserInfo("user2");
      user2.setNickname("bob");
      user2.setCity("BUSAN");
      user2.setSex('M');
      user2.addGenre(InitTableMapper.genreNameTable.get(2)); // Jazz
      user2.addInstrument(InitTableMapper.instrumentNameTable.get(2)); // Piano
      userInfoRepository.save(user2);

      UserInfo user3 = createDefaultUserInfo("user3");
      user3.setNickname("charlie");
      user3.setCity("SEOUL");
      user3.setSex('M');
      user3.addGenre(InitTableMapper.genreNameTable.get(1)); // Rock
      user3.addInstrument(InitTableMapper.instrumentNameTable.get(3)); // Drum
      userInfoRepository.save(user3);

      UserInfo user4 = createDefaultUserInfo("user4");
      user4.setNickname("diana");
      user4.setCity("DAEGU");
      user4.setSex('F');
      userInfoRepository.save(user4);
    }

    @Test
    @DisplayName("성공 - 조건 없이 모든 사용자 검색")
    void search_NoCriteria_ReturnsAll() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(4);
      assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("성공 - city로 검색")
    void search_ByCity_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("SEOUL").build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent()).allMatch(user -> user.getCity().equals("SEOUL"));
    }

    @Test
    @DisplayName("성공 - nickname으로 검색 (부분 일치)")
    void search_ByNickname_PartialMatch_Success() {
      // given
      ProfileSearchCriteria criteria =
          ProfileSearchCriteria.builder()
              .nickName("li") // alice, charlie
              .build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("성공 - sex로 검색")
    void search_BySex_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().sex('F').build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent()).allMatch(user -> user.getSex() == 'F');
    }

    @Test
    @DisplayName("성공 - 장르로 검색")
    void search_ByGenres_Success() {
      // given
      ProfileSearchCriteria criteria =
          ProfileSearchCriteria.builder()
              .genres(List.of(1)) // Rock
              .build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(2); // user1, user3
    }

    @Test
    @DisplayName("성공 - 악기로 검색")
    void search_ByInstruments_Success() {
      // given
      ProfileSearchCriteria criteria =
          ProfileSearchCriteria.builder()
              .instruments(List.of(1)) // Guitar
              .build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getNickname()).isEqualTo("alice");
    }

    @Test
    @DisplayName("성공 - 복합 조건으로 검색 (city + sex)")
    void search_ByCityAndSex_Success() {
      // given
      ProfileSearchCriteria criteria =
          ProfileSearchCriteria.builder().city("SEOUL").sex('M').build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getNickname()).isEqualTo("charlie");
    }

    @Test
    @DisplayName("성공 - 복합 조건으로 검색 (city + genre + instrument)")
    void search_ByCityGenreInstrument_Success() {
      // given
      ProfileSearchCriteria criteria =
          ProfileSearchCriteria.builder()
              .city("SEOUL")
              .genres(List.of(1)) // Rock
              .instruments(List.of(1)) // Guitar
              .build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getNickname()).isEqualTo("alice");
    }

    @Test
    @DisplayName("성공 - 조건에 맞는 결과가 없을 때 빈 페이지 반환")
    void search_NoMatches_ReturnsEmptyPage() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("NONEXISTENT").build();
      PageRequest pageable = PageRequest.of(0, 10);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isZero();
    }
  }

  @Nested
  @DisplayName("페이징 테스트")
  class PagingTests {

    @BeforeEach
    void setUpPagingData() {
      for (int i = 1; i <= 15; i++) {
        UserInfo user = createDefaultUserInfo("pagingUser" + i);
        user.setNickname("user" + i);
        user.setCity("SEOUL");
        userInfoRepository.save(user);
      }
    }

    @Test
    @DisplayName("성공 - 첫 번째 페이지 조회")
    void search_FirstPage_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("SEOUL").build();
      PageRequest pageable = PageRequest.of(0, 5);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(5);
      assertThat(result.getTotalElements()).isEqualTo(15);
      assertThat(result.getTotalPages()).isEqualTo(3);
      assertThat(result.isFirst()).isTrue();
      assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("성공 - 두 번째 페이지 조회")
    void search_SecondPage_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("SEOUL").build();
      PageRequest pageable = PageRequest.of(1, 5);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(5);
      assertThat(result.getNumber()).isEqualTo(1);
      assertThat(result.hasNext()).isTrue();
      assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("성공 - 마지막 페이지 조회")
    void search_LastPage_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("SEOUL").build();
      PageRequest pageable = PageRequest.of(2, 5);

      // when
      Page<UserInfo> result = profileSearchRepository.search(criteria, pageable);

      // then
      assertThat(result.getContent()).hasSize(5);
      assertThat(result.isLast()).isTrue();
      assertThat(result.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("커서 기반 페이징 테스트")
  class CursorPagingTests {

    @BeforeEach
    void setUpCursorData() {
      for (int i = 1; i <= 10; i++) {
        UserInfo user = createDefaultUserInfo(String.format("cursorUser%02d", i));
        user.setNickname("cursor" + i);
        user.setCity("SEOUL");
        userInfoRepository.save(user);
      }
    }

    @Test
    @DisplayName("성공 - 첫 페이지 조회 (cursor 없음)")
    void searchByCursor_FirstPage_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("SEOUL").build();

      // when
      Slice<UserInfo> result = profileSearchRepository.searchByCursor(criteria, null, 5);

      // then
      assertThat(result.getContent()).hasSize(5);
      assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("성공 - 다음 페이지 조회 (cursor 사용)")
    void searchByCursor_NextPage_Success() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("SEOUL").build();

      // 첫 페이지 조회
      Slice<UserInfo> firstPage = profileSearchRepository.searchByCursor(criteria, null, 5);
      String cursor = firstPage.getContent().get(4).getUserId();

      // when - 두 번째 페이지 조회
      Slice<UserInfo> secondPage = profileSearchRepository.searchByCursor(criteria, cursor, 5);

      // then
      assertThat(secondPage.getContent()).hasSize(5);
      assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("성공 - 마지막 페이지에서 hasNext는 false")
    void searchByCursor_LastPage_HasNextFalse() {
      // given
      ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().city("SEOUL").build();

      // when
      Slice<UserInfo> result = profileSearchRepository.searchByCursor(criteria, null, 15);

      // then
      assertThat(result.getContent()).hasSize(10);
      assertThat(result.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("일괄 검색 테스트")
  class BatchSearchTests {

    @Test
    @DisplayName("성공 - 여러 userId로 일괄 검색")
    void searchByUserIds_Success() {
      // given
      userInfoRepository.save(createDefaultUserInfo("batch1"));
      userInfoRepository.save(createDefaultUserInfo("batch2"));
      userInfoRepository.save(createDefaultUserInfo("batch3"));

      List<String> userIds = List.of("batch1", "batch2", "batch3");

      // when
      List<UserInfo> result = profileSearchRepository.searchByUserIds(userIds);

      // then
      assertThat(result).hasSize(3);
      assertThat(result)
          .extracting("userId")
          .containsExactlyInAnyOrder("batch1", "batch2", "batch3");
    }

    @Test
    @DisplayName("성공 - 빈 리스트로 검색 시 빈 결과 반환")
    void searchByUserIds_EmptyList_ReturnsEmpty() {
      // when
      List<UserInfo> result = profileSearchRepository.searchByUserIds(List.of());

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - null 리스트로 검색 시 빈 결과 반환")
    void searchByUserIds_NullList_ReturnsEmpty() {
      // when
      List<UserInfo> result = profileSearchRepository.searchByUserIds(null);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - 일부만 존재하는 userId로 검색")
    void searchByUserIds_PartialExists_ReturnsExisting() {
      // given
      userInfoRepository.save(createDefaultUserInfo("exists1"));
      userInfoRepository.save(createDefaultUserInfo("exists2"));

      List<String> userIds = List.of("exists1", "exists2", "notExists1", "notExists2");

      // when
      List<UserInfo> result = profileSearchRepository.searchByUserIds(userIds);

      // then
      assertThat(result).hasSize(2);
      assertThat(result).extracting("userId").containsExactlyInAnyOrder("exists1", "exists2");
    }
  }
}
