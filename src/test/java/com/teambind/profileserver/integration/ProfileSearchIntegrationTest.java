package com.teambind.profileserver.integration;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.assertj.core.api.Assertions.*;

import com.teambind.profileserver.config.TestConfig;
import com.teambind.profileserver.dto.response.BatchUserSummaryResponse;
import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import com.teambind.profileserver.service.search.ProfileSearchService;
import com.teambind.profileserver.utils.InitTableMapper;
import java.util.ArrayList;
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
 * ProfileSearch 통합 테스트
 *
 * 테스트 전략:
 * 1. Service → Repository → DB 전체 플로우 검증
 * 2. DTO 변환 로직 검증
 * 3. 실제 검색 시나리오 테스트
 * 4. 예외 상황 처리 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("ProfileSearch 통합 테스트")
class ProfileSearchIntegrationTest {

    @Autowired
    private ProfileSearchService profileSearchService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private InitTableMapper initTableMapper;

    @BeforeEach
    void setUp() {
        // @Transactional이 각 테스트를 롤백하므로 데이터 격리 보장
        // initTableMapper.initializeTables()는 DB 테이블을 읽으므로 호출 안 함

        // 테스트용 장르/악기/지역 데이터를 정적 맵에 직접 설정
        InitTableMapper.genreNameTable = new java.util.HashMap<>();
        InitTableMapper.instrumentNameTable = new java.util.HashMap<>();
        InitTableMapper.locationNamesTable = new java.util.HashMap<>();

        createGenres().forEach(genre ->
                InitTableMapper.genreNameTable.put(genre.getGenreId(), genre));
        createInstruments().forEach(instrument ->
                InitTableMapper.instrumentNameTable.put(instrument.getInstrumentId(), instrument));

        InitTableMapper.locationNamesTable.put("SEOUL", "서울");
        InitTableMapper.locationNamesTable.put("BUSAN", "부산");
        InitTableMapper.locationNamesTable.put("DAEGU", "대구");
        InitTableMapper.locationNamesTable.put("INCHEON", "인천");
    }

    @Test
    @DisplayName("성공 - 여러 ID로 일괄 검색 및 요약 DTO 반환")
    void searchProfilesByIds_Success() {
    // given

    List<UserInfo> users = new ArrayList<>();
	for(int i = 0 ; i < 10 ; i++) {
		users.add(createDefaultUserInfo("batch" + i));
		users.get(i).setNickname("batchUser" + i);
		users.get(i).setCity("SEOUL");
		users.get(i).addGenre(InitTableMapper.genreNameTable.get(1)); // Rock
		users.get(i).addInstrument(InitTableMapper.instrumentNameTable.get(1)); // Guitar
		
		
	}
	userInfoRepository.saveAll(users);
        List<String> userIds = List.of("batch1", "batch2", "batch3");

        // when
        List<BatchUserSummaryResponse> result = profileSearchService.searchProfilesByIds(userIds);

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting("userId")
                .containsExactlyInAnyOrder("batch1", "batch2", "batch3");
    }

    @Nested
    @DisplayName("단일 프로필 검색 통합 테스트")
    class SearchProfileByIdTests {

        @Test
        @DisplayName("성공 - ID로 프로필 검색 및 DTO 변환")
        void searchProfileById_Success() {
            // given
            UserInfo userInfo = createDefaultUserInfo("integrationUser1");
            userInfo.setCity("SEOUL");
            userInfo.setNickname("alice");
            userInfo.addGenre(InitTableMapper.genreNameTable.get(1));
            userInfoRepository.save(userInfo);

            // when
            UserResponse response = profileSearchService.searchProfileById("integrationUser1");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo("integrationUser1");
            assertThat(response.getNickname()).isEqualTo("alice");
            assertThat(response.getCity()).isEqualTo("서울"); // Location ID → Name 매핑 확인
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID로 검색 시 예외 발생")
        void searchProfileById_NotFound_ThrowsException() {
            // when & then
            assertThatThrownBy(() -> profileSearchService.searchProfileById("nonExistentUser"))
                    .isInstanceOf(ProfileException.class);
        }
    }

    @Nested
    @DisplayName("조건별 프로필 검색 통합 테스트")
    class SearchProfilesTests {

