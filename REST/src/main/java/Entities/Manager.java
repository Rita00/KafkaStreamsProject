package Entities;

import javax.persistence.*;
import java.util.List;

@SuppressWarnings("unused")
@Entity
public class Manager {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    @OneToMany(mappedBy = "manager")
    private List<Person> clients;

    public Manager() {}

    public Manager(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Person> getClients() {
        return clients;
    }

    public void setClients(List<Person> clients) {
        this.clients = clients;
    }
}
