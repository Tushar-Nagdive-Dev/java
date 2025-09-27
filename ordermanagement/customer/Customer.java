package ordermanagement.customer;

/**
* Represents a customer in the order management system
*/
public class Customer {

    private final String customerId;
    private String customerName;
    private String email;
    private String phoneNumber;

    private static final EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    private static final String PHONE_REGEX = "^(\\+\\d{1,3}[-.\\s]?)?((\\(\\d{3}\\))|\\d{3})[-.\\s]?\\d{3}[-.\\s]?\\d{4}$";

    public Customer(String customerId, String customerName, String email, String phoneNumber) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    private Boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private Boolean isValidPhone(String phone) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    publiv void updateContactDetails(String email, String phoneNumber) {
        if(email != null && isValidEmail(email)) {
            this.email = email;
        }

        if (phoneNumber != null && isValidPhone(phoneNumber)) {
            this.phoneNumber = phoneNumber;
        }
    }

}