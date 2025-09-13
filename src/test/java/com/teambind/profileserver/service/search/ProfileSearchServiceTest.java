package com.teambind.profileserver.service.search;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = com.teambind.profileserver.ProfileServerApplication.class)
class ProfileSearchServiceTest {

    @Autowired
    private ProfileSearchService profileSearchService;


    @AfterEach
    void tearDown() {

    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일 필터 : 닉네임 ")
    void searchProfilesByNickname() {


    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일 필터 : 성별 ")
    void searchProfilesBySex() {

    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일필터, 단일속성: Genre ")
    void searchProfilesByGenre() {

    }
    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일필터, 다중 속성 : Genres")
    void searchProfilesByGenres() {

    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일 필터, 다중 속성 : instruments")
    void searchProfilesByInstruments() {

    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일필터, 단일 속성 : instrument")
    void searchProfilesByInstrument() {

    }


}
