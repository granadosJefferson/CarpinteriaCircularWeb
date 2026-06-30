# Carpintería Circular 360

Sistema web desarrollado para apoyar la gestión administrativa y comercial de una carpintería dedicada a la fabricación y venta de productos de madera.

La aplicación permite administrar productos, clientes, pedidos, pagos, envíos, inventario y reportes. También incluye un catálogo público, carrito de compras, registro de clientes, inicio de sesión, control de roles y un proceso de compra conectado con una base de datos MySQL.

## Funcionalidades principales

### Módulo público

- Página de inicio.
- Catálogo de productos.
- Consulta del detalle de productos.
- Visualización de existencias disponibles.
- Carrito de compras.
- Registro de clientes.
- Inicio y cierre de sesión.
- Perfil del cliente.
- Checkout.
- Pago simulado.
- Pago mediante tarjeta.
- Pago mediante SINPE Móvil.
- Pago al retirar.
- Selección entre entrega a domicilio o retiro personal.
- Pantalla de compra exitosa.

### Módulo administrativo

- Menú administrativo.
- Gestión de productos.
- Gestión de clientes.
- Gestión de pedidos.
- Gestión de envíos.
- Control de estados de pedidos y envíos.
- Registro de pagos al retirar.
- Reportes generales.
- Control de inventario.
- Métricas de ventas y pedidos.
- Carga y administración de imágenes de productos.

## Tecnologías utilizadas

- Java 21.
- Spring Boot.
- Spring MVC.
- Spring Data JPA.
- Hibernate.
- Thymeleaf.
- MySQL.
- Maven.
- Bootstrap 5.
- Bootstrap Icons.
- HTML.
- CSS.
- JavaScript.
- Apache Tomcat embebido.
- Git.
- GitHub.

## Arquitectura del proyecto

El sistema utiliza una arquitectura organizada por capas:

```text
controller
service
repository
model
dto
config
templates
static
```

### Responsabilidad de cada capa

- `model`: representa las entidades principales del sistema.
- `dto`: transporta datos entre formularios, controladores y servicios.
- `repository`: gestiona el acceso a MySQL mediante Spring Data JPA.
- `service`: contiene las reglas de negocio y las validaciones.
- `controller`: recibe y procesa las solicitudes HTTP.
- `templates`: contiene las vistas HTML desarrolladas con Thymeleaf.
- `static`: contiene recursos estáticos como CSS, JavaScript e imágenes.
- `config`: contiene configuraciones generales del sistema.

En esta arquitectura, los repositorios cumplen una función equivalente a la capa DAO, los controladores procesan las solicitudes que tradicionalmente manejarían los Servlets y Thymeleaf sustituye el uso de páginas JSP.

## Requisitos

Antes de ejecutar el proyecto se debe tener instalado:

- Java 21.
- MySQL Server.
- MySQL Workbench, opcional.
- Visual Studio Code, IntelliJ IDEA, Eclipse o NetBeans.
- Git, para clonar o actualizar el repositorio.

No es obligatorio instalar Maven de forma independiente porque el proyecto incluye Maven Wrapper.

## Clonar el repositorio

Ejecutar:

```bash
git clone https://github.com/granadosJefferson/CarpinteriaCircularWeb.git
```

Después, ingresar a la carpeta del proyecto:

```bash
cd CarpinteriaCircularWeb
```

La carpeta raíz correcta es la que contiene el archivo:

```text
pom.xml
```

## Configuración de la base de datos

El proyecto utiliza MySQL como sistema gestor de base de datos.

La configuración principal se encuentra en:

```text
src/main/resources/application.properties
```

Ejemplo de configuración:

```properties
spring.application.name=carpinteria-circular

spring.datasource.url=jdbc:mysql://localhost:3306/carpinteria_circular
spring.datasource.username=root
spring.datasource.password=TU_CONTRASENA

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

app.upload.dir=uploads/productos
```

