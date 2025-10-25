package com.teambind.profileserver.repository;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.assertj.core.api.Assertions.*;

import com.teambind.profileserver.config.TestConfig;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.utils.InitTableMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserInfoRepository 테스트
 *
 * <p>테스트 전략: 1. @DataJpaTest로 JPA 슬라이스 테스트 2. 실제 H2 in-memory DB 사용 3. Repository 메서드 단위 테스트 4. 저장,
 * 조회, 존재 여부 확인 등 기본 CRUD 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({TestConfig.class})
@DisplayName("UserInfoRepository 테스트")
class UserInfoRepositoryTest {

  @Autowired private UserInfoRepository userInfoRepository;

  @BeforeEach
  void setUp() {
    // 테스트 전 DB 클리어
    userInfoRepository.deleteAll();
  }

  @Nested
  @DisplayName("기본 CRUD 테스트")
  class BasicCrudTests {

    @Test
    @DisplayName("성공 - 사용자 저장 및 조회")
    void save_AndFindById_Success() {
      // given
      UserInfo userInfo = createDefaultUserInfo("testUser1");

      // when
      UserInfo saved = userInfoRepository.save(userInfo);
      UserInfo found = userInfoRepository.findById("testUser1").orElse(null);

      // then
      assertThat(saved).isNotNull();
      assertThat(found).isNotNull();
      assertThat(found.getUserId()).isEqualTo("testUser1");
      assertThat(found.getNickname()).isEqualTo(saved.getNickname());
    }

    @Test
    @DisplayName("성공 - 사용자 삭제")
    void delete_Success() {
      // given
      UserInfo userInfo = createDefaultUserInfo("testUser2");
      userInfoRepository.save(userInfo);

      // when
      userInfoRepository.deleteById("testUser2");

      // then
      assertThat(userInfoRepository.findById("testUser2")).isEmpty();
    }

    @Test
    @DisplayName("성공 - 모든 사용자 조회")
    void findAll_Success() {
      // given
      userInfoRepository.save(createDefaultUserInfo("user1"));
      userInfoRepository.save(createDefaultUserInfo("user2"));
      userInfoRepository.save(createDefaultUserInfo("user3"));

      // when
      var allUsers = userInfoRepository.findAll();

      // then
      assertThat(allUsers).hasSize(3);
    }

    @Test
    @DisplayName("성공 - 사용자 업데이트")
    void update_Success() {
      // given
      UserInfo userInfo = createDefaultUserInfo("testUser3");
      userInfoRepository.save(userInfo);

      // when
      UserInfo found = userInfoRepository.findById("testUser3").orElseThrow();
      found.setNickname("updatedNickname");
      found.setCity("BUSAN");
      userInfoRepository.save(found);

      // then
      UserInfo updated = userInfoRepository.findById("testUser3").orElseThrow();
      assertThat(updated.getNickname()).isEqualTo("updatedNickname");
      assertThat(updated.getCity()).isEqualTo("BUSAN");
    }
  }

  @Nested
  @DisplayName("커스텀 쿼리 메서드 테스트")
  class CustomQueryMethodTests {

