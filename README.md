# Carpintería Circular 360

Sistema web desarrollado para apoyar la gestión administrativa y comercial de una carpintería dedicada a la fabricación y venta de productos de madera.

La aplicación permite administrar productos, clientes, pedidos, pagos, envíos y reportes. También cuenta con un catálogo público, carrito de compras, inicio de sesión, control de roles y conexión con una base de datos MySQL.

## Funcionalidades principales

### Módulo público

* Página de inicio.
* Catálogo de productos.
* Consulta del detalle de productos.
* Carrito de compras.
* Registro de clientes.
* Inicio y cierre de sesión.
* Perfil del cliente.
* Checkout y pago simulado.
* Selección entre entrega a domicilio o retiro.
* Pantalla de compra exitosa.

### Módulo administrativo

* Panel administrativo.
* CRUD de productos.
* CRUD de clientes.
* Gestión de pedidos.
* Gestión de envíos.
* Control de estados.
* Registro de pagos al retirar.
* Reportes generales.
* Control de inventario.
* Métricas de ventas y pedidos.

## Tecnologías utilizadas

* Java
* Spring Boot
* Spring MVC
* Spring Data JPA
* Hibernate
* Thymeleaf
* MySQL
* Maven
* Bootstrap 5
* Bootstrap Icons
* HTML
* CSS
* JavaScript
* Apache Tomcat embebido
* Git y GitHub

## Arquitectura del proyecto

El sistema utiliza una arquitectura organizada por capas:

```text
controller
service
repository
model
config
templates
static
```

### Responsabilidad de cada capa

* `model`: representa las entidades principales del sistema.
* `repository`: gestiona el acceso a MySQL mediante Spring Data JPA.
* `service`: contiene reglas de negocio y validaciones.
* `controller`: recibe y procesa solicitudes HTTP.
* `templates`: contiene las vistas HTML con Thymeleaf.
* `static`: contiene recursos como CSS, JavaScript e imágenes.
* `config`: contiene configuraciones generales del sistema.

En esta arquitectura, los repositorios cumplen la función equivalente a la capa DAO, los controladores sustituyen a los Servlets y Thymeleaf sustituye a las páginas JSP.

## Requisitos

Antes de ejecutar el proyecto se debe tener instalado:

* Java 21 o una versión compatible con el proyecto.
* MySQL Server.
* MySQL Workbench, opcional.
* Visual Studio Code, IntelliJ IDEA, Eclipse o NetBeans.
* Maven, aunque el proyecto incluye Maven Wrapper.
* Git, para clonar o actualizar el repositorio.

## Clonar el repositorio

```bash
git clone https://github.com/granadosJefferson/CarpinteriaCircularWeb.git
```

Después, ingresar a la carpeta del proyecto:

```bash
cd CarpinteriaCircularWeb
```

## Configuración de la base de datos

El proyecto utiliza MySQL como sistema gestor de base de datos.

La configuración se encuentra en:

```text
src/main/resources/application.properties
```

Ejemplo de configuración:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/carpinteria_circular
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

La contraseña debe adaptarse al entorno local de cada integrante.

No se recomienda publicar credenciales reales en el repositorio.

## Scripts SQL

La carpeta:

```text
database
```

contiene los archivos:

```text
schema.sql
data.sql
```

* `schema.sql`: contiene la estructura de la base de datos.
* `data.sql`: contiene datos iniciales y usuarios de prueba.

Para preparar la base de datos:

1. Abrir MySQL Workbench.
2. Ejecutar `database/schema.sql`.
3. Ejecutar `database/data.sql`.
4. Verificar que las tablas y los usuarios se hayan creado correctamente.

## Ejecución del proyecto

### Desde Visual Studio Code

1. Abrir la carpeta del proyecto.
2. Buscar la clase principal:

```text
CarpinteriaCircularApplication.java
```

3. Ejecutar la aplicación.
4. Esperar el mensaje:

```text
Started CarpinteriaCircularApplication
```

5. Abrir en el navegador:

```text
http://localhost:8080
```

### Desde la terminal en Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Desde Linux o macOS

```bash
./mvnw spring-boot:run
```

## Usuarios de prueba

### Administrador

