package com.teambind.profileserver.controller;


import com.teambind.profileserver.utils.InitTableMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles/enums")
public class EnumsController {

    @RequestMapping("/genres")
    public Map<Integer, String> getGenres() {
        return InitTableMapper.genreNameTable;
    }
    @RequestMapping("/instruments")
    public Map<Integer, String> getInstruments() {
        return InitTableMapper.instrumentNameTable;
    }
}
