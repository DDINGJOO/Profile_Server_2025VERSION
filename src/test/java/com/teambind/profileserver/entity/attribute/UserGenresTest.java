package com.teambind.profileserver.entity.attribute;

import static org.assertj.core.api.Assertions.assertThat;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.base.UserAttributeBase;
import com.teambind.profileserver.entity.attribute.key.UserGenreKey;
import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserGenresTest {

  @Test
  @DisplayName("UserGenres는 UserAttributeBase를 상속받는다")
  void userGenres_extendsUserAttributeBase() {
    UserGenres userGenres = new UserGenres();

    assertThat(userGenres).isInstanceOf(UserAttributeBase.class);
  }

  @Test
  @DisplayName("getId 메서드는 복합키를 반환한다")
  void getId_returnsCompositeKey() {
    UserGenreKey key = new UserGenreKey("user123", 1);
    UserGenres userGenres = UserGenres.builder().id(key).build();

    UserGenreKey result = userGenres.getId();

    assertThat(result).isEqualTo(key);
  }

  @Test
  @DisplayName("setId 메서드는 복합키를 설정한다")
  void setId_setsCompositeKey() {
    UserGenres userGenres = new UserGenres();
    UserGenreKey key = new UserGenreKey("user456", 2);

    userGenres.setId(key);

    assertThat(userGenres.getId()).isEqualTo(key);
  }

  @Test
  @DisplayName("getAttribute 메서드는 장르를 반환한다")
  void getAttribute_returnsGenre() {
    GenreNameTable genre = GenreNameTable.builder().genreId(1).genreName("Rock").build();
    UserGenres userGenres = UserGenres.builder().genre(genre).build();

    GenreNameTable result = userGenres.getAttribute();

    assertThat(result).isEqualTo(genre);
    assertThat(result.getGenreName()).isEqualTo("Rock");
  }

  @Test
  @DisplayName("setAttribute 메서드는 장르를 설정한다")
  void setAttribute_setsGenre() {
    UserGenres userGenres = new UserGenres();
    GenreNameTable genre = GenreNameTable.builder().genreId(2).genreName("Jazz").build();

    userGenres.setAttribute(genre);

    assertThat(userGenres.getGenre()).isEqualTo(genre);
    assertThat(userGenres.getAttribute()).isEqualTo(genre);
  }

  @Test
  @DisplayName("version 필드는 부모 클래스에서 상속된다")
  void version_isInheritedFromParent() {
    UserGenres userGenres = new UserGenres();

    userGenres.setVersion(5);

    assertThat(userGenres.getVersion()).isEqualTo(5);
  }

  @Test
  @DisplayName("Builder를 통해 모든 필드를 설정할 수 있다")
  void builder_setsAllFields() {
    UserGenreKey key = new UserGenreKey("user789", 3);
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("user789");
    GenreNameTable genre = GenreNameTable.builder().genreId(3).genreName("Classical").build();

    UserGenres userGenres = UserGenres.builder().id(key).userInfo(userInfo).genre(genre).build();

    assertThat(userGenres.getId()).isEqualTo(key);
    assertThat(userGenres.getUserInfo()).isEqualTo(userInfo);
    assertThat(userGenres.getGenre()).isEqualTo(genre);
    assertThat(userGenres.getAttribute()).isEqualTo(genre);
  }

  @Test
  @DisplayName("NoArgsConstructor를 통해 객체를 생성할 수 있다")
  void noArgsConstructor_createsInstance() {
    UserGenres userGenres = new UserGenres();

    assertThat(userGenres).isNotNull();
    assertThat(userGenres.getId()).isNull();
    assertThat(userGenres.getGenre()).isNull();
  }

  @Test
  @DisplayName("AllArgsConstructor를 통해 모든 필드를 초기화할 수 있다")
  void allArgsConstructor_initializesAllFields() {
    UserGenreKey key = new UserGenreKey("user999", 4);
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("user999");
    GenreNameTable genre = GenreNameTable.builder().genreId(4).genreName("Pop").build();

    UserGenres userGenres = new UserGenres(key, userInfo, genre);

    assertThat(userGenres.getId()).isEqualTo(key);
    assertThat(userGenres.getUserInfo()).isEqualTo(userInfo);
    assertThat(userGenres.getGenre()).isEqualTo(genre);
  }
}
