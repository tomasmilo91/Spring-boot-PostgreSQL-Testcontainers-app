package com.example.demopostgresql.service;

import com.example.demopostgresql.dao.PersonRepository;
import com.example.demopostgresql.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private PersonRepository personRepository;

    @Autowired
    public DatabaseSeeder (PersonRepository personRepository){
        this.personRepository=personRepository;
    }

    @Override
    public void run(String... strings) throws Exception {
        List<Person> persons= new ArrayList<>();

        persons.add(new Person("Employee First",39, "0904567888","emailemployeefirst@email.cz", 3000));
        persons.add(new Person("Employee First",39, "0909867888","emailemployeesecond@email.cz", 1600));

        personRepository.saveAll(persons);
    }
}
