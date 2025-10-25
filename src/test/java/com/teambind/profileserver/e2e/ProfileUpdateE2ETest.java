package com.teambind.profileserver.e2e;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.profileserver.config.TestConfig;
import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.utils.InitTableMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileUpdate E2E 테스트
 *
 * <p>테스트 전략: 1. API 엔드포인트부터 DB까지 전체 플로우 테스트 2. 실제 HTTP 요청/응답 검증 3. Controller → Service →
 * Repository → DB 전체 흐름 확인 4. Location 검증 로직 포함한 실제 사용 시나리오 테스트 5. 성공/실패 케이스 모두 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("ProfileUpdate E2E 테스트")
class ProfileUpdateE2ETest {

  private static final String BASE_URL = "/api/profiles/profiles";
  private static final String TEST_USER_ID = "e2eTestUser";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserInfoRepository userInfoRepository;

  @Autowired private InitTableMapper initTableMapper;

  @BeforeEach
  void setUp() {
    // InitTableMapper 초기화
    initTableMapper.initializeTables();

    // 테스트용 장르/악기/지역 데이터 초기화
    InitTableMapper.genreNameTable = new java.util.HashMap<>();
    InitTableMapper.instrumentNameTable = new java.util.HashMap<>();
    InitTableMapper.locationNamesTable = new java.util.HashMap<>();

    createGenres().forEach(genre -> InitTableMapper.genreNameTable.put(genre.getGenreId(), genre));
    createInstruments()
        .forEach(
            instrument ->
                InitTableMapper.instrumentNameTable.put(instrument.getInstrumentId(), instrument));

    // 테스트용 지역 데이터 추가
    InitTableMapper.locationNamesTable.put("SEOUL", "서울");
    InitTableMapper.locationNamesTable.put("BUSAN", "부산");
    InitTableMapper.locationNamesTable.put("DAEGU", "대구");
    InitTableMapper.locationNamesTable.put("INCHEON", "인천");
    InitTableMapper.locationNamesTable.put("GWANGJU", "광주");

    // 테스트용 사용자 생성 및 저장
    UserInfo testUser = createDefaultUserInfo(TEST_USER_ID);
    testUser.setCity("SEOUL");
    testUser.setNickname("originalNick");
    userInfoRepository.save(testUser);
  }

  @Nested
  @DisplayName("Location 필드 E2E 테스트")
  class LocationE2ETests {

    @Test
    @DisplayName("성공 - 유효한 지역으로 업데이트 (API → DB 전체 플로우)")
    void updateCity_ValidLocation_E2E() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("updatedNick")
              .city("BUSAN")
              .introduction("부산 사는 뮤지션")
              .chattable(true)
              .publicProfile(true)
              .build();

      // when - API 호출
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").value(true));

