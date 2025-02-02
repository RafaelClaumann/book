package microservices.book.multiplication.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import microservices.book.multiplication.domain.Multiplication;
import microservices.book.multiplication.domain.MultiplicationResultAttempt;
import microservices.book.multiplication.domain.User;
import microservices.book.multiplication.service.MultiplicationService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MultiplicationResultAttemptController.class)
class MultiplicationResultAttemptControllerTest {

	@MockBean
	private MultiplicationService multiplicationService;

	@Autowired
	private MockMvc mvc;
	
	private JacksonTester<MultiplicationResultAttempt> jsonResultAttempt;
	private JacksonTester<List<MultiplicationResultAttempt>> jsonResultAttemptList;

	@BeforeEach
	public void setup() {
		JacksonTester.initFields(this, new ObjectMapper());
	}

	@Test
	public void postResultReturnCorrect() throws Exception {
		genericParameterizedTest(true);
	}

	@Test
	public void postResultReturnNotCorrect() throws Exception {
		genericParameterizedTest(false);
	}

	void genericParameterizedTest(final boolean correct) throws Exception {
		given(multiplicationService.checkAttempt(any(MultiplicationResultAttempt.class))).willReturn(correct);

		User user = new User("rafael");
		Multiplication multiplication = new Multiplication(50, 70);
		MultiplicationResultAttempt attempt = new MultiplicationResultAttempt(user, multiplication, 3500, correct);

		MockHttpServletResponse response = mvc.perform(
				post("/results")
				.contentType(MediaType.APPLICATION_JSON).content(jsonResultAttempt.write(attempt).getJson()))
				.andReturn().getResponse();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonResultAttempt.write(
				new MultiplicationResultAttempt(
						attempt.getUser(),
						attempt.getMultiplication(),
						attempt.getResultAttempt(),
						correct))
				.getJson());
	}
	
	@Test
    public void getUserStatsTest() throws Exception {
        User user = new User("john_doe");
        Multiplication multiplication = new Multiplication(50, 70);
        
        MultiplicationResultAttempt attempt =
        		new MultiplicationResultAttempt(user, multiplication, 3500, true);
        
        List<MultiplicationResultAttempt> recentAttempts = Lists.newArrayList(attempt, attempt);
        given(multiplicationService.getStatsForUser("john_doe")).willReturn(recentAttempts);

        MockHttpServletResponse response = mvc.perform(
                get("/results").param("alias", "john_doe"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonResultAttemptList.write(recentAttempts).getJson());
    }
	
	@Test
	public void getResultAttemptByIdTest() throws Exception {
        User user = new User("john_doe");
        Multiplication multiplication = new Multiplication(50, 70);
        
        MultiplicationResultAttempt attempt =
        		new MultiplicationResultAttempt(user, multiplication, 3500, true);
        
        given(multiplicationService.getResultAttemptById(any(Long.class))).willReturn(attempt);
        
        MockHttpServletResponse response = mvc.perform(get("/results/{attemptId}", "1")).andReturn().getResponse(); 
        
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(jsonResultAttempt.write(attempt).getJson());
	}

}
