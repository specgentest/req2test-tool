package req2test.tool.outputArtefacts.project1.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import req2test.tool.outputArtefacts.project1.exception.CustomerException;
import req2test.tool.outputArtefacts.project1.exception.DataBaseException;
import req2test.tool.outputArtefacts.project1.usecase.DeleteCustomerUseCase;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@WebMvcTest(CustomerController.class)
public class DeleteCustomerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeleteCustomerUseCase deleteCustomerUseCase;

    @Test
    public void deleteExistingCustomerReturns204() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/customer/{idCustomer}", idCustomer))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        verify(deleteCustomerUseCase).execute(idCustomer);
    }

    @Test
    public void deleteNonExistingCustomerReturns404() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doThrow(new CustomerException("Customer not found")).when(deleteCustomerUseCase).execute(idCustomer);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/customer/{idCustomer}", idCustomer))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Customer not found")));

        verify(deleteCustomerUseCase).execute(idCustomer);
    }

    @Test
    public void deleteCustomerWithInvalidUUIDReturns400() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/customer/{idCustomer}", "invalid-uuid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("invalid idCustomer type")));
    }

    @Test
    public void deleteCustomerReturns500OnDatabaseError() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doThrow(new DataBaseException("Failed to delete Customer from database")).when(deleteCustomerUseCase).execute(idCustomer);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/customer/{idCustomer}", idCustomer))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Failed to delete Customer from database")));

        verify(deleteCustomerUseCase).execute(idCustomer);
    }
}
