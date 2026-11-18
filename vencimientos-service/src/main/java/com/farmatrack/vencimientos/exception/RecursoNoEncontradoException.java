package com.farmatrack.vencimientos.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    private final String codigo;

    public RecursoNoEncontradoException(String message) {
        this("RECURSO_NO_ENCONTRADO", message);
    }

    public RecursoNoEncontradoException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