La contraseña debe sustituirse por la contraseña configurada en el servidor MySQL de la computadora donde se ejecutará el proyecto.

No se deben publicar contraseñas reales ni otras credenciales privadas en el repositorio.

## Scripts SQL

La carpeta:

```text
database
```

contiene los scripts utilizados para preparar la base de datos:

```text
schema.sql
data.sql
```

- `schema.sql`: contiene la creación inicial de la base de datos y su estructura.
- `data.sql`: contiene datos iniciales o registros necesarios para las pruebas.

Para preparar la base de datos:

1. Abrir MySQL Workbench.
2. Abrir y ejecutar `database/schema.sql`.
3. Abrir y ejecutar `database/data.sql`.
4. Verificar que la base de datos `carpinteria_circular` exista.
5. Verificar que las tablas y los datos iniciales se hayan creado correctamente.
6. Configurar el usuario y la contraseña de MySQL en `application.properties`.

Hibernate también está configurado con:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Esta configuración permite actualizar la estructura de las tablas de acuerdo con las entidades del proyecto.

## Ejecución del proyecto

### Desde Visual Studio Code

1. Abrir la carpeta que contiene `pom.xml`.
2. Buscar la clase principal:

```text
CarpinteriaCircularApplication.java
```

3. Ejecutar la clase principal.
4. Esperar en la terminal un mensaje similar a:

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

Antes de entregar el proyecto, esta sección debe actualizarse con las credenciales creadas en la base de datos.

### Administrador

```text
Correo o usuario: PENDIENTE
Contraseña: PENDIENTE
Rol: ADMIN
```

### Cliente

```text
Correo o usuario: PENDIENTE
Contraseña: PENDIENTE
Rol: CLIENTE
```

Las credenciales deben coincidir con los usuarios almacenados en MySQL.

No se recomienda utilizar contraseñas personales o sensibles como credenciales de prueba.

## Control de sesiones y roles

El sistema utiliza `HttpSession` para almacenar los datos del usuario autenticado.

Los roles disponibles son:

- `ADMIN`.
- `CLIENTE`.

El administrador puede acceder a los módulos internos de productos, clientes, pedidos, envíos y reportes.

El cliente puede acceder al catálogo, perfil, carrito y proceso de compra.

Las páginas privadas validan la sesión y el rol desde el backend antes de permitir el acceso.

## Gestión de productos

El módulo de productos permite:

- Registrar productos.
- Listar productos.
- Consultar información.
- Editar productos.
- Activar productos.
- Desactivar productos.
- Eliminar productos.
- Cargar imágenes.
- Controlar existencias.
- Validar datos obligatorios.
- Mostrar los productos activos en el catálogo público.

Archivos principales:

```text
Producto.java
ProductoRepository.java
ProductoService.java
ProductoController.java
templates/admin/productos.html
templates/admin/producto-formulario.html
```

## Gestión de clientes

El módulo de clientes permite:

- Registrar clientes.
- Listar clientes.
- Editar información.
- Activar clientes.
- Desactivar clientes.
- Consultar sus datos.
- Asociar clientes con pedidos.

## Gestión de pedidos

El módulo de pedidos permite:

- Registrar pedidos administrativos.
- Registrar varios productos en un pedido.
- Crear pedidos desde el checkout.
- Validar existencias.
- Descontar inventario.
- Calcular subtotales y totales.
- Cambiar el estado de un pedido.
- Consultar el detalle del pedido.
- Cancelar pedidos.
- Devolver existencias cuando corresponde.
- Mantener los pedidos cancelados como historial.

### Estados de pedidos

- `PENDIENTE`.
- `EN_PROCESO`.
- `COMPLETADO`.
- `CANCELADO`.

## Checkout y pagos

El checkout permite:

- Recibir los productos guardados en el carrito.
- Validar cantidades y existencias.
- Elegir entre retiro personal y entrega a domicilio.
- Calcular el costo de envío.
- Registrar el pedido en MySQL.
- Descontar las existencias desde el backend.
- Vaciar el carrito después de una compra exitosa.

