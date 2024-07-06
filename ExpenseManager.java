import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ExpenseManager {

    private static final Map<String, User> users = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.print("Choose an action: register, login, quit: ");
            String action = scanner.nextLine();
            switch (action) {
                case "register":
                    registerUser();
                    break;
                case "login":
                    loginUser();
                    break;
                case "quit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid action.");
            }
        }
    }

    private static void registerUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        users.put(username, new User(username, password));
        System.out.println("User registered successfully.");
    }

    private static void loginUser() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            System.out.println("Login successful!");
            userMenu(user);
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void userMenu(User user) {
        while (true) {
            System.out.print("Choose an action: add, list, summary, save, load, logout: ");
            String action = scanner.nextLine();
            switch (action) {
                case "add":
                    addExpense(user);
                    break;
                case "list":
                    listExpenses(user);
                    break;
                case "summary":
                    summarizeExpenses(user);
                    break;
                case "save":
                    saveUser(user);
                    break;
                case "load":
                    user = loadUser(user.getUsername());
                    break;
                case "logout":
                    return;
                default:
                    System.out.println("Invalid action.");
            }
        }
    }

    private static void addExpense(User user) {
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());
        user.addExpense(date, category, amount);
        System.out.println("Expense added successfully.");
    }

    private static void listExpenses(User user) {
        System.out.print("Sort by (date, category, amount) or leave blank: ");
        String sortBy = scanner.nextLine();
        System.out.print("Filter by category or leave blank: ");
        String filterBy = scanner.nextLine().isEmpty() ? null : scanner.nextLine();
        List<Expense> expenses = user.listExpenses(sortBy, filterBy);
        expenses.forEach(System.out::println);
    }

    private static void summarizeExpenses(User user) {
        System.out.print("Enter category to summarize: ");
        String category = scanner.nextLine();
        double total = user.categoryWiseSummation(category);
        System.out.println("Total for " + category + ": " + total);
    }

    private static void saveUser(User user) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(user.getUsername() + ".dat"))) {
            oos.writeObject(user);
            System.out.println("User data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    private static User loadUser(String username) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(username + ".dat"))) {
            User user = (User) ois.readObject();
            users.put(username, user);
            System.out.println("User data loaded successfully.");
            return user;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading user data: " + e.getMessage());
            return null;
        }
    }

    static class User implements Serializable {
        private final String username;
        private final String password;
        private final List<Expense> expenses;

        public User(String username, String password) {
            this.username = username;
            this.password = hashPassword(password);
            this.expenses = new ArrayList<>();
        }

        private String hashPassword(String password) {
            return Integer.toString(password.hashCode());
        }

        public boolean checkPassword(String password) {
            return hashPassword(password).equals(this.password);
        }

        public void addExpense(String date, String category, double amount) {
            expenses.add(new Expense(date, category, amount));
        }

        public List<Expense> listExpenses(String sortBy, String filterBy) {
            return expenses.stream()
                    .filter(e -> filterBy == null || e.getCategory().equalsIgnoreCase(filterBy))
                    .sorted((e1, e2) -> {
                        switch (sortBy) {
                            case "date":
                                return e1.getDate().compareTo(e2.getDate());
                            case "category":
                                return e1.getCategory().compareToIgnoreCase(e2.getCategory());
                            case "amount":
                                return Double.compare(e1.getAmount(), e2.getAmount());
                            default:
                                return 0;
                        }
                    })
                    .collect(Collectors.toList());
        }

        public double categoryWiseSummation(String category) {
            return expenses.stream()
                    .filter(e -> e.getCategory().equalsIgnoreCase(category))
                    .mapToDouble(Expense::getAmount)
                    .sum();
        }

        public String getUsername() {
            return username;
        }
    }

    static class Expense implements Serializable {
        private final String date;
        private final String category;
        private final double amount;

        public Expense(String date, String category, double amount) {
            this.date = date;
            this.category = category;
            this.amount = amount;
        }

        public String getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return "Expense{" +
                    "date='" + date + '\'' +
                    ", category='" + category + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }
}
