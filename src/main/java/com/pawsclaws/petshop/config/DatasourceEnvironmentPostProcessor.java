package com.pawsclaws.petshop.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Falha cedo (e com uma mensagem clara) quando nenhuma URL de banco foi
 * configurada, em vez do erro genérico do Spring:
 * "Failed to configure a DataSource: 'url' attribute is not specified".
 *
 * <p>Roda depois do carregamento dos arquivos de configuração/`.env`.</p>
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DatasourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String MESSAGE = """

            ==========================================================================
             Nenhuma URL de banco de dados foi configurada.

             Defina TURSO_DATABASE_URL (e TURSO_AUTH_TOKEN) em backend/.env, por exemplo:

                 TURSO_DATABASE_URL=libsql://<banco>-<org>.turso.io
                 TURSO_AUTH_TOKEN=<seu-token>

             Regras do .env: CHAVE=valor, uma por linha, SEM aspas e salvo como
             UTF-8 SEM BOM. O arquivo deve estar na pasta backend/ (veja .env.example).

             Alternativas:
               - variáveis de ambiente de verdade (export/$env/TURSO_DATABASE_URL=...);
               - desenvolvimento local sem Turso: --spring.profiles.active=dev
            ==========================================================================
            """;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (url == null || url.isBlank() || url.contains("${")) {
            throw new IllegalStateException(MESSAGE);
        }
    }
}
