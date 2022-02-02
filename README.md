# Shopping Cart

Aplicación de ejemplo echa en kotlin para carrito de compras de ecommerce

Antes de ejecutar la aplicación, se debe tener una base de datos postgresql, el esquema se genera automáticamente.

Para levantar una imagen de postgres usando docker:

```bash
 docker-compose -f database/postgresql.yml up -d    # Levanta la imagen de postgres
```

## Tests

Los test se implementaron usando el framework [JUnit](https://junit.org/) y testcontainers.

Nota: Antes de ejecutar los test verificar que docker esté instalado y corriendo.

```bash
docker ps
```

Ejecutar los test:

```bash
mvn test
```


