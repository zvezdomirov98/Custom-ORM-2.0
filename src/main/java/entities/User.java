package entities;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import annotations.generationstrategy.GeneratedValue;
import annotations.generationstrategy.GenerationType;

import java.util.Date;

@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username",
            unique = true,
            nullable = false)
    private String username;

    @Column(name = "password",
            nullable = false)
    private String password;

    @Column(name = "registration_date")
    private Date registrationDate;

//        @Column(name = "birth_date")
    private Date birthDate;

    public User() {

    }

    public User(String username,
                String password,
                Date registrationDate,
                Date birthDate) {
        setUsername(username);
        setPassword(password);
        setRegistrationDate(registrationDate);
        setBirthDate(birthDate);
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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
