package M2fst.demo.controller;

import M2fst.demo.dto.ErreurDto;
import M2fst.demo.exception.ForbiddenException;
import M2fst.demo.exception.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ErreurDto> gererNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(new ErreurDto(404, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErreurDto> gererForbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(new ErreurDto(403, ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<ErreurDto> gererAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(new ErreurDto(403, "Vous n'avez pas les droits pour effectuer cette action"));
    }

    @ExceptionHandler
    public ResponseEntity<ErreurDto> gererException(Exception ex) {
        return ResponseEntity.status(500).body(new ErreurDto(500, "Une erreur est survenue"));
    }

    // Override car ResponseEntityExceptionHandler gère déjà les MethodArgumentNotValidException mais on veut renvoyer un message personnalisé
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.status(400).body(new ErreurDto(400, "Les données envoyées sont invalides"));
    }
}
