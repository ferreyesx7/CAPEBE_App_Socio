# CAPEBE — App Socio (Android / Kotlin)

Aplicación móvil para el **Rol Socio** del **Club Aéreo CAPEBE**, desarrollada en **Kotlin** y **Jetpack Compose** bajo el patrón arquitectónico **MVVM**.

## Funcionalidades Principales
- **Autenticación y Sesión Local:** Registro, inicio y cierre de sesión con persistencia en **Room SQLite** y formularios validados.
- **Gestión de Vuelos (REST API):** Reserva de vuelos y consulta de historial conectado mediante **Retrofit 2** al microservicio `service-vuelos` (Spring Boot + PostgreSQL en Docker).
- **Pase de Embarque QR:** Generación de código QR único para validación en pista.
- **Recursos Nativos:** Posicionamiento **GPS con mapa** (bases Tobalaba SCTB y Talcahuano SCIE) y **Brújula de cabina** (`SensorManager`).

## Configuración de Red
Editar la constante `HOST` en `app/src/main/java/com/example/appvuelos_capebe/network/ServerConfig.kt`:
- Emulador Android: `"10.0.2.2"`
- Producción AWS EC2: IP pública del servidor backend.
