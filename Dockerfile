# --- ESTÁGIO 1: Build da aplicação ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia os arquivos de configuração do Maven/Gradle primeiro (para aproveitar o cache de dependências)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Baixa as dependências (em cache se o pom.xml não mudar)
RUN ./mvnw dependency:go-offline

# Copia o código-fonte restante e faz o build do projeto
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- ESTÁGIO 2: Imagem final leve para execução ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia apenas o arquivo .jar gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# O Render injeta a porta automaticamente na variável de ambiente PORT.
# Como o Spring Boot precisa escutar essa porta, usamos um script/comando para repassar.
ENV PORT=8080
EXPOSE 8080

# Comando para rodar a aplicação ajustando a porta dinâmica do Render
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]