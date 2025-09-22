package com.teambind.profileserver.utils;


import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitTableMapper {


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
            InitTableMapper.genreNameTable.put(genreNameTable.getGenreId(), genreNameTable.getGenreName());
        });
        instrumentNameTables.forEach(instrumentNameTable -> {
            InitTableMapper.instrumentNameTable.put(instrumentNameTable.getInstrumentId(), instrumentNameTable.getInstrumentName());
        });
    }


    public List<GenreNameTable> convertGenreNameTable(List<Integer> names) {
        List<GenreNameTable> convertedNames = new ArrayList<>();
        names.forEach(name -> {
            if( !InitTableMapper.genreNameTable.containsKey(name)){
                convertedNames.add(genreNameTableRepository.findById(name).get());}
        });
        return convertedNames;
    }

    public List<InstrumentNameTable> convertInstrumentNameTable(List<Integer> names) {
        List<InstrumentNameTable> convertedNames = new ArrayList<>();
        names.forEach(name -> {
            if( !InitTableMapper.instrumentNameTable.containsKey(name)){
                convertedNames.add(instrumentNameTableRepository.findById(name).get());}
        });
        return convertedNames;
    }


}
