package com.teambind.profileserver.utils;

import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NamingMapperTest {

    @Mock
    private GenreNameTableRepository genreRepo;
    @Mock
    private InstrumentNameTableRepository instrumentRepo;

    @InjectMocks
    private NamingMapper namingMapper;

    @BeforeEach
    void setUp() {
        // 매 테스트 전 캐시 초기화
        NamingMapper.genreNameTable.clear();
        NamingMapper.instrumentNameTable.clear();
    }

    @Test
    @DisplayName("convertGenreNameTable - 캐시에 없는 ID만 조회하여 반환")
    void convertGenreNameTable() {
        // given: 1은 캐시에 존재, 2는 캐시에 없음
        NamingMapper.genreNameTable.put(1, "ROCK");
        GenreNameTable g2 = GenreNameTable.builder().genreId(2).genreName("JAZZ").build();
        when(genreRepo.findById(2)).thenReturn(Optional.of(g2));

        // when
        List<GenreNameTable> result = namingMapper.convertGenreNameTable(List.of(1, 2));

        // then: 캐시에 없는 2만 반환되고, 1은 조회하지 않음
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getGenreId());
        verify(genreRepo, times(1)).findById(2);
        verify(genreRepo, never()).findById(1);
    }

    @Test
    @DisplayName("convertInstrumentNameTable - 캐시에 없는 ID만 조회하여 반환")
    void convertInstrumentNameTable() {
        // given: 10은 캐시에 존재, 20은 캐시에 없음
        NamingMapper.instrumentNameTable.put(10, "GUITAR");
        InstrumentNameTable i20 = InstrumentNameTable.builder().instrumentId(20).instrumentName("PIANO").build();
        when(instrumentRepo.findById(20)).thenReturn(Optional.of(i20));

        // when
        List<InstrumentNameTable> result = namingMapper.convertInstrumentNameTable(List.of(10, 20));

        // then: 캐시에 없는 20만 반환되고, 10은 조회하지 않음
        assertEquals(1, result.size());
        assertEquals(20, result.get(0).getInstrumentId());
        verify(instrumentRepo, times(1)).findById(20);
        verify(instrumentRepo, never()).findById(10);
    }

}
