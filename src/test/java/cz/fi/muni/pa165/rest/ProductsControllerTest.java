package cz.fi.muni.pa165.rest;

import cz.fi.muni.pa165.PersistenceSampleApplicationContext;
import cz.fi.muni.pa165.SpringMVCConfig;
import cz.fi.muni.pa165.dto.NewPriceDTO;
import cz.fi.muni.pa165.dto.ProductDTO;
import cz.fi.muni.pa165.enums.Currency;
import cz.fi.muni.pa165.facade.ProductFacade;
import cz.fi.muni.pa165.rest.controllers.ProductsController;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.testng.annotations.BeforeClass;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import org.testng.annotations.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.fi.muni.pa165.dto.CategoryDTO;
import cz.fi.muni.pa165.dto.ProductCreateDTO;
import java.io.IOException;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;


@WebAppConfiguration
@ContextConfiguration(classes = {PersistenceSampleApplicationContext.class, SpringMVCConfig.class})
public class ProductsControllerTest extends AbstractTestNGSpringContextTests  {
    
    @Mock
    private ProductFacade productFacade;

    @Autowired
    @InjectMocks
    private ProductsController productsController;

    private MockMvc mockMvc;
    
    @BeforeClass
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(productsController).setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
    }
    
    @Test
    public void debugTest() throws Exception {
        doReturn(Collections.unmodifiableList(this.createProducts())).when(productFacade).getAllProducts();
        mockMvc.perform(get("/products")).andDo(print());
    }

    @Test
    public void getAllProducts() throws Exception {

        doReturn(Collections.unmodifiableList(this.createProducts())).when(productFacade).getAllProducts();

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[?(@.id==10)].name").value("Raspberry PI"))
                .andExpect(jsonPath("$.[?(@.id==20)].name").value("Arduino"))
                .andExpect(jsonPath("$.[?(@.id==10)].priceHistory[0].value").value(34))
                .andExpect(jsonPath("$.[?(@.id==20)].priceHistory[0].value").value(44));
        
    }
    
    @Test
    public void getValidProduct() throws Exception {

        List <ProductDTO> products = this.createProducts();
        
        doReturn(products.get(0)).when(productFacade).getProductWithId(10l);
        doReturn(products.get(1)).when(productFacade).getProductWithId(20l);

        mockMvc.perform(get("/products/10"))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name").value("Raspberry PI"))
                .andExpect(jsonPath("$.priceHistory[0].value").value(34));

        
        mockMvc.perform(get("/products/20"))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name").value("Arduino"))
                .andExpect(jsonPath("$.priceHistory[0].value").value(44));

    }
    
    
    @Test
    public void getInvalidProduct() throws Exception {
        doReturn(null).when(productFacade).getProductWithId(1l);
        
        mockMvc.perform(get("/products/1"))
                .andExpect(status().is4xxClientError());
            
    }
    
    
    @Test
    public void deleteProduct() throws Exception {

        List <ProductDTO> products = this.createProducts();
        
        doReturn(products.get(0)).when(productFacade).getProductWithId(10l);
        doReturn(products.get(1)).when(productFacade).getProductWithId(20l);

        mockMvc.perform(delete("/products/10"))    // TODO: delete always returns 204?
                .andExpect(status().isOk());

    }

    @Test
    public void createProduct() throws Exception {

        ProductCreateDTO productCreateDTO = new ProductCreateDTO();
        productCreateDTO.setName("Raspberry PI");
        
        doReturn(1l).when(productFacade).createProduct(any(ProductCreateDTO.class));
        
        String json = this.convertObjectToJsonBytes(productCreateDTO);
        
        
        System.out.println(json);
        
        mockMvc.perform(post("/products/test").contentType(MediaType.APPLICATION_JSON).content(json)).andDo(print())  
                .andExpect(status().isOk());
    }
    
    
    @Test
    public void updateProduct() throws Exception {
        List <ProductDTO> products = this.createProducts();
        
        doReturn(products.get(0)).when(productFacade).getProductWithId(10l);
        doReturn(products.get(1)).when(productFacade).getProductWithId(20l);
        
        ProductDTO product = new ProductDTO();
        product.setName("test");
        doNothing().when(productFacade).changePrice(anyLong(), any(NewPriceDTO.class));
        
        String json = this.convertObjectToJsonBytes(product);
        
        mockMvc.perform(put("/products/10").contentType(MediaType.APPLICATION_JSON).content(json)).andDo(print())  
           .andExpect(status().isOk());
        
    }
    
    @Test
    public void addCategory() throws Exception {
        List <ProductDTO> products = this.createProducts();
        
        doReturn(products.get(0)).when(productFacade).getProductWithId(10l);
        doReturn(products.get(1)).when(productFacade).getProductWithId(20l);
        
        CategoryDTO category = new CategoryDTO();
        category.setId(1l);
        
        String json = this.convertObjectToJsonBytes(category);
        
        mockMvc.perform(post("/products/10/categories").contentType(MediaType.APPLICATION_JSON).content(json)).andDo(print())  
           .andExpect(status().isOk());
        
        // TODO: need to check JSON response
    }
    
    private List<ProductDTO> createProducts(){
        ProductDTO productOne = new ProductDTO();
        productOne.setId(10L);
        productOne.setName("Raspberry PI");
        NewPriceDTO currentPrice = new NewPriceDTO();
	currentPrice.setCurrency(Currency.EUR);
	currentPrice.setValue(new BigDecimal("34"));
	productOne.setCurrentPrice(currentPrice);
        productOne.setColor(ProductDTO.Color.BLACK);
      
        ProductDTO productTwo = new ProductDTO();
        productTwo.setId(20L);
        productTwo.setName("Arduino");
        NewPriceDTO price = new NewPriceDTO();
	price.setCurrency(Currency.EUR);
	price.setValue(new BigDecimal("44"));
	productTwo.setCurrentPrice(price);
        productTwo.setColor(ProductDTO.Color.WHITE);

        return Arrays.asList(productOne, productTwo);
    }
    
    private static String convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(object);
    }
}
