package org.hadatac.console.models;

/**
 * Presentation object used for displaying org.hadatac.data in a template.
 *
 * Note that it's a good practice to keep the presentation DTO,
 * which are used for reads, distinct from the form processing DTO,
 * which are used for writes.
 */
public class SignUp {
    public String name;
    public String email;
    public String password;
    public  String repeatPassword;
//    public int price;

    public SignUp(String name, String email, String password, String repeatPassword) {//int price) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.repeatPassword = repeatPassword;
//        this.price = price;
    }
}