```text
Correo: PENDIENTE
Contraseña: PENDIENTE
Rol: ADMIN
```

### Cliente

```text
Correo: PENDIENTE
Contraseña: PENDIENTE
Rol: CLIENTE
```

Las credenciales deben coincidir con los usuarios almacenados en MySQL.

## Control de sesiones y roles

El sistema utiliza `HttpSession` para almacenar el usuario autenticado.

Los roles disponibles son:

* `ADMIN`
* `CLIENTE`

El administrador puede acceder a los módulos internos de productos, clientes, pedidos, envíos y reportes.

El cliente puede acceder al catálogo, perfil, carrito y proceso de compra.

Las páginas privadas validan la sesión y el rol desde el backend.

## CRUD principal

El primer CRUD funcional implementado corresponde al módulo de productos.

Permite:

* Registrar productos.
* Listar productos.
* Consultar información.
* Editar productos.
* Activar productos.
* Desactivar productos.
* Cargar imágenes.
* Controlar existencias.
* Validar datos obligatorios.

Archivos principales:

```text
Producto.java
ProductoRepository.java
ProductoService.java
ProductoController.java
templates/admin/productos.html
templates/admin/producto-formulario.html
```

## Validaciones

El sistema aplica validaciones en el servidor y en los formularios.

Entre las validaciones implementadas se encuentran:

* Campos obligatorios.
* Precios no negativos.
* Existencias no negativas.
* Longitud máxima de textos.
* Formatos de datos.
* Existencia de registros.
* Correos duplicados.
* Productos activos o inactivos.
* Restricción de acceso según rol.
* Validación de existencias antes de confirmar una compra.
* Restricción de pagos según el tipo de entrega.

## Imágenes de productos

Las imágenes se almacenan en:

```text
uploads/productos
```

La configuración para servir los archivos se encuentra en:

```text
WebConfig.java
```

Los formatos permitidos son:

* JPG
* JPEG
* PNG
* WEBP

## Rutas principales

### Rutas públicas

```text
/
```

Página de inicio.

```text
/catalogo
```

Catálogo de productos.

```text
/login
```

Inicio de sesión.

```text
/registro
```

Registro de usuario.

```text
/perfil
```

Perfil del cliente.

```text
/checkout
```

Proceso de compra.

### Rutas administrativas

```text
/admin
```

Panel administrativo.

```text
/admin/productos
```

Gestión de productos.

```text
/admin/clientes
```

Gestión de clientes.

```text
/admin/pedidos
```

Gestión de pedidos.

```text
/admin/envios
```

Gestión de envíos.

```text
/admin/reportes
```

Reportes administrativos.

## Estados del sistema

### Estados de pedidos

* `PENDIENTE`
* `EN_PROCESO`
* `COMPLETADO`
* `CANCELADO`

### Estados de envíos

* `PENDIENTE`
* `PREPARANDO`
* `EN_CAMINO`
* `ENTREGADO`
* `CANCELADO`

### Métodos de pago

* Tarjeta.
* SINPE Móvil.
* Pago al retirar.

## Repositorio

El código fuente se encuentra disponible en:

```text
https://github.com/granadosJefferson/CarpinteriaCircularWeb
```

## Evidencias

Las capturas y documentos del proyecto pueden almacenarse en:

```text
docs
```

Se recomienda incluir evidencias de:

* Ejecución de Spring Boot.
* Conexión con MySQL.
* Inicio de sesión.
* Control de roles.
* CRUD de productos.
* Validaciones.
* Pedidos.
* Envíos.
* Reportes.
* Scripts SQL.

## Autores

* Jefferson Granados
* PENDIENTE: agregar los demás integrantes del equipo.

## Estado del proyecto

El proyecto cuenta actualmente con una base funcional que incluye:

* Conexión a MySQL.
* Inicio y cierre de sesión.
* Manejo de `HttpSession`.
* Roles `ADMIN` y `CLIENTE`.
* Protección de páginas privadas.
* CRUD de productos.
* CRUD de clientes.
* Gestión de pedidos.
* Gestión de envíos.
* Reportes.
* Carrito de compras.
* Checkout.
* Pagos simulados.
* Descuento de inventario.

El proyecto se encuentra en etapa de revisión, pruebas finales y mejora de documentación.
