package microservices.book.multiplication.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import microservices.book.multiplication.domain.Multiplication;
import microservices.book.multiplication.service.RandomGeneratorService;

class MultiplicationServiceImplTest {

	private MultiplicationServiceImpl multiplicationServiceImpl;

	@Mock
	private RandomGeneratorService randomGeneratorService;

	@BeforeEach
	public void setUp() {
		// With this call to initMocks we tell Mockito to process the annotations
		MockitoAnnotations.initMocks(this);
		multiplicationServiceImpl = new MultiplicationServiceImpl(randomGeneratorService);
	}

	@Test
	void createRandomMultiplicationTest() {
		// given (our mocked Random Generator service will return first 50, then 30
		given(randomGeneratorService.generateRandomFactor()).willReturn(50, 30);

		// when
		Multiplication multiplication = multiplicationServiceImpl.createRandomMultiplication();

		// assert
		assertThat(multiplication.getFactorA()).isEqualTo(50);
		assertThat(multiplication.getFactorB()).isEqualTo(30);
	}

}