        @BeforeEach
        void setUpSearchData() {
            // 서울 거주 Rock 뮤지션
            UserInfo seoul1 = createDefaultUserInfo("seoul_rock_1");
            seoul1.setNickname("seoulRocker");
            seoul1.setCity("SEOUL");
            seoul1.setSex('M');
            seoul1.addGenre(InitTableMapper.genreNameTable.get(1)); // Rock
            seoul1.addInstrument(InitTableMapper.instrumentNameTable.get(1)); // Guitar
            userInfoRepository.save(seoul1);

            // 서울 거주 Jazz 뮤지션
            UserInfo seoul2 = createDefaultUserInfo("seoul_jazz_1");
            seoul2.setNickname("seoulJazzer");
            seoul2.setCity("SEOUL");
            seoul2.setSex('F');
            seoul2.addGenre(InitTableMapper.genreNameTable.get(2)); // Jazz
            seoul2.addInstrument(InitTableMapper.instrumentNameTable.get(2)); // Piano
            userInfoRepository.save(seoul2);

            // 부산 거주 Rock 뮤지션
            UserInfo busan1 = createDefaultUserInfo("busan_rock_1");
            busan1.setNickname("busanRocker");
            busan1.setCity("BUSAN");
            busan1.setSex('M');
            busan1.addGenre(InitTableMapper.genreNameTable.get(1)); // Rock
            busan1.addInstrument(InitTableMapper.instrumentNameTable.get(3)); // Drum
            userInfoRepository.save(busan1);
        }

        @Test
        @DisplayName("성공 - 지역으로 검색")
        void searchProfiles_ByCity_Success() {
            // given
            ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
                    .city("SEOUL")
                    .build();
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .allMatch(response -> response.getCity().equals("서울"));
        }

        @Test
        @DisplayName("성공 - 장르로 검색")
        void searchProfiles_ByGenre_Success() {
            // given
            ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
                    .genres(List.of(1)) // Rock
                    .build();
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting("nickname")
                    .containsExactlyInAnyOrder("seoulRocker", "busanRocker");
        }

        @Test
        @DisplayName("성공 - 복합 조건 검색 (지역 + 장르)")
        void searchProfiles_ByCityAndGenre_Success() {
            // given
            ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
                    .city("SEOUL")
                    .genres(List.of(1)) // Rock
                    .build();
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNickname()).isEqualTo("seoulRocker");
        }

        @Test
        @DisplayName("성공 - 성별로 검색")
        void searchProfiles_BySex_Success() {
            // given
            ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
                    .sex('F')
                    .build();
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getNickname()).isEqualTo("seoulJazzer");
        }

        @Test
        @DisplayName("성공 - 닉네임 부분 일치 검색")
        void searchProfiles_ByNickname_PartialMatch_Success() {
            // given
            ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
                    .nickName("Rock")
                    .build();
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<UserResponse> result = profileSearchService.searchProfiles(criteria, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("커서 기반 페이징 통합 테스트")
    class SearchProfilesByCursorTests {

        @BeforeEach
        void setUpCursorData() {
            for (int i = 1; i <= 10; i++) {
                UserInfo user = createDefaultUserInfo(String.format("cursor_%02d", i));
                user.setNickname("user" + i);
                user.setCity("SEOUL");
                userInfoRepository.save(user);
            }
        }

        @Test
        @DisplayName("성공 - 첫 페이지 조회")
        void searchByCursor_FirstPage_Success() {
            // given
            ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
                    .city("SEOUL")
                    .build();

            // when
            Slice<UserResponse> result = profileSearchService.searchProfilesByCursor(criteria, null, 5);

            // then
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("성공 - 다음 페이지 조회")
        void searchByCursor_NextPage_Success() {
            // given
            ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
                    .city("SEOUL")
                    .build();

            Slice<UserResponse> firstPage = profileSearchService.searchProfilesByCursor(criteria, null, 5);
            String cursor = firstPage.getContent().get(4).getUserId();

            // when
            Slice<UserResponse> secondPage = profileSearchService.searchProfilesByCursor(criteria, cursor, 5);

            // then
            assertThat(secondPage.getContent()).hasSize(5);
            assertThat(secondPage.hasNext()).isFalse();
        }
    }
}

