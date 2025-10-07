package req2test.tool.outputArtefacts.project1.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDate;

public class CustomerGeneratedAttributeTest {

    @Test
    public void testLicenseNumberExpirationDateGeneratedCorrectly() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);
        LocalDate expectedExpirationDate = LocalDate.now().plusYears(5);

        // Act
        Customer customer = new Customer(fullName, licenseNumber, birthDate);

        // Assert
        Assertions.assertNotNull(customer);
        Assertions.assertEquals(expectedExpirationDate, customer.getLicenseNumberExpirationDate());
    }
}
