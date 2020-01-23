package com.example.demopostgresql;

import com.example.demopostgresql.dao.PersonRepository;
import com.example.demopostgresql.model.Person;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(initializers = DemopostgresqlApplicationTests.Initializer.class)
class DemopostgresqlApplicationTests {

    @Autowired
    PersonRepository personRepository;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private TestRestTemplate restTemplate;

    HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    private int port;

    //anotation container that are shared between all methods of a test class
    @Container
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres")
            .withDatabaseName("integration-tests-db")
            .withUsername("sa")
            .withPassword("sa");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private void insertUsers() {
        personRepository.save(new Person("Employee First Test", 39, "0904567888", "emailemployeefirst@email.cz", 1337));
        personRepository.save(new Person("Employee Second Test", 39, "0904567888", "emailemployeefirst@email.cz", 1338));
        personRepository.save(new Person("Employee Third Test", 39, "0904567888", "emailemployeefirst@email.cz", 1339));
        personRepository.save(new Person("Employee Fourth Test", 39, "0904567888", "emailemployeefirst@email.cz", 1340));
        personRepository.flush();
    }

    @Test
    public void testDatabase() {
        insertUsers();
        List<Person> all = personRepository.findAll();

        //there are 6 entities of person because 2 we already inserted at DatabaseSeeder.class and another 4 we inserted here in insertUsers method
        Assertions.assertEquals(6, all.size());
    }

    @Test
    //RestTemplate you should use when you want to test Rest Client-side application
    //RestTemplate sends real HTTP requests to the endpoints.
    public void testGetAllPersonsRestTemplate() throws JSONException {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/persons/all"),
                HttpMethod.GET, entity, String.class);

        String expected = "[{\"id\":1,\"name\":\"Employee First\",\"age\":39,\"telephone\":\"0904567888\",\"email\":\"emailemployeefirst@email.cz\",\"salary\":3000},{\"id\":2,\"name\":\"Employee First\",\"age\":39,\"telephone\":\"0909867888\",\"email\":\"emailemployeesecond@email.cz\",\"salary\":1600},{\"id\":3,\"name\":\"Employee First Test\",\"age\":39,\"telephone\":\"0904567888\",\"email\":\"emailemployeefirst@email.cz\",\"salary\":1337},{\"id\":4,\"name\":\"Employee Second Test\",\"age\":39,\"telephone\":\"0904567888\",\"email\":\"emailemployeefirst@email.cz\",\"salary\":1338},{\"id\":5,\"name\":\"Employee Third Test\",\"age\":39,\"telephone\":\"0904567888\",\"email\":\"emailemployeefirst@email.cz\",\"salary\":1339},{\"id\":6,\"name\":\"Employee Fourth Test\",\"age\":39,\"telephone\":\"0904567888\",\"email\":\"emailemployeefirst@email.cz\",\"salary\":1340}]";

        JSONAssert.assertEquals(expected.toString(), response.getBody().toString(), false);
    }

    @Test
    //You should use MockMvc when you want to test Server-side of application.
    //MockMvc  considers controller endpoints as the methods of the class and tests method behavior.
    public void testGetAllPersonsMVC() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/persons/all")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testRepository() {
        Person p = new Person("Employee First", 39, "0904567888", "emailemployeefirst@email.cz", 1337);
        List<Person> books = personRepository.findAll();
        assertNotNull(books);
    }
}
