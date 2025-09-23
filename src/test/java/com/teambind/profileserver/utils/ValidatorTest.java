package com.teambind.profileserver.utils;

import com.teambind.profileserver.utils.validator.GenreValidator;
import com.teambind.profileserver.utils.validator.InstrumentsValidator;
import com.teambind.profileserver.utils.validator.NickNameValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Profile(value = "test")
public class ValidatorTest {

    @Test
    @DisplayName("닉네임 정규식 위반했을시 오류 발행")
    void validateNickname_invalid() {
        NickNameValidator nicknameValidate = new NickNameValidator();
        nicknameValidate.setRegex("^[a-zA-Z0-9_]{3,15}$");


        String expectNickName = "   sajdalsdjaldhwaldhawldjawldjakldja";
        var result = nicknameValidate.isValidNickName(expectNickName);
        assertFalse(result);

    }

    @Test
    @DisplayName("닉네임 정규식 위반했을시 오류 발행")
    void validateNickname_invalid2() {
        NickNameValidator nicknameValidate = new NickNameValidator();
        nicknameValidate.setRegex("^[a-zA-Z0-9_]{3,15}$");


        String expectNickName = "  ";
        var result = nicknameValidate.isValidNickName(expectNickName);
        assertFalse(result);
    }

    @Test
    @DisplayName("닉네임 정규식 정상 닉네임")
    void validateNickname() {
        NickNameValidator nicknameValidate = new NickNameValidator();
        nicknameValidate.setRegex("^[a-zA-Z0-9_]{3,15}$");


        String expectNickName = "ddinasd";
        var result = nicknameValidate.isValidNickName(expectNickName);
        assertTrue(result);
    }

    @Test
    @DisplayName("악기 3개 이하 선택시 정상 실행")
    void validateInstruments_valid() {

        InstrumentsValidator instrumentsValidate = new InstrumentsValidator();
        instrumentsValidate.setMaxSize(3);
        Map<Integer,String > instrumentsMap = Map.of(1, "test1", 2, "test2", 3, "test3");
        assertThat(instrumentsValidate.isValidInstrumentByIds(instrumentsMap)).isTrue();
        instrumentsMap = Map.of(1, "test1", 2, "test2");
        assertThat(instrumentsValidate.isValidInstrumentByIds(instrumentsMap)).isTrue();
        instrumentsMap = Map.of(1, "test1");
        assertThat(instrumentsValidate.isValidInstrumentByIds(instrumentsMap)).isTrue();

    }

    @Test
    @DisplayName("악기 4개이상 선택시 오류 발생")
    void validateInstruments_invalid() {
        InstrumentsValidator instrumentsValidate = new InstrumentsValidator();
        instrumentsValidate.setMaxSize(3);
        Map<Integer,String > instrumentsMap = Map.of(1, "test1", 2, "test2", 3, "test3", 4, "test4");

        for (Map.Entry<Integer, String> entry : instrumentsMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        assertThrows(IllegalArgumentException.class, () -> {
            instrumentsValidate.isValidInstrumentByIds(instrumentsMap);
        });
    }

    @Test
    @DisplayName("장르 3개 이하 선택시 정상 실행")
    void validateGenre_valid() {
        GenreValidator genreValidate = new GenreValidator();
        genreValidate.setMaxSize(3);

        Map<Integer,String > genreMap = Map.of(1, "ROCK", 2, "POP", 3, "JAZZ");
        assertThat(genreValidate.isValidGenreByIds(genreMap)).isTrue();
        genreMap = Map.of(1, "ROCK", 2, "POP");
        assertThat(genreValidate.isValidGenreByIds(genreMap)).isTrue();
        genreMap = Map.of(1, "ROCK");
        assertThat(genreValidate.isValidGenreByIds(genreMap)).isTrue();
    }

    @Test
    @DisplayName("장르 4개이상 선택시 오류 발생")
    void validateGenre_invalid() {
        GenreValidator genreValidate = new GenreValidator();
        Map<Integer,String > genreMap = Map.of(1, "test1", 2, "test2", 3, "test3", 4, "test4");
        assertThrows(IllegalArgumentException.class, () -> {
            genreValidate.isValidGenreByIds(genreMap);
        });
    }





}
