package entities;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.util.Date;

@Entity(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "registration_date")
    private Date registrationDate;

    public User() {

    }

    public User(String username,
                String password,
                Date registrationDate) {
        setUsername(username);
        setPassword(password);
        setRegistrationDate(registrationDate);
    }


    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    private void setRegistrationDate(Date registrationDate) {

        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
}
