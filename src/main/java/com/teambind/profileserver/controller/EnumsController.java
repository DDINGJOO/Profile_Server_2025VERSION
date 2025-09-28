package com.teambind.profileserver.controller;


import com.teambind.profileserver.utils.InitTableMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/profiles/enums")
public class EnumsController {

    @GetMapping("/genres")
    public Map<Integer, String> getGenres() {
        return InitTableMapper.genreNameTable;
    }
    @GetMapping("/instruments")
    public Map<Integer, String> getInstruments() {
        return InitTableMapper.instrumentNameTable;
    }
    @GetMapping("/locations")
    public Map<String, String> getLocations() {
        return InitTableMapper.locationNamesTable;
    }
}
