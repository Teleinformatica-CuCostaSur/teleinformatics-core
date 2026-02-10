# 🎓 Teleinformatics Core API - Guía para Desarrolladores

> **API REST base** desarrollada con Spring Boot 3.5.10 y Java 17 para la gestión de usuarios y autenticación JWT sobre MySQL. Este proyecto es una **base fundacional** con casos de uso esenciales de autenticación que sirve como punto de partida para cualquier desarrollador que necesite agregar nuevos módulos y funcionalidades.

---

## **📋 Tabla de Contenidos**

1. [Requisitos Previos](#-requisitos-previos)
2. [Configuración Inicial](#%EF%B8%8F-configuración-inicial)
3. [Arquitectura del Proyecto](#-arquitectura-del-proyecto)
4. [Stack Tecnológico](#-stack-tecnológico)
5. [Estructura de Carpetas](#-estructura-de-carpetas)
6. [Guía de Desarrollo](#-guía-de-desarrollo)
   - [Crear Nuevos Endpoints (Controllers)](#1-crear-nuevos-endpoints-controllers)
   - [Crear Servicios (Services)](#2-crear-servicios-services)
   - [Crear Excepciones Personalizadas](#3-crear-excepciones-personalizadas)
   - [Crear DTOs con Validaciones](#4-crear-dtos-con-validaciones)
   - [Crear Entidades JPA](#5-crear-entidades-jpa)
   - [Agregar Migraciones de Base de Datos](#6-agregar-migraciones-de-base-de-datos)
   - [Implementar Seguridad por Roles](#7-implementar-seguridad-por-roles)
7. [Sistema de Manejo de Errores](#-sistema-de-manejo-de-errores)
8. [Autenticación JWT](#-autenticación-jwt---flujo-completo)
9. [Testing](#-testing)
10. [Herramientas Extras](#-herramientas-extras)
11. [Mejores Prácticas](#-mejores-prácticas-y-convenciones)
12. [Comandos Útiles](#-comandos-útiles)
13. [Resolución de Problemas](#-resolución-de-problemas)

---

## **📋 Requisitos Previos**

Antes de empezar, asegúrate de tener instalado:

- ☕ **Java 17+** (JDK) - [Descargar OpenJDK](https://adoptium.net/)
- 🗄️ **Acceso a MySQL** (local o remoto como Hostgator)
- 🔧 **Git** - [Descargar](https://git-scm.com/)
- 💻 **IDE recomendado**: IntelliJ IDEA Community/Ultimate o VS Code

> **Nota:** No necesitas instalar Gradle manualmente, el proyecto incluye Gradle Wrapper (`gradlew`).

---

## **🛠️ Configuración Inicial**

### **Paso 1: Clonar el Repositorio**

```powershell
git clone <URL_DEL_REPOSITORIO>
cd teleinformatics-core
```

### **Paso 2: Configurar Variables de Entorno**

1. **Copia el archivo de ejemplo:**
   ```powershell
   Copy-Item .env.example .env
   ```

2. **Edita el archivo `.env`** con tus credenciales:
   ```env
   # Database (MySQL)
   DB_HOST=localhost                    # O tu servidor remoto (ej: serverXXX.hostgator.com)
   DB_PORT=3306
   MYSQL_DB_NAME=teleinformatics_db
   MYSQL_USER=root
   MYSQL_PASSWORD=tu_password

   # JWT Configuration
   JWT_SECRET=mi-secreto-super-seguro-de-al-menos-32-caracteres-para-produccion
   JWT_EXPIRATION=8640000               # 2.4 horas en milisegundos
   ```

   > ⚠️ **IMPORTANTE:** 
   > - El `JWT_SECRET` **debe tener mínimo 32 caracteres**
   > - Usa secretos diferentes para desarrollo y producción
   > - Nunca subas el archivo `.env` a Git (ya está en `.gitignore`)

3. **Cargar variables de entorno:**

   **Con el script incluido (recomendado):**
   ```powershell
   .\load-env.ps1
   ```

   **Manualmente:**
   ```powershell
   Get-Content .env | ForEach-Object {
       if ($_ -match '^\s*([^#][^=]+)=(.+)$') {
           $name = $matches[1].Trim()
           $value = $matches[2].Trim()
           [Environment]::SetEnvironmentVariable($name, $value, 'Process')
       }
   }
   ```

### **Paso 3: Configurar la Base de Datos**

**Opción A: MySQL Local**
```sql
CREATE DATABASE teleinformatics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Opción B: MySQL Remoto (Hostgator)**
1. Accede a cPanel → **MySQL Databases**
2. Crea una nueva base de datos
3. Crea un usuario y asígnale privilegios completos
4. En **Remote MySQL**, autoriza tu IP pública:
   ```powershell
   curl ifconfig.me   # Ver tu IP pública
   ```

### **Paso 4: Compilar y Ejecutar**

```powershell
# Compilar el proyecto (primera vez puede tardar descargando dependencias)
.\gradlew.bat clean build -x test

# Ejecutar la aplicación
.\gradlew.bat bootRun
```

La aplicación iniciará en: **http://localhost:8080**

✅ **Primera ejecución:** Flyway ejecutará automáticamente las migraciones y creará las tablas necesarias.

### **Paso 5: Verificar Funcionamiento**

Prueba el endpoint de registro:

```powershell
curl -X POST http://localhost:8080/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "developer@test.com",
    "password": "password123"
  }'
```

**Respuesta esperada:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## **🏛️ Arquitectura del Proyecto**

El proyecto sigue una **arquitectura en capas (Layered Architecture)** con separación de responsabilidades:

```
┌─────────────────────────────────────┐
│         Controllers                 │  ← REST API endpoints
│   (Reciben requests HTTP)           │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          Services                   │  ← Lógica de negocio
│   (Procesan datos, aplican reglas)  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│        Repositories                 │  ← Acceso a datos (JPA)
│   (CRUD en base de datos)           │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         Entities                    │  ← Modelos de datos (JPA)
│   (Mapeo de tablas MySQL)           │
└─────────────────────────────────────┘
```

**Componentes transversales:**
- **DTOs**: Objetos de transferencia de datos (Request/Response)
- **Security**: Autenticación JWT, filtros, configuración Spring Security
- **Exception Handling**: Manejo centralizado de errores con `@RestControllerAdvice`
- **Config**: Configuraciones de beans (JWT, OpenAPI/Swagger)
- **Flyway**: Migraciones versionadas de base de datos

---

## **🔧 Stack Tecnológico**

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Spring Boot** | 3.5.10 | Framework principal |
| **Java** | 17 | Lenguaje de programación |
| **MySQL** | 5.7+ | Base de datos relacional |
| **Spring Security** | 6.x | Autenticación y autorización |
| **JWT (JJWT)** | 0.13.0 | Tokens de autenticación |
| **Flyway** | Included | Migraciones de base de datos |
| **Hibernate** | 6.x | ORM (JPA implementation) |
| **Lombok** | Latest | Reducción de boilerplate |
| **Gradle** | 8.14.4 | Build tool (Kotlin DSL) |
| **SpringDoc OpenAPI** | 2.7.0 | Documentación Swagger UI |

---

## **📁 Estructura de Carpetas**

```
teleinformatics-core/
│
├── src/main/java/edu/teleinformatics/core/
│   │
│   ├── TeleinformaticsCoreApplication.java    # Clase principal @SpringBootApplication
│   │
│   ├── auth/                                   # Módulo de Autenticación
│   │   ├── controller/
│   │   │   └── AuthController.java             # Endpoints: /auth/register, /auth/login
│   │   ├── dto/
│   │   │   ├── AuthComplete.java               # Response: {id, jwt}
│   │   │   ├── CreateUser.java                 # Request: registro con validaciones
│   │   │   └── LoginUser.java                  # Request: login {email, password}
│   │   ├── exception/
│   │   │   ├── UserAlreadyExistsException.java
│   │   │   ├── JwtExpiredException.java
│   │   │   └── JwtInvalidException.java
│   │   └── service/
│   │       └── AuthService.java                # Lógica: registro, login, generación JWT
│   │
│   ├── db/user/                                 # Módulo de Usuarios (renombrado de "user" a "db/user")
│   │   ├── entity/
│   │   │   ├── User.java                       # Entidad: tabla users
│   │   │   ├── UserDetails.java                # Entidad: tabla user_details (perfil)
│   │   │   ├── Role.java                       # Entidad: tabla roles
│   │   │   └── RoleEnum.java                   # Enum: ROLE_STUDENT, ROLE_TEACHER, etc.
│   │   ├── repository/
│   │   │   ├── UserRepository.java             # JPA Repository para User
│   │   │   └── RoleRepository.java             # JPA Repository para Role
│   │   └── exception/
│   │       └── RoleNotFoundException.java
│   │
│   ├── security/                               # Configuración de Seguridad
│   │   ├── SecurityConfig.java                 # Configuración principal Spring Security
│   │   ├── CustomUserDetails.java              # Implementación de UserDetails
│   │   ├── CustomUserDetailsService.java       # Carga usuarios desde DB
│   │   ├── CustomAuthenticationEntryPoint.java # Manejo de errores de autenticación
│   │   └── jwt/
│   │       ├── JwtService.java                 # Generación y validación de tokens
│   │       └── JwtFilter.java                  # Filtro que intercepta requests y valida JWT
│   │
│   ├── config/                                 # Configuraciones
│   │   ├── JwtProperties.java                  # Propiedades JWT desde application.yml
│   │   └── OpenApiConfig.java                  # Configuración Swagger/OpenAPI
│   │
│   └── exception/                              # Manejo Global de Excepciones
│       ├── GlobalExceptionHandler.java         # @RestControllerAdvice - Captura excepciones
│       ├── ErrorHandler.java                   # Enum con códigos de error (AUTH-001, DB-002, etc.)
│       ├── NotFoundException.java              # Excepción base para 404
│       └── ExceptionBuilder.java               # (Legacy - usado solo en CustomAuthenticationEntryPoint)
│
├── src/main/resources/
│   ├── application.yml                         # Configuración Spring (DB, JPA, JWT)
│   └── db/migration/
│       └── V1__init_schema.sql                 # Migración Flyway: schema inicial (roles, users, etc.)
│
├── src/test/java/                              # Tests unitarios e integración
│
├── build.gradle.kts                            # Dependencias y configuración Gradle
├── gradlew / gradlew.bat                       # Gradle Wrapper
├── .env.example                                # Plantilla de variables de entorno
├── load-env.ps1                                # Script PowerShell para cargar .env
├── docker-compose.yml                          # (Opcional) Stack MySQL local con Docker
└── README.md                                   # Este archivo
```

---

## **💻 Guía de Desarrollo**

Esta sección te guiará paso a paso para agregar nuevas funcionalidades al proyecto siguiendo los patrones establecidos.

---

### **1. Crear Nuevos Endpoints (Controllers)**

Los **Controllers** son el punto de entrada de las peticiones HTTP. Exponen endpoints REST y delegan la lógica al Service.

#### **Ejemplo: Crear un módulo de `Student` con endpoint para obtener estudiantes**

**Paso 1:** Crear el Controller

```java
package edu.teleinformatics.core.student.controller;

import edu.teleinformatics.core.student.dto.StudentResponse;
import edu.teleinformatics.core.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")    // Base path: /api/students
@RequiredArgsConstructor             // Lombok: inyección por constructor
@SecurityRequirement(name = "Bearer Authentication")  // Swagger: indica que requiere JWT
public class StudentController {
    
    private final StudentService studentService;  // Inyección del servicio
    
    @GetMapping                      // GET /api/students
    @Operation(summary = "Get all students", description = "Returns a list of all registered students")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")  // Solo teachers y admins
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.findAll());
    }
    
    @GetMapping("/{id}")             // GET /api/students/{id}
    @Operation(summary = "Get student by ID")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable UUID id) {
        return ResponseEntity.ok(studentService.findById(id));
    }
}
```

**Convenciones:**
- Usa `@RestController` (ya incluye `@ResponseBody`)
- Base path en `@RequestMapping` debe empezar con `/api/` (excepto `/auth`)
- Inyecta servicios con `@RequiredArgsConstructor` (Lombok)
- Usa `@Operation` para documentar en Swagger
- Retorna siempre `ResponseEntity<T>` para control del status HTTP
- Usa `@PreAuthorize` para control de acceso por roles (ver sección [Implementar Seguridad por Roles](#7-implementar-seguridad-por-roles))

---

### **2. Crear Servicios (Services)**

Los **Services** contienen la **lógica de negocio**. Coordinan repositorios, aplican validaciones y transforman datos.

#### **Ejemplo: Service de Student**

```java
package edu.teleinformatics.core.student.service;

import edu.teleinformatics.core.db.user.entity.User;
import edu.teleinformatics.core.db.user.repository.UserRepository;
import edu.teleinformatics.core.exception.NotFoundException;
import edu.teleinformatics.core.student.dto.StudentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service                          // Marca como componente de servicio
@RequiredArgsConstructor          // Inyección por constructor (Lombok)
@Slf4j                            // Logger automático (log.info, log.error, etc.)
public class StudentService {
    
    private final UserRepository userRepository;  // Inyecta repositorios necesarios
    
    @Transactional(readOnly = true)   // Optimización: transacción de solo lectura
    public List<StudentResponse> findAll() {
        log.debug("Finding all students");
        
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ROLE_STUDENT")))
            .map(user -> new StudentResponse(
                user.getId(),
                user.getEmail(),
                user.isEnabled()
            ))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public StudentResponse findById(UUID id) {
        log.debug("Finding student with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Student not found with ID: " + id));
        
        return new StudentResponse(user.getId(), user.getEmail(), user.isEnabled());
    }
}
```

**Convenciones:**
- Usa `@Service` para marcar la clase como servicio
- Agrega `@Slf4j` para logging (muy importante para debugging)
- Usa `@Transactional` para operaciones de base de datos:
  - `@Transactional(readOnly = true)` para consultas (optimización)
  - `@Transactional` para operaciones de escritura (insert/update/delete)
- Lanza excepciones personalizadas (ver sección [Crear Excepciones Personalizadas](#3-crear-excepciones-personalizadas))
- **NUNCA** retornes entidades JPA directamente en controllers, usa DTOs

---

### **3. Crear Excepciones Personalizadas**

El proyecto usa un sistema **centralizado** de manejo de errores con códigos estandarizados.

#### **Paso 1: Agregar el código de error en `ErrorHandler.java`**

```java
// src/main/java/edu/teleinformatics/core/exception/ErrorHandler.java

@Getter
@AllArgsConstructor
public enum ErrorHandler {
    // ... códigos existentes ...
    
    // STUDENT (agregar nueva categoría)
    STUDENT_NOT_FOUND("STU-001", "The requested student does not exist", HttpStatus.NOT_FOUND),
    STUDENT_ALREADY_ENROLLED("STU-002", "Student is already enrolled in this course", HttpStatus.CONFLICT),
    INVALID_ENROLLMENT_DATE("STU-003", "Enrollment date must be in the future", HttpStatus.BAD_REQUEST);
    
    private final String code;           // Código único (ej: STU-001)
    private final String defaultMessage; // Mensaje por defecto
    private final HttpStatus httpStatus; // Status HTTP (404, 409, etc.)
}
```

**Convención de códigos:**
- `GEN-XXX`: Errores genéricos
- `AUTH-XXX`: Errores de autenticación
- `DB-XXX`: Errores de base de datos / entidades no encontradas
- `STU-XXX`: Errores de estudiantes
- `CRS-XXX`: Errores de cursos
- (Agrega tus propios prefijos según el módulo)

#### **Paso 2: Crear la excepción personalizada**

```java
package edu.teleinformatics.core.student.exception;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String message) {
        super(message);
    }
}
```

> **Nota:** Todas las excepciones personalizadas deben extender `RuntimeException` (excepciones no chequeadas).

#### **Paso 3: Registrar el handler en `GlobalExceptionHandler.java`**

```java
// src/main/java/edu/teleinformatics/core/exception/GlobalExceptionHandler.java

@ExceptionHandler(StudentNotFoundException.class)
public ResponseEntity<ApiErrorResponse> handleStudentNotFoundException(
    StudentNotFoundException ex, 
    HttpServletRequest request
) {
    logException(ex, request, ErrorHandler.STUDENT_NOT_FOUND.getDefaultMessage(), 
                 ErrorHandler.STUDENT_NOT_FOUND.getCode());
    
    return ResponseEntity.status(ErrorHandler.STUDENT_NOT_FOUND.getHttpStatus())
        .body(new ApiErrorResponse(
            ErrorHandler.STUDENT_NOT_FOUND.getDefaultMessage(),  // Usa mensaje genérico
            ErrorHandler.STUDENT_NOT_FOUND.getCode()
        ));
}
```

#### **Paso 4: Usar la excepción en tu Service**

```java
@Transactional(readOnly = true)
public StudentResponse findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + id));
    
    // ... lógica ...
}
```

**¿Qué sucede cuando se lanza la excepción?**

1. El `GlobalExceptionHandler` la captura automáticamente
2. Se loguea con todos los detalles (IP, path, stack trace, etc.)
3. Se retorna una respuesta JSON estandarizada:
   ```json
   {
     "message": "The requested student does not exist",
     "errorCode": "STU-001"
   }
   ```
4. El status HTTP es 404 (NOT_FOUND)

---

### **4. Crear DTOs con Validaciones**

Los **DTOs (Data Transfer Objects)** son objetos que transportan datos entre capas. Usan `records` de Java 17+ para inmutabilidad.

#### **Ejemplo: DTO para crear un curso**

```java
package edu.teleinformatics.core.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateCourseRequest(
    
    @NotBlank(message = "Course name cannot be blank")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    @Schema(description = "Name of the course", example = "Introduction to Computer Science")
    String name,
    
    @NotNull(message = "Credits cannot be null")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    @Schema(description = "Number of academic credits", example = "4")
    Integer credits,
    
    @NotNull(message = "Start date cannot be null")
    @Future(message = "Start date must be in the future")
    @Schema(description = "Course start date", example = "2026-03-01")
    LocalDate startDate,
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be at most 100 characters")
    @Schema(description = "Instructor email", example = "professor@university.edu")
    String instructorEmail
) {}
```

**Anotaciones de validación más comunes:**

| Anotación | Uso |
|-----------|------|
| `@NotNull` | El campo no puede ser `null` |
| `@NotBlank` | String no puede ser null, vacío o solo espacios |
| `@NotEmpty` | Colecciones/arrays no pueden estar vacíos |
| `@Size(min, max)` | Tamaño de String, Collection, Array |
| `@Min(value)` | Valor numérico mínimo |
| `@Max(value)` | Valor numérico máximo |
| `@Email` | Validación de formato email |
| `@Pattern(regexp)` | Validación con expresión regular |
| `@Future` | Fecha debe estar en el futuro |
| `@Past` | Fecha debe estar en el pasado |

**¿Cómo se validan automáticamente?**

En el Controller, usa `@Valid`:

```java
@PostMapping
public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
    // Si la validación falla, Spring lanza MethodArgumentNotValidException
    // El GlobalExceptionHandler la captura y retorna errores de validación automáticamente
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(courseService.create(request));
}
```

**Respuesta de error de validación:**

```json
{
  "message": "Course name cannot be blank; Credits must be at least 1; Start date must be in the future",
  "errorCode": "GEN-002"
}
```

---

### **5. Crear Entidades JPA**

Las **Entidades** mapean tablas de la base de datos usando JPA/Hibernate.

#### **Ejemplo: Entidad `Course`**

```java
package edu.teleinformatics.core.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_courses_name", columnList = "name")  // Índice para búsquedas por nombre
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Constructor sin argumentos para JPA
@Getter
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // UUID generado por JPA
    @JdbcTypeCode(SqlTypes.VARCHAR)                  // Forzar VARCHAR(36) en MySQL 5.7
    @Column(length = 36)
    private UUID id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "credits", nullable = false)
    private Integer credits;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "instructor_email", length = 100)
    private String instructorEmail;
    
    @Column(name = "is_active", nullable = false)
    @Setter  // Permite cambiar solo este campo
    private boolean isActive = true;
    
    @CreationTimestamp  // Hibernate setea automáticamente al crear
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp    // Hibernate actualiza automáticamente al modificar
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructor para crear instancias (sin ID, timestamps se generan solos)
    public Course(String name, Integer credits, LocalDate startDate, String instructorEmail) {
        this.name = name;
        this.credits = credits;
        this.startDate = startDate;
        this.instructorEmail = instructorEmail;
    }
    
    // Equals y HashCode basados en ID (para trabajar con Hibernate proxies)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return id != null && id.equals(course.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

**Convenciones para Entidades:**

1. **Anotaciones de clase:**
   - `@Entity`: Marca la clase como entidad JPA
   - `@Table(name = "...")`: Nombre de la tabla en minúsculas con guiones bajos
   - `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: Constructor para JPA

2. **Identificador:**
   - Usa `UUID` como tipo de ID
   - `@GeneratedValue(strategy = GenerationType.UUID)`
   - `@JdbcTypeCode(SqlTypes.VARCHAR)` + `@Column(length = 36)` para MySQL 5.7 compatibility

3. **Columnas:**
   - `@Column(name = "...")`: Usa snake_case en nombres de columnas
   - `nullable = false` para campos requeridos
   - `length = X` para limitar tamaño de Strings
   - `unique = true` para campos únicos (como email)

4. **Timestamps:**
   - `@CreationTimestamp` para fecha de creación
   - `@UpdateTimestamp` para última modificación

5. **Relaciones:**
   ```java
   // One-to-Many
   @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<Enrollment> enrollments = new ArrayList<>();
   
   // Many-to-One
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "instructor_id", nullable = false)
   private User instructor;
   
   // Many-to-Many
   @ManyToMany
   @JoinTable(
       name = "course_students",
       joinColumns = @JoinColumn(name = "course_id"),
       inverseJoinColumns = @JoinColumn(name = "student_id")
   )
   private Set<User> students = new HashSet<>();
   ```

---

### **6. Agregar Migraciones de Base de Datos**

El proyecto usa **Flyway** para versionar cambios en el esquema de la base de datos.

#### **¿Cómo funciona Flyway?**

- Lee archivos SQL en `src/main/resources/db/migration/`
- Los aplica en **orden alfabético** una sola vez
- Registra qué migraciones ya se ejecutaron en la tabla `flyway_schema_history`

#### **Convención de nombres:**

```
V{VERSION}__{DESCRIPTION}.sql

Ejemplos:
V1__init_schema.sql               # Migración inicial (ya existe)
V2__add_courses_table.sql         # Nueva tabla courses
V3__add_enrollments_table.sql     # Nueva tabla enrollments
V4__add_course_capacity_column.sql  # Agregar columna
V5__rename_profile_to_user_details.sql  # Renombrar tabla
```

#### **Ejemplo: Crear tabla `courses`**

**Archivo:** `src/main/resources/db/migration/V2__add_courses_table.sql`

```sql
-- ===========================================
-- Migration V2: Add courses table
-- Description: Creates courses table with basic fields
-- ===========================================

CREATE TABLE courses (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    credits INT NOT NULL CHECK (credits > 0 AND credits <= 10),
    start_date DATE NOT NULL,
    instructor_email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_credits_range CHECK (credits BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for name searches
CREATE INDEX idx_courses_name ON courses(name);

-- Index for active courses
CREATE INDEX idx_courses_active ON courses(is_active);
```

#### **Ejemplo: Agregar columna a tabla existente**

**Archivo:** `src/main/resources/db/migration/V3__add_course_description.sql`

```sql
-- Add description column to courses table
ALTER TABLE courses 
ADD COLUMN description TEXT AFTER instructor_email;
```

#### **Buenas prácticas para migraciones:**

- ✅ Cada migración debe ser **idempotente** (se puede ejecutar múltiples veces sin errores)
- ✅ Usa comentarios descriptivos
- ✅ Crea índices para columnas que se consultan frecuentemente
- ✅ Define `CHECK` constraints para validaciones a nivel de base de datos
- ✅ Usa `ENGINE=InnoDB` y `CHARSET=utf8mb4` para MySQL
- ❌ **NUNCA modifiques una migración ya ejecutada** (crea una nueva)
- ❌ **NUNCA elimines migraciones existentes**

---

### **7. Implementar Seguridad por Roles**

El proyecto usa **Spring Security** con autenticación JWT y control de acceso basado en roles.

#### **Roles disponibles:**

| Rol | Enum | Descripción |
|-----|------|-------------|
| `ROLE_STUDENT` | `RoleEnum.ROLE_STUDENT` | Estudiante (rol por defecto al registrarse) |
| `ROLE_TEACHER` | `RoleEnum.ROLE_TEACHER` | Profesor |
| `ROLE_COORDINATOR` | `RoleEnum.ROLE_COORDINATOR` | Coordinador de programa |
| `ROLE_ADMIN` | `RoleEnum.ROLE_ADMIN` | Administrador del sistema |

#### **Proteger endpoints por rol:**

**En el Controller, usa `@PreAuthorize`:**

```java
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    // Acceso público (no requiere autenticación)
    @GetMapping("/public")
    public ResponseEntity<List<CourseResponse>> getPublicCourses() {
        return ResponseEntity.ok(courseService.findPublicCourses());
    }
    
    // Solo usuarios autenticados (cualquier rol)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }
    
    // Solo estudiantes
    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> enrollInCourse(@PathVariable UUID id) {
        // Lógica de inscripción...
        return ResponseEntity.ok().build();
    }
    
    // Solo profesores y administradores
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(courseService.create(request));
    }
    
    // Solo administradores
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // Combinación compleja: Solo coordinadores y admins, o el creador del curso
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN') or @courseService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<CourseResponse> updateCourse(
        @PathVariable UUID id, 
        @Valid @RequestBody UpdateCourseRequest request
    ) {
        return ResponseEntity.ok(courseService.update(id, request));
    }
}
```

#### **Expresiones de `@PreAuthorize`:**

| Expresión | Descripción |
|-----------|-------------|
| `isAuthenticated()` | Usuario autenticado (cualquier rol) |
| `hasRole('ADMIN')` | Usuario tiene rol específico |
| `hasAnyRole('TEACHER', 'ADMIN')` | Usuario tiene uno o más roles |
| `authentication.principal.id` | UUID del usuario autenticado |
| `@service.method(#param)` | Llamar método del bean para lógica personalizada |

#### **Configurar rutas públicas en `SecurityConfig.java`:**

Si necesitas agregar rutas totalmente públicas (sin JWT):

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(request -> request
            .requestMatchers("/auth/login", "/auth/register").permitAll()  // Ya configurado
            .requestMatchers("/api/courses/public").permitAll()            // Nueva ruta pública
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Swagger público
            .anyRequest().authenticated()  // Todo lo demás requiere autenticación
        )
        // ... resto de configuración ...
}
```

#### **Obtener el usuario autenticado en un Service:**

```java
@Service
@RequiredArgsConstructor
public class CourseService {
    
    // Opción 1: Inyectar Authentication desde el Controller
    public void enrollInCourse(UUID courseId, UUID userId) {
        // ...
    }
    
    // Opción 2: Obtener desde SecurityContext (menos recomendado)
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }
}
```

---

## **🚨 Sistema de Manejo de Errores**

El proyecto implementa un **manejo centralizado de excepciones** con respuestas estandarizadas.

### **Componentes:**

1. **`GlobalExceptionHandler`** (`@RestControllerAdvice`): Captura todas las excepciones
2. **`ErrorHandler`** (enum): Define códigos de error, mensajes y status HTTP
3. **`ApiErrorResponse`** (record): Formato de respuesta de error

### **Formato de respuesta de error:**

```json
{
  "message": "The requested student does not exist",
  "errorCode": "DB-001",
  "hash": null  // Solo se incluye en errores 500 (Internal Server Error)
}
```

### **¿Qué es el campo `hash`?**

En errores **500 (Internal Server Error)**, el campo `hash` contiene el **stack trace completo cifrado en Base64**.

**Ejemplo de error 500:**

```json
{
  "message": "An unexpected error occurred",
  "errorCode": "GEN-001",
  "hash": "amF2YS5sYW5nLk51bGxQb2ludGVyRXhjZXB0aW9uOiBDYW5ub3QgaW52b2tlIC..."
}
```

### **¿Cómo descifrar el stack trace?**

1. Copia el valor del campo `hash`
2. Ve a: **https://base64-zeta.vercel.app/**
3. Pega el hash y decodifica
4. Obtendrás el stack trace completo:

```
java.lang.NullPointerException: Cannot invoke "User.getEmail()" because "user" is null
    at edu.teleinformatics.core.student.service.StudentService.findById(StudentService.java:42)
    at edu.teleinformatics.core.student.controller.StudentController.getStudentById(StudentController.java:28)
    ...
```

> **Por qué cifrar el stack trace:** Evitar exponer información sensible (rutas, versiones) en respuestas API de producción, pero permitir debugging cuando sea necesario.

### **Logging de excepciones:**

Todas las excepciones se loguean automáticamente con:
- Código de error
- Mensaje
- Tipo de excepción
- Request method y URI
- IP del cliente
- User-Agent
- Stack trace completo

**Ejemplo de log:**

```
ERROR [http-nio-8080-exec-1] GlobalExceptionHandler - 
An exception occurred while processing the request:
ErrorCode: DB-001
Message: The requested student does not exist
Exception: StudentNotFoundException
Request: GET
IP: 192.168.1.100
User-Agent: Mozilla/5.0 ...
Stacktrace: ...
```

---

## **🔐 Autenticación JWT - Flujo Completo**

### **¿Cómo funciona la autenticación JWT en este proyecto?**

```
┌──────────────┐                                       ┌──────────────┐
│   Cliente    │                                       │   Servidor   │
└──────────────┘                                       └──────────────┘
       │                                                       │
       │  1. POST /auth/register                             │
       │  {"email": "user@test.com", "password": "pass123"}  │
       ├──────────────────────────────────────────────────────>
       │                                                       │
       │  2. Server crea User, genera JWT                    │
       │    JWT contiene: {id, email, roles}                 │
       │                                                       │
       │  3. Response: {"id": "uuid", "jwt": "eyJhbG..."}    │
       <───────────────────────────────────────────────────────┤
       │                                                       │
       │  4. Cliente guarda JWT (localStorage, cookie, etc.) │
       │                                                       │
       │  5. POST /api/courses                               │
       │  Authorization: Bearer eyJhbG...                     │
       ├──────────────────────────────────────────────────────>
       │                                                       │
       │  6. JwtFilter valida token:                         │
       │     - Verifica firma (HMAC SHA-256)                 │
       │     - Verifica expiración                           │
       │     - Extrae claims (id, email, roles)              │
       │     - Carga UserDetails desde DB                    │
       │     - Setea Authentication en SecurityContext       │
       │                                                       │
       │  7. Controller ejecuta con usuario autenticado      │
       │     @PreAuthorize valida roles                      │
       │                                                       │
       │  8. Response con datos del curso creado             │
       <───────────────────────────────────────────────────────┤
```

### **Componentes del flujo JWT:**

1. **`JwtService`**: Genera y valida tokens
   - Usa HMAC SHA-256 para firmar
   - Token contiene: `{sub: userId, email, roles, iat, exp}`
   - Expira según `JWT_EXPIRATION` (en milisegundos)

2. **`JwtFilter`**: Intercepta todas las requests
   - Extrae token del header `Authorization: Bearer <token>`
   - Valida firma y expiración
   - Si es válido, autentica al usuario en `SecurityContext`
   - Si es inválido/expirado, lanza excepción

3. **`SecurityConfig`**: Configura Spring Security
   - Define rutas públicas (`/auth/login`, `/auth/register`)
   - Todas las demás rutas requieren JWT válido
   - Deshabilita sesiones (stateless API)

### **Estructura del JWT:**

**Header:**
```json
{
  "alg": "HS256",   // Algoritmo: HMAC SHA-256
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",  // User ID
  "email": "user@test.com",
  "roles": ["ROLE_STUDENT"],
  "iat": 1738886400,   // Issued At (timestamp)
  "exp": 1738890000    // Expiration (timestamp)
}
```

**Signature:**
```
HMAC-SHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  JWT_SECRET
)
```

### **Endpoints de autenticación:**

#### **1. Registro**

**Request:**
```http
POST /auth/register
Content-Type: application/json

{
  "email": "student@university.edu",
  "password": "securePassword123"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Proceso interno:**
1. Valida formato de email y longitud de password
2. Hashea password con BCrypt
3. Crea User con `ROLE_STUDENT` por defecto
4. Genera JWT
5. Retorna ID y token

#### **2. Login**

**Request:**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "student@university.edu",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Proceso interno:**
1. Busca User por email
2. Valida password con BCrypt
3. Si es válido, genera nuevo JWT
4. Retorna ID y token

---

## **🧪 Testing**

### **Ejecutar tests:**

```powershell
# Todos los tests
.\gradlew.bat test

# Tests específicos
.\gradlew.bat test --tests "AuthServiceTest"

# Con reporte HTML
.\gradlew.bat test
# Ver reporte en: build/reports/tests/test/index.html
```

### **Estructura de tests:**

```
src/test/java/edu/teleinformatics/core/
├── auth/
│   ├── controller/
│   │   └── AuthControllerTest.java       # Tests de integración del controller
│   └── service/
│       └── AuthServiceTest.java          # Tests unitarios del servicio
├── student/
│   └── service/
│       └── StudentServiceTest.java
└── TeleinformaticsCoreApplicationTests.java  # Test de arranque de Spring
```

### **Ejemplo: Test unitario de Service**

```java
@ExtendWith(MockitoExtension.class)  // JUnit 5 con Mockito
class StudentServiceTest {
    
    @Mock
    private UserRepository userRepository;  // Mock del repositorio
    
    @InjectMocks
    private StudentService studentService;  // Servicio bajo prueba
    
    @Test
    @DisplayName("findById should return StudentResponse when student exists")
    void findById_shouldReturnStudentResponse_whenStudentExists() {
        // Arrange (preparar datos)
        UUID userId = UUID.randomUUID();
        User mockUser = new User("student@test.com", "hashedPassword", new Role());
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // Act (ejecutar método)
        StudentResponse result = studentService.findById(userId);
        
        // Assert (verificar resultado)
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("student@test.com", result.email());
        
        verify(userRepository, times(1)).findById(userId);  // Verificar interacción
    }
    
    @Test
    @DisplayName("findById should throw NotFoundException when student does not exist")
    void findById_shouldThrowNotFoundException_whenStudentDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(NotFoundException.class, () -> studentService.findById(userId));
    }
}
```

---

## **🛠️ Herramientas Extras**

### **1. Swagger UI (OpenAPI)**

Accede a la documentación interactiva de la API:

**URL:** http://localhost:8080/swagger-ui/index.html

**Características:**
- Visualiza todos los endpoints disponibles
- Prueba requests directamente desde el navegador
- Ve modelos de request/response
- Autenticación JWT integrada:
  1. Click en botón "Authorize" (candado)
  2. Ingresa: `Bearer <tu_jwt_aqui>`
  3. Todos los requests posteriores incluirán el JWT

### **2. Descifrador de Stack Traces (Base64)**

Cuando recibas un error 500 con campo `hash`:

**URL:** **https://base64-zeta.vercel.app/**

**Pasos:**
1. Copia el valor del campo `hash` del JSON de error
2. Pégalo en la herramienta web
3. Click en "Decode"
4. Obtendrás el stack trace completo para debugging

**Ejemplo:**
```json
// Error response
{
  "message": "An unexpected error occurred",
  "errorCode": "GEN-001",
  "hash": "amF2YS5sYW5nLk51bGxQb2ludGVyRXhjZXB0aW9uOiBDYW5ub3QgaW..."
}
```

### **3. MySQL Workbench**

Herramienta gráfica para gestionar la base de datos:

**Descargar:** https://dev.mysql.com/downloads/workbench/

**Conexión:**
- Hostname: `localhost` (o tu servidor remoto)
- Port: `3306`
- Username: tu usuario MySQL
- Password: tu password

### **4. Postman / Insomnia**

Clientes HTTP para probar la API:

- **Postman:** https://www.postman.com/downloads/
- **Insomnia:** https://insomnia.rest/download

**Tip:** Exporta la colección desde Swagger UI para importarla en Postman.

### **5. Script PowerShell para cargar .env**

El proyecto incluye `load-env.ps1` para cargar variables de entorno automáticamente:

```powershell
.\load-env.ps1
```

Muestra las variables cargadas (ocultando passwords) y confirma que todo está listo para ejecutar `.\gradlew.bat bootRun`.

---

## **✨ Mejores Prácticas y Convenciones**

### **Código**

- ✅ Usa **Lombok** para reducir boilerplate (`@RequiredArgsConstructor`, `@Slf4j`, `@Getter`)
- ✅ Usa **records** para DTOs inmutables (Java 17+)
- ✅ Separa responsabilidades: Controller → Service → Repository
- ✅ **NUNCA** retornes entidades JPA directamente desde Controllers, siempre usa DTOs
- ✅ Usa `@Transactional` en Services para operaciones de base de datos
- ✅ Lanza excepciones personalizadas, no uses `if (...) return error;`
- ✅ Loguea todo evento importante (`log.info`, `log.debug`, `log.error`)

### **Naming**

- **Packages:** Minúsculas, singular (`auth`, `course`, `student`)
- **Classes:** PascalCase (`StudentService`, `AuthController`)
- **Methods:** camelCase, verbos (`findAll()`, `createCourse()`, `isUserEnrolled()`)
- **Variables:** camelCase (`studentList`, `enrollmentDate`)
- **Constants:** UPPER_SNAKE_CASE (`MAX_CREDITS`, `DEFAULT_ROLE`)

### **DTOs**

- Request: `CreateXxxRequest`, `UpdateXxxRequest`
- Response: `XxxResponse` (ej: `StudentResponse`, `CourseResponse`)
- Auth: `AuthComplete`, `LoginUser`

### **Excepciones**

- Sufijo `Exception` (ej: `StudentNotFoundException`)
- Agregar código en `ErrorHandler` enum **antes** de crear la excepción
- Registrar handler en `GlobalExceptionHandler`

### **Base de Datos**

- Nombres de tablas: Minúsculas, plural (`users`, `courses`, `enrollments`)
- Columnas: snake_case (`first_name`, `created_at`)
- Foreign keys: `<tabla_singular>_id` (ej: `user_id`, `course_id`)
- Índices: `idx_<tabla>_<columna>` (ej: `idx_users_email`)

### **Migraciones Flyway**

- Formato: `V{VERSION}__{DESCRIPTION}.sql`
- Incrementar version secuencialmente (`V1`, `V2`, `V3`, ...)
- Nunca modificar migraciones ya ejecutadas
- Usar comentarios descriptivos

### **Git**

- Branch naming: `feature/descripcion`, `bugfix/descripcion`
- Commits claros: "Add student enrollment endpoint", "Fix JWT expiration bug"
- Pull requests con descripción detallada

---

## **🔧 Comandos Útiles**

### **Gradle**

| Comando | Descripción |
|---------|-------------|
| `.\gradlew.bat clean` | Limpia archivos compilados |
| `.\gradlew.bat build` | Compila el proyecto |
| `.\gradlew.bat build -x test` | Compila sin ejecutar tests |
| `.\gradlew.bat test` | Ejecuta tests unitarios |
| `.\gradlew.bat bootRun` | Inicia la aplicación Spring Boot |
| `.\gradlew.bat dependencies` | Muestra árbol de dependencias |
| `.\gradlew.bat flywayInfo` | Info de migraciones Flyway |
| `.\gradlew.bat flywayMigrate` | Ejecuta migraciones pendientes |
| `.\gradlew.bat flywayRepair` | Repara historial de Flyway |

### **PowerShell**

| Comando | Descripción |
|---------|-------------|
| `.\load-env.ps1` | Carga variables del archivo .env |
| `java -version` | Verifica versión de Java |
| `curl ifconfig.me` | Obtiene tu IP pública |
| `netstat -ano \| findstr :8080` | Ver qué proceso usa el puerto 8080 |
| `taskkill /PID <PID> /F` | Matar proceso por PID |
| `Get-Process java` | Lista procesos Java ejecutándose |

### **MySQL**

| Comando | Descripción |
|---------|-------------|
| `SHOW TABLES;` | Lista todas las tablas |
| `DESCRIBE <tabla>;` | Muestra estructura de tabla |
| `SELECT * FROM users;` | Consulta todos los usuarios |
| `SELECT * FROM flyway_schema_history;` | Historial de migraciones |
| `DROP TABLE flyway_schema_history;` | Resetea Flyway (⚠️ solo en dev) |

---

## **🐛 Resolución de Problemas**

### **Error: "Communications link failure"**

**Causa:** No se puede conectar a MySQL.

**Solución:**
1. Verifica que MySQL esté corriendo: `mysql --version`
2. Confirma credenciales en `.env`
3. Para MySQL remoto: verifica firewall y acceso remoto habilitado
4. Para Hostgator: cPanel → Remote MySQL → Agrega tu IP

---

### **Error: "Access denied for user"**

**Causa:** Credenciales incorrectas o usuario sin privilegios.

**Solución:**
1. Confirma usuario y password en MySQL Workbench
2. Verifica que el usuario tenga privilegios sobre la base de datos:
   ```sql
   SHOW GRANTS FOR 'tu_usuario'@'%';
   ```
3. Revisa caracteres especiales en password (pueden necesitar escape en `.env`)

---

### **Error: "Unknown database"**

**Causa:** La base de datos no existe.

**Solución:**
```sql
CREATE DATABASE teleinformatics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### **Error: "The specified key byte array is X bits"**

**Causa:** `JWT_SECRET` muy corto (mínimo 256 bits = 32 caracteres).

**Solución:**
```powershell
$env:JWT_SECRET="un-secreto-muy-largo-de-al-menos-treinta-y-dos-caracteres"
```

---

### **Error: "Port 8080 already in use"**

**Causa:** Otro proceso está usando el puerto 8080.

**Solución:**

**Opción 1:** Cambiar puerto en `application.yml`:
```yaml
server:
  port: 8081
```

**Opción 2:** Matar el proceso:
```powershell
netstat -ano | findstr :8080
# Output: TCP 0.0.0.0:8080 ... LISTENING 12345
taskkill /PID 12345 /F
```

---

### **Error: "Flyway validation failed"**

**Causa:** El checksum de una migración cambió, o la tabla `flyway_schema_history` está corrupta.

**Solución:**

**Opción 1:** Reparar Flyway
```powershell
.\gradlew.bat flywayRepair
```

**Opción 2:** Resetear completamente (⚠️ **solo en desarrollo**)
```sql
DROP DATABASE teleinformatics_db;
CREATE DATABASE teleinformatics_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Luego reinicia la aplicación para que Flyway recree todo.

---

### **Error: "Could not create connection to database server" (Hostgator)**

**Causa:** Hostgator bloquea conexiones remotas por defecto.

**Solución:**
1. cPanel → **Remote MySQL**
2. Agrega tu IP pública o `%` (todas las IPs - solo para dev)
3. Si el problema persiste, contacta soporte de Hostgator (algunos planes restringen conexiones externas)
4. Alternativa: Usa SSH tunnel:
   ```powershell
   ssh -L 3306:localhost:3306 usuario@serverXXX.hostgator.com
   ```

---

### **Error: JWT inválido / expirado**

**Causa:** El token JWT expiró o la firma no es válida.

**Síntomas:**
- Status 401 Unauthorized
- Mensaje: "Jwt is expired" o "Invalid JWT"

**Solución:**
1. Para JWT expirado: Haz login nuevamente para obtener un nuevo token
2. Para JWT inválido: Verifica que el `JWT_SECRET` sea el mismo que cuando se generó el token
3. En desarrollo, puedes aumentar `JWT_EXPIRATION` en `.env`:
   ```env
   JWT_EXPIRATION=86400000  # 24 horas
   ```

---

### **Logs no se muestran**

**Causa:** Nivel de logging muy alto.

**Solución:** Agrega en `application.yml`:
```yaml
logging:
  level:
    root: INFO
    edu.teleinformatics.core: DEBUG  # Debug para tu código
    org.springframework.security: DEBUG  # Debug para Security
    org.hibernate.SQL: DEBUG  # Ver queries SQL
```

---

## **📚 Referencias**

- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **Spring Security:** https://spring.io/projects/spring-security
- **JWT (JJWT):** https://github.com/jwtk/jjwt
- **Flyway:** https://flywaydb.org/documentation/
- **Lombok:** https://projectlombok.org/features/
- **MySQL Docs:** https://dev.mysql.com/doc/
- **SpringDoc OpenAPI:** https://springdoc.org/

---

## **👥 Roles del Sistema**

| Rol | Código | Descripción | Permisos típicos |
|-----|--------|-------------|------------------|
| `ROLE_STUDENT` | Default al registro | Estudiante universitario | Ver cursos, inscribirse, ver sus calificaciones |
| `ROLE_TEACHER` | Asignado por admin | Personal académico / Profesor | Crear cursos, calificar, ver estudiantes |
| `ROLE_COORDINATOR` | Asignado por admin | Coordinador de programa | Gestionar currículum, aprobar cursos |
| `ROLE_ADMIN` | Asignado manualmente | Administrador del sistema | Todos los permisos |

---

## **🤝 Contribuir**

1. Crea un branch desde `main`:
   ```powershell
   git checkout -b feature/nombre-funcionalidad
   ```

2. Haz commits descriptivos:
   ```powershell
   git commit -m "Add course enrollment endpoint with validation"
   ```

3. Push y abre un Pull Request:
   ```powershell
   git push origin feature/nombre-funcionalidad
   ```

4. Describe los cambios en el PR:
   - ¿Qué problema resuelve?
   - ¿Cómo probaste los cambios?
   - ¿Hay breaking changes?

---

## **📄 Licencia**

Este proyecto es de uso **académico** para fines educativos.

---

## **📞 Soporte**

Si encuentras problemas:

1. **Revisa los logs:** La mayoría de errores están detallados en la consola
2. **Consulta esta guía:** Especialmente la sección [Resolución de Problemas](#-resolución-de-problemas)
3. **Usa el descifrador Base64:** Para errores 500, descifra el `hash` en https://base64-zeta.vercel.app/
4. **Revisa Swagger UI:** http://localhost:8080/swagger-ui/index.html para documentación de endpoints
5. **Contacta al equipo:** Abre un issue en el repositorio con:
   - Descripción del error
   - Stack trace (del log o desencriptado)
   - Pasos para reproducir

---

**¡Listo para desarrollar! 🚀**

> **Este README es un documento vivo.** Actualízalo cada vez que agregues nuevos módulos, endpoints o cambies arquitectura. Los futuros desarrolladores te lo agradecerán.
