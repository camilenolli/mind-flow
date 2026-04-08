# ============================================================
# MindFlow — Dockerfile multi-stage para deploy no Render
# (também roda em qualquer host que aceite Docker)
# ============================================================

# ---------- Estágio 1: build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build

# 1) Copia só o pom para cachear download de dependências
COPY backend/pom.xml ./pom.xml
RUN mvn -B -q dependency:go-offline

# 2) Copia o código e empacota o fat-jar
COPY backend/src ./src
RUN mvn -B -q -DskipTests package


# ---------- Estágio 2: runtime ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia somente o jar resultante (deixa as camadas de build pra trás)
COPY --from=build /build/target/mindflow-0.1.0-SNAPSHOT.jar app.jar

# Otimizações para o free tier do Render (512MB RAM, 0.1 CPU):
#   - Serial GC: melhor para baixa carga e pouca memória
#   - TieredStopAtLevel=1: pula compilação C2, startup ~30% mais rápido
#   - MaxRAMPercentage=70: heap = 70% da RAM disponível (~358MB em 512MB)
#                          deixa espaço para metaspace, stacks e nativo
#   - InitialRAMPercentage=50: começa em 50% (256MB) e cresce conforme precisa
#   - Xss256k: stacks de threads menores liberam memória
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=70.0 -XX:InitialRAMPercentage=50.0 -Xss256k"

# Render injeta $PORT em runtime; application.properties já lê via ${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
