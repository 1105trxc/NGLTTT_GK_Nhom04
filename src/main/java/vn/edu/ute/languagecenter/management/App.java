package vn.edu.ute.languagecenter.management;

import vn.edu.ute.languagecenter.management.db.Jpa;

public class App {
    public static void main(String[] args) {
        try {
            Jpa.em().close();
            Jpa.shutdown();
            System.out.println("JPA OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}