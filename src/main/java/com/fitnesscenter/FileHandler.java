package com.fitnesscenter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    private static final String MEMBER_FILE = "data/members.txt";
    private static final String PAYMENT_FILE = "data/payments.txt";
    private static final String ID_FILE = "data/id_tracker.txt"; // NEW: The sequence tracker

    // ================= REAL-WORLD ID GENERATOR =================
    public static String generateNextMemberId() {
        new File("data").mkdirs();
        int nextId = 1; // Default to 1 if the system is brand new

        // 1. Check the tracker file to see what the next number should be
        File file = new File(ID_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(ID_FILE))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    nextId = Integer.parseInt(line.trim());
                }
            } catch (Exception e) { System.out.println("Error reading ID tracker"); }
        }

        // 2. Prepare the ID for the new member (e.g., "001", "002")
        String formattedId = String.format("%03d", nextId);

        // 3. Immediately save the NEXT number (nextId + 1) to the file for the future
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ID_FILE))) {
            writer.write(String.valueOf(nextId + 1));
        } catch (Exception e) { System.out.println("Error saving ID tracker"); }

        return formattedId;
    }
    // NEW: Safely reads total historical members for the Homepage without increasing the tracker!
    public static int getTotalHistoricalMembers() {
        File file = new File(ID_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(ID_FILE))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return Integer.parseInt(line.trim()) - 1; // Current next ID minus 1
                }
            } catch (Exception e) { System.out.println("Error reading ID tracker for homepage"); }
        }
        return 0; // If the tracker doesn't exist yet, there are 0 members
    }

    // ================= MEMBERS LOGIC =================

    public static boolean saveMember(Member member) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEMBER_FILE, true))) {
            writer.write(member.toFileString());
            writer.newLine();
            return true;
        } catch (IOException e) { return false; }
    }

    public static List<Member> getAllMembers() {
        new File("data").mkdirs();
        List<Member> memberList = new ArrayList<>();
        File file = new File(MEMBER_FILE);
        if (!file.exists()) return memberList;

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(MEMBER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");

                if (data.length >= 10) {
                    memberList.add(new Member(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim(), data[4].trim(), data[5].trim(), data[6].trim(), data[7].trim(), data[8].trim(), data[9].trim()));
                } else if (data.length >= 7) {
                    memberList.add(new Member(data[0].trim(), data[1].trim(), "N/A", "N/A", data[2].trim(), data[3].trim(), data[4].trim(), data[5].trim(), data[6].trim(), "Pending"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading file.");
        }
        return memberList;
    }

    public static void approveMember(String id) {
        List<Member> members = getAllMembers();
        for (Member m : members) {
            if (m.getId().equals(id)) {
                m.setStatus("Approved");
                m.setRegisteredDate(java.time.LocalDate.now().toString());
                break;
            }
        }
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(MEMBER_FILE))) {
            for (Member m : members) {
                writer.write(m.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {}
    }

    public static void updateMember(Member updatedMember) {
        List<Member> members = getAllMembers();
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId().equals(updatedMember.getId())) {
                members.set(i, updatedMember);
                break;
            }
        }

        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(MEMBER_FILE))) {
            for (Member m : members) {
                writer.write(m.toFileString());
                writer.newLine();
            }
        } catch (java.io.IOException e) {
            System.out.println("Error updating member file.");
        }
    }

    public static boolean deleteMember(String idToRemove) {
        List<Member> members = getAllMembers();
        boolean isDeleted = false;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEMBER_FILE, false))) {
            for (Member member : members) {
                if (member.getId().equals(idToRemove)) {
                    isDeleted = true;
                } else {
                    writer.write(member.toFileString()); writer.newLine();
                }
            }
        } catch (IOException e) { return false; }
        return isDeleted;
    }

    // ================= PAYMENTS LOGIC =================

    public static boolean savePayment(Payment payment) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENT_FILE, true))) {
            writer.write(payment.toFileString()); writer.newLine();
            return true;
        } catch (IOException e) { return false; }
    }

    public static List<Payment> getAllPayments() {
        List<Payment> paymentList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    paymentList.add(new Payment(data[0], data[1], data[2], data[3]));
                }
            }
        } catch (IOException e) {}
        return paymentList;
    }

    public static void clearPayments(String filterType, String customDateStr) {
        new File("data").mkdirs();
        List<Payment> allPayments = getAllPayments();
        List<Payment> paymentsToKeep = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        for (Payment p : allPayments) {
            java.time.LocalDate pDate;
            try {
                pDate = java.time.LocalDate.parse(p.getDate());
            } catch (Exception e) {
                paymentsToKeep.add(p);
                continue;
            }

            boolean shouldDelete = false;
            if (filterType.equals("today") && pDate.equals(today)) {
                shouldDelete = true;
            } else if (filterType.equals("week") && !pDate.isBefore(today.minusDays(7))) {
                shouldDelete = true;
            } else if (filterType.equals("custom") && pDate.equals(java.time.LocalDate.parse(customDateStr))) {
                shouldDelete = true;
            } else if (filterType.equals("all")) {
                shouldDelete = true;
            }

            if (!shouldDelete) {
                paymentsToKeep.add(p);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENT_FILE, false))) {
            for (Payment p : paymentsToKeep) {
                writer.write(p.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {}
    }
}