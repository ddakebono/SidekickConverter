/*
 * Copyright (c) 2017 Owen Bennett.
 *  You may use, distribute and modify this code under the terms of the MIT licence.
 *  You should have obtained a copy of the MIT licence with this software,
 *  if not please obtain one from https://opensource.org/licences/MIT
 */

import java.io.*;
import java.util.ArrayList;

public class SidekickConverter {
    public static void main(String[] args){
        ArrayList<sidekickContact> contacts = new ArrayList<>();
        long totalBytes;
        if(args.length>1){
            FileInputStream oldData = null;
            try {
                oldData = new FileInputStream(args[0]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                if (oldData != null) {
                    totalBytes = oldData.getChannel().size();
                    System.out.println(totalBytes);
                    for (int i = 0; i < totalBytes; i+=442) {
                        if (totalBytes >= i) {
                            boolean badContact = false;
                            sidekickContact contact = new sidekickContact();
                            byte[] contactBytes = new byte[442];
                            oldData.read(contactBytes);
                            contact.name = bytesToString(contactBytes, 0, 30);
                            if(contact.name==null)
                                badContact = true;
                            String ac = bytesToString(contactBytes, 30, 6);
                            contact.phonenumber = bytesToString(contactBytes, 36, 30);
                            if(ac!=null)
                                if(ac.length()>0)
                                    contact.phonenumber = "(" + ac + ")" + contact.phonenumber;

                            contact.company = bytesToString(contactBytes, 84, 30);
                            contact.address = bytesToString(contactBytes, 114, 30);
                            contact.city = bytesToString(contactBytes, 144, 30);
                            contact.state = bytesToString(contactBytes, 174, 22);
                            contact.zip = bytesToString(contactBytes, 196, 12);
                            contact.country = bytesToString(contactBytes, 208, 22);
                            contact.notes = bytesToString(contactBytes, 230, 200);
                            contact.category = bytesToString(contactBytes, 430, 12);
                            if(!badContact) {
                                contacts.add(contact);
                            } else {
                                System.out.println("Contact \"" + contact.name + "\" contains errors, couldn't import properly!");
                            }
                        } else {
                            System.out.println("Uh oh, looks like something went weird and out loop died.");
                        }

                    }
                    PrintWriter exported = new PrintWriter(args[1]);
                    exported.println("Name, Company, Address, City, Category, State, Zip, Country, Phonenumber, Notes");
                    for(sidekickContact contact : contacts){
                        exported.println(contact.toString());
                        System.out.println(contact.toString());
                    }

                    System.out.println("Successfully exported Sidekick phonebook!");
                    exported.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        } else {
            System.out.println("Missing Arguments! Ex. java -jar SidekickConverter.jar OldPhonebook Output.csv");
            System.exit(1);
        }
    }

    private static String bytesToString(byte[] array, int curPos, int length) throws UnsupportedEncodingException {
        byte[] target = new byte[length];
        System.arraycopy(array, curPos, target, 0, length);
        int wordLength = target[0];
        if(wordLength<0)
            wordLength+=256;
        byte[] stringByteArray = new byte[wordLength];
        try {
            System.arraycopy(target, 1, stringByteArray, 0, wordLength);
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
        String out = new String(stringByteArray, "UTF-8");
        System.out.println("Length: " + wordLength + ":" + out);
        return out;
    }
}

class sidekickContact {
    String name; //30 bytes
    String phonenumber; //30
    //18 byte gap for calling charge info block
    String company; //30 bytes
    String address; //30
    String city; //30
    String state; //22
    String zip; //12
    String country; //22
    String notes; //200
    String category; //12

    public String toString(){
        return "\"" + name + "\",\"" + company + "\",\"" + address + "\",\"" + city + "\",\"" +category + "\",\"" + state + "\",\"" + zip + "\",\"" +country
                    + "\",\"" + phonenumber + "\",\"" + notes + "\"";
    }
}
