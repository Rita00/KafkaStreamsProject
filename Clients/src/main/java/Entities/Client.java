package Entities;

public class Client {
    private Long id;
    private String name;

    public Client(){

    }

    public Client(Long client_id, String client_name){
        this.id = client_id;
        this.name = client_name;
    }

    public Long getClient_id() {
        return id;
    }

    public void setClient_id(Long client_id) {
        this.id = client_id;
    }

    public String getClient_name() {
        return name;
    }

    public void setClient_name(String client_name) {
        this.name = client_name;
    }
}
