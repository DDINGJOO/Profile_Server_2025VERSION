package com.teambind.profileserver.utils;


import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NamingMapper {
//TODO: When server starts, initialize GenreName And InstrumentName Tables

    private final GenreNameTableRepository genreNameTableRepository;
    private final InstrumentNameTableRepository instrumentNameTableRepository;
    public static HashMap<Integer, String> genreNameTable = new HashMap<>();
    public static HashMap<Integer, String> instrumentNameTable = new HashMap<>();


    @PostConstruct
    public void initOnStartup() {
        initializeTables();
    }


    public void initializeTables() {
        List<GenreNameTable> genreNameTables = genreNameTableRepository.findAll();
        List<InstrumentNameTable> instrumentNameTables = instrumentNameTableRepository.findAll();




        genreNameTables.forEach(genreNameTable -> {
            NamingMapper.genreNameTable.put(genreNameTable.getGenreId(), genreNameTable.getGenreName());
        });
        instrumentNameTables.forEach(instrumentNameTable -> {
            NamingMapper.instrumentNameTable.put(instrumentNameTable.getInstrumentId(), instrumentNameTable.getInstrumentName());
        });







    }


}
