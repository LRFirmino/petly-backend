package com.pawsclaws.petshop.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrega um arquivo {@code .env} (CHAVE=valor) para o Environment.
 *
 * <p>Feito à mão de propósito: o leitor padrão do Spring <b>não remove o BOM</b>
 * (que o PowerShell/Notepad do Windows costuma adicionar), o que faria a
 * primeira chave ser lida como {@code \uFEFFTURSO_DATABASE_URL}. Aqui tratamos
 * BOM, aspas, comentários e linhas {@code export ...}.</p>
 *
 * <p>Procura em: {@code ./.env}, {@code ./backend/.env} e {@code ../.env}
 * (relativo ao diretório de trabalho).</p>
 *
 * <p><b>Deploy:</b> em produção normalmente não existe arquivo {@code .env}, então
 * este processador não faz nada e o Spring lê as <b>variáveis de ambiente</b> do
 * provedor normalmente. Mesmo quando o arquivo existe, ele entra com a
 * precedência mais baixa ({@code addLast}), ou seja, as variáveis de ambiente
 * reais sempre vencem o {@code .env}.</p>
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class EnvFileEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(EnvFileEnvironmentPostProcessor.class);
    private static final String SOURCE_NAME = "petlyDotEnv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Path candidate : candidates()) {
            if (Files.isRegularFile(candidate)) {
                Map<String, Object> parsed = parse(candidate);
                values.putAll(parsed);
                log.info("Carregando {} variável(is) de {} (variáveis de ambiente têm precedência).",
                        parsed.size(), candidate.toAbsolutePath().normalize());
            }
        }
        if (!values.isEmpty()) {
            // Precedência mais baixa: variáveis de ambiente reais vencem o .env.
            environment.getPropertySources().addLast(new MapPropertySource(SOURCE_NAME, values));
        } else {
            log.debug("Nenhum arquivo .env encontrado; usando variáveis de ambiente/configuração do ambiente.");
        }
    }

    private static Path[] candidates() {
        return new Path[]{
                Path.of(".env"),
                Path.of("backend", ".env"),
                Path.of("..", ".env"),
        };
    }

    private static Map<String, Object> parse(Path file) {
        Map<String, Object> values = new LinkedHashMap<>();
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return values;
        }
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        for (String rawLine : text.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("export ")) {
                line = line.substring("export ".length()).trim();
            }
            int separator = line.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }
            if (!key.isEmpty()) {
                values.put(key, value);
            }
        }
        return values;
    }
}
