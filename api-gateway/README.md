# ⛩️ API Gateway - TimeLogic

El **API Gateway** es la puerta de entrada a todos los microservicios del ecosistema **TimeLogic**.  
Está construido con **Spring Cloud Gateway** y proporciona **enrutamiento dinámico**, **autenticación JWT (Firebase Authentication)**, **CORS seguro**, **filtros globales** y **monitorización de endpoints**.

---

## 📌 Funcionalidades principales

- 🔀 **Enrutamiento dinámico** hacia los microservicios:
  - `user-service` → gestión de usuarios y autenticación
  - `timeclock-service` → control horario y fichajes
- 🛡️ **Autenticación basada en JWT** (tokens emitidos por Firebase).
- 🌍 **CORS configurado** para permitir llamadas desde `localhost:4200` (Angular) y otros clientes autorizados.
- 🧹 **Filtros globales** para limpieza de cabeceras sensibles (`Cookie`, `Set-Cookie`).
- 🔎 **Actuator** para monitorización (`/actuator/health`, `/actuator/info`, etc.).
- 📊 **Logs estructurados** en `./logs`.

---

## ⚙️ Requisitos previos

- **Java 21**
- **Maven 3.9+**
- **Spring Boot 3.3.13**
- **Spring Cloud 2023.0.4**
- **Eureka Discovery Server** corriendo en `http://localhost:8761/eureka`
- Proyecto padre: [`timelogic-backend`](../timelogic-backend)

---

## 🚀 Ejecución local

1. Arranca el **Eureka Discovery Server**:
   ```bash
   cd discovery-server
   mvn spring-boot:run
   ´´´

2. Arranca el API Gateway:
	```bash
	cd api-gateway
	mvn spring-boot:run
	´´´
3. Endpoints disponibles:
 -  

