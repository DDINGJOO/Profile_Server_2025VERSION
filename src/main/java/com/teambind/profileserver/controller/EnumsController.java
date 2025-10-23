package com.teambind.profileserver.controller;


import com.teambind.profileserver.utils.InitTableMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
public class EnumsController {

	//TEST Required
    @GetMapping("/genres")
    public Map<Integer, String> getGenres() {
		Map<Integer, String> result = new HashMap<>();
		InitTableMapper.genreNameTable.entrySet().stream().forEach(entry -> {
			result.put(entry.getKey(), entry.getValue().getGenreName());
		});
		return result;
    }
    @GetMapping("/instruments")
    public Map<Integer, String> getInstruments() {
		Map<Integer, String> result = new HashMap<>();
		InitTableMapper.instrumentNameTable.entrySet().stream().forEach(entry -> {
			result.put(entry.getKey(), entry.getValue().getInstrumentName());
		});
		return result;
    }
    @GetMapping("/locations")
    public Map<String, String> getLocations() {
        return InitTableMapper.locationNamesTable;
    }
}
