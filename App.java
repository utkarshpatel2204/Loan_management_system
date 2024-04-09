import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Loan {
    private final int loanId;
    private final String customerName;
    private final double loanAmount;
    private final double interestRate;
    private final int loanPeriodMonths;
    private double remainingAmount;
    private final int loanTypeId;
    private final String loanTypename;

    public Loan(int loanId, String customerName, double loanAmount, double interestRate, int loanPeriodMonths,
            int loanTypeId, String loanTypename) {
        this.loanId = loanId;
        this.customerName = customerName;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanPeriodMonths = loanPeriodMonths;
        this.remainingAmount = loanAmount;
        this.loanTypeId = loanTypeId;
        this.loanTypename = loanTypename;
    }

    public int getLoanId() {
        return loanId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public int getLoanPeriodMonths() {
        return loanPeriodMonths;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public int getLoanTypeId() {
        return loanTypeId;
    }

    public String loanTypename() {
        return loanTypename;
    }

    public void makePayment(double paymentAmount) {
        if (paymentAmount <= remainingAmount) {
            remainingAmount -= paymentAmount;
            System.out.println("Payment of Rs" + paymentAmount + " made. Remaining balance: Rs" + remainingAmount);
        } else {
            System.out.println("Payment amount exceeds the remaining balance.");
        }
    }

    public double calculateInterest() {

        double monthlyInterestRate = interestRate / 12 / 100;
        double totalInterest = (loanAmount - remainingAmount) * monthlyInterestRate * loanPeriodMonths;
        return totalInterest;
    }

    public double getTotalAmount() {
        return loanAmount + calculateInterest();
    }
}

class LoanManagementSystem {
    private static final Map<Integer, Loan> loans = new HashMap<>();
    private static int loanIdCounter = 1;
    private static Connection con;

    public static void main(String[] args) {
        try {
            String dburl = "jdbc:mysql://localhost/loan";
            String dbuser = "root";
            String dbpass = "";

            // Connection with the Database
            con = DriverManager.getConnection(dburl, dbuser, dbpass);
            if (con != null) {
                System.out.println("Connection Successfully");
            } else {
                System.out.println("Connection Fail");
            }

            Scanner sc = new Scanner(System.in);

            while (true) {
                displayMenu();
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        applyForLoan();
                        break;
                    case 2:
                        makePayment();
                        break;
                    case 3:
                        viewLoanStatus();
                        break;
                    case 4:
                        // Close the database connection and exit

                        con.close();
                        System.out.println("Goodbye!");
                        return;
                        case 5:
                        search();
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayMenu() {
        System.out.println("-----------------------------------");
        System.out.println("Loan Management System :");
        System.out.println("-----------------------------------");
        System.out.println("1. Apply for a Loan");
        System.out.println("2. Make a Payment");
        System.out.println("3. View Loan Status");
        System.out.println("4. Exit");
        System.out.println("5. for search");
        System.out.print("Enter your choice: ");
    }
    private static void search() {
    Scanner sc = new Scanner(System.in);
    System.out.print("Enter customer name: ");
    String searchName = sc.nextLine();
    
    boolean found = false;
    
    for (Loan loan : loans.values()) {
        if (loan.getCustomerName().equals(searchName)) {
            System.out.println("Loan ID: " + loan.getLoanId());
            System.out.println("Customer Name: " + loan.getCustomerName());
            System.out.println("Loan Amount: Rs" + loan.getLoanAmount());
            System.out.println("Interest Rate: " + loan.getInterestRate() + "%");
            System.out.println("Loan Period (Months): " + loan.getLoanPeriodMonths());
            System.out.println("Remaining Amount: Rs" + loan.getRemainingAmount());
            System.out.println("Total Amount (including interest): Rs" + loan.getTotalAmount());
            System.out.println("Loan type ID :" + loan.getLoanTypeId());
            System.out.println("Loan type name :" + loan.loanTypename());
            System.out.println();
            found = true;
        }
    }
    
    if (!found) {
        System.out.println("Customer with name '" + searchName + "' not found.");
    }
}


    private static void displayLoanTypes() {
        try {

            Statement st = con.createStatement();
            String query = "select * from loan_types";
            ResultSet resultSet = st.executeQuery(query);
            int option = 1;
            while (resultSet.next()) {
                int loantypeid = resultSet.getInt("loan_type_id");
                String loantypename = resultSet.getString("loan_type_name");
                BigDecimal interestRate = resultSet.getBigDecimal("interest_rate");
                double maxamount = resultSet.getDouble("max_loan_amount");
                System.out.println(option++ + ".   " + loantypeid + "           " + loantypename + "               "
                        + interestRate + "             " + maxamount);
            }
        } catch (Exception e) {

        }

    }

    // This method work to add the customer which came for Loan
    private static void applyForLoan() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Available Loan types : ");
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  Loan_type_id      Loan_name            interest_rate        max_amount");
        System.out.println("--------------------------------------------------------------------------");
        displayLoanTypes();

        System.out.print("Select Loan type Id: ");
        int selectedloanTypeId = sc.nextInt();
        sc.nextLine();

        // Check if the selected loan type ID is valid
        if (!isValidLoanType(selectedloanTypeId)) {
            System.out.println("Invalid loan type ID. Please select a valid loan type.");
            return;
        }

        System.out.print("Enter customer name: ");
        String customerName = sc.nextLine();
        System.out.print("Enter loan amount: ");
        double loanAmount = sc.nextDouble();
        System.out.print("Enter interest rate: ");
        double interestRate = sc.nextDouble();

        // Check if the entered interest rate exists in the database
        if (!isValidInterestRate(selectedloanTypeId, interestRate)) {
            System.out.println("Invalid interest rate for the selected loan type. Please enter a valid interest rate.");
            return;
        }

        System.out.print("Enter loan period (in months): ");
        int loanPeriodMonths = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Loan Type name: ");
        String loanTypename = sc.nextLine();
        if (!isValidLoanTypeName(selectedloanTypeId, loanTypename)) {
            System.out
                    .println("Invalid loan type name for the selected loan type. Please enter a valid loan type name.");
            return;
        }

        Loan loan = new Loan(loanIdCounter++, customerName, loanAmount, interestRate, loanPeriodMonths,
                selectedloanTypeId, loanTypename);
        loans.put(loan.getLoanId(), loan);

        try {
            // insert the customer data in the database
            String sql = "INSERT INTO loans (loan_id, customer_name, loan_amount, loan_type_id, loan_period_months, remaining_amount) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                preparedStatement.setInt(1, loan.getLoanId());
                preparedStatement.setString(2, loan.getCustomerName());
                preparedStatement.setDouble(3, loan.getLoanAmount());
                preparedStatement.setDouble(4, loan.getLoanTypeId());
                preparedStatement.setInt(5, loan.getLoanPeriodMonths());
                preparedStatement.setDouble(6, loan.getRemainingAmount());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error applying for a loan: " + e.getMessage());
            }

            System.out.println("Loan successfully applied. Loan ID: " + loan.getLoanId());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    // method to check if the selected loan type ID is valid
    private static boolean isValidLoanType(int loanTypeId) {
        try {
            Statement st = con.createStatement();
            String query = "SELECT loan_type_id FROM loan_types WHERE loan_type_id = " + loanTypeId;
            ResultSet resultSet = st.executeQuery(query);
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error checking loan type validity: " + e.getMessage());
            return false;
        }
    }

    // method to check if the entered interest rate exists for the selected loan
    private static boolean isValidInterestRate(int loanTypeId, double interestRate) {
        try {
            Statement st = con.createStatement();
            String query = "SELECT interest_rate FROM loan_types WHERE loan_type_id = " + loanTypeId;
            ResultSet resultSet = st.executeQuery(query);
            if (resultSet.next()) {
                double validInterestRate = resultSet.getDouble("interest_rate");
                return validInterestRate == interestRate;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error checking interest rate validity: " + e.getMessage());
            return false;
        }
    }

    //  method to check if the entered loan type name is valid for the
    // selected loan type
    private static boolean isValidLoanTypeName(int loanTypeId, String loanTypename) {
        try {
            Statement st = con.createStatement();
            String query = "SELECT loan_type_name FROM loan_types WHERE loan_type_id = " + loanTypeId;
            ResultSet resultSet = st.executeQuery(query);
            if (resultSet.next()) {
                String validLoanTypeName = resultSet.getString("loan_type_name");
                return validLoanTypeName.equals(loanTypename);
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error checking loan type name validity: " + e.getMessage());
            return false;
        }
    }

    // Payment method for paying loan

    private static void makePayment() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Loan ID: ");
        int loanId = sc.nextInt();
        System.out.print("Enter payment amount: ");
        double paymentAmount = sc.nextDouble();

        Loan loan = loans.get(loanId);
        if (loan != null) {
            loan.makePayment(paymentAmount);
            try {

                // update the amount in Database

                String sql = "UPDATE loans SET remaining_amount = ? WHERE loan_id = ?";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setDouble(1, loan.getRemainingAmount());
                preparedStatement.setInt(2, loan.getLoanId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Loan not found with Loan ID: " + loanId);
        }
    }

    // method to print the Loan Status

    private static void viewLoanStatus() {
        System.out.println("Loan Status:");
        for (Loan loan : loans.values()) {
            System.out.println("Loan ID: " + loan.getLoanId());
            System.out.println("Customer Name: " + loan.getCustomerName());
            System.out.println("Loan Amount: Rs" + loan.getLoanAmount());
            System.out.println("Interest Rate: " + loan.getInterestRate() + "%");
            System.out.println("Loan Period (Months): " + loan.getLoanPeriodMonths());
            System.out.println("Remaining Amount: Rs" + loan.getRemainingAmount());
            System.out.println("Total Amount (including interest): Rs" + loan.getTotalAmount());
            System.out.println("Loan type ID :" + loan.getLoanTypeId());
            System.out.println("Loan type name :" + loan.loanTypename());
            System.out.println();
        }
    }
}
