package com.shepherdmoney.interviewproject.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "MyUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    private String email;

    // TODO: User's credit card
    // HINT: A user can have one or more, or none at all. We want to be able to query credit cards by user
    //       and user by a credit card.
    @OneToMany(mappedBy = "userID", cascade = CascadeType.ALL)
    private List<CreditCard> creditCards = new ArrayList<>();
}
