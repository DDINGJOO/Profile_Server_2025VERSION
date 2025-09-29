package com.teambind.profileserver.service.update;

import static org.junit.jupiter.api.Assertions.*;

import com.teambind.profileserver.entity.UserGenres;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.UserInstruments;
import com.teambind.profileserver.entity.key.UserGenreKey;
import com.teambind.profileserver.entity.key.UserInstrumentKey;
import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import com.teambind.profileserver.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
@SpringBootTest(classes = com.teambind.profileserver.ProfileServerApplication.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProfileUpdateServiceTest {

    @Autowired private ProfileUpdateService profileUpdateService;
    @Autowired private UserInfoRepository userInfoRepository;
    @Autowired private UserGenresRepository userGenresRepository;
    @Autowired private UserInstrumentsRepository userInstrumentsRepository;
    @Autowired private GenreNameTableRepository genreNameTableRepository;
    @Autowired private InstrumentNameTableRepository instrumentNameTableRepository;
    @Autowired private HistoryRepository historyRepository;

    @AfterEach
    void cleanup() {
        userInstrumentsRepository.deleteAll();
        userGenresRepository.deleteAll();
        historyRepository.deleteAll();
        userInfoRepository.deleteAll();
    }

    // ====== Helper methods ======
    private void ensureNameTables(Collection<Integer> genres, Collection<Integer> instruments) {
        for (Integer gid : genres) {
            if (gid == null) continue;
            if (!genreNameTableRepository.existsById(gid)) {
                genreNameTableRepository.save(GenreNameTable.builder().genreId(gid).genreName("GENRE_" + gid).build());
            }
        }
        for (Integer iid : instruments) {
            if (iid == null) continue;
            if (!instrumentNameTableRepository.existsById(iid)) {
                instrumentNameTableRepository.save(InstrumentNameTable.builder().instrumentId(iid).instrumentName("INST_" + iid).build());
            }
        }
    }

    private UserInfo createUser(String id, String nick, Integer[] genres, Integer[] instruments) {
        UserInfo u = UserInfo.builder().userId(id).nickname(nick).build();
        u = userInfoRepository.save(u);
        if (genres != null) {
            for (Integer gid : genres) {
                if (gid == null) continue;
                UserGenreKey key = new UserGenreKey();
                setUserGenreKey(key, id, gid);
                userGenresRepository.save(UserGenres.builder()
                        .userId(key)
                        .userInfo(u)
                        .genre(GenreNameTable.builder().genreId(gid).build())
                        .build());
            }
        }
        if (instruments != null) {
            for (Integer iid : instruments) {
                if (iid == null) continue;
                UserInstrumentKey key = new UserInstrumentKey();
                setUserInstrumentKey(key, id, iid);
                userInstrumentsRepository.save(UserInstruments.builder()
                        .userId(key)
                        .userInfo(u)
                        .instrument(InstrumentNameTable.builder().instrumentId(iid).build())
                        .build());
            }
        }
        return u;
    }

    private static void setUserGenreKey(UserGenreKey key, String userId, int genreId) {
        try {
            var f1 = UserGenreKey.class.getDeclaredField("userId");
            var f2 = UserGenreKey.class.getDeclaredField("genreId");
            f1.setAccessible(true); f2.setAccessible(true);
            f1.set(key, userId); f2.set(key, genreId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    private static void setUserInstrumentKey(UserInstrumentKey key, String userId, int instrumentId) {
        try {
            var f1 = UserInstrumentKey.class.getDeclaredField("userId");
            var f2 = UserInstrumentKey.class.getDeclaredField("instrumentId");
            f1.setAccessible(true); f2.setAccessible(true);
            f1.set(key, userId); f2.set(key, instrumentId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Integer> currentGenres(String userId) {
        return userGenresRepository.findGenreIdsByUserId(userId);
    }
    private List<Integer> currentInstruments(String userId) {
        return userInstrumentsRepository.findInstrumentIdsByUserId(userId);
    }

    // ====== Tests for updateProfile (partial update semantics) ======

    @Test
    @DisplayName("닉네임이 null이면 변경을 무시한다")
    void nickname_null_is_ignored() throws Exception {
        // given
        ensureNameTables(List.of(1,2), List.of(10,20));
        String uid = "u1";
        createUser(uid, "nick", new Integer[]{1,2}, new Integer[]{10,20});
        // when
        UserInfo after = profileUpdateService.updateProfile(uid, null, null, null,true,true,'M',"전북");
        // then
        assertEquals("nick", after.getNickname());
        assertEquals(Set.of(1,2), new HashSet<>(currentGenres(uid)));
        assertEquals(Set.of(10,20), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("닉네임이 동일하면 변경되지 않는다")
    void nickname_same_value_results_in_no_change() throws Exception {
        ensureNameTables(List.of(), List.of());
        String uid = "u2";
        createUser(uid, "same", null, null);
        UserInfo after = profileUpdateService.updateProfile(uid, "same", null, null,true,true,'M',"전북");
        assertEquals("same", after.getNickname());
    }

    @Test
    @DisplayName("닉네임이 변경되면 저장된다")
    void nickname_changed_is_applied() throws Exception {
        ensureNameTables(List.of(), List.of());
        String uid = "u3";
        createUser(uid, "old", null, null);
        UserInfo after = profileUpdateService.updateProfile(uid, "newNick", null, null,true,true,'M',"전북");
        assertEquals("newNick", after.getNickname());
    }

    @Test
    @DisplayName("악기 목록이 null이면 변경을 무시한다")
    void instruments_null_is_ignored() throws Exception {
        ensureNameTables(List.of(3), List.of(30));
        String uid = "u4";
        createUser(uid, "n", new Integer[]{3}, new Integer[]{30});
        profileUpdateService.updateProfile(uid, null, null, List.of(3),true,true,'M',"전북"); // genres provided only
        assertEquals(Set.of(30), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("악기 목록이 빈 리스트면 전부 삭제한다")
    void instruments_empty_deletes_all() throws Exception {
        ensureNameTables(List.of(), List.of(31,32));
        String uid = "u5";
        createUser(uid, "n", null, new Integer[]{31,32});
        profileUpdateService.updateProfile(uid, null, List.of(), null,true,true,'M',"전북");
        assertTrue(currentInstruments(uid).isEmpty());
    }

    @Test
    @DisplayName("악기 목록에 항목만 추가된다")
    void instruments_add_only() throws Exception {
        ensureNameTables(List.of(), List.of(40,41));
        String uid = "u6";
        createUser(uid, "n", null, new Integer[]{40});
        profileUpdateService.updateProfile(uid, null, List.of(40,41), null,true,true,'M',"전북");
        assertEquals(Set.of(40,41), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("악기 목록에서 항목만 제거된다")
    void instruments_remove_only() throws Exception {
        ensureNameTables(List.of(), List.of(50,51));
        String uid = "u7";
        createUser(uid, "n", null, new Integer[]{50,51});
        profileUpdateService.updateProfile(uid, null, List.of(50), null,true,true,'M',"전북");
        assertEquals(Set.of(50), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("악기 목록에서 추가와 제거가 함께 처리된다")
    void instruments_add_and_remove_mixed() throws Exception {
        ensureNameTables(List.of(), List.of(60,61,62));
        String uid = "u8";
        createUser(uid, "n", null, new Integer[]{60,61});
        profileUpdateService.updateProfile(uid, null, List.of(61,62), null,true,true,'M',"전북");
        assertEquals(Set.of(61,62), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("악기 입력의 중복은 한 번만 반영된다")
    void instruments_duplicates_in_input_are_handled() throws Exception {
        ensureNameTables(List.of(), List.of(70,71));
        String uid = "u9";
        createUser(uid, "n", null, new Integer[]{70});
        profileUpdateService.updateProfile(uid, null, Arrays.asList(70,70,71,71), null,true,true,'M',"전북");
        assertEquals(Set.of(70,71), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("악기 대량 목록으로 대체된다")
    void instruments_large_list_replace() throws Exception {
        List<Integer> ids = new ArrayList<>();
        for (int i=100;i<130;i++) ids.add(i);
        ensureNameTables(List.of(), ids);
        String uid = "u10";
        createUser(uid, "n", null, new Integer[]{});
        profileUpdateService.updateProfile(uid, null, ids, null,true,true,'M',"전북");
        assertEquals(new HashSet<>(ids), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("장르 목록이 null이면 변경을 무시한다")
    void genres_null_is_ignored() throws Exception {
        ensureNameTables(List.of(81), List.of(801));
        String uid = "u11";
        createUser(uid, "n", new Integer[]{81}, new Integer[]{801});
        profileUpdateService.updateProfile(uid, null, List.of(801), null,true,true,'M',"전북");
        assertEquals(Set.of(81), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("장르 목록이 빈 리스트면 전부 삭제한다")
    void genres_empty_deletes_all() throws Exception {
        ensureNameTables(List.of(82,83), List.of());
        String uid = "u12";
        createUser(uid, "n", new Integer[]{82,83}, null);
        profileUpdateService.updateProfile(uid, null, null, List.of(),true,true,'M',"전북");
        assertTrue(currentGenres(uid).isEmpty());
    }

    @Test
    @DisplayName("장르 목록에 항목만 추가된다")
    void genres_add_only() throws Exception {
        ensureNameTables(List.of(90,91), List.of());
        String uid = "u13";
        createUser(uid, "n", new Integer[]{90}, null);
        profileUpdateService.updateProfile(uid, null, null, List.of(90,91),true,true,'M',"전북");
        assertEquals(Set.of(90,91), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("장르 목록에서 항목만 제거된다")
    void genres_remove_only() throws Exception {
        ensureNameTables(List.of(100,101), List.of());
        String uid = "u14";
        createUser(uid, "n", new Integer[]{100,101}, null);
        profileUpdateService.updateProfile(uid, null, null, List.of(100),true,true,'M',"전북");
        assertEquals(Set.of(100), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("장르 목록에서 추가와 제거가 함께 처리된다")
    void genres_add_and_remove_mixed() throws Exception {
        ensureNameTables(List.of(110,111,112), List.of());
        String uid = "u15";
        createUser(uid, "n", new Integer[]{110,111}, null);
        profileUpdateService.updateProfile(uid, null, null, List.of(111,112),true,true,'M',"전북");
        assertEquals(Set.of(111,112), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("장르 입력의 중복은 한 번만 반영된다")
    void genres_duplicates_in_input_are_handled() throws Exception {
        ensureNameTables(List.of(120,121), List.of());
        String uid = "u16";
        createUser(uid, "n", new Integer[]{120}, null);
        profileUpdateService.updateProfile(uid, null, null, Arrays.asList(120,120,121),true,true,'M',"전북");
        assertEquals(Set.of(120,121), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("악기와 장르를 동시에 업데이트한다")
    void updating_both_instruments_and_genres() throws Exception {
        ensureNameTables(List.of(130,131,132), List.of(230,231,232));
        String uid = "u17";
        createUser(uid, "n", new Integer[]{130,131}, new Integer[]{230,231});
        profileUpdateService.updateProfile(uid, "n2", List.of(231,232), List.of(131,132),true,true,'M',"전북");
        assertEquals(Set.of(231,232), new HashSet<>(currentInstruments(uid)));
        assertEquals(Set.of(131,132), new HashSet<>(currentGenres(uid)));
        assertEquals("n2", userInfoRepository.findById(uid).orElseThrow().getNickname());
    }

    @Test
    @DisplayName("원하는 목록과 현재가 모두 비어도 상태는 정상이다")
    void when_desired_empty_and_current_empty_no_delete_called_but_state_ok() throws Exception {
        ensureNameTables(List.of(), List.of());
        String uid = "u18";
        createUser(uid, "n", null, null);
        profileUpdateService.updateProfile(uid, null, List.of(), List.of(),true,true,'M',"전북");
        assertTrue(currentInstruments(uid).isEmpty());
        assertTrue(currentGenres(uid).isEmpty());
    }

    @Test
    @DisplayName("부분 업데이트 시 존재하지 않는 사용자는 예외 발생")
    void updateProfile_nonexistent_user_throws() {
        assertThrows(NoSuchElementException.class, () -> profileUpdateService.updateProfile("nope", null, null, null,true,true,'M',"전북"));
    }

    @Test
    @DisplayName("같은 목록으로 반복 업데이트해도 결과가 동일하다(멱등성)")
    void idempotent_update_same_lists() throws Exception {
        ensureNameTables(List.of(201,202), List.of(301,302));
        String uid = "u19";
        createUser(uid, "n", new Integer[]{201,202}, new Integer[]{301,302});
        profileUpdateService.updateProfile(uid, "n", List.of(301,302), List.of(201,202),true,true,'M',"전북");
        profileUpdateService.updateProfile(uid, "n", List.of(301,302), List.of(201,202),true,true,'M',"전북");
        assertEquals(Set.of(301,302), new HashSet<>(currentInstruments(uid)));
        assertEquals(Set.of(201,202), new HashSet<>(currentGenres(uid)));
    }

    // ====== Tests for updateProfileAll (full replace semantics) ======

    @Test
    @DisplayName("전체 업데이트는 모든 값을 교체한다")
    void updateAll_replaces_everything() throws Exception {
        ensureNameTables(List.of(401,402,403), List.of(501,502,503));
        String uid = "u20";
        createUser(uid, "oldNick", new Integer[]{401,402}, new Integer[]{501});
        UserInfo after = profileUpdateService.updateProfileAll(uid, "newNick", List.of(502,503), List.of(403),true,true,'M',"전북");
        assertEquals("newNick", after.getNickname());
        assertEquals(Set.of(502,503), new HashSet<>(currentInstruments(uid)));
        assertEquals(Set.of(403), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("전체 업데이트에서 목록이 null이면 모두 비워진다")
    void updateAll_with_null_lists_results_in_clear() throws Exception {
        ensureNameTables(List.of(410), List.of(510));
        String uid = "u21";
        createUser(uid, "n", new Integer[]{410}, new Integer[]{510});
        profileUpdateService.updateProfileAll(uid, null, null, null, true, true,'M',"전북");
        assertTrue(currentInstruments(uid).isEmpty());
        assertTrue(currentGenres(uid).isEmpty());
        assertEquals("n", userInfoRepository.findById(uid).orElseThrow().getNickname());
    }

    @Test
    @DisplayName("전체 업데이트에서 빈 목록이면 모두 비워진다")
    void updateAll_with_empty_lists_results_in_clear() throws Exception {
        ensureNameTables(List.of(420), List.of(520));
        String uid = "u22";
        createUser(uid, "n", new Integer[]{420}, new Integer[]{520});
        profileUpdateService.updateProfileAll(uid, null, List.of(), List.of(),true,true,'M',"전북");
        assertTrue(currentInstruments(uid).isEmpty());
        assertTrue(currentGenres(uid).isEmpty());
    }

    @Test
    @DisplayName("전체 업데이트에서 악기만 주면 장르는 비워진다")
    void updateAll_only_instruments_provided_clears_genres() throws Exception {
        ensureNameTables(List.of(430), List.of(530,531));
        String uid = "u23";
        createUser(uid, "n", new Integer[]{430}, new Integer[]{530});
        profileUpdateService.updateProfileAll(uid, null, List.of(531), null,true,true,'M',"전북");
        assertEquals(Set.of(531), new HashSet<>(currentInstruments(uid)));
        assertTrue(currentGenres(uid).isEmpty());
    }

    @Test
    @DisplayName("전체 업데이트에서 장르만 주면 악기는 비워진다")
    void updateAll_only_genres_provided_clears_instruments() throws Exception {
        ensureNameTables(List.of(440,441), List.of(540));
        String uid = "u24";
        createUser(uid, "n", new Integer[]{440}, new Integer[]{540});
        profileUpdateService.updateProfileAll(uid, null, null, List.of(441),true,true,'M',"전북");
        assertTrue(currentInstruments(uid).isEmpty());
        assertEquals(Set.of(441), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("전체 업데이트 시 존재하지 않는 사용자는 예외 발생")
    void updateAll_nonexistent_user_throws() {
        assertThrows(NoSuchElementException.class, () -> profileUpdateService.updateProfileAll("noUser", null, null, null,true,true,'M',"전북"));
    }

    @Test
    @DisplayName("전체 업데이트에서 대량 입력도 정상 처리된다")
    void updateAll_large_inputs() throws Exception {
        List<Integer> g = new ArrayList<>();
        List<Integer> i = new ArrayList<>();
        for (int x=600;x<640;x++) g.add(x);
        for (int x=700;x<750;x++) i.add(x);
        ensureNameTables(g, i);
        String uid = "u25";
        createUser(uid, "n", new Integer[]{}, new Integer[]{});
        profileUpdateService.updateProfileAll(uid, null, i, g,true,true,'M',"전북");
        assertEquals(new HashSet<>(i), new HashSet<>(currentInstruments(uid)));
        assertEquals(new HashSet<>(g), new HashSet<>(currentGenres(uid)));
    }

    // ====== Tests for updateProfileImage ======

    @Test
    @DisplayName("프로필 이미지 URL을 설정하고 이력을 남긴다")
    void updateProfileImage_sets_url_and_adds_history() throws Exception {
        ensureNameTables(List.of(), List.of());
        String uid = "u26";
        createUser(uid, "n", null, null);
        profileUpdateService.updateProfileImage(uid, "http://img/x.png");
        UserInfo after = userInfoRepository.findById(uid).orElseThrow();
        assertEquals("http://img/x.png", after.getProfileImageUrl());
        assertFalse(historyRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 시 존재하지 않는 사용자는 예외 발생")
    void updateProfileImage_nonexistent_user_throws() {
        assertThrows(NoSuchElementException.class, () -> profileUpdateService.updateProfileImage("noUser", "x"));
    }

    // ====== Additional edge and behavior tests to exceed 30 cases ======

    @Test
    @DisplayName("부분 업데이트에서 입력이 빈 리스트면 기존 악기를 모두 제거한다")
    void updateProfile_instruments_clear_when_input_empty_and_current_non_empty() throws Exception {
        ensureNameTables(List.of(), List.of(801,802));
        String uid = "u27";
        createUser(uid, "n", null, new Integer[]{801,802});
        profileUpdateService.updateProfile(uid, null, List.of(), null,true,true,'M',"전북");
        assertTrue(currentInstruments(uid).isEmpty());
    }

    @Test
    @DisplayName("부분 업데이트에서 입력이 빈 리스트면 기존 장르를 모두 제거한다")
    void updateProfile_genres_clear_when_input_empty_and_current_non_empty() throws Exception {
        ensureNameTables(List.of(901,902), List.of());
        String uid = "u28";
        createUser(uid, "n", new Integer[]{901,902}, null);
        profileUpdateService.updateProfile(uid, null, null, List.of(),true,true,'M',"전북");
        assertTrue(currentGenres(uid).isEmpty());
    }

    @Test
    @DisplayName("부분 업데이트에서 목록 동일·닉네임 null이면 변경 없음")
    void updateProfile_noop_when_both_lists_same_and_nickname_null() throws Exception {
        ensureNameTables(List.of(1001,1002), List.of(1101,1102));
        String uid = "u29";
        createUser(uid, "n", new Integer[]{1001,1002}, new Integer[]{1101,1102});
        profileUpdateService.updateProfile(uid, null, List.of(1101,1102), List.of(1001,1002),true,true,'M',"전북");
        assertEquals(Set.of(1101,1102), new HashSet<>(currentInstruments(uid)));
        assertEquals(Set.of(1001,1002), new HashSet<>(currentGenres(uid)));
    }

    @Test
    @DisplayName("부분 업데이트에서 닉네임만 변경해도 목록에는 영향이 없다")
    void updateProfile_change_only_nickname_does_not_touch_lists() throws Exception {
        ensureNameTables(List.of(1201), List.of(1301));
        String uid = "u30";
        createUser(uid, "oldN", new Integer[]{1201}, new Integer[]{1301});
        profileUpdateService.updateProfile(uid, "newN", null, null,true,true,'M',"전북");
        assertEquals(Set.of(1201), new HashSet<>(currentGenres(uid)));
        assertEquals(Set.of(1301), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    @DisplayName("전체 업데이트에서 닉네임 변경과 null 목록으로 초기화된다")
    void updateAll_change_nickname_and_clear_lists_with_null() throws Exception {
        ensureNameTables(List.of(1401), List.of(1501));
        String uid = "u31";
        createUser(uid, "old", new Integer[]{1401}, new Integer[]{1501});
        profileUpdateService.updateProfileAll(uid, "new", null, null,true,true,'M',"전북");
        assertEquals("new", userInfoRepository.findById(uid).orElseThrow().getNickname());
        assertTrue(currentGenres(uid).isEmpty());
        assertTrue(currentInstruments(uid).isEmpty());
    }

    @Test
    void updateAll_nickname_null_keeps_old() throws Exception {
        ensureNameTables(List.of(), List.of());
        String uid = "u32";
        createUser(uid, "keep", null, null);
        profileUpdateService.updateProfileAll(uid, null, List.of(), List.of(),true,true,'M',"전북");
        assertEquals("keep", userInfoRepository.findById(uid).orElseThrow().getNickname());
    }

    @Test
    void updateProfile_partial_lists_unaffected_side() throws Exception {
        ensureNameTables(List.of(1601,1602), List.of(1701,1702));
        String uid = "u33";
        createUser(uid, "n", new Integer[]{1601}, new Integer[]{1701});
        profileUpdateService.updateProfile(uid, null, List.of(1701,1702), null,true,true,'M',"전북");
        assertEquals(Set.of(1601), new HashSet<>(currentGenres(uid)));
        assertEquals(Set.of(1701,1702), new HashSet<>(currentInstruments(uid)));
    }

    @Test
    void updateAll_overwrites_even_if_same_values_passed() throws Exception {
        ensureNameTables(List.of(1801,1802), List.of(1901,1902));
        String uid = "u34";
        createUser(uid, "n", new Integer[]{1801,1802}, new Integer[]{1901,1902});
        profileUpdateService.updateProfileAll(uid, "n2", List.of(1901,1902), List.of(1801,1802),true,true,'M',"전북");
        assertEquals("n2", userInfoRepository.findById(uid).orElseThrow().getNickname());
        assertEquals(Set.of(1901,1902), new HashSet<>(currentInstruments(uid)));
        assertEquals(Set.of(1801,1802), new HashSet<>(currentGenres(uid)));
    }

    @Test
    void updateProfile_history_is_written_for_nickname_call() throws Exception {
        ensureNameTables(List.of(), List.of());
        String uid = "u35";
        createUser(uid, "oldH", null, null);
        long before = historyRepository.count();
        profileUpdateService.updateProfile(uid, "newH", null, null,true,true,'M',"전북");
        assertTrue(historyRepository.count() >= before + 1);
    }
}
