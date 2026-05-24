/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.quickchat;

/**
 *
 * @author Lionel Mthunzi
 */
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class QuickChat {
    private static int totalMessagesSent = 0;

    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        boolean isLoggedIn = true; 

        if (isLoggedIn) {
            System.out.println("Welcome to QuickChat.");
            
            System.out.print("How many messages do you wish to enter? ");
            int maxMessages = input.nextInt();
            input.nextLine(); 
            
            Message[] messagesArray = new Message[maxMessages];
            int choice = 0;
            
            while (choice != 3) {
                System.out.println("\n--- Menu ---");
                System.out.println("1) Send Messages");
                System.out.println("2) Show recently sent messages");
                System.out.println("3) Quit");
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
                        
                        long randomNum = (long)(Math.random() * 9000000000L) + 1000000000L;
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
                        
                        if (action == 2) {
                            System.out.print("Confirm disregard by entering 0: ");
                            int confirmDelete = input.nextInt();
                            input.nextLine();
                            if (confirmDelete == 0) {
                                System.out.println("Message deleted.");
                            }
                        } else if (action == 1 || action == 3) {
                            messagesArray[totalMessagesSent] = newMsg;
                            totalMessagesSent++;
                            
                            System.out.println("\n--- Message Details ---");
                            System.out.println(newMsg.printMessages());
                            newMsg.storeMessage();
                            
                            if (totalMessagesSent == maxMessages) {
                                System.out.println("\n** All " + maxMessages + " messages completed! Total Sent: " + returnTotalMessages() + " **");
                            }
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

    public boolean checkMessageID() {
        return this.messageId.length() <= 10;
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
        if (this.recipientCell != null && this.recipientCell.length() <= 10 && !this.recipientCell.isEmpty()) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }

    public String createMessageHash() {
        String[] words = this.messageText.split(" ");
        String[] firstWord = words;
        String lastWord = words[words.length - 1];
        
        String rawHash = this.messageId.substring(0, 2) + ":" + this.messageNumber + ";" + firstWord + lastWord;
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
                "  \"messageId\": \"" + this.messageId + "\",\n" +
                "  \"recipient\": \"" + this.recipientCell + "\",\n" +
                "  \"message\": \"" + this.messageText + "\",\n" +
                "  \"hash\": \"" + this.messageHash + "\"\n" +
                "}";
        
        try (FileWriter file = new FileWriter("message_" + this.messageId + ".json")) {
            file.write(jsonFormat);
        } catch (IOException e) {
        
        }
    }
}

    
