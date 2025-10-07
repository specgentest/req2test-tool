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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetCustomerUseCaseUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private GetCustomerUseCase getCustomerUseCase;

    public GetCustomerUseCaseUnitTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCustomer_ValidIDSuccessfully() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate licenseNumberExpirationDate = LocalDate.now().plusYears(5);
        LocalDate createdAt = LocalDate.now();
        LocalDate birthDate = LocalDate.now().minusYears(20);

        Customer customer = new Customer(idCustomer, fullName, licenseNumber, licenseNumberExpirationDate, createdAt, birthDate);
        when(customerRepository.findById(idCustomer)).thenReturn(Optional.of(customer));

        // Act
        Customer result = getCustomerUseCase.execute(idCustomer);

        // Assert
        assertNotNull(result);
        assertEquals(idCustomer, result.getIdCustomer());
        assertEquals(fullName, result.getFullName());
        assertEquals(licenseNumber, result.getLicenseNumber());
        assertEquals(licenseNumberExpirationDate, result.getLicenseNumberExpirationDate());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(birthDate, result.getBirthDate());
        verify(customerRepository).findById(idCustomer);
    }

    @Test
    void testGetCustomer_InstanceNotFound() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(customerRepository.findById(idCustomer)).thenReturn(Optional.empty());

        // Act & Assert
        CustomerException exception = assertThrows(CustomerException.class, () -> getCustomerUseCase.execute(idCustomer));
        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository).findById(idCustomer);
    }

    @Test
    void testGetCustomer_DBCommunicationError() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(customerRepository.findById(idCustomer)).thenThrow(new DataIntegrityViolationException("Database error"));

        // Act & Assert
        assertThrows(DataBaseException.class, () -> getCustomerUseCase.execute(idCustomer));
        verify(customerRepository).findById(idCustomer);
    }
}
