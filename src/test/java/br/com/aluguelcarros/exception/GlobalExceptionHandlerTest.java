package br.com.aluguelcarros.exception;

import br.com.aluguelcarros.dto.ErroResponse;
import br.com.aluguelcarros.dto.ErroValidacaoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void deveTratarCategoriaDuplicada() {

        var exception = new CategoriaDuplicadaException("Categoria já cadastrada");

        ResponseEntity<ErroResponse> response = handler.tratarCategoriaDuplicada(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Categoria já cadastrada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarClienteNaoEncontrado() {

        var exception = new ClienteNaoEncontradoException("Cliente não encontrado");

        ResponseEntity<ErroResponse> response = handler.tratarClienteNaoEncontrado(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Cliente não encontrado", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarVeiculoNaoEncontrado() {

        var exception = new VeiculoNaoEncontradoException("Veiculo não encontrado");

        ResponseEntity<ErroResponse> response = handler.tratarVeiculoNaoEncontrado(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Veiculo não encontrado", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarCategoriaNaoEncontrada() {

        var exception = new CategoriaNaoEncontradaException("Categoria não encontrada");

        ResponseEntity<ErroResponse> response = handler.tratarCategoriaNaoEncontrada(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Categoria não encontrada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarReservaNaoEncontrada() {

        var exception = new ReservaNaoEncontradaException("Reserva não encontrada");

        ResponseEntity<ErroResponse> response = handler.tratarReservaNaoEncontrada(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Reserva não encontrada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarVeiculoInativo() {

        var exception = new VeiculoInativoException("Veículo inativo");

        ResponseEntity<ErroResponse> response = handler.tratarVeiculoInativo(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Veículo inativo", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarPlacaVeiculoDuplicada() {

        var exception = new PlacaVeiculoDuplicadaException("Placa já cadastrada");

        ResponseEntity<ErroResponse> response = handler.tratarPlacaVeiculoDuplicada(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Placa já cadastrada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarCategoriaInativa() {

        var exception = new CategoriaInativaException("Categoria inativa");

        ResponseEntity<ErroResponse> response = handler.tratarCategoriaInativa(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Categoria inativa", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarClienteInativo() {

        var exception = new ClienteInativoException("Cliente inativo");

        ResponseEntity<ErroResponse> response = handler.tratarClienteInativo(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Cliente inativo", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarVeiculoIndisponivel() {

        var exception = new VeiculoIndisponivelException("Veiculo indisponivel");

        ResponseEntity<ErroResponse> response = handler.tratarVeiculoIndisponivel(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Veiculo indisponivel", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarCpfClienteDuplicado() {

        var exception = new CpfClienteDuplicadoException("CPF já cadastrado");

        ResponseEntity<ErroResponse> response = handler.tratarCpfClienteDuplicado(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("CPF já cadastrado", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarReservaJaCancelada() {

        var exception = new ReservaJaCanceladaException("Reserva já cancelada");

        ResponseEntity<ErroResponse> response = handler.tratarReservaCancelada(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Reserva já cancelada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarReservaNaoPodeSerCancelada() {

        var exception = new ReservaNaoPodeSerCanceladaException("Reserva não pode ser cancelada");

        ResponseEntity<ErroResponse> response = handler.tratarReservaNaoPodeSerCancelada(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Reserva não pode ser cancelada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarReservaNaoPodeSerConfirmada() {

        var exception = new ReservaNaoPodeSerConfirmadaException("Reserva não pode ser confirmada");

        ResponseEntity<ErroResponse> response = handler.tratarReservaNaoPodeSerConfirmada(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Reserva não pode ser confirmada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarReservaJaConfirmada() {

        var exception = new ReservaJaConfirmadaException("Reserva já confirmada");

        ResponseEntity<ErroResponse> response = handler.tratarReservaJaConfirmada(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Reserva já confirmada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarReservaNaoPodeSerAlterada() {

        var exception = new ReservaNaoPodeSerAlteradaException("Reserva não  pode ser alterada");

        ResponseEntity<ErroResponse> response = handler.tratarReservaNaoPodeSerAlterada(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Reserva não  pode ser alterada", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarEmailUsuarioDuplicado() {

        var exception = new EmailUsuarioDuplicadoException("E-mail já cadastrado");

        ResponseEntity<ErroResponse> response = handler.tratarEmailUsuarioDuplicado(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("E-mail já cadastrado", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarPeriodoReservaInvalido() {

        var exception = new PeriodoReservaInvalidoException("Período de reserva invalido");

        ResponseEntity<ErroResponse> response = handler.tratarPeriodoReservaInvalido(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Período de reserva invalido", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarErroDeAutenticacao() {

        var exception = new BadCredentialsException("Credenciais inválidas");

        ResponseEntity<ErroResponse> response = handler.tratarErroAutenticacao(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Credenciais inválidas ou não autorizadas.", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarAcessoNegadoPorAuthorizationDeniedException() {

        var exception = new AuthorizationDeniedException("Acesso não autorizado");

        ResponseEntity<ErroResponse> response = handler.tratarAcessoNegado(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Você não tem permissão para executar esta operação.", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarAcessoNegadoPorAccessDeniedException() {

        var exception = new AccessDeniedException("Acesso negado");

        ResponseEntity<ErroResponse> response = handler.tratarAcessoNegado(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Acesso negado", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarErroInterno() {

        var exception = new RuntimeException("Erro inesperado");

        ResponseEntity<ErroResponse> response = handler.tratarErroInterno(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Ocorreu um erro interno inesperado.", response.getBody().getMensagem());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void deveTratarErrosDeValidacao() throws NoSuchMethodException {
        var fieldErrors = List.of(
                new FieldError("objeto", "nome", "Nome é obrigatório"),
                new FieldError("objeto", "email", "E-mail inválido"),
                new FieldError("objeto", "nome", "Nome deve ter pelo menos 3 caracteres")
        );

        var exception = criarExcecaoValidacao(fieldErrors);

        ResponseEntity<ErroValidacaoResponse> response =
                handler.tratarErroValidacao(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var body = response.getBody();

        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals(2, body.getErros().size());
        assertEquals("Nome é obrigatório", body.getErros().get("nome"));
        assertEquals("E-mail inválido", body.getErros().get("email"));
        assertNotNull(body.getTimestamp());
    }

    private MethodArgumentNotValidException criarExcecaoValidacao(List<FieldError> fieldErrors) throws NoSuchMethodException {

        var objeto = new Object();

        var bindingResult = new BeanPropertyBindingResult(objeto, "objeto");

        fieldErrors.forEach(bindingResult::addError);

        Method metodo = GlobalExceptionHandlerTest.class.getDeclaredMethod("metodoAuxiliar");

        MethodParameter methodParameter = new MethodParameter(metodo, -1);

        return new MethodArgumentNotValidException(
                methodParameter,
                bindingResult
        );
    }

    private void metodoAuxiliar() {

    }
}
