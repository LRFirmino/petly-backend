package com.pawsclaws.petshop.exception;

/**
 * Operação proibida (HTTP 403) com mensagem de negócio, por exemplo uma
 * funcionalidade desabilitada ou uma ação que o usuário não pode executar.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
