package req2test.tool.outputArtefacts.project1.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import req2test.tool.outputArtefacts.project1.exception.CustomerException;

import java.time.LocalDate;

public class CustomerTest {

    @Test
    public void testValidfullNameAlphabetic() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act
        Customer customer = new Customer(fullName, licenseNumber, birthDate);

        // Assert
        Assertions.assertNotNull(customer);
        Assertions.assertEquals(fullName, customer.getFullName());
        Assertions.assertEquals(licenseNumber, customer.getLicenseNumber());
        Assertions.assertEquals(birthDate, customer.getBirthDate());
    }

    @Test
    public void testInvalidfullNameWithNumbers() {
        // Arrange
        String fullName = "Invalid123";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act & Assert
        CustomerException exception = Assertions.assertThrows(CustomerException.class, () -> {
            new Customer(fullName, licenseNumber, birthDate);
        });
        Assertions.assertEquals("fullName does not allow numbers and special characters", exception.getMessage());
    }

    @Test
    public void testInvalidfullNameWithSpecialCharacters() {
        // Arrange
        String fullName = "Invalid@String";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act & Assert
        CustomerException exception = Assertions.assertThrows(CustomerException.class, () -> {
            new Customer(fullName, licenseNumber, birthDate);
        });
        Assertions.assertEquals("fullName does not allow numbers and special characters", exception.getMessage());
    }

    @Test
    public void testNullfullName() {
        // Arrange
        String fullName = null;
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act & Assert
        CustomerException exception = Assertions.assertThrows(CustomerException.class, () -> {
            new Customer(fullName, licenseNumber, birthDate);
        });
        Assertions.assertEquals("fullName cannot be null", exception.getMessage());
    }

    @Test
    public void testValidlicenseNumberFormat() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act
        Customer customer = new Customer(fullName, licenseNumber, birthDate);

        // Assert
        Assertions.assertNotNull(customer);
        Assertions.assertEquals(fullName, customer.getFullName());
        Assertions.assertEquals(licenseNumber, customer.getLicenseNumber());
        Assertions.assertEquals(birthDate, customer.getBirthDate());
    }

    @Test
    public void testInvalidlicenseNumberFormat() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "123456";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act & Assert
        CustomerException exception = Assertions.assertThrows(CustomerException.class, () -> {
            new Customer(fullName, licenseNumber, birthDate);
        });
        Assertions.assertEquals("licenseNumber must match format XXXX-XX", exception.getMessage());
    }

    @Test
    public void testNulllicenseNumber() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = null;
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act & Assert
        CustomerException exception = Assertions.assertThrows(CustomerException.class, () -> {
            new Customer(fullName, licenseNumber, birthDate);
        });
        Assertions.assertEquals("licenseNumber cannot be null", exception.getMessage());
    }

    @Test
    public void testValidbirthDateDate() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act
        Customer customer = new Customer(fullName, licenseNumber, birthDate);

        // Assert
        Assertions.assertNotNull(customer);
        Assertions.assertEquals(fullName, customer.getFullName());
        Assertions.assertEquals(licenseNumber, customer.getLicenseNumber());
        Assertions.assertEquals(birthDate, customer.getBirthDate());
    }

    @Test
    public void testInvalidbirthDateDateTooRecent() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(10);

        // Act & Assert
        CustomerException exception = Assertions.assertThrows(CustomerException.class, () -> {
            new Customer(fullName, licenseNumber, birthDate);
        });
        Assertions.assertEquals("birthDate must be a date less than the current date - 18 years", exception.getMessage());
    }

    @Test
    public void testNullbirthDate() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = null;

        // Act & Assert
        CustomerException exception = Assertions.assertThrows(CustomerException.class, () -> {
            new Customer(fullName, licenseNumber, birthDate);
        });
        Assertions.assertEquals("birthDate cannot be null", exception.getMessage());
    }
}
