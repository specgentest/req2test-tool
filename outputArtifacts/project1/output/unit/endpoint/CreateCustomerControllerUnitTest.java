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
import req2test.tool.outputArtefacts.project1.usecase.CreateCustomerUseCase;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(CustomerController.class)
public class CreateCustomerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateCustomerUseCase createCustomerUseCase;

    @Test
    public void createCustomerReturns201WhenDataIsValidAndUnique() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);
        Customer customer = new Customer(
                idCustomer,
                fullName,
                licenseNumber,
                LocalDate.now().plusYears(5),
                LocalDate.now(),
                birthDate
        );
        when(createCustomerUseCase.execute(fullName, licenseNumber, birthDate)).thenReturn(customer);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"ValidString\",\"licenseNumber\":\"1234-56\",\"birthDate\":\"" + birthDate.toString() + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idCustomer").value(idCustomer.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullName").value(fullName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licenseNumber").value(licenseNumber))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licenseNumberExpirationDate").value(LocalDate.now().plusYears(5).toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(LocalDate.now().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value(birthDate.toString()));

        verify(createCustomerUseCase).execute(fullName, licenseNumber, birthDate);
    }

    @Test
    public void createCustomerReturns400WhenFullNameIsInvalid() throws Exception {
        // Arrange
        String fullName = "Invalid123";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);
        doThrow(new CustomerException("fullName does not allow numbers and special characters")).when(createCustomerUseCase).execute(fullName, licenseNumber, birthDate);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Invalid123\",\"licenseNumber\":\"1234-56\",\"birthDate\":\"" + birthDate.toString() + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string(containsString("fullName does not allow numbers and special characters")));

        verify(createCustomerUseCase).execute(fullName, licenseNumber, birthDate);
    }

    @Test
    public void createCustomerReturns500OnDatabaseError() throws Exception {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);
        doThrow(new DataBaseException("Failed to save Customer in the database")).when(createCustomerUseCase).execute(fullName, licenseNumber, birthDate);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"ValidString\",\"licenseNumber\":\"1234-56\",\"birthDate\":\"" + birthDate.toString() + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Failed to save Customer in the database")));

        verify(createCustomerUseCase).execute(fullName, licenseNumber, birthDate);
    }
}
