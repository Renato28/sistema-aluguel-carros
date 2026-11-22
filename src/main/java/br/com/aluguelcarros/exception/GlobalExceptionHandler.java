package br.com.aluguelcarros.exception;

import br.com.aluguelcarros.dto.ErroResponse;
import br.com.aluguelcarros.dto.ErroValidacaoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CategoriaDuplicadaException.class)
    public ResponseEntity<ErroResponse> tratarCategoriaDuplicada(CategoriaDuplicadaException exception) {

        logger.warn("Tentativa de cadastrar uma categoria duplicada: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoResponse> tratarErroValidacao(MethodArgumentNotValidException exception) {

        java.util.Map<String, String> erros = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        erro -> Objects.toString(erro.getDefaultMessage(), "Valor inválido"),
                        (mensagemExistente, novaMensagem)  -> mensagemExistente
                ));

        ErroValidacaoResponse erro = new ErroValidacaoResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação de dados",
                erros,
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erro);
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarClienteNaoEncontrado(ClienteNaoEncontradoException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }

    @ExceptionHandler(VeiculoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarVeiculoNaoEncontrado(VeiculoNaoEncontradoException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }

    @ExceptionHandler(CategoriaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarCategoriaNaoEncontrada(CategoriaNaoEncontradaException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }


    @ExceptionHandler(ReservaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarReservaNaoEncontrada(ReservaNaoEncontradaException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }

    @ExceptionHandler(VeiculoInativoException.class)
    public ResponseEntity<ErroResponse> tratarVeiculoInativo(VeiculoInativoException exception) {

        logger.warn("Operação não permitida para veiculo inativo: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(PlacaVeiculoDuplicadaException.class)
    public ResponseEntity<ErroResponse> tratarPlacaVeiculoDuplicada(PlacaVeiculoDuplicadaException exception) {

        logger.warn("Tentativa de utilizar uma placa duplicada: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(CategoriaInativaException.class)
    public ResponseEntity<ErroResponse> tratarCategoriaInativa(CategoriaInativaException exception) {

        logger.warn("Tentativa de inativar uma categoria inativa: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(PeriodoReservaInvalidoException.class)
    public ResponseEntity<ErroResponse> tratarPeriodoReservaInvalido(PeriodoReservaInvalidoException exception) {

        logger.warn(
                "Tentativa de criar reserva com periodo inválido: {}", exception.getMessage()
        );

        ErroResponse erro = new ErroResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erro);
    }

    @ExceptionHandler(ClienteInativoException.class)
    public ResponseEntity<ErroResponse> tratarClienteInativo(ClienteInativoException exception) {

        logger.warn(
                "Tentativa de criar reserva para cliente inativo: {}", exception.getMessage()
        );

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(VeiculoIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratarVeiculoIndisponivel(VeiculoIndisponivelException exception) {

        logger.warn(
                "Tentativa de reservar veiculo indisponivel: {}", exception.getMessage()
        );

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(CpfClienteDuplicadoException.class)
    public ResponseEntity<ErroResponse> tratarCpfClienteDuplicado(CpfClienteDuplicadoException exception) {

        logger.warn(
                "Conflito no cadastro de cliente: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(ReservaJaCanceladaException.class)
    public ResponseEntity<ErroResponse> tratarReservaCancelada(ReservaJaCanceladaException exception) {

        logger.warn("Tentativa de cancelar reserva já cancelada: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(ReservaNaoPodeSerCanceladaException.class)
    public ResponseEntity<ErroResponse> tratarReservaNaoPodeSerCancelada(ReservaNaoPodeSerCanceladaException exception) {

        logger.warn("Tentativa de cancelar reserva concluida: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(ReservaNaoPodeSerConfirmadaException.class)
    public ResponseEntity<ErroResponse> tratarReservaNaoPodeSerConfirmada(ReservaNaoPodeSerConfirmadaException exception) {

        logger.warn("Tentativa de confirmar reserva inválida: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(ReservaJaConfirmadaException.class)
    public ResponseEntity<ErroResponse> tratarReservaJaConfirmada(ReservaJaConfirmadaException exception) {

        logger.warn("Tentativa de confirmar reserva já confirmada: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(ReservaNaoPodeSerAlteradaException.class)
    public ResponseEntity<ErroResponse> tratarReservaNaoPodeSerAlterada(
            ReservaNaoPodeSerAlteradaException exception) {

        logger.warn("Tentativa de alterar reserva não permitida: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(EmailUsuarioDuplicadoException.class)
    public ResponseEntity<ErroResponse> tratarEmailUsuarioDuplicado(EmailUsuarioDuplicadoException exception) {

        logger.warn("Conflito no cadastro de usuário: {}", exception.getMessage());

        ErroResponse erro = new ErroResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> tratarErroAutenticacao(AuthenticationException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas ou não autorizadas.",
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(erro);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> tratarAcessoNegado(
            AccessDeniedException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.FORBIDDEN.value(),
                exception.getMessage(),
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(erro);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErroResponse> tratarAcessoNegado(
            AuthorizationDeniedException exception) {

        ErroResponse erro = new ErroResponse(
                HttpStatus.FORBIDDEN.value(),
                "Você não tem permissão para executar esta operação.",
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInterno(Exception exception) {

        logger.error("Erro interno inesperado na aplicação.", exception);

        ErroResponse erro = new ErroResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno inesperado.",
                LocalDateTime.now(ZoneId.systemDefault())
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(erro);
    }
}
