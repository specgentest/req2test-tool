package req2test.tool.outputArtefacts.project1.usecase;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import req2test.tool.outputArtefacts.project1.model.Customer;
import req2test.tool.outputArtefacts.project1.repository.CustomerRepository;
import req2test.tool.outputArtefacts.project1.exception.DataBaseException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilterCustomerUseCaseUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private FilterCustomerUseCase filterCustomerUseCase;

    public FilterCustomerUseCaseUnitTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFilterCustomer_AllFiltersFilled() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate licenseNumberExpirationDate = LocalDate.now().plusYears(5);
        LocalDate createdAt = LocalDate.now();
        LocalDate birthDate = LocalDate.now().minusYears(20);

        Customer customer = new Customer(idCustomer, fullName, licenseNumber, licenseNumberExpirationDate, createdAt, birthDate);
        when(customerRepository.filter(fullName, licenseNumber, birthDate, createdAt)).thenReturn(List.of(customer));

        // Act
        List<Customer> result = filterCustomerUseCase.execute(fullName, licenseNumber, birthDate, createdAt);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        Customer retrievedCustomer = result.get(0);
        assertEquals(idCustomer, retrievedCustomer.getIdCustomer());
        assertEquals(fullName, retrievedCustomer.getFullName());
        assertEquals(licenseNumber, retrievedCustomer.getLicenseNumber());
        assertEquals(licenseNumberExpirationDate, retrievedCustomer.getLicenseNumberExpirationDate());
        assertEquals(createdAt, retrievedCustomer.getCreatedAt());
        assertEquals(birthDate, retrievedCustomer.getBirthDate());
        verify(customerRepository).filter(fullName, licenseNumber, birthDate, createdAt);
    }

    @Test
    void testFilterCustomer_OneFilterFilled() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate licenseNumberExpirationDate = LocalDate.now().plusYears(5);
        LocalDate createdAt = LocalDate.now();
        LocalDate birthDate = LocalDate.now().minusYears(20);

        Customer customer = new Customer(idCustomer, fullName, licenseNumber, licenseNumberExpirationDate, createdAt, birthDate);
        when(customerRepository.filter(fullName, null, null, null)).thenReturn(List.of(customer));

        // Act
        List<Customer> result = filterCustomerUseCase.execute(fullName, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        Customer retrievedCustomer = result.get(0);
        assertEquals(idCustomer, retrievedCustomer.getIdCustomer());
        assertEquals(fullName, retrievedCustomer.getFullName());
        assertEquals(licenseNumber, retrievedCustomer.getLicenseNumber());
        assertEquals(licenseNumberExpirationDate, retrievedCustomer.getLicenseNumberExpirationDate());
        assertEquals(createdAt, retrievedCustomer.getCreatedAt());
        assertEquals(birthDate, retrievedCustomer.getBirthDate());
        verify(customerRepository).filter(fullName, null, null, null);
    }

    @Test
    void testFilterCustomer_NoFilterFilledReturnsAllInstances() {
        // Arrange
        UUID idCustomer1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID idCustomer2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        UUID idCustomer3 = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");

        Customer customer1 = new Customer(idCustomer1, "ValidString", "1234-56", LocalDate.now().plusYears(5), LocalDate.now(), LocalDate.now().minusYears(20));
        Customer customer2 = new Customer(idCustomer2, "AnotherValid", "5678-90", LocalDate.now().plusYears(5), LocalDate.now(), LocalDate.now().minusYears(25));
        Customer customer3 = new Customer(idCustomer3, "MoreValidData", "4321-12", LocalDate.now().plusYears(5), LocalDate.now(), LocalDate.now().minusYears(30));

        when(customerRepository.filter(null, null, null, null)).thenReturn(List.of(customer1, customer2, customer3));

        // Act
        List<Customer> result = filterCustomerUseCase.execute(null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(customer1));
        assertTrue(result.contains(customer2));
        assertTrue(result.contains(customer3));
        verify(customerRepository).filter(null, null, null, null);
    }

    @Test
    void testFilterCustomer_OneFilterFilledReturnsEmptyList() {
        // Arrange
        String fullName = "ValidString";

        when(customerRepository.filter(fullName, null, null, null)).thenReturn(Collections.emptyList());

        // Act
        List<Customer> result = filterCustomerUseCase.execute(fullName, null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerRepository).filter(fullName, null, null, null);
    }

    @Test
    void testFilterCustomer_DBCommunicationError() {
        // Arrange
        when(customerRepository.filter(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // Act & Assert
        assertThrows(DataBaseException.class, () -> filterCustomerUseCase.execute("ValidString", "1234-56", LocalDate.now().minusYears(20), LocalDate.now()));
        verify(customerRepository).filter(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
    }
}
