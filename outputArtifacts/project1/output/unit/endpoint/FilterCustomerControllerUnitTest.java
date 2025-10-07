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
import req2test.tool.outputArtefacts.project1.exception.DataBaseException;
import req2test.tool.outputArtefacts.project1.model.Customer;
import req2test.tool.outputArtefacts.project1.usecase.FilterCustomerUseCase;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(CustomerController.class)
public class FilterCustomerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilterCustomerUseCase filterCustomerUseCase;

    @Test
    public void getFilteredCustomerListReturns200() throws Exception {
        // Arrange
        UUID idCustomer = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);
        LocalDate createdAt = LocalDate.now();
        Customer customer = new Customer(
                idCustomer,
                fullName,
                licenseNumber,
                LocalDate.now().plusYears(5),
                createdAt,
                birthDate
        );
        when(filterCustomerUseCase.execute(fullName, licenseNumber, birthDate, createdAt)).thenReturn(Collections.singletonList(customer));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/filter")
                .param("fullName", fullName)
                .param("licenseNumber", licenseNumber)
                .param("birthDate", birthDate.toString())
                .param("createdAt", createdAt.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].idCustomer").value(idCustomer.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].fullName").value(fullName))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].licenseNumber").value(licenseNumber))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].licenseNumberExpirationDate").value(LocalDate.now().plusYears(5).toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdAt").value(createdAt.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].birthDate").value(birthDate.toString()));

        verify(filterCustomerUseCase).execute(fullName, licenseNumber, birthDate, createdAt);
    }

    @Test
    public void filterCustomerReturns500OnDatabaseError() throws Exception {
        // Arrange
        String fullName = "ValidString";
        String licenseNumber = "1234-56";
        LocalDate birthDate = LocalDate.now().minusYears(20);
        LocalDate createdAt = LocalDate.now();
        doThrow(new DataBaseException("Failed to retrieve and filter list of Customer from database")).when(filterCustomerUseCase).execute(fullName, licenseNumber, birthDate, createdAt);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/filter")
                .param("fullName", fullName)
                .param("licenseNumber", licenseNumber)
                .param("birthDate", birthDate.toString())
                .param("createdAt", createdAt.toString()))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content().string(containsString("Failed to retrieve and filter list of Customer from database")));

        verify(filterCustomerUseCase).execute(fullName, licenseNumber, birthDate, createdAt);
    }
}
