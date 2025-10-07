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
import req2test.tool.outputArtefacts.project1.model.Customer;
import req2test.tool.outputArtefacts.project1.usecase.GetCustomerUseCase;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(CustomerController.class)
public class GetCustomerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetCustomerUseCase getCustomerUseCase;

    @Test
    public void getExistingCustomerReturns200() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Customer customer = new Customer(
                idCustomer,
                "ValidString",
                "1234-56",
                LocalDate.now().plusYears(5),
                LocalDate.now(),
                LocalDate.now().minusYears(20)
        );
        when(getCustomerUseCase.execute(idCustomer)).thenReturn(customer);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/{idCustomer}", idCustomer))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idCustomer").value(idCustomer.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value("ValidString"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licenseNumber").value("1234-56"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licenseNumberExpirationDate").value(LocalDate.now().plusYears(5).toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(LocalDate.now().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value(LocalDate.now().minusYears(20).toString()));

        verify(getCustomerUseCase).execute(idCustomer);
    }

    @Test
    public void getNonExistingCustomerReturns404() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doThrow(new CustomerException("Customer not found")).when(getCustomerUseCase).execute(idCustomer);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/{idCustomer}", idCustomer))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Customer not found")));

        verify(getCustomerUseCase).execute(idCustomer);
    }

    @Test
    public void getCustomerWithInvalidUUIDReturns400() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/{idCustomer}", "invalid-uuid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("invalid idCustomer type")));
    }

    @Test
    public void getCustomerReturns500OnDatabaseError() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doThrow(new DataBaseException("Failed to retrieve Customer from database")).when(getCustomerUseCase).execute(idCustomer);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/{idCustomer}", idCustomer))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Failed to retrieve Customer from database")));

        verify(getCustomerUseCase).execute(idCustomer);
    }
}
