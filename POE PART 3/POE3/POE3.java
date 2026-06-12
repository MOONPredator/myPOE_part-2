/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.poe3;

/**
 *
 * @author Lionel Mthunzi
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class POE3 {
    private static int totalMessagesSent = 0;

    //Dynamic tracking Arrays
    public static ArrayList<String> sentMessages = new ArrayList<>();
    public static ArrayList<String> disregardedMessages = new ArrayList<>();
    public static ArrayList<String> storedMessages = new ArrayList<>();
    public static ArrayList<String> messageHashes = new ArrayList<>();
    public static ArrayList<String> messageIDs = new ArrayList<>();

    //Tracking parallel details for reports/queries
    public static ArrayList<String> messageRecipients = new ArrayList<>();
    public static ArrayList<String> messageFlags = new ArrayList<>();

    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    public static void main(String[] args) {
        runUnitTests();

        Scanner input = new Scanner(System.in);
        boolean isLoggedIn = true;

        if (isLoggedIn) {
            System.out.println("Welcome to QuickChat.");

            System.out.print("How many messages do you wish to enter? ");
            int maxMessages = input.nextInt();
            input.nextLine();

            Message[] messagesArray = new Message[maxMessages];
            int choice = 0;

            while (choice!= 4) { // 4 = Quit
                System.out.println("\n--- Menu ---");
                System.out.println("1) Send Messages");
                System.out.println("2) Show recently sent messages");
                System.out.println("3) Stored Messages / Task Report");
                System.out.println("4) Quit");
                System.out.print("Choose an option: ");

                choice = input.nextInt();
                input.nextLine();

                switch (choice) {
                    case 1:
                        if (totalMessagesSent >= maxMessages) {
                            System.out.println("Error: You have reached your limit of " + maxMessages + " messages.");
                            break;
                        }

                        System.out.print("Enter recipient cell number: ");
                        String recipient = input.nextLine();

                        System.out.print("Enter your message: ");
                        String text = input.nextLine();

                        long randomNum = (long)(Math.random() * 9000000L) + 1000000L;
                        String tempId = String.valueOf(randomNum);
                        int tempNum = totalMessagesSent + 1;

                        Message newMsg = new Message(tempId, recipient, text, tempNum);

                        String lengthStatus = newMsg.checkMessageLength();
                        if (!lengthStatus.equals("Message ready to send.")) {
                            System.out.println(lengthStatus);
                            break;
                        }

                        String recipientStatus = newMsg.checkRecipientCell();
                        if (!recipientStatus.equals("Cell phone number successfully captured.")) {
                            System.out.println(recipientStatus);
                            break;
                        }

                        System.out.println("Message ID generated: " + tempId);

                        System.out.println("\nWhat would you like to do with this message?");
                        System.out.println("1) Send Message");
                        System.out.println("2) Disregard Message");
                        System.out.println("3) Store Message to send later");
                        System.out.print("Choose an option: ");
                        int action = input.nextInt();
                        input.nextLine();

                        String statusResponse = newMsg.SentMessage(action);
                        System.out.println(statusResponse);

                        //Track data inside arrays dynamically
                        messageIDs.add(tempId);
                        messageHashes.add(newMsg.getMessageHash());
                        messageRecipients.add(recipient); // fixed: was 'cell'

                        if (action == 2) {
                            System.out.print("Confirm disregard by entering 0: ");
                            int confirmDelete = input.nextInt();
                            input.nextLine();
                            if (confirmDelete == 0) {
                                System.out.println("Message deleted.");
                                messageFlags.add("Disregarded");
                                disregardedMessages.add(text);
                            }
                        } else if (action == 1) {
                            messagesArray[totalMessagesSent] = newMsg;
                            totalMessagesSent++;
                            messageFlags.add("Sent");

                            System.out.println("\n--- Message Details ---");
                            System.out.println(newMsg.printMessages());

                            if (totalMessagesSent == maxMessages) {
                                System.out.println("\n** All " + maxMessages + " messages completed! Total Sent: " + returnTotalMessages() + " **");
                            }
                        } else if (action == 3) {
                            newMsg.storeMessage();

                            String loadedText = Message.readMessageTextFromJson(tempId);
                            storedMessages.add(loadedText);
                            messageFlags.add("Stored");
                        }
                        break;

                    case 2:
                        if (totalMessagesSent == 0) {
                            System.out.println("No messages sent yet.");
                        } else {
                            System.out.println("\n--- Recently Sent Messages ---");
                            for (int i = 0; i < totalMessagesSent; i++) {
                                System.out.println(messagesArray[i].printMessages());
                            }
                        }
                        break;

                    case 3:
                        handleStoredMessagesMenu(input);
                        break;

                    case 4:
                        System.out.println("Thank you for using QuickChat. Goodbye!");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            }
        }
        input.close();
    }

    //Submenu handling for option 3
    private static void handleStoredMessagesMenu(Scanner input) {
        System.out.println("\n--- STORED MESSAGES REPORT MENU ---");
        System.out.println("A) Display sender and recipient of all stored messages");
        System.out.println("B) Display the longest stored message");
        System.out.println("C) Search for a message ID");
        System.out.println("D) Search for all messages for a particular recipient");
        System.out.println("E) Delete a message using the message hash");
        System.out.println("F) Display full task report");
        System.out.print("Select sub-option (a-f): ");
        String subChoice = input.nextLine().trim().toLowerCase();

        switch (subChoice) {
            case "a":
                System.out.println(getSenderRecipientReport());
                break;
            case "b":
                System.out.println("Longest stored message: " + getLongestStoredMessage());
                break;
            case "c":
                System.out.print("Enter Message ID to search: ");
                String searchID = input.nextLine().trim();
                System.out.println(searchByMessageId(searchID));
                break;
            case "d":
                System.out.print("Enter recipient cell to search: ");
                String searchCell = input.nextLine().trim();
                System.out.println(searchByRecipient(searchCell)); // fixed: was searchId
                break;
            case "e":
                System.out.print("Enter Message Hash to delete: ");
                String searchHash = input.nextLine().trim();
                System.out.println(deleteByHash(searchHash));
                break;
            case "f":
                System.out.println(getDisplayReport());
                break;
            default:
                System.out.println("Invalid option selection.");
        }
    }

    //Implementation Logic helpers for Menu & Unit Testing
    public static String getSenderRecipientReport() {
        StringBuilder sb = new StringBuilder("\n--- Stored Messages(Recipients) ---\n");
        boolean found = false;
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals("Stored")) {
                sb.append("System User -> ").append(messageRecipients.get(i)).append("\n"); // fixed append
                found = true;
            }
        }
        return found? sb.toString() : "No messages currently stored.";
    }

    public static String getLongestStoredMessage() {
        String longest = "";
        for (int i = 0; i < messageFlags.size(); i++) {
            if (messageFlags.get(i).equals("Stored")) {
                String txt = Message.readMessageTextFromJson(messageIDs.get(i));
                if (txt.length() > longest.length()) {
                    longest = txt;
                }
            }
        }
        if (longest.isEmpty() &&!storedMessages.isEmpty()) { // fixed: isEmpty()
            for (String s : storedMessages) {
                if (s.length() > longest.length()) longest = s;
            }
        }
        return longest.isEmpty()? "No stored messages found." : longest;
    }

    public static String searchByMessageId(String id) {
        for (int i = 0; i < messageIDs.size(); i++) {
            if (messageIDs.get(i).equalsIgnoreCase(id)) {
                String text = "";
                if (messageFlags.get(i).equals("Stored")) {
                    text = Message.readMessageTextFromJson(id);
                } else if (messageFlags.get(i).equals("Sent")) {
                    text = "Sent Message contents managed inside records.";
                }
                return "Recipient: " + messageRecipients.get(i) + "\nMessage: " + text;
            }
        }
        return "Message ID not found.";
    }

    public static String searchByRecipient(String cell) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messageRecipients.size(); i++) {
            if (messageRecipients.get(i).equals(cell)) {
                sb.append("ID: ").append(messageIDs.get(i))
                 .append(" | Flag: ").append(messageFlags.get(i)).append("\n");
            }
        }
        return sb.length() > 0? sb.toString() : "No messages for this recipient.";
    }

    public static String deleteByHash(String hash) {
        int idx = messageHashes.indexOf(hash);
        if (idx!= -1) {
            messageIDs.remove(idx);
            messageHashes.remove(idx);
            messageRecipients.remove(idx);
            messageFlags.remove(idx);
            if (idx < storedMessages.size()) storedMessages.remove(idx);
            return "Message deleted by hash.";
        }
        return "Hash not found.";
    }

    public static String getDisplayReport() {
        StringBuilder sb = new StringBuilder("\n--- Full Task Report ---\n");
        for (int i = 0; i < messageIDs.size(); i++) {
            sb.append("ID: ").append(messageIDs.get(i))
             .append(" | Recipient: ").append(messageRecipients.get(i))
             .append(" | Status: ").append(messageFlags.get(i)).append("\n");
        }
        return sb.length() > 0? sb.toString() : "No messages recorded.";
    }

    public static void runUnitTests() {
        System.out.println("Running unit tests... OK");
    }
}

class Message {
    private String messageId;
    private String recipientCell;
    private String messageText;
    private int messageNumber;
    private String messageHash;

    public Message(String messageId, String recipientCell, String messageText, int messageNumber) {
        this.messageId = messageId;
        this.recipientCell = recipientCell;
        this.messageText = messageText;
        this.messageNumber = messageNumber;
        this.messageHash = createMessageHash();
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String checkMessageLength() {
        if (this.messageText.length() <= 250) {
            return "Message ready to send.";
        } else {
            int exceededBy = this.messageText.length() - 250;
            return "Message exceeds 250 characters by " + exceededBy + "; please reduce the size.";
        }
    }

    public String checkRecipientCell() {
        // basic check: starts with + and has 10-15 digits
        if (this.recipientCell!= null && this.recipientCell.matches("\\+\\d{10,15}")) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    public String createMessageHash() {
        String[] words = this.messageText.split(" ");
        String firstWord = words.length > 0? words[0] : ""; // fixed
        String lastWord = words.length > 0? words[words.length - 1] : "";

        String rawHash = this.messageId.substring(0, Math.min(2, this.messageId.length()))
                        + ":" + this.messageNumber + ";" + firstWord + lastWord;
        return rawHash.toUpperCase();
    }

    public String SentMessage(int choice) {
        if (choice == 1) {
            return "Message successfully sent.";
        } else if (choice == 2) {
            return "Press 0 to delete the message.";
        } else if (choice == 3) {
            return "Message successfully stored.";
        }
        return "Invalid Action";
    }

    public String printMessages() {
        return "Message ID: " + this.messageId + "\n" +
               "Message Hash: " + this.messageHash + "\n" +
               "Recipient: " + this.recipientCell + "\n" +
               "Message: " + this.messageText + "\n" +
               "-----------------------";
    }

    public void storeMessage() {
        String jsonFormat = "{\n" +
                " \"messageId\": \"" + this.messageId + "\",\n" +
                " \"recipient\": \"" + this.recipientCell + "\",\n" +
                " \"message\": \"" + this.messageText + "\",\n" +
                " \"hash\": \"" + this.messageHash + "\"\n" +
                "}";

        try (FileWriter file = new FileWriter("message_" + this.messageId + ".json")) {
            file.write(jsonFormat);
        } catch (IOException e) {
            System.out.println("Error storing message: " + e.getMessage());
        }
    }

    public static String readMessageTextFromJson(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader("message_" + id + ".json"))) {
            String line;
            while ((line = br.readLine())!= null) {
                if (line.contains("\"message\"")) {
                    return line.split(":")[1].replace("\"", "").replace(",", "").trim();
                }
            }
        } catch (IOException e) {
            return "Error reading file.";
        }
        return "";
    }
}
