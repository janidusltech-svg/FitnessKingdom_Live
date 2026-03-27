package com.fitnesscenter;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MemberController {

    private int getMonthlyPrice(String plan) {
        if (plan.equalsIgnoreCase("Basic")) return 3000;
        if (plan.equalsIgnoreCase("Standard")) return 5000;
        if (plan.equalsIgnoreCase("VIP")) return 8000;
        return 0;
    }

    private int calculatePrice(String plan, String duration) {
        int months = 1;
        try { months = Integer.parseInt(duration); } catch (Exception e) {}
        return getMonthlyPrice(plan) * months;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        // Grab the historical member count and send it to the HTML for the animation!
        model.addAttribute("totalMembers", FileHandler.getTotalHistoricalMembers());
        return "index";
    }
    @GetMapping("/plans") public String showPlansPage() { return "plans"; }
    @GetMapping("/contact") public String showContactPage() { return "contact"; }
    @GetMapping("/register") public String showRegistrationForm() { return "register"; }

    @PostMapping("/saveMember")
    public String saveNewMember(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("age") String age,
            @RequestParam("phone") String phone,
            @RequestParam("nic") String nic,
            @RequestParam("membershipPlan") String plan,
            @RequestParam("duration") String duration, Model model) {

        // THE UPGRADE: Ask the FileHandler for the permanent, real-world ID!
        String newId = FileHandler.generateNextMemberId();

        Member newMember = new Member(newId, name, email, age, phone, nic, plan, duration, "Pending", "Pending Approval");
        FileHandler.saveMember(newMember);

        int total = calculatePrice(plan, duration);
        int monthly = getMonthlyPrice(plan);

        model.addAttribute("memberId", newId);
        model.addAttribute("totalPrice", total);
        model.addAttribute("monthlyPrice", monthly);
        return "success";
    }

    @GetMapping("/login") public String showLoginPage() { return "login"; }

    @PostMapping("/adminLogin")
    public String processLogin(@RequestParam("username") String username, @RequestParam("password") String password,
                               HttpSession session, Model model) {
        if (username.equals("admin") && password.equals("admin123")) {
            session.setAttribute("adminLogged", true);
            return "redirect:/members";
        }
        model.addAttribute("error", "Invalid Credentials!");
        return "login";
    }

    @GetMapping("/logout") public String logout(HttpSession session) {
        session.invalidate(); return "redirect:/";
    }

    @GetMapping("/members")
    public String showAllMembers(HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") == null) return "redirect:/login";
        List<Member> pending = new ArrayList<>();
        List<Member> active = new ArrayList<>();
        for (Member m : FileHandler.getAllMembers()) {
            if (m.getStatus().equals("Pending")) pending.add(m);
            else active.add(m);
        }
        model.addAttribute("pendingMembers", pending);
        model.addAttribute("activeMembers", active);
        return "members";
    }

    @GetMapping("/approve/{id}")
    public String approveMember(@PathVariable("id") String id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("adminLogged") == null) return "redirect:/login";

        Member target = null;
        for (Member m : FileHandler.getAllMembers()) {
            if (m.getId().equals(id)) { target = m; break; }
        }

        if (target != null) {
            int oneMonthPrice = getMonthlyPrice(target.getMembershipPlan());
            int totalPaid = 0;

            for (Payment p : FileHandler.getAllPayments()) {
                if (p.getMemberId().equals(id)) totalPaid += Integer.parseInt(p.getAmount());
            }

            if (totalPaid >= oneMonthPrice) {
                FileHandler.approveMember(id);
                redirectAttributes.addFlashAttribute("successMsg", "Member " + id + " Approved successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Member " + id + " needs to pay at least Rs. " + oneMonthPrice + " before they can be registered. They have only paid Rs. " + totalPaid + ".");
            }
        }
        return "redirect:/members";
    }

    // --- SECURE DELETE LOGIC ---
    @PostMapping("/deleteMember")
    public String secureDeleteMember(@RequestParam("id") String id,
                                     @RequestParam("adminPassword") String adminPassword,
                                     HttpSession session, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("adminLogged") == null) return "redirect:/login";

        // VERIFY ADMIN PASSWORD
        if (!"admin123".equals(adminPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Security Alert: Incorrect Admin Password. Deletion Cancelled for Member #" + id);
            return "redirect:/members";
        }

        FileHandler.deleteMember(id);
        redirectAttributes.addFlashAttribute("successMsg", "Member #" + id + " has been permanently deleted from the system.");

        return "redirect:/members";
    }

    @GetMapping("/payments")
    public String showPaymentsPage(HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") == null) return "redirect:/login";

        List<Member> members = FileHandler.getAllMembers();
        List<Payment> payments = FileHandler.getAllPayments();

        int maxId = 999;
        for (Payment p : payments) {
            try {
                int currentId = Integer.parseInt(p.getReceiptId());
                if (currentId > maxId) maxId = currentId;
            } catch (Exception e) {}
        }
        model.addAttribute("nextReceiptId", maxId + 1);

        for (Member m : members) {
            int price = calculatePrice(m.getMembershipPlan(), m.getDuration());
            m.setPlanPrice(price);

            int totalPaid = 0;
            for (Payment p : payments) {
                if (p.getMemberId().equals(m.getId())) totalPaid += Integer.parseInt(p.getAmount());
            }
            m.setTotalPaid(totalPaid);
            m.setBalance(Math.max(price - totalPaid, 0));
        }
        model.addAttribute("members", members);
        model.addAttribute("payments", payments);
        return "payments";
    }

    @PostMapping("/recordPayment")
    public String recordNewPayment(@RequestParam("receiptId") String receiptId, @RequestParam("memberId") String memberId,
                                   @RequestParam("amount") String amount, HttpSession session) {
        if (session.getAttribute("adminLogged") == null) return "redirect:/login";
        FileHandler.savePayment(new Payment(receiptId, memberId, amount, LocalDate.now().toString()));
        return "redirect:/payments";
    }

    @PostMapping("/clearPayments")
    public String clearPayments(@RequestParam("clearType") String clearType, @RequestParam(value = "customDate", required = false) String customDate, HttpSession session) {
        if (session.getAttribute("adminLogged") == null) return "redirect:/login";
        FileHandler.clearPayments(clearType, customDate);
        return "redirect:/payments";
    }

    @GetMapping("/search")
    public String searchMember(@RequestParam(value = "query", required = false) String query, HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") == null) return "redirect:/login";

        if (query != null && !query.trim().isEmpty()) {
            Member foundMember = null;
            for (Member m : FileHandler.getAllMembers()) {
                if (m.getId().equals(query.trim())) {
                    foundMember = m; break;
                }
            }

            if (foundMember != null) {
                int price = calculatePrice(foundMember.getMembershipPlan(), foundMember.getDuration());
                foundMember.setPlanPrice(price);

                int totalPaid = 0;
                List<Payment> memberPayments = new ArrayList<>();
                for (Payment p : FileHandler.getAllPayments()) {
                    if (p.getMemberId().equals(foundMember.getId())) {
                        totalPaid += Integer.parseInt(p.getAmount());
                        memberPayments.add(p);
                    }
                }
                foundMember.setTotalPaid(totalPaid);
                foundMember.setBalance(Math.max(price - totalPaid, 0));

                model.addAttribute("member", foundMember);
                model.addAttribute("paymentHistory", memberPayments);
            } else {
                model.addAttribute("errorMsg", "No member found with ID: " + query);
            }
        }
        return "search";
    }

    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable("id") String id, HttpSession session, Model model) {
        if (session.getAttribute("adminLogged") == null) return "redirect:/login";

        for (Member m : FileHandler.getAllMembers()) {
            if (m.getId().equals(id)) {
                model.addAttribute("member", m);
                return "edit";
            }
        }
        return "redirect:/members";
    }

    @PostMapping("/updateMember")
    public String processUpdate(
            @RequestParam("id") String id, @RequestParam("name") String name,
            @RequestParam("email") String email, @RequestParam("age") String age,
            @RequestParam("phone") String phone, @RequestParam("nic") String nic,
            @RequestParam("membershipPlan") String plan, @RequestParam("duration") String duration,
            @RequestParam("status") String status, @RequestParam("registeredDate") String registeredDate,
            @RequestParam("adminPassword") String adminPassword,
            HttpSession session, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("adminLogged") == null) return "redirect:/login";

        if (!"admin123".equals(adminPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Security Alert: Incorrect Admin Password. Update Cancelled.");
            return "redirect:/edit/" + id;
        }

        Member updatedMember = new Member(id, name, email, age, phone, nic, plan, duration, status, registeredDate);
        FileHandler.updateMember(updatedMember);

        redirectAttributes.addFlashAttribute("successMsg", "Member #" + id + " has been successfully updated.");
        return "redirect:/members";
    }
}