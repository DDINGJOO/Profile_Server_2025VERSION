package com.teambind.profileserver.utils;


import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import com.teambind.profileserver.entity.attribute.nameTable.LocationNameTable;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import com.teambind.profileserver.repository.LocationNameTableRepository;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitTableMapper {


    public static HashMap<Integer, GenreNameTable> genreNameTable = new HashMap<>();
    public static HashMap<Integer, InstrumentNameTable> instrumentNameTable = new HashMap<>();
    public static HashMap<String, String> locationNamesTable = new HashMap<>();
    private final GenreNameTableRepository genreNameTableRepository;
    private final InstrumentNameTableRepository instrumentNameTableRepository;
    private final LocationNameTableRepository locationNameTableRepository;

    @PostConstruct
    public void initOnStartup() {
        initializeTables();
    }


	@Scheduled(cron = "0 0 6 * * *")
    public void initializeTables() {
		genreNameTable.clear();
		instrumentNameTable.clear();
		locationNamesTable.clear();
		
        List<GenreNameTable> genreNameTables = genreNameTableRepository.findAll();
        List<InstrumentNameTable> instrumentNameTables = instrumentNameTableRepository.findAll();
        List<LocationNameTable> locationNameTables = locationNameTableRepository.findAll();

        locationNameTables.forEach(locationNameTable -> {
            InitTableMapper.locationNamesTable.put(locationNameTable.getId(), locationNameTable.getCity());
        });


        genreNameTable= new HashMap<>();
        instrumentNameTable= new HashMap<>();

        genreNameTables.forEach(genreNameTable -> {
            InitTableMapper.genreNameTable.put(genreNameTable.getGenreId(), genreNameTable);
        });
        instrumentNameTables.forEach(instrumentNameTable -> {
            InitTableMapper.instrumentNameTable.put(instrumentNameTable.getInstrumentId(), instrumentNameTable);
        });
    }
	
	
}