### Métodos de pago

- Tarjeta.
- SINPE Móvil.
- Pago al retirar.

El pago al retirar solo está disponible cuando se selecciona retiro personal.

Los pagos mediante tarjeta y SINPE Móvil son simulados con fines académicos y no procesan dinero real.

## Gestión de envíos

El módulo de envíos permite:

- Crear envíos asociados con pedidos.
- Consultar la información de entrega.
- Modificar el estado de un envío.
- Registrar fechas y observaciones.
- Consultar el detalle de cada envío.

### Estados de envíos

- `PENDIENTE`.
- `PREPARANDO`.
- `EN_CAMINO`.
- `ENTREGADO`.
- `CANCELADO`.

## Reportes

El módulo de reportes permite consultar:

- Cantidad total de productos.
- Cantidad total de clientes.
- Cantidad total de pedidos.
- Pedidos pendientes.
- Pedidos en proceso.
- Pedidos completados.
- Pedidos cancelados.
- Total vendido.
- Promedio por venta.
- Productos con pocas existencias.
- Pedidos recientes.

## Validaciones

El sistema aplica validaciones tanto en el servidor como en los formularios.

Entre las validaciones implementadas se encuentran:

- Campos obligatorios.
- Precios no negativos.
- Existencias no negativas.
- Cantidades válidas.
- Longitud máxima de textos.
- Formatos de datos.
- Existencia de registros.
- Correos duplicados.
- Productos activos o inactivos.
- Clientes activos.
- Restricción de acceso según el rol.
- Validación de existencias antes de confirmar una compra.
- Restricción de métodos de pago según el tipo de entrega.
- Validación del comprobante de SINPE Móvil.
- Prevención de productos repetidos dentro de un pedido.

## Imágenes de productos

Las imágenes se almacenan en:

```text
uploads/productos
```

La carpeta se encuentra en la raíz del proyecto, al mismo nivel que `pom.xml`.

La configuración para servir los archivos se encuentra en:

```text
WebConfig.java
```

Los formatos permitidos son:

- JPG.
- JPEG.
- PNG.
- WEBP.

El tamaño máximo configurado para cada archivo es de 10 MB.

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

Registro de clientes.

```text
/perfil
```

Perfil del cliente autenticado.

```text
/checkout
```

Proceso de compra.

### Rutas administrativas

```text
/admin
```

Menú administrativo.

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

## Repositorio

El código fuente se encuentra disponible en:

```text
https://github.com/granadosJefferson/CarpinteriaCircularWeb
```

## Evidencias y documentación

La carpeta:

```text
docs
```

puede utilizarse para almacenar capturas, diagramas y documentos relacionados con el proyecto.

Se recomienda incluir evidencias de:

- Ejecución de Spring Boot.
- Conexión con MySQL.
- Inicio de sesión.
- Control de roles.
- Registro de clientes.
- CRUD de productos.
- Validaciones.
- Carrito de compras.
- Checkout.
- Pedidos.
- Pagos.
- Envíos.
- Reportes.
- Scripts SQL.

## Autores

- Jefferson Granados.
- PENDIENTE: agregar los demás integrantes del equipo.

## Estado del proyecto

El sistema cuenta actualmente con las siguientes funcionalidades:

- Conexión con MySQL.
- Inicio y cierre de sesión.
- Manejo de `HttpSession`.
- Roles `ADMIN` y `CLIENTE`.
- Protección de páginas privadas.
- Gestión de productos.
- Gestión de clientes.
- Gestión de pedidos.
- Gestión de envíos.
- Reportes administrativos.
- Catálogo público.
- Carrito de compras.
- Checkout.
- Pagos simulados.
- Control y descuento de inventario.
- Carga de imágenes.
- Diseño responsive con Bootstrap.

El proyecto se encuentra en etapa de revisión final, ejecución de pruebas integrales y actualización de la documentación.