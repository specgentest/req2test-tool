package req2test.tool.outputArtefacts.project1.usecase;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import req2test.tool.outputArtefacts.project1.exception.CustomerException;
import req2test.tool.outputArtefacts.project1.exception.DataBaseException;
import req2test.tool.outputArtefacts.project1.model.Customer;
import req2test.tool.outputArtefacts.project1.repository.CustomerRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateCustomerUseCaseUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CreateCustomerUseCase createCustomerUseCase;

    public CreateCustomerUseCaseUnitTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCustomer_ValidAndNonDuplicateData() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate licenseNumberExpirationDate = LocalDate.now().plusYears(5);
        LocalDate createdAt = LocalDate.now();
        LocalDate birthDate = LocalDate.now().minusYears(20);

        Customer customer = new Customer(idCustomer, fullName, licenseNumber, licenseNumberExpirationDate, createdAt, birthDate);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        Customer result = createCustomerUseCase.execute(fullName, licenseNumber, birthDate);

        // Assert
        assertNotNull(result);
        assertEquals(idCustomer, result.getIdCustomer());
        assertEquals(fullName, result.getFullName());
        assertEquals(licenseNumber, result.getLicenseNumber());
        assertEquals(licenseNumberExpirationDate, result.getLicenseNumberExpirationDate());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(birthDate, result.getBirthDate());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testCreateCustomer_DBCommunicationError() {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        when(customerRepository.save(any(Customer.class))).thenThrow(new DataIntegrityViolationException("Database error"));

        // Act & Assert
        assertThrows(DataBaseException.class, () -> createCustomerUseCase.execute(fullName, licenseNumber, birthDate));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testCreateCustomer_InvalidData() {
        // Arrange
        String fullName = "Invalid123";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        // Act & Assert
        CustomerException exception = assertThrows(CustomerException.class, () -> createCustomerUseCase.execute(fullName, licenseNumber, birthDate));
        assertEquals("fullName does not allow numbers and special characters", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
