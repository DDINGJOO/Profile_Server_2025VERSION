package com.teambind.profileserver.controller;


import com.teambind.profileserver.utils.NamingMapper;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles/enums")
public class EnumsController {

    @RequestMapping("/genres")
    public Map<Integer, String> getGenres() {
        return NamingMapper.genreNameTable;
    }
    @RequestMapping("/instruments")
    public Map<Integer, String> getInstruments() {
        return NamingMapper.instrumentNameTable;
    }
}
