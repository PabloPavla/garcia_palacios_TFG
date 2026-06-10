package com.tfg.esports.league;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tfg.esports.league.entity.League;

public class TestJackson {
    public static void main(String[] args) throws Exception {
        League league = League.builder().initialRp(30000).name("Test").build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(league);
        System.out.println(json);
    }
}
