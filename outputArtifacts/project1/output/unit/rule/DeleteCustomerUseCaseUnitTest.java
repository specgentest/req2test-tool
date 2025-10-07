package req2test.tool.outputArtefacts.project1.usecase;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import req2test.tool.outputArtefacts.project1.exception.CustomerException;
import req2test.tool.outputArtefacts.project1.exception.DataBaseException;
import req2test.tool.outputArtefacts.project1.repository.CustomerRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DeleteCustomerUseCaseUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private DeleteCustomerUseCase deleteCustomerUseCase;

    public DeleteCustomerUseCaseUnitTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteCustomer_ValidData() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(customerRepository.existsByIdCustomer(idCustomer)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(idCustomer);

        // Act
        deleteCustomerUseCase.execute(idCustomer);

        // Assert
        verify(customerRepository).existsByIdCustomer(idCustomer);
        verify(customerRepository).deleteById(idCustomer);
    }

    @Test
    void testDeleteCustomer_InstanceNotFound() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(customerRepository.existsByIdCustomer(idCustomer)).thenReturn(false);

        // Act & Assert
        CustomerException exception = assertThrows(CustomerException.class, () -> deleteCustomerUseCase.execute(idCustomer));
        assertEquals("Customer not found", exception.getMessage());
        verify(customerRepository).existsByIdCustomer(idCustomer);
        verify(customerRepository, never()).deleteById(idCustomer);
    }

    @Test
    void testDeleteCustomer_DBCommunicationError() {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(customerRepository.existsByIdCustomer(idCustomer)).thenThrow(new DataIntegrityViolationException("Database error"));

        // Act & Assert
        assertThrows(DataBaseException.class, () -> deleteCustomerUseCase.execute(idCustomer));
        verify(customerRepository).existsByIdCustomer(idCustomer);
    }
}
