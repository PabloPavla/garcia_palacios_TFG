import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJackson {
    public static void main(String[] args) throws Exception {
        String json = "{\"name\":\"foo\",\"acronym\":\"bar\",\"initialRp\":2000000}";
        ObjectMapper mapper = new ObjectMapper();
        
        // I will just create a mock class with Lombok-like getters/setters
        MockRequest req = mapper.readValue(json, MockRequest.class);
        System.out.println("initialRp: " + req.getInitialRp());
    }

    public static class MockRequest {
        private String name;
        private String acronym;
        private Integer initialRp;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAcronym() { return acronym; }
        public void setAcronym(String acronym) { this.acronym = acronym; }
        public Integer getInitialRp() { return initialRp; }
        public void setInitialRp(Integer initialRp) { this.initialRp = initialRp; }
    }
}