      // then - DB 확인
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getCity()).isEqualTo("BUSAN");
      assertThat(updatedUser.getNickname()).isEqualTo("updatedNick");
      assertThat(updatedUser.getIntroduction()).isEqualTo("부산 사는 뮤지션");
    }

    @Test
    @DisplayName("실패 - 잘못된 지역으로 업데이트 (400 Bad Request)")
    void updateCity_InvalidLocation_Returns400() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testNick")
              .city("INVALID_CITY") // 존재하지 않는 지역
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then - API 호출 시 400 에러
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest());

      // then - DB는 변경되지 않음
      UserInfo unchangedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(unchangedUser.getCity()).isEqualTo("SEOUL"); // 원래 값 유지
      assertThat(unchangedUser.getNickname()).isEqualTo("originalNick"); // 원래 값 유지
    }

    @Test
    @DisplayName("성공 - null로 전달하면 기존 값 유지")
    void updateCity_Null_KeepsOriginalValue() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("newNick")
              .city(null) // null은 변경하지 않음
              .chattable(true)
              .publicProfile(true)
              .build();

      // when
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk());

      // then - city는 변경되지 않음
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getCity()).isEqualTo("SEOUL"); // 원래 값 유지
      assertThat(updatedUser.getNickname()).isEqualTo("newNick"); // nickname만 변경됨
    }

    @Test
    @DisplayName("성공 - 여러 번 지역 변경 시나리오")
    void updateCity_MultipleChanges_E2E() throws Exception {
      // given - 첫 번째 변경
      ProfileUpdateRequest request1 =
          ProfileUpdateRequest.builder()
              .nickname("testNick1")
              .city("BUSAN")
              .chattable(true)
              .publicProfile(true)
              .build();

      // when - 첫 번째 API 호출
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request1)))
          .andExpect(status().isOk());

      // then - 첫 번째 변경 확인
      UserInfo afterFirst = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(afterFirst.getCity()).isEqualTo("BUSAN");

      // given - 두 번째 변경
      ProfileUpdateRequest request2 =
          ProfileUpdateRequest.builder()
              .nickname("testNick2")
              .city("DAEGU")
              .chattable(true)
              .publicProfile(true)
              .build();

      // when - 두 번째 API 호출
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request2)))
          .andExpect(status().isOk());

      // then - 최종 값 확인
      UserInfo afterSecond = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(afterSecond.getCity()).isEqualTo("DAEGU");
      assertThat(afterSecond.getNickname()).isEqualTo("testNick2");
    }
  }

  @Nested
  @DisplayName("전체 프로필 업데이트 E2E 테스트")
  class FullProfileUpdateE2ETests {

    @Test
    @DisplayName("성공 - 모든 필드 업데이트 (E2E 전체 플로우)")
    void updateAllFields_Success_E2E() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("completeUpdate")
              .city("GWANGJU")
              .introduction("광주에서 활동하는 뮤지션입니다")
              .sex('F')
              .chattable(true)
              .publicProfile(false)
              .genres(List.of(1, 2, 3))
              .instruments(List.of(1, 2))
              .build();

      // when - API 호출
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value(true));

      // then - DB에 모든 변경사항 반영 확인
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getNickname()).isEqualTo("completeUpdate");
      assertThat(updatedUser.getCity()).isEqualTo("GWANGJU");
      assertThat(updatedUser.getIntroduction()).isEqualTo("광주에서 활동하는 뮤지션입니다");
      assertThat(updatedUser.getSex()).isEqualTo('F');
      assertThat(updatedUser.getIsChatable()).isTrue();
      assertThat(updatedUser.getIsPublic()).isFalse();
      assertThat(updatedUser.getUserGenres()).hasSize(3);
      assertThat(updatedUser.getUserInstruments()).hasSize(2);
    }

    @Test
    @DisplayName("성공 - 부분 업데이트만 수행")
    void updatePartialFields_E2E() throws Exception {
      // given - 초기 상태 설정
      UserInfo initialUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      initialUser.setIntroduction("기존 소개");
      initialUser.setSex('M');
      userInfoRepository.save(initialUser);

      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("partialUpdate")
              .city("INCHEON")
              // introduction, sex는 null (변경하지 않음)
              .chattable(false)
              .publicProfile(true)
              .build();

      // when
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      // then - 변경된 필드만 업데이트, 나머지는 유지
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getNickname()).isEqualTo("partialUpdate");
      assertThat(updatedUser.getCity()).isEqualTo("INCHEON");
      assertThat(updatedUser.getIntroduction()).isEqualTo("기존 소개"); // 유지
      assertThat(updatedUser.getSex()).isEqualTo('M'); // 유지
      assertThat(updatedUser.getIsChatable()).isFalse();
      assertThat(updatedUser.getIsPublic()).isTrue();
    }
  }

  @Nested
  @DisplayName("예외 상황 E2E 테스트")
  class ExceptionE2ETests {

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자 (404 Not Found)")
    void updateProfile_UserNotFound_Returns404() throws Exception {
      // given
      String nonExistentUserId = "nonExistentUser";
      ProfileUpdateRequest request = createBasicUpdateRequest();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", nonExistentUserId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("실패 - 닉네임 중복 (409 Conflict)")
    void updateProfile_DuplicateNickname_Returns409() throws Exception {
      // given - 다른 사용자 생성
      String anotherUserId = "anotherUser";
      UserInfo anotherUser = createDefaultUserInfo(anotherUserId);
      anotherUser.setNickname("existingNick");
      userInfoRepository.save(anotherUser);

      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("existingNick") // 중복된 닉네임
              .city("SEOUL")
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isConflict());

      // then - DB는 변경되지 않음
      UserInfo unchangedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(unchangedUser.getNickname()).isEqualTo("originalNick");
    }

    @Test
    @DisplayName("실패 - 잘못된 JSON 형식 (400 Bad Request)")
    void updateProfile_InvalidJson_Returns400() throws Exception {
      // given
      String invalidJson = "{ invalid json }";

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidJson))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("장르/악기 E2E 테스트")
  class GenreInstrumentE2ETests {

    @Test
    @DisplayName("성공 - 장르 추가 E2E")
    void addGenres_E2E() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("genreUser")
              .genres(List.of(1, 2, 3))
              .chattable(true)
              .publicProfile(true)
              .build();

      // when
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      // then
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getUserGenres()).hasSize(3);
    }

    @Test
    @DisplayName("성공 - 악기 변경 E2E")
    void updateInstruments_E2E() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("instrumentUser")
              .instruments(List.of(1, 2))
              .chattable(true)
              .publicProfile(true)
              .build();

      // when
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      // then
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getUserInstruments()).hasSize(2);
    }

    @Test
    @DisplayName("성공 - 장르/악기/지역 동시 업데이트 E2E")
    void updateGenresInstrumentsAndCity_E2E() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("completeUser")
              .city("BUSAN")
              .genres(List.of(1, 2))
              .instruments(List.of(3, 4))
              .chattable(true)
              .publicProfile(true)
              .build();

      // when
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      // then
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getCity()).isEqualTo("BUSAN");
      assertThat(updatedUser.getUserGenres()).hasSize(2);
      assertThat(updatedUser.getUserInstruments()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("실제 사용 시나리오 E2E 테스트")
  class RealWorldScenarioE2ETests {

    @Test
    @DisplayName("시나리오: 사용자가 서울에서 부산으로 이사 후 프로필 업데이트")
    void scenario_UserRelocatesFromSeoulToBusan() throws Exception {
      // given - 초기 상태: 서울 거주
      UserInfo initialUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(initialUser.getCity()).isEqualTo("SEOUL");

      // when - 부산으로 이사 후 프로필 업데이트
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("busanMusician")
              .city("BUSAN")
              .introduction("Relocated from Seoul to Busan!")
              .chattable(true)
              .publicProfile(true)
              .build();

      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());

      // then - 지역 정보 변경 확인
      UserInfo relocatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(relocatedUser.getCity()).isEqualTo("BUSAN");
      assertThat(relocatedUser.getNickname()).isEqualTo("busanMusician");
      assertThat(relocatedUser.getIntroduction()).contains("Busan");
    }

    @Test
    @DisplayName("시나리오: 뮤지션이 장르를 록에서 재즈로 변경")
    void scenario_MusicianChangesGenreFromRockToJazz() throws Exception {
      // given - 초기: 록 장르
      ProfileUpdateRequest initialRequest =
          ProfileUpdateRequest.builder()
              .nickname("rockMusician")
              .genres(List.of(1)) // Rock
              .chattable(true)
              .publicProfile(true)
              .build();

      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(initialRequest)))
          .andExpect(status().isOk());

      // when - 재즈로 변경
      ProfileUpdateRequest changeRequest =
          ProfileUpdateRequest.builder()
              .nickname("jazzMusician")
              .genres(List.of(2)) // Jazz
              .chattable(true)
              .publicProfile(true)
              .build();

      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(changeRequest)))
          .andExpect(status().isOk());

      // then - 장르 변경 확인
      UserInfo updatedUser = userInfoRepository.findById(TEST_USER_ID).orElseThrow();
      assertThat(updatedUser.getUserGenres()).hasSize(1);
      assertThat(updatedUser.getUserGenres().get(0).getGenre().getGenreId()).isEqualTo(2);
    }
  }
}
