package com.teambind.profileserver.entity;

import static org.assertj.core.api.Assertions.*;

import com.teambind.profileserver.entity.attribute.UserGenres;
import com.teambind.profileserver.entity.attribute.UserInstruments;
import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserInfo 연관관계 편의 메소드 테스트")
class UserInfoRelationTest {

  private UserInfo userInfo;
  private GenreNameTable genre1;
  private GenreNameTable genre2;
  private InstrumentNameTable instrument1;
  private InstrumentNameTable instrument2;
  private History history1;
  private History history2;

  @BeforeEach
  void setUp() {
    userInfo = UserInfo.builder().userId("testUser").nickname("테스트유저").build();

    genre1 = GenreNameTable.builder().genreId(1).genreName("Rock").build();

    genre2 = GenreNameTable.builder().genreId(2).genreName("Jazz").build();

    instrument1 = InstrumentNameTable.builder().instrumentId(1).instrumentName("Guitar").build();

    instrument2 = InstrumentNameTable.builder().instrumentId(2).instrumentName("Piano").build();

    history1 =
        History.builder()
            .historyId(1L)
            .fieldName("nickname")
            .oldVal("oldNick")
            .newVal("newNick")
            .updatedAt(LocalDateTime.now())
            .build();

    history2 =
        History.builder()
            .historyId(2L)
            .fieldName("city")
            .oldVal("Seoul")
            .newVal("Busan")
            .updatedAt(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("addGenre - 장르 추가 성공")
  void addGenre_Success() {
    // when
    userInfo.addGenre(genre1);

    // then
    assertThat(userInfo.getUserGenres()).hasSize(1);
    UserGenres addedGenre = userInfo.getUserGenres().get(0);
    assertThat(addedGenre.getAttribute()).isEqualTo(genre1);
    assertThat(addedGenre.getUserInfo()).isEqualTo(userInfo);
  }

  @Test
  @DisplayName("addGenre - null 추가 시 무시")
  void addGenre_NullIgnored() {
    // when
    userInfo.addGenre(null);

    // then
    assertThat(userInfo.getUserGenres()).isEmpty();
  }

  @Test
  @DisplayName("addGenre - 중복 장르 추가 방지")
  void addGenre_DuplicatePrevented() {
    // given
    userInfo.addGenre(genre1);

    // when
    userInfo.addGenre(genre1);

    // then
    assertThat(userInfo.getUserGenres()).hasSize(1);
  }

  @Test
  @DisplayName("addGenre - 여러 장르 추가")
  void addGenre_Multiple() {
    // when
    userInfo.addGenre(genre1);
    userInfo.addGenre(genre2);

    // then
    assertThat(userInfo.getUserGenres()).hasSize(2);
    assertThat(userInfo.getUserGenres())
        .extracting(UserGenres::getAttribute)
        .containsExactly(genre1, genre2);
  }

  @Test
  @DisplayName("removeGenre - 장르 제거 성공")
  void removeGenre_Success() {
    // given
    userInfo.addGenre(genre1);
    userInfo.addGenre(genre2);

    // when
    userInfo.removeGenre(genre1);

    // then
    assertThat(userInfo.getUserGenres()).hasSize(1);
    assertThat(userInfo.getUserGenres().get(0).getAttribute()).isEqualTo(genre2);
  }

  @Test
  @DisplayName("removeGenre - null 제거 시 무시")
  void removeGenre_NullIgnored() {
    // given
    userInfo.addGenre(genre1);

    // when
    userInfo.removeGenre(null);

    // then
    assertThat(userInfo.getUserGenres()).hasSize(1);
  }

  @Test
  @DisplayName("removeGenre - 양방향 관계 해제 확인")
  void removeGenre_BidirectionalUnlink() {
    // given
    userInfo.addGenre(genre1);
    UserGenres link = userInfo.getUserGenres().get(0);

    // when
    userInfo.removeGenre(genre1);

    // then
    assertThat(link.getUserInfo()).isNull();
  }

  @Test
  @DisplayName("clearGenres - 모든 장르 제거")
  void clearGenres_Success() {
    // given
    userInfo.addGenre(genre1);
    userInfo.addGenre(genre2);
    UserGenres link1 = userInfo.getUserGenres().get(0);
    UserGenres link2 = userInfo.getUserGenres().get(1);

    // when
    userInfo.clearGenres();

    // then
    assertThat(userInfo.getUserGenres()).isEmpty();
    assertThat(link1.getUserInfo()).isNull();
    assertThat(link2.getUserInfo()).isNull();
  }

  @Test
  @DisplayName("addInstrument - 악기 추가 성공")
  void addInstrument_Success() {
    // when
    userInfo.addInstrument(instrument1);

    // then
    assertThat(userInfo.getUserInstruments()).hasSize(1);
    UserInstruments addedInstrument = userInfo.getUserInstruments().get(0);
    assertThat(addedInstrument.getAttribute()).isEqualTo(instrument1);
    assertThat(addedInstrument.getUserInfo()).isEqualTo(userInfo);
  }

  @Test
  @DisplayName("addInstrument - null 추가 시 무시")
  void addInstrument_NullIgnored() {
    // when
    userInfo.addInstrument(null);

    // then
    assertThat(userInfo.getUserInstruments()).isEmpty();
  }

  @Test
  @DisplayName("addInstrument - 중복 악기 추가 방지")
  void addInstrument_DuplicatePrevented() {
    // given
    userInfo.addInstrument(instrument1);

    // when
    userInfo.addInstrument(instrument1);

    // then
    assertThat(userInfo.getUserInstruments()).hasSize(1);
  }

  @Test
  @DisplayName("addInstrument - 여러 악기 추가")
  void addInstrument_Multiple() {
    // when
    userInfo.addInstrument(instrument1);
    userInfo.addInstrument(instrument2);

    // then
    assertThat(userInfo.getUserInstruments()).hasSize(2);
    assertThat(userInfo.getUserInstruments())
        .extracting(UserInstruments::getAttribute)
        .containsExactly(instrument1, instrument2);
  }

  @Test
  @DisplayName("removeInstrument - 악기 제거 성공")
  void removeInstrument_Success() {
    // given
    userInfo.addInstrument(instrument1);
    userInfo.addInstrument(instrument2);

    // when
    userInfo.removeInstrument(instrument1);

    // then
    assertThat(userInfo.getUserInstruments()).hasSize(1);
    assertThat(userInfo.getUserInstruments().get(0).getAttribute()).isEqualTo(instrument2);
  }

  @Test
  @DisplayName("removeInstrument - null 제거 시 무시")
  void removeInstrument_NullIgnored() {
    // given
    userInfo.addInstrument(instrument1);

    // when
    userInfo.removeInstrument(null);

    // then
    assertThat(userInfo.getUserInstruments()).hasSize(1);
  }

  @Test
  @DisplayName("removeInstrument - 양방향 관계 해제 확인")
  void removeInstrument_BidirectionalUnlink() {
    // given
    userInfo.addInstrument(instrument1);
    UserInstruments link = userInfo.getUserInstruments().get(0);

    // when
    userInfo.removeInstrument(instrument1);

    // then
    assertThat(link.getUserInfo()).isNull();
  }

  @Test
  @DisplayName("clearInstruments - 모든 악기 제거")
  void clearInstruments_Success() {
    // given
    userInfo.addInstrument(instrument1);
    userInfo.addInstrument(instrument2);
    UserInstruments link1 = userInfo.getUserInstruments().get(0);
    UserInstruments link2 = userInfo.getUserInstruments().get(1);

    // when
    userInfo.clearInstruments();

    // then
    assertThat(userInfo.getUserInstruments()).isEmpty();
    assertThat(link1.getUserInfo()).isNull();
    assertThat(link2.getUserInfo()).isNull();
  }

  @Test
  @DisplayName("addHistory - 히스토리 추가 성공")
  void addHistory_Success() {
    // when
    userInfo.addHistory(history1);

    // then
    assertThat(userInfo.getUserHistory()).hasSize(1);
    assertThat(userInfo.getUserHistory().get(0)).isEqualTo(history1);
  }

  @Test
  @DisplayName("addHistory - null 추가 시 무시")
  void addHistory_NullIgnored() {
    // when
    userInfo.addHistory(null);

    // then
    assertThat(userInfo.getUserHistory()).isNull();
  }

  @Test
  @DisplayName("addHistory - 여러 히스토리 추가")
  void addHistory_Multiple() {
    // when
    userInfo.addHistory(history1);
    userInfo.addHistory(history2);

    // then
    assertThat(userInfo.getUserHistory()).hasSize(2);
    assertThat(userInfo.getUserHistory()).containsExactly(history1, history2);
  }

  @Test
  @DisplayName("복합 시나리오 - 장르와 악기를 동시에 관리")
  void complexScenario_GenresAndInstruments() {
    // when
    userInfo.addGenre(genre1);
    userInfo.addGenre(genre2);
    userInfo.addInstrument(instrument1);
    userInfo.addInstrument(instrument2);

    // then
    assertThat(userInfo.getUserGenres()).hasSize(2);
    assertThat(userInfo.getUserInstruments()).hasSize(2);

    // when - 일부 제거
    userInfo.removeGenre(genre1);
    userInfo.removeInstrument(instrument1);

    // then
    assertThat(userInfo.getUserGenres()).hasSize(1);
    assertThat(userInfo.getUserInstruments()).hasSize(1);
    assertThat(userInfo.getUserGenres().get(0).getAttribute()).isEqualTo(genre2);
    assertThat(userInfo.getUserInstruments().get(0).getAttribute()).isEqualTo(instrument2);
  }

  @Test
  @DisplayName("복합 시나리오 - 전체 초기화")
  void complexScenario_ClearAll() {
    // given
    userInfo.addGenre(genre1);
    userInfo.addGenre(genre2);
    userInfo.addInstrument(instrument1);
    userInfo.addInstrument(instrument2);

    // when
    userInfo.clearGenres();
    userInfo.clearInstruments();

    // then
    assertThat(userInfo.getUserGenres()).isEmpty();
    assertThat(userInfo.getUserInstruments()).isEmpty();
  }

  @Test
  @DisplayName("엣지 케이스 - 빈 리스트에서 제거 시도")
  void edgeCase_RemoveFromEmptyList() {
    // when & then - 예외 발생하지 않아야 함
    assertThatCode(
            () -> {
              userInfo.removeGenre(genre1);
              userInfo.removeInstrument(instrument1);
            })
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("엣지 케이스 - 빈 리스트 초기화")
  void edgeCase_ClearEmptyList() {
    // when & then - 예외 발생하지 않아야 함
    assertThatCode(
            () -> {
              userInfo.clearGenres();
              userInfo.clearInstruments();
            })
        .doesNotThrowAnyException();
  }
}
