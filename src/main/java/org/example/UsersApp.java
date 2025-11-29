package org.example;

//imports
import java.util.*;
import java.io.*;

//Users App (list) class
public class UsersApp {
    public static void main(String[] args) {
        //creating an empty list for the Users
        ArrayList<User> users = new ArrayList<>();

        //Reading from the given file (Users.txt)
        try (Scanner reader = new Scanner(new File("Users.txt"))) {
            //reading line by line
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {//an Email without a password
                    try {
                        users.add(new User(parts[0], ""));
                    } catch (Exception ex) {
                        System.out.println(line + " --> " + ex.getMessage());
                    }
                    continue;
                }else if (parts.length > 2){//a password with an illegal char " "
                    String password = "";
                    for (int i = 1 ; i < parts.length - 1; i++) {
                        password += parts[i];
                    }
                    try {
                        users.add(new User(parts[0], password));
                    } catch (Exception ex) {
                        System.out.println(line + " --> " + ex.getMessage());
                    }
                    continue;
                }

                //assigning the first part to the username and last one to the password
                String username = parts[0];
                String password = parts[1];

                //adding then user to list
                try {
                    users.add(new User(username, password));
                } catch (Exception ex) {
                    System.out.println(line + " --> " + ex.getMessage());
                }
            }
        } catch (FileNotFoundException e) {//failed to open the file
            System.out.println("File not found!");
            return;
        }

        //Sorting the valid Emails by lexicographic order
        users.sort(Comparator.comparing(User::getName));

        //outputting all the valid Emails
        try (PrintWriter out = new PrintWriter("out.txt")) {
            for (User u : users) {
                out.println(u.getName() + " " + u.getPassword());
            }
        } catch (Exception e) {
            System.out.println("Could not write output file!");
        }
    }
}