    @Test
    @DisplayName("성공 - 닉네임 존재 여부 확인 (존재함)")
    void existsByNickname_Exists_ReturnsTrue() {
      // given
      UserInfo userInfo = createDefaultUserInfo("testUser4");
      userInfo.setNickname("uniqueNick");
      userInfoRepository.save(userInfo);

      // when
      boolean exists = userInfoRepository.existsByNickname("uniqueNick");

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("성공 - 닉네임 존재 여부 확인 (존재하지 않음)")
    void existsByNickname_NotExists_ReturnsFalse() {
      // when
      boolean exists = userInfoRepository.existsByNickname("nonExistentNick");

      // then
      assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("성공 - 특정 prefix로 시작하는 userId 개수 세기")
    void countByUserIdStartingWith_Success() {
      // given
      userInfoRepository.save(createDefaultUserInfo("test_001"));
      userInfoRepository.save(createDefaultUserInfo("test_002"));
      userInfoRepository.save(createDefaultUserInfo("test_003"));
      userInfoRepository.save(createDefaultUserInfo("other_001"));

      // when
      long count = userInfoRepository.countByUserIdStartingWith("test_");

      // then
      assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("성공 - prefix로 시작하는 userId가 없을 때 0 반환")
    void countByUserIdStartingWith_NoMatches_ReturnsZero() {
      // given
      userInfoRepository.save(createDefaultUserInfo("user001"));

      // when
      long count = userInfoRepository.countByUserIdStartingWith("test_");

      // then
      assertThat(count).isZero();
    }
  }

  @Nested
  @DisplayName("제약 조건 테스트")
  class ConstraintTests {

    @Test
    @DisplayName("성공 - 서로 다른 userId로 저장")
    void save_DifferentUserId_Success() {
      // given
      UserInfo user1 = createDefaultUserInfo("user1");
      UserInfo user2 = createDefaultUserInfo("user2");

      // when
      userInfoRepository.save(user1);
      userInfoRepository.save(user2);
      userInfoRepository.flush();

      // then
      assertThat(userInfoRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("실패 - 중복된 nickname으로 저장 시 예외 발생")
    void save_DuplicateNickname_ThrowsException() {
      // given
      UserInfo user1 = createDefaultUserInfo("user1");
      user1.setNickname("sameNick");
      userInfoRepository.save(user1);

      // when & then
      UserInfo user2 = createDefaultUserInfo("user2");
      user2.setNickname("sameNick");
      assertThatThrownBy(
              () -> {
                userInfoRepository.save(user2);
                userInfoRepository.flush();
              })
          .isInstanceOf(Exception.class);
    }
  }

  @Nested
  @DisplayName("연관관계 테스트")
  class RelationshipTests {

    @BeforeEach
    void setUpRelationships() {
      // 장르/악기 데이터 초기화
      InitTableMapper.genreNameTable = new java.util.HashMap<>();
      InitTableMapper.instrumentNameTable = new java.util.HashMap<>();

      createGenres()
          .forEach(genre -> InitTableMapper.genreNameTable.put(genre.getGenreId(), genre));
      createInstruments()
          .forEach(
              instrument ->
                  InitTableMapper.instrumentNameTable.put(
                      instrument.getInstrumentId(), instrument));
    }

    @Test
    @DisplayName("성공 - 장르와 함께 사용자 저장")
    void save_UserWithGenres_Success() {
      // given
      UserInfo userInfo = createDefaultUserInfo("userWithGenres");
      userInfo.addGenre(InitTableMapper.genreNameTable.get(1));
      userInfo.addGenre(InitTableMapper.genreNameTable.get(2));

      // when
      UserInfo saved = userInfoRepository.save(userInfo);
      userInfoRepository.flush();

      // then
      UserInfo found = userInfoRepository.findById("userWithGenres").orElseThrow();
      assertThat(found.getUserGenres()).hasSize(2);
    }

    @Test
    @DisplayName("성공 - 악기와 함께 사용자 저장")
    void save_UserWithInstruments_Success() {
      // given
      UserInfo userInfo = createDefaultUserInfo("userWithInstruments");
      userInfo.addInstrument(InitTableMapper.instrumentNameTable.get(1));
      userInfo.addInstrument(InitTableMapper.instrumentNameTable.get(2));

      // when
      UserInfo saved = userInfoRepository.save(userInfo);
      userInfoRepository.flush();

      // then
      UserInfo found = userInfoRepository.findById("userWithInstruments").orElseThrow();
      assertThat(found.getUserInstruments()).hasSize(2);
    }

    @Test
    @DisplayName("성공 - cascade를 통한 장르 삭제")
    void delete_UserWithGenres_CascadeDeletes() {
      // given
      UserInfo userInfo = createDefaultUserInfo("userToDelete");
      userInfo.addGenre(InitTableMapper.genreNameTable.get(1));
      userInfoRepository.save(userInfo);

      // when
      userInfoRepository.deleteById("userToDelete");
      userInfoRepository.flush();

      // then
      assertThat(userInfoRepository.findById("userToDelete")).isEmpty();
    }
  }

  @Nested
  @DisplayName("페이징 및 정렬 테스트")
  class PagingAndSortingTests {

    @Test
    @DisplayName("성공 - 페이징 처리")
    void findAll_WithPaging_Success() {
      // given
      for (int i = 1; i <= 10; i++) {
        userInfoRepository.save(createDefaultUserInfo("user" + i));
      }

      // when
      var page = userInfoRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 5));

      // then
      assertThat(page.getContent()).hasSize(5);
      assertThat(page.getTotalElements()).isEqualTo(10);
      assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("성공 - 정렬 처리")
    void findAll_WithSort_Success() {
      // given
      UserInfo user1 = createDefaultUserInfo("user1");
      user1.setNickname("charlie");
      UserInfo user2 = createDefaultUserInfo("user2");
      user2.setNickname("alice");
      UserInfo user3 = createDefaultUserInfo("user3");
      user3.setNickname("bob");

      userInfoRepository.save(user1);
      userInfoRepository.save(user2);
      userInfoRepository.save(user3);

      // when
      var sorted =
          userInfoRepository.findAll(
              org.springframework.data.domain.Sort.by("nickname").ascending());

      // then
      assertThat(sorted).hasSize(3);
      assertThat(sorted.get(0).getNickname()).isEqualTo("alice");
      assertThat(sorted.get(1).getNickname()).isEqualTo("bob");
      assertThat(sorted.get(2).getNickname()).isEqualTo("charlie");
    }
  }
}